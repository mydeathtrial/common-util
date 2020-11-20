package cloud.agileframework.common.util.object;

import cloud.agileframework.common.annotation.Alias;
import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.clazz.ClassInfo;
import cloud.agileframework.common.util.clazz.ClassUtil;
import cloud.agileframework.common.util.clazz.FieldInfo;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.date.DateUtil;
import cloud.agileframework.common.util.map.MapUtil;
import cloud.agileframework.common.util.number.NumberUtil;
import cloud.agileframework.common.util.pattern.PatternUtil;
import cloud.agileframework.common.util.string.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 佟盟
 * 日期 2019/10/21 15:51
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class ObjectUtil extends ObjectUtils {


    private static final String SERIAL_VERSION_UID = "serialVersionUID";

    public static <T> T to(Object from, TypeReference<T> toClass) {
        return to(from, toClass, false);
    }

    /**
     * 对象深度转换工具
     * <p>
     * 1、不同pojo类型数据之间转换
     * 2、不同容器类型数据之间转换，支持不同内部泛型类转换，如List<A>转HashSet<B>,HashMap<K,V>转LinkedHashMap<K1,V2>
     * 3、Map与pojo类型数据转换，并支持驼峰式与下划线式属性间的模糊匹配
     * 4、逗号分隔字符串与Collection、Array之间转换，如"str1,str2"转["str1","str2"]，且容器元素同样支持转换如”1,2,3“可转List<Long>等，同样支持json字符串解析
     * 5、日期字符串与日期Date类型转换，其不需要提供format模板，而是根据字符串内容动态分析准确日期，避免如pojo类型字符串与Date属性转换过程中提供额外的format参数
     * 6、字符串与枚举之间转换
     * 7、基本类型数据转换
     * 8、超类属性转换
     * 9、以上转换均支持各类型间的多层嵌套转换
     *
     * @param from    被转换对象
     * @param toClass 转换后的对象类型，利用匿名内部类方式传递该类型，以解决容器类泛型类解析问题
     * @param alias   是否启用Alias注解解析
     * @param <T>     转换后的对象类型泛型
     * @return 转换后的toClass类型对象
     */
    public static <T> T to(Object from, TypeReference<T> toClass, boolean alias) {
        T result;
        if (from == null) {
            return null;
        }
        if (toClass.isEnum()) {
            result = toEnum(from, toClass);
        } else if (toClass.isArray()) {
            result = toArray(from, toClass);
        } else if (toClass.isExtendsFrom(Collection.class)) {
            result = toCollection(from, toClass);
        } else if (toClass.isExtendsFrom(Map.class)) {
            result = toMap(from, toClass);
        } else if (toClass.isWrapOrPrimitive()) {
            // 基本类型转换
            result = (T) to(from, toClass.getWrapperClass());
        } else if (toClass.isExtendsFrom(Date.class)) {
            if (from instanceof Date) {
                result = (T) from;
            } else {
                // 日期类型转换
                GregorianCalendar calendar = DateUtil.parse(from.toString());
                if (calendar != null) {
                    result = (T) calendar.getTime();
                } else {
                    result = null;
                }
            }
        } else if (toClass.isAssignableFrom(from.getClass()) || toClass.getWrapperClass() == Object.class) {
            // 类型相同，直接返回
            result = (T) from;
        } else {
            // POJO类型转换
            try {
                result = toPOJO(from, (Class<T>) toClass.getType(), alias);
            } catch (Exception e) {
                result = null;
            }

            if (result == null) {
                try {
                    Constructor<T> construct = toClass.getConstruct(from.getClass());
                    result = construct.newInstance(from);
                } catch (Exception ignored) {
                }

            }

            if (result == null) {
                try {
                    Constructor<T> construct = toClass.getConstruct(String.class);
                    result = construct.newInstance(toString(from));
                } catch (Exception ignored) {
                }
            }
        }

        return result;
    }

    /**
     * 对象转字符串
     * 支持任意形式对象如容器、数组、枚举等
     *
     * @param from 被转换对象
     * @return 转换后的字符串
     */
    public static String toString(Object from) {
        String result;
        if (from.getClass().isArray()) {
            result = ArrayUtils.toString(from);
        } else if (Collection.class.isAssignableFrom(from.getClass())) {
            result = ArrayUtils.toString(((Collection<?>) from).toArray());
        } else if (Map.class.isAssignableFrom(from.getClass())) {
            result = from.toString();
        } else if (from.getClass().isEnum()) {
            result = ((Enum<?>) from).name();
        } else {
            result = from.toString();
        }
        return result;
    }


    /**
     * 转换为枚举类型
     *
     * @param from    被转换对象
     * @param toClass 目标类型
     * @param <T>     泛型
     * @return 转换后的对象
     */
    private static <T> T toEnum(Object from, TypeReference<T> toClass) {
        if (toClass.isEnum()) {
            try {
                Class<?> enumClass = toClass.getWrapperClass();

                String sourceName;

                if (from.getClass().isEnum()) {
                    sourceName = ((Enum) from).name();
                } else {
                    sourceName = from.toString();
                }
                Method values = enumClass.getMethod("values");
                if (!values.isAccessible()) {
                    values.setAccessible(true);
                }

                Enum<?>[] v = (Enum<?>[]) values.invoke(null);

                List<String> nameList = Stream.of(v).map(Enum::name).collect(Collectors.toList());
                String targetName = StringUtil.vagueMatches(sourceName, nameList);

                if (targetName != null) {
                    Method valueOf = enumClass.getMethod("valueOf", String.class);
                    if (!valueOf.isAccessible()) {
                        valueOf.setAccessible(true);
                    }
                    return (T) valueOf.invoke(null, targetName);
                } else {
                    HashMap<String, Enum<?>> map = Maps.newHashMapWithExpectedSize(v.length);
                    nameList = Stream.of(v)
                            .map(node -> map.put(node.toString(), node))
                            .filter(Objects::nonNull)
                            .map(Enum::toString)
                            .collect(Collectors.toList());
                    targetName = StringUtil.vagueMatches(sourceName, nameList);
                    return (T) map.get(targetName);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    /**
     * 转换为数组类型
     *
     * @param from    被转换对象
     * @param toClass 目标类型
     * @param <T>     泛型
     * @return 转换后的对象
     */
    private static <T> T toArray(Object from, TypeReference<T> toClass) {
        Object array = null;

        Class<?> innerClass = toClass.getWrapperClass().getComponentType();
        if (ClassUtil.isExtendsFrom(from.getClass(), Collection.class)) {
            array = Array.newInstance(innerClass, ((Collection<?>) from).size());
            int i = 0;
            for (Object node : (Collection<?>) from) {
                Array.set(array, i++, to(node, new TypeReference<Object>(innerClass) {
                }));
            }
        } else if (from.getClass().isArray()) {
            int length = Array.getLength(from);
            array = Array.newInstance(innerClass, length);

            for (int i = 0; i < length; i++) {
                Array.set(array, i, to(Array.get(from, i), new TypeReference<Object>(innerClass) {
                }));
            }
        } else if (from instanceof String) {
            try {
                JSONArray jsonArray = JSON.parseArray((String) from);
                return to(jsonArray, toClass);
            } catch (Exception e) {
                String[] strings = ((String) from).split(",");
                return toArray(strings, toClass);
            }
        }

        return (T) array;
    }

    /**
     * 转换为Map类型
     *
     * @param from    被转换对象
     * @param toClass 转换后的类型
     * @param <T>     泛型
     * @return 转换后的toClass类型对象
     */
    private static <T> T toMap(Object from, TypeReference<T> toClass) {
        if (toClass.isExtendsFrom(Map.class)) {
            if (ClassUtil.isExtendsFrom(from.getClass(), Collection.class) || from.getClass().isArray()) {
                return null;
            }
            Map<?, ?> map = MapUtil.parse(from);
            return (T) MapUtil.toMap((Map<Object, Object>) map, (TypeReference<Map<Object, Object>>) toClass);
        }
        return null;
    }

    /**
     * 转换为Collection集合类型
     *
     * @param from    转换对象
     * @param toClass 集合类型
     * @param <T>     泛型
     * @return 转换结果
     */
    private static <T> T toCollection(Object from, TypeReference<T> toClass) {
        if (toClass.isExtendsFrom(Collection.class)) {

            if (ClassUtil.isExtendsFrom(from.getClass(), Collection.class) || from.getClass().isArray()) {
                Type nodeType = toClass.getParameterizedType(0);
                if (nodeType == null) {
                    nodeType = Object.class;
                }

                Collection<?> collection;
                if ((toClass.getWrapperClass()).isInterface()) {
                    if (toClass.isExtendsFrom(Queue.class)) {
                        collection = new ArrayDeque<>();
                    } else if (toClass.isExtendsFrom(Set.class)) {
                        collection = new HashSet<>();
                    } else {
                        collection = new ArrayList<>();
                    }
                } else {
                    collection = (Collection<?>) ClassUtil.newInstance(toClass.getWrapperClass());
                }

                if (collection != null) {
                    if (ClassUtil.isExtendsFrom(from.getClass(), Collection.class)) {
                        for (Object o : (Collection<?>) from) {
                            collection.add(to(o, new TypeReference<>(nodeType)));
                        }
                    } else if (from.getClass().isArray()) {
                        for (Object o : (Object[]) from) {
                            collection.add(to(o, new TypeReference<>(nodeType)));
                        }
                    }
                }
                return (T) collection;
            } else if (from instanceof String) {
                try {
                    JSONArray array = JSON.parseArray((String) from);
                    return to(array, toClass);
                } catch (Exception e) {
                    String[] strings = ((String) from).split(",");
                    return toCollection(strings, toClass);
                }
            }
        }

        return null;
    }

    /**
     * 转换成POJO
     *
     * @param from    被转换对象
     * @param toClass 转换的目标POJO类型un
     * @param <T>     泛型
     * @return 转换后的POJO
     */
    private static <T> T toPOJO(Object from, Class<? extends T> toClass, boolean alias) {
        if (from == null) {
            return null;
        }
        final Class<?> sourceClass = from.getClass();
        if (toClass.isAssignableFrom(sourceClass)) {
            return (T) from;
        }
        if (Map.class.isAssignableFrom(sourceClass)) {
            Map<String, Object> map = (Map<String, Object>) from;

            T object = ClassUtil.newInstance(toClass);
            if (object != null) {

                ClassUtil.getAllField(toClass)
                        .parallelStream()
                        .forEach(field -> {
                            String key = StringUtil.vagueMatches(field.getName(), map.keySet());

                            if (key != null) {
                                try {
                                    Object value = map.get(key);
                                    setValue(object, field, value);
                                } catch (IllegalArgumentException ignored) {
                                }
                            }
                        });
                if (!isNotChange(object)) {
                    return object;
                }
            }
        } else if (from instanceof String) {
            try {
                Object json = JSON.parse((String) from);
                if (json instanceof JSON) {
                    return toPOJO(json, toClass, alias);
                }
            } catch (Exception ignored) {
            }
        } else if (sourceClass.isPrimitive() || Iterable.class.isAssignableFrom(sourceClass)) {
            return null;
        } else {
            T object = ClassUtil.newInstance(toClass);
            copyProperties(from, object, alias);
            if (!isNotChange(object)) {
                return object;
            }
        }
        return null;
    }

    /**
     * 基本类型转换
     *
     * @param from    将被转换的对象
     * @param toClass 转换成的目标类型
     * @param <T>     结果泛型
     * @return 转换后的结果
     */
    private static <T> T to(Object from, Class<T> toClass) {
        if (from == null) {
            return null;
        }

        Object temp = null;

        if (from.getClass().isArray() && Array.getLength(from) > 0) {
            from = Array.get(from, 0);
        }

        if (Collection.class.isAssignableFrom(from.getClass()) && !((Collection<?>) from).isEmpty()) {
            from = ((Collection<?>) from).iterator().next();
        }

        String valueStr = from.toString();
        if (NumberUtil.isNumber(toClass)) {
            Number number = NumberUtils.createNumber(valueStr);
            if (toClass == Short.class || toClass == short.class) {
                temp = number.shortValue();
            } else if (toClass == Integer.class || toClass == int.class) {
                temp = number.intValue();
            } else if (toClass == Long.class || toClass == long.class) {
                temp = number.longValue();
            } else if (toClass == Float.class || toClass == float.class) {
                temp = number.floatValue();
            } else if (toClass == Double.class || toClass == double.class) {
                temp = number.doubleValue();
            } else if (toClass == Byte.class || toClass == byte.class) {
                temp = number.byteValue();
            }
        } else if (toClass == Boolean.class || toClass == boolean.class) {
            if (NumberUtils.isParsable(valueStr)) {
                temp = NumberUtils.createNumber(valueStr).intValue() > 0;
            } else {
                temp = Boolean.parseBoolean(valueStr);
            }
        } else if (toClass == Byte.class || toClass == byte.class) {
            temp = Byte.parseByte(valueStr);
        } else if (toClass == Character.class || toClass == char.class) {
            char[] array = valueStr.toCharArray();
            temp = array.length > 0 ? array[0] : null;
        } else if (toClass == String.class) {
            temp = valueStr;
        }
        return (T) temp;
    }

    /**
     * 判断pojo对象属性是否全空
     * 该方法用于对象转换结果是否成功的判断
     *
     * @param object 判断属性是否全部为空的对象
     */
    public static boolean isNotChange(Object object) {
        if (object == null) {
            return true;
        }
        Class<?> clazz = object.getClass();
        try {
            Object newObject = clazz.newInstance();
            Set<Field> haveValueFields = ClassUtil.getAllField(clazz).parallelStream().filter(field -> {
                try {
                    if (SERIAL_VERSION_UID.equals(field.getName())) {
                        return false;
                    }

                    Object currentValue = field.get(object);
                    Object initValue = field.get(newObject);
                    return currentValue != null && !Objects.deepEquals(currentValue, initValue);
                } catch (Exception e) {
                    return false;
                }

            }).collect(Collectors.toSet());

            return haveValueFields.isEmpty();
        } catch (InstantiationException | IllegalAccessException e) {
            return false;
        }
    }

    /**
     * 为object对象的field字段设置value值
     * 方法按顺序执行以下各类属性值设置尝试，当尝试成功时立即返回
     * 1、调用方法入参类型与属性field类型相同的set方法尝试
     * 2、调用方法入参类型与参数value值类型相同set方法尝试
     * 3、调用同名set方法
     * 4、强制设值
     *
     * @param object 对象
     * @param field  字段
     * @param value  值
     */
    public static void setValue(Object object, Field field, Object value) {
        Class<?> objectClass = object.getClass();

        final Class<?> typeTpye = field.getType();
        if (value != null) {
            FieldInfo fieldInfo = ClassInfo.getCache(objectClass).getFieldInfo(field);

            // 如果set方法还未初始化，则开始初始化
            if (fieldInfo.isNoSetters() == null) {
                final String fieldName = field.getName();
                String setMethodName = "set" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);

                List<Method> list = ClassUtil.getAllMethod(objectClass).stream()
                        .filter(method -> setMethodName.equals(method.getName()) && method.getParameterCount() == 1)
                        .sorted((a, b) -> {
                            int result = b.getName().compareTo(a.getName());
                            if (result == 0) {
                                int aI = typeTpye == a.getParameterTypes()[0] ? 1 : 0;
                                int bI = typeTpye == b.getParameterTypes()[0] ? 1 : 0;
                                result = bI - aI;
                            }
                            return result;
                        })
                        .map(method -> {
                            fieldInfo.putSetter(method);
                            return method;
                        })
                        .collect(Collectors.toList());

                // 设置该属性是否具备set方法
                fieldInfo.setNoSetters(list.isEmpty());
            }

            // set方法初始化后，如果没有set方法之际调用set
            if (Boolean.FALSE.equals(fieldInfo.isNoSetters())) {
                // 如果存在set方法，则挨个调用
                List<Method> setters = fieldInfo.setters();
                Object initValue = null;
                try {
                    initValue = field.get(object);
                } catch (Exception ignored) {
                }
                for (Method setter : setters) {
                    try {
                        invokeMethodIfParamNotNull(object, setter, value);
                        Object newValue = field.get(object);
                        if (newValue != null && !newValue.equals(initValue)) {
                            // 新增设置值方法缓存
                            fieldInfo.putSetter(setter);
                            return;
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            try {
                //将来值类型与属性类型相同时，直接设置
                setValueIfNotNull(object, field, value);
            } catch (Exception ignored) {
            }
        } else {
            if (!typeTpye.isPrimitive()) {
                try {
                    field.set(object, null);
                } catch (Exception ignored) {
                }
            }
        }

    }

    /**
     * 如果值转换不为空情况下，强调用
     *
     * @param object 对象
     * @param method 方法
     * @param value  值
     */
    private static void invokeMethodIfParamNotNull(Object object, Method method, Object value) throws InvocationTargetException, IllegalAccessException {
        Type[] parameterTypes = method.getGenericParameterTypes();
        if (parameterTypes.length == 1) {
            Object v = to(value, new TypeReference<>(parameterTypes[0]));
            if (value != null && v == null) {
                throw new RuntimeException();
            }
            method.invoke(object, v);
        }
    }

    /**
     * 如果value值转换为目标属性field类型后，不为空情况时，强设置值。否则不操作
     *
     * @param object 对象
     * @param field  属性
     * @param value  值
     */
    public static void setValueIfNotNull(Object object, Field field, Object value) throws IllegalAccessException {
        Object v = to(value, new TypeReference<>(field.getGenericType()));
        if (value != null && v == null) {
            throw new RuntimeException();
        }
        field.set(object, v);
    }

    /**
     * 值比对方式
     */
    public enum Compare {
        /**
         * 相同
         */
        SAME,
        /**
         * 值不同的
         */
        DIFF,
        /**
         * source和target比对的字段都不空,并且值不相同的
         */
        DIFF_ALL_NOT_NULL,
        /**
         * source是空,并且值不相同的
         */
        DIFF_SOURCE_NULL,
        /**
         * target是空,并且值不相同的
         */
        DIFF_TARGET_NULL,
        /**
         * target属性值是默认值的,并且值不相同的
         */
        DIFF_TARGET_DEFAULT,
        /**
         * target属性值是默认值的,source属性不为空的,并且值不相同的
         */
        DIFF_SOURCE_NOT_NULL_AND_TARGET_DEFAULT,
        /**
         * source属性值不空的,并且值不相同的
         */
        DIFF_SOURCE_NOT_NULL,
        /**
         * target属性值不空的,并且值不相同的
         */
        DIFF_TARGET_NOT_NULL

    }

    /**
     * 包含或者排除
     */
    public enum ContainOrExclude {
        /**
         * 包含
         */
        INCLUDE,
        /**
         * 不包含
         */
        EXCLUDE
    }

    /**
     * 对象属性拷贝
     *
     * @param source  从哪个对象
     * @param target  拷贝到哪个对象
     * @param compare 拷贝方式中的属性对比方式
     *                <p>
     *                SAME:相同
     *                DIFF:值不同的
     *                DIFF_ALL_NOT_NULL:source和target比对的字段都不空,并且值不相同的
     *                DIFF_SOURCE_NULL:source是空,并且值不相同的
     *                DIFF_TARGET_NULL:target是空,并且值不相同的
     *                DIFF_TARGET_DEFAULT:target属性值是默认值的,并且值不相同的
     *                DIFF_SOURCE_NOT_NULL_AND_TARGET_DEFAULT:target属性值是默认值的,source属性不为空的,并且值不相同的
     *                DIFF_SOURCE_NOT_NULL:source属性值不空的,并且值不相同的
     *                DIFF_TARGET_NOT_NULL:target属性值不空的,并且值不相同的
     */
    public static void copyProperties(Object source, Object target, Compare compare) {
        copyProperties(source, target, Constant.RegularAbout.BLANK, Constant.RegularAbout.BLANK, new String[]{}, ContainOrExclude.EXCLUDE, compare, false);
    }

    /**
     * 通过不同的比较方式，获取同名属性集合
     *
     * @param source  比较对象
     * @param target  比较对象
     * @param compare 比对方式
     * @return 属性名集合
     */
    private static Map<Field, Set<Field>> getSameField(Object source, Object target, String prefix, String suffix, Compare compare, boolean isAlias) {
        Map<Field, Set<Field>> result = Maps.newConcurrentMap();
        if (ObjectUtils.isEmpty(source) || ObjectUtils.isEmpty(target)) {
            return result;
        }
        if (isAlias) {
            result = getSameFieldByAlias(source.getClass(), target.getClass(), prefix, suffix);
        } else {
            result = getSameFieldByBlurry(source.getClass(), target.getClass(), prefix, suffix);
        }

        if (result.isEmpty()) {
            return result;
        }
        //根据比较规则，过滤字段映射情况
        parseCompare(source, target, compare, result);
        return result;
    }

    /**
     * 处理Compare，更具Compare比较规则，提取字段映射
     *
     * @param source  从对象
     * @param target  到对象
     * @param compare 比较规则
     * @param result  字段映射情况，key为从对象字段，value为到对象字段集合
     */
    private static void parseCompare(Object source, Object target, Compare compare, Map<Field, Set<Field>> result) {
        Object targetNew = null;
        try {
            targetNew = target.getClass().newInstance();
        } catch (Exception ignored) {

        }

        Object finalTargetNew = targetNew;
        result.entrySet().parallelStream().forEach(e -> {
            Field fromField = e.getKey();
            Set<Field> toFields = e.getValue();

            Iterator<Field> it = toFields.iterator();
            while (it.hasNext()) {
                Field toField = it.next();

                Object sourceValue = getFieldValue(source, fromField);
                Object targetValue = getFieldValue(target, toField);
                switch (compare) {
                    case DIFF_ALL_NOT_NULL:
                        if (sourceValue != null && targetValue != null && !equals(sourceValue, targetValue)) {
                            continue;
                        }
                        break;
                    case DIFF_TARGET_NULL:
                        if (sourceValue != null && targetValue == null) {
                            continue;
                        }
                        break;
                    case DIFF_SOURCE_NULL:
                        if (sourceValue == null && targetValue != null) {
                            continue;
                        }
                        break;
                    case DIFF_SOURCE_NOT_NULL:
                        if (sourceValue != null && !equals(sourceValue, targetValue)) {
                            continue;
                        }
                        break;
                    case DIFF_TARGET_NOT_NULL:
                        if (targetValue != null && !equals(sourceValue, targetValue)) {
                            continue;
                        }
                        break;
                    case DIFF_SOURCE_NOT_NULL_AND_TARGET_DEFAULT:
                        if (finalTargetNew == null) {
                            throw new RuntimeException(String.format("目标对象创建失败，请检查类“%s”是否包含空参构造函数", target.getClass()));
                        } else if (sourceValue != null && !equals(sourceValue, targetValue) && equals(getFieldValue(finalTargetNew, toField), targetValue)) {
                            continue;
                        }
                        break;
                    case DIFF_TARGET_DEFAULT:
                        if (finalTargetNew == null) {
                            throw new RuntimeException(String.format("目标对象创建失败，请检查类“%s”是否包含空参构造函数", target.getClass()));
                        } else if (!equals(sourceValue, targetValue) && equals(getFieldValue(finalTargetNew, toField), targetValue)) {
                            continue;
                        }
                        break;
                    case DIFF:
                        if (!equals(sourceValue, targetValue)) {
                            continue;
                        }
                        break;
                    case SAME:
                        if (equals(sourceValue, targetValue)) {
                            continue;
                        }
                        break;
                    default:
                }
                it.remove();
            }

        });
    }

    /**
     * 判断俩对象是否相等
     *
     * @param object1 对象一
     * @param object2 对象二
     * @return 是否
     */
    public static boolean equals(Object object1, Object object2) {
        if (object1 == object2) {
            return true;
        } else {
            return object1 != null && object1.equals(object2);
        }
    }

    public static Map<Field, Set<Field>> getSameFieldByBlurry(Class<?> sourceClass, Class<?> targetClass) {
        return getSameFieldByBlurry(sourceClass, targetClass, Constant.RegularAbout.BLANK, Constant.RegularAbout.BLANK);
    }

    /**
     * 模糊匹配方式匹配，提取两个类型属性之间得映射关系
     *
     * @param sourceClass 比较对象
     * @param targetClass 比较对象
     * @param prefix      只处理属性前缀为prefix的属性
     * @param suffix      只处理属性后缀为suffix的属性
     * @return 同名属性集合
     */
    public static Map<Field, Set<Field>> getSameFieldByBlurry(Class<?> sourceClass, Class<?> targetClass, String prefix, String suffix) {
        if (ObjectUtils.isEmpty(sourceClass) || ObjectUtils.isEmpty(targetClass)) {
            return null;
        }
        assert prefix != null;
        assert suffix != null;

        Map<Field, Set<Field>> result = Maps.newConcurrentMap();
        if (sourceClass == targetClass) {
            ClassUtil.getAllField(sourceClass)
                    .parallelStream()
                    .filter(field -> field.getName().startsWith(prefix) && field.getName().endsWith(prefix))
                    .forEach(field -> result.put(field, Sets.newHashSet(field)));
        } else {
            ClassUtil.getAllField(targetClass).parallelStream().forEach(toField -> {
                String name = toField.getName();

                Field fromField = ClassUtil.getField(sourceClass, name);
                if (fromField != null && fromField.getName().startsWith(prefix) && fromField.getName().endsWith(suffix)) {
                    Set<Field> set = result.get(fromField);
                    if (set == null) {
                        set = Sets.newHashSet();
                    }
                    set.add(toField);
                    result.put(fromField, set);
                }
            });
        }
        return result;
    }

    public static Map<Field, Set<Field>> getSameFieldByAlias(Class<?> sourceClass, Class<?> targetClass) {
        return getSameFieldByAlias(sourceClass, targetClass, Constant.RegularAbout.BLANK, Constant.RegularAbout.BLANK);
    }

    /**
     * 根据别名、属性名，提取两个类型属性映射关系
     *
     * @param sourceClass 从
     * @param targetClass 到
     * @param prefix      只处理属性前缀为prefix的属性
     * @param suffix      只处理属性后缀为suffix的属性
     * @return 属性映射关系
     */
    public static Map<Field, Set<Field>> getSameFieldByAlias(Class<?> sourceClass, Class<?> targetClass, String prefix, String suffix) {
        if (ObjectUtils.isEmpty(sourceClass) || ObjectUtils.isEmpty(targetClass)) {
            return null;
        }
        assert prefix != null;
        assert suffix != null;

        Map<Field, Set<Field>> map = Maps.newConcurrentMap();
        Map<Field, Set<String>> sourceMap = parseFieldAlias(sourceClass);
        Map<Field, Set<String>> targetMap = parseFieldAlias(targetClass);

        sourceMap.entrySet().parallelStream().filter(e -> {
            final String name = e.getKey().getName();
            return name.startsWith(prefix) && name.endsWith(suffix);
        }).forEach(e -> {
            Set<String> sourceAlias = e.getValue();
            Set<Field> set = targetMap.entrySet().stream().filter(te ->
                    !CollectionUtils.retainAll(te.getValue(), sourceAlias).isEmpty()
            ).map(Map.Entry::getKey).collect(Collectors.toSet());
            map.put(e.getKey(), set);
        });

        return map;
    }

    /**
     * 整理类属性的别名
     *
     * @param sourceClass 将被整理的类
     * @return
     */
    public static Map<Field, Set<String>> parseFieldAlias(Class<?> sourceClass) {
        Map<Field, Set<String>> sourceMap = Maps.newConcurrentMap();
        Set<ClassUtil.Target<Alias>> sourceAnnotations = ClassUtil.getAllEntityAnnotation(sourceClass, Alias.class);

        ClassUtil.getAllField(sourceClass).forEach(field -> {
            ClassUtil.Target<Alias> an = sourceAnnotations.stream().filter(sourceAnnotation -> sourceAnnotation.getMember() == field).findFirst().orElse(null);
            Set<String> aliases;
            if (an != null) {
                aliases = Sets.newHashSet(an.getAnnotation().value());
                aliases.add(field.getName());
            } else {
                aliases = Sets.newHashSet(field.getName());
            }

            sourceMap.put(field, aliases);
        });
        return sourceMap;
    }

    /**
     * 对象属性拷贝
     *
     * @param source           从哪个对象
     * @param target           拷贝到哪个对象
     * @param arguments        属性名集合
     * @param containOrExclude INCLUDE时，则拷贝arguments中的属性；EXCLUDE时，则拷贝arguments以外的属性。
     */
    public static void copyProperties(Object source, Object target, String[] arguments, ContainOrExclude containOrExclude) {
        copyProperties(source, target, Constant.RegularAbout.BLANK, Constant.RegularAbout.BLANK, arguments, containOrExclude, Compare.DIFF, false);
    }

    /**
     * 对象属性拷贝
     *
     * @param source 从哪个对象
     * @param target 拷贝到哪个对象
     * @param prefix 要拷贝属性的前缀，仅拷贝符合该前缀的属性
     * @param suffix 要拷贝属性的后缀，仅拷贝符合该后缀的属性
     */
    public static void copyProperties(Object source, Object target, String prefix, String suffix) {
        copyProperties(source, target, prefix, suffix, new String[]{}, ContainOrExclude.EXCLUDE, Compare.DIFF, false);
    }

    /**
     * 复制对象属性
     * 仅拷贝属性值不相同属性
     *
     * @param source 原对象
     * @param target 新对象
     */
    public static void copyProperties(Object source, Object target) {
        copyProperties(source, target, Constant.RegularAbout.BLANK, Constant.RegularAbout.BLANK, new String[]{}, ContainOrExclude.EXCLUDE, Compare.DIFF, false);

    }

    /**
     * 复制对象属性
     * 仅拷贝属性值不相同属性
     *
     * @param source 原对象
     * @param target 新对象
     * @param alias  是否识别别名注解@Alias
     */
    public static void copyProperties(Object source, Object target, boolean alias) {
        copyProperties(source, target, Constant.RegularAbout.BLANK, Constant.RegularAbout.BLANK, new String[]{}, ContainOrExclude.EXCLUDE, Compare.DIFF, alias);
    }

    /**
     * 复制对象属性
     *
     * @param source           从哪个对象
     * @param target           复制到哪个对象
     * @param prefix           要拷贝属性的前缀，仅拷贝符合该前缀的属性
     * @param suffix           要拷贝属性的后缀，仅拷贝符合该后缀的属性
     * @param arguments        属性列表
     * @param containOrExclude 包含或排除
     */
    public static void copyProperties(Object source, Object target, String prefix, String suffix, String[] arguments, ContainOrExclude containOrExclude, Compare compare, boolean isAlias) {
        if (ObjectUtils.isEmpty(source) || ObjectUtils.isEmpty(target)) {
            return;
        }

        Map<Field, Set<Field>> argumentsMap = getSameField(source, target, prefix, suffix, compare, isAlias);
        argumentsMap.entrySet()
                .parallelStream()
                .forEach(e -> {
                    try {
                        final Field fromField = e.getKey();

                        switch (containOrExclude) {
                            case EXCLUDE:
                                if (ArrayUtils.contains(arguments, fromField.getName())) {
                                    return;
                                }
                                break;
                            case INCLUDE:
                                if (!ArrayUtils.contains(arguments, fromField.getName())) {
                                    return;
                                }
                                break;
                            default:
                        }

                        Object value = fromField.get(source);

                        Set<Field> toFields = e.getValue();
                        for (Field toField : toFields) {
                            Type type = toField.getGenericType();
                            TypeReference<Object> typeReference = new TypeReference<>(type);
                            if (value != null) {
                                value = to(value, typeReference);
                            }
                            setValue(target, toField, value);
                        }

                    } catch (Exception ignored) {

                    }
                });
    }

    /**
     * 取属性值
     * 取值方式为优先使用属性get方法，当未定义get方法时，则直接通过反射获取属性值
     *
     * @param o         对象
     * @param fieldName 属性名
     * @return 属性值
     */
    public static Object getFieldValue(Object o, String fieldName) {
        Field field = ClassUtil.getField(o.getClass(), fieldName);
        if (field == null) {
            return null;
        }
        return getFieldValue(o, field);
    }

    /**
     * 取属性值
     * 取值方式为优先使用属性get方法，当未定义get方法时，则直接通过反射获取属性值
     *
     * @param o     对象
     * @param field 属性
     * @return 值
     */
    public static Object getFieldValue(Object o, Field field) {
        if (field == null) {
            return null;
        }
        final FieldInfo fieldInfo = ClassInfo.getCache(o.getClass()).getFieldInfo(field);

        // 判断get方法是否初始化
        if (fieldInfo.isNoGetters() == null) {
            List<Method> methods;

            final String fieldName = field.getName();
            String getMethodName = "get" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1);
            if (field.getType() == Boolean.TYPE) {
                String getMethodName2 = "is" + StringUtil.toUpperName(field.getName());
                methods = ClassInfo.getCache(o.getClass()).getAllMethod().stream().filter(method ->
                        (method.getName().equals(getMethodName) || method.getName().equals(getMethodName2)) && method.getParameterCount() == 0)
                        .map(method -> {
                            fieldInfo.putGetter(method);
                            return method;
                        }).collect(Collectors.toList());
            } else {
                methods = ClassInfo.getCache(o.getClass()).getAllMethod().stream().filter(method ->
                        method.getName().equals(getMethodName) && method.getParameterCount() == 0)
                        .map(method -> {
                            fieldInfo.putGetter(method);
                            return method;
                        }).collect(Collectors.toList());
            }
            if (!methods.isEmpty()) {
                for (Method method : methods) {
                    try {
                        Object v = method.invoke(o);

                        //调整getter优先级
                        fieldInfo.putGetter(method);
                        return v;
                    } catch (Exception ignored) {
                    }
                }
            } else {
                fieldInfo.setNoGetters(true);
            }
        }
        if (Boolean.FALSE.equals(fieldInfo.isNoGetters())) {
            List<Method> getters = fieldInfo.getters();
            for (Method getter : getters) {
                try {
                    Object v = getter.invoke(o);

                    //调整getter优先级
                    fieldInfo.putGetter(getter);
                    return v;
                } catch (Exception ignored) {
                }
            }
        }
        try {
            return field.get(o);
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 从Map对象中获取指定类型对象，也可以达到Map转POJO对象的效果
     *
     * @param clazz 想要获取的对象类型
     * @param map   Map结构属性集合
     * @return 返回指定对象类型对象
     */
    public static <T> T getObjectFromMap(Class<T> clazz, Map<?, ?> map) {
        return getObjectFromMap(clazz, map, "", "");
    }

    /**
     * 从Map对象中获取指定类型对象，也可以达到Map转POJO对象的效果
     *
     * @param clazz  想要获取的对象类型
     * @param map    Map结构属性集合
     * @param prefix 前缀，转换前先根据前缀过滤Map的key值，仅针对过滤后的Map.Entry做转换操作
     * @return 返回指定对象类型对象
     */
    public static <T> T getObjectFromMap(Class<T> clazz, Map<String, Object> map, String prefix) {
        return getObjectFromMap(clazz, map, prefix, "");
    }

    /**
     * 从Map对象中获取指定类型对象，也可以达到Map转POJO对象的效果
     *
     * @param clazz  想要获取的对象类型
     * @param map    Map结构属性集合
     * @param prefix 前缀，转换前先根据前缀过滤Map的key值，仅针对过滤后的Map.Entry做转换操作
     * @param suffix 后缀，转换前先根据缀过滤Map的key值，仅针对过滤后的Map.Entry做转换操作
     * @return 返回指定对象类型对象
     */
    public static <T> T getObjectFromMap(Class<T> clazz, Map<?, ?> map, String prefix, String suffix) {
        if (Map.class.isAssignableFrom(clazz)) {
            return (T) map;
        }
        if (!ObjectUtils.isEmpty(map)) {
            try {
                T object = clazz.newInstance();

                Set<Field> fields = ClassUtil.getAllField(clazz);
                fields.parallelStream().forEach(field -> {
                    String key = coverFieldNameToMapKey(clazz, field, prefix, suffix, map);
                    if (key != null) {
                        try {
                            Object value = map.get(key);

                            setValue(object, field, value);
                        } catch (IllegalArgumentException ignored) {
                        }
                    }
                });
                if (!isAllNullValidity(object)) {
                    return object;
                }
            } catch (InstantiationException | IllegalAccessException ignored) {
            }
        }
        return null;
    }

    /**
     * 在map集中，根据field属性名字或Alias别名注解，推断与属性对应的key值
     * 该方法常用于Map转POJO对象使用，如持久层组件查询结果为List<Map<K,V>>结构转List<POJO>时使用
     *
     * @param clazz  类
     * @param field  字段
     * @param prefix 前缀
     * @param suffix 后缀
     * @param map    集合
     * @return key值
     */
    public static String coverFieldNameToMapKey(Class<?> clazz, Field field, String prefix, String suffix, Map<?, ?> map) {
        String key = null;

        try {
            Alias column = getAllEntityPropertyAnnotation(clazz, field, Alias.class);
            if (column != null) {
                for (String alias : column.value()) {
                    if (map.containsKey(alias)) {
                        key = alias;
                        break;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        if (key != null) {
            return key;
        }

        String propertyName = prefix + field.getName() + suffix;

        String propertyRegex = StringUtil.camelToMatchesRegex(propertyName);
        Set<String> keys = new HashSet<>();
        for (Object mapKey : map.keySet()) {
            if (PatternUtil.matches(propertyRegex, mapKey.toString(), Pattern.CASE_INSENSITIVE)) {
                keys.add(mapKey.toString());
            }
        }

        if (keys.size() > 1) {
            if (keys.contains(propertyName)) {
                key = propertyName;
            } else {
                String camelToUnderlineKey = StringUtil.toUnderline(propertyName);
                String camelToUnderlineKeyUpper = camelToUnderlineKey.toUpperCase();
                String camelToUnderlineKeyLower = camelToUnderlineKey.toLowerCase();

                if (keys.contains(camelToUnderlineKey)) {
                    key = camelToUnderlineKey;
                } else if (keys.contains(camelToUnderlineKeyUpper)) {
                    key = camelToUnderlineKeyUpper;
                } else if (keys.contains(camelToUnderlineKeyLower)) {
                    key = camelToUnderlineKeyLower;
                }
            }
        } else if (keys.size() == 1) {
            key = keys.iterator().next();
        }


        return key;
    }

    /**
     * 获取所有属性注解,包括属性对应的get方法，其中能类似于JPA注解的解析过程
     *
     * @param clazz      类
     * @param field      属性
     * @param annotation 要取的注解
     * @return 注解结果集
     */
    public static <T extends Annotation> T getAllEntityPropertyAnnotation(Class<?> clazz, Field field, Class<T> annotation) throws NoSuchMethodException {
        T result = null;
        T fieldDeclaredAnnotations = field.getDeclaredAnnotation(annotation);
        if (fieldDeclaredAnnotations != null) {
            result = fieldDeclaredAnnotations;
        }

        T fieldAnnotations = field.getAnnotation(annotation);
        if (fieldAnnotations != null) {
            result = fieldAnnotations;
        }

        String getMethodName = String.format("get%s", StringUtil.toUpperName(field.getName()));
        Method declaredMethod = clazz.getDeclaredMethod(getMethodName);
        T methodDeclaredAnnotations = declaredMethod.getDeclaredAnnotation(annotation);
        if (methodDeclaredAnnotations != null) {
            result = methodDeclaredAnnotations;
        }

        Method method = clazz.getMethod(getMethodName);
        T methodAnnotations = method.getAnnotation(annotation);
        if (methodAnnotations != null) {
            result = methodAnnotations;
        }

        return result;
    }

    /**
     * 判断对象属性经过初始化后，是否改变
     *
     * @param object 对象
     */
    public static boolean isAllNullValidity(Object object) {
        Class<?> clazz = object.getClass();
        try {
            Object newObject = clazz.newInstance();
            Set<Field> haveValueFields = ClassUtil.getAllField(clazz).parallelStream().filter(field -> {
                try {
                    if (SERIAL_VERSION_UID.equals(field.getName())) {
                        return false;
                    }
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    Object currentValue = field.get(object);
                    Object initValue = field.get(newObject);
                    return currentValue != null && currentValue != initValue;
                } catch (Exception e) {
                    return false;
                }

            }).collect(Collectors.toSet());

            return haveValueFields.isEmpty();
        } catch (InstantiationException | IllegalAccessException e) {
            return false;
        }
    }

    /**
     * 对象转json字符串，类似于自定义的toString
     *
     * @param o       pojo对象
     * @param exclude 排除转换的字段名字
     * @return 字符串
     */
    public static String objectToString(Object o, String... exclude) {
        Class<?> clazz = o.getClass();
        StringBuilder target = new StringBuilder(clazz.getSimpleName()).append("{");
        Set<Field> fields = ClassUtil.getAllField(clazz);
        int i = 0;
        for (Field field : fields) {
            try {
                if (ArrayUtils.contains(exclude, field.getName())) {
                    i++;
                    continue;
                }
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                if (i != 0) {
                    target.append(", ");
                }
                target.append(field.getName()).append("='").append(field.get(o));
                if (i == fields.size() - 1) {
                    target.append('}');
                } else {
                    target.append('\'');
                }
            } catch (IllegalAccessException e) {
                continue;
            }
            i++;
        }
        return target.toString();
    }

    /**
     * 对象转Map结构，属性名在Map结构中映射为下划线式key值
     *
     * @param o 对象
     * @return Map
     */
    public static Map<String, Object> getUnderlineMapFromObject(Object o) {
        Set<Field> fields = ClassUtil.getAllField(o.getClass());
        Map<String, Object> result = new HashMap<>(fields.size());
        if (!fields.isEmpty()) {
            fields.parallelStream().forEach(field -> {
                String key = StringUtil.toUnderline(field.getName());
                try {
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    result.put(key, field.get(o));
                } catch (IllegalAccessException ignored) {
                }
            });
        }
        return result;
    }

    /**
     * 对象集转Map结构，属性名在Map结构中映射为下划线式key值
     *
     * @param list 对象集合
     * @return map集合
     */
    public static List<Map<String, Object>> getUnderlineMapFromListObject(Iterable<Object> list) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (list != null) {
            for (Object o : list) {
                result.add(getUnderlineMapFromObject(o));
            }
        }

        return result;
    }

    /**
     * 比较两个对象属性是否相同
     *
     * @param source 源对象
     * @param target 目标对象
     * @return 是否相同
     */
    public static boolean compare(Object source, Object target) {
        return isEmpty(source) && isEmpty(target) || source.equals(target);
    }

    /**
     * 获取两个同类型对象的同属性不同值信息列表
     * 该方法可用于比较两个同类型对象的不同情况，例如可用于表变动操作日志记录
     *
     * @param source          源对象
     * @param target          目标对象
     * @param excludeProperty 排除的属性
     * @return 值不相同的属性列表
     * @throws IllegalAccessException 调用过程异常
     */
    public static List<Different> getDifferenceProperties(Object source, Object target, String... excludeProperty) throws IllegalAccessException {

        List<Different> result = new ArrayList<>();

        if ((!ClassUtil.compareClass(source, target) || compare(source, target) || isEmpty(source)) != isEmpty(target)) {
            return result;
        }

        Object sourceObject = isEmpty(source) ? target : source;
        Object targetObject = isEmpty(source) ? source : target;
        Class<?> sourceClass = sourceObject.getClass();
        ClassUtil.getAllField(sourceClass)
                .parallelStream()
                .forEach(field -> {
                    if (excludeProperty != null && ArrayUtils.contains(excludeProperty, field.getName())) {
                        return;
                    }
                    if (!field.isAccessible()) {
                        field.setAccessible(true);
                    }
                    Object sourceValue;
                    Object targetValue;
                    try {
                        sourceValue = field.get(sourceObject);
                        targetValue = field.get(targetObject);
                    } catch (IllegalAccessException e) {
                        return;
                    }

                    if (compare(sourceValue, targetValue)) {
                        return;
                    }

                    result.add(new Different(field.getName(), field.getType().getTypeName(), String.valueOf(targetValue), String.valueOf(sourceValue)));
                });

        return result;
    }

    /**
     * 比较两个对象属性是否相同
     *
     * @param source          源对象
     * @param target          目标对象
     * @param excludeProperty 排除的比较属性
     * @return 是否相同
     */
    public static boolean compareValue(Object source, Object target, String... excludeProperty) {
        if (isEmpty(source)) {
            return isEmpty(target);
        } else {
            if (isEmpty(target)) {
                return false;
            }
            try {
                List<Different> list = getDifferenceProperties(source, target, excludeProperty);
                if (!ObjectUtils.isEmpty(list)) {
                    return false;
                }
            } catch (IllegalAccessException ignored) {
            }
        }
        return true;
    }

    /**
     * 区别信息
     */
    @Data
    @AllArgsConstructor
    public static class Different {
        /**
         * 属性名
         */
        private final String propertyName;
        /**
         * 属性类型
         */
        private final String propertyType;
        /**
         * 新值
         */
        private final String newValue;
        /**
         * 旧值
         */
        private final String oldValue;
    }

    /**
     * 只比较source中不为空的字段
     *
     * @param source 原对象
     * @param target 目标对象
     * @return 是否
     */
    public static boolean compareOfNotNull(Object source, Object target) {
        Field[] fields = source.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (SERIAL_VERSION_UID.equals(field.getName())) {
                continue;
            }
            try {
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                Object sourceValue = field.get(source);
                if (sourceValue == null) {
                    continue;
                }
                Object targetValue = field.get(target);
                if (!sourceValue.equals(targetValue)) {
                    return false;
                }
            } catch (Exception ignored) {
            }
        }
        return true;
    }
}
