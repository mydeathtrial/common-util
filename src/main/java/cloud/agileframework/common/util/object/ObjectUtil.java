package cloud.agileframework.common.util.object;

import cloud.agileframework.common.annotation.Alias;
import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.clazz.ClassUtil;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.date.DateUtil;
import cloud.agileframework.common.util.map.MapUtil;
import cloud.agileframework.common.util.number.NumberUtil;
import cloud.agileframework.common.util.pattern.PatternUtil;
import cloud.agileframework.common.util.string.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
     * @param <T>     转换后的对象类型泛型
     * @return 转换后的toClass类型对象
     */
    public static <T> T to(Object from, TypeReference<T> toClass) {
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
            result = toPOJO(from, (Class<T>) toClass.getType());

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
                Method values = enumClass.getMethod("values");
                values.setAccessible(true);
                Enum<?>[] v = (Enum<?>[]) values.invoke(null);

                List<String> nameList = Stream.of(v).map(Enum::name).collect(Collectors.toList());
                String targetName = StringUtil.vagueMatches(from.toString(), nameList);

                if (targetName != null) {
                    Method valueOf = enumClass.getMethod("valueOf", String.class);
                    valueOf.setAccessible(true);
                    return (T) valueOf.invoke(null, targetName);
                } else {
                    HashMap<String, Enum<?>> map = Maps.newHashMapWithExpectedSize(v.length);
                    nameList = Stream.of(v)
                            .map(node -> map.put(node.toString(), node))
                            .filter(Objects::nonNull)
                            .map(Enum::toString)
                            .collect(Collectors.toList());
                    targetName = StringUtil.vagueMatches(from.toString(), nameList);
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
    private static <T> T toPOJO(Object from, Class<? extends T> toClass) {

        if (from == null || toClass.isAssignableFrom(from.getClass())) {
            return (T) from;
        }
        final Class<?> sourceClass = from.getClass();
        if (sourceClass.isPrimitive() || Iterable.class.isAssignableFrom(sourceClass)) {
            return null;
        }
        if (ClassUtil.isExtendsFrom(sourceClass, Map.class)) {
            Map<String, Object> map = (Map<String, Object>) from;

            T object = ClassUtil.newInstance(toClass);
            if (object != null) {
                Set<Field> fields = ClassUtil.getAllField(toClass);
                fields.forEach(field -> {
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
            Object json = JSON.parse((String) from);
            if (json instanceof JSON) {
                return toPOJO(json, toClass);
            }
        } else {
            T object = ClassUtil.newInstance(toClass);
            copyProperties(from, object);
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
            Set<Field> haveValueFields = ClassUtil.getAllField(clazz).stream().filter(field -> {
                field.setAccessible(true);
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
        object = Optional.ofNullable(object).orElseThrow(IllegalArgumentException::new);

        field.setAccessible(true);
        Class<?> objectClass = object.getClass();
        String fieldName = field.getName();
        Optional<Object> optional = Optional.ofNullable(value);
        if (optional.isPresent()) {
            Type fieldType = field.getGenericType();
            String setMethodName = "set" + StringUtil.toUpperName(fieldName);
            Method setMethod;
            //取默认值
            Object initValue = null;
            try {
                initValue = field.get(object);
            } catch (Exception ignored) {
            }

            //取方法入参类型与属性类型相同方法尝试
            try {
                if (ParameterizedType.class.isAssignableFrom(fieldType.getClass())) {
                    Type rawType = ((ParameterizedType) fieldType).getRawType();
                    setMethod = ClassUtil.getMethod(objectClass, setMethodName, (Class<?>) rawType);
                    invokeMethodIfParamNotNull(object, setMethod, to(value, new TypeReference<>(fieldType)));
                } else {
                    setMethod = ClassUtil.getMethod(objectClass, setMethodName, (Class<?>) fieldType);

                    invokeMethodIfParamNotNull(object, setMethod, value);
                }

            } catch (Exception ignored) {
            }

            //取方法入参类型与值类型相同方法尝试
            try {
                if (Objects.equals(field.get(object), initValue)) {
                    setMethod = ClassUtil.getMethod(objectClass, setMethodName, value.getClass());
                    invokeMethodIfParamNotNull(object, setMethod, value);
                }
            } catch (Exception ignored) {
            }

            //终极解决方式
            try {
                if (Objects.equals(field.get(object), initValue)) {
                    List<Method> list = Arrays.stream(objectClass.getMethods())
                            .filter(method -> setMethodName.equals(method.getName()))
                            .collect(Collectors.toList());

                    for (Method method : list) {
                        try {
                            invokeMethodIfParamNotNull(object, method, value);
                            if (field.get(object) != initValue) {
                                return;
                            }
                        } catch (Exception ignored) {
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            try {
                if (Objects.equals(field.get(object), initValue)) {
                    //将来值类型与属性类型相同时，直接设置
                    setValueIfNotNull(object, field, value);
                }
            } catch (Exception ignored) {
            }

        } else {
            if (!field.getType().isPrimitive()) {
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
    private static void invokeMethodIfParamNotNull(Object object, Method method, Object value) {
        if (object == null || method == null || value == null) {
            return;
        }
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes.length == 1) {
            Optional.ofNullable(to(value, new TypeReference<>(parameterTypes[0])))
                    .ifPresent(v -> {
                        try {
                            method.invoke(object, v);
                        } catch (Exception ignored) {
                        }
                    });
        }
    }

    /**
     * 如果value值转换为目标属性field类型后，不为空情况时，强设置值。否则不操作
     *
     * @param object 对象
     * @param field  属性
     * @param value  值
     */
    public static void setValueIfNotNull(Object object, Field field, Object value) {

        Optional.ofNullable(to(value, new TypeReference<>(field.getGenericType()))).ifPresent(v -> {
            try {
                field.set(object, v);
            } catch (Exception ignored) {
            }
        });
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
        Set<String> fields = getSameField(source, target, compare);
        copyProperties(source, target, fields.toArray(new String[]{}), ContainOrExclude.INCLUDE);
    }

    /**
     * 通过不同的比较方式，获取同名属性集合
     *
     * @param source  比较对象
     * @param target  比较对象
     * @param compare 比对方式
     * @return 属性名集合
     */
    private static Set<String> getSameField(Object source, Object target, Compare compare) {
        Set<String> result = new HashSet<>();
        if (ObjectUtils.isEmpty(source) || ObjectUtils.isEmpty(target)) {
            return result;
        }
        Set<String> sameField = getSameField(source, target);
        if (sameField.isEmpty()) {
            return result;
        }

        Object targetNew = null;
        try {
            targetNew = target.getClass().newInstance();
        } catch (Exception ignored) {

        }
        for (String fieldName : sameField) {
            Object sourceValue = getFieldValue(source, fieldName);
            Object targetValue = getFieldValue(target, fieldName);
            switch (compare) {
                case DIFF_ALL_NOT_NULL:
                    if (sourceValue != null && targetValue != null && (!sourceValue.equals(targetValue))) {
                        result.add(fieldName);
                    }
                    break;
                case DIFF_TARGET_NULL:
                    if (sourceValue != null && targetValue == null) {
                        result.add(fieldName);
                    }
                    break;
                case DIFF_SOURCE_NULL:
                    if (sourceValue == null && targetValue != null) {
                        result.add(fieldName);
                    }
                    break;
                case DIFF_SOURCE_NOT_NULL:
                    if (sourceValue != null && !sourceValue.equals(targetValue)) {
                        result.add(fieldName);
                    }
                    break;
                case DIFF_TARGET_NOT_NULL:
                    if (targetValue != null && !targetValue.equals(sourceValue)) {
                        result.add(fieldName);
                    }
                    break;
                case DIFF_SOURCE_NOT_NULL_AND_TARGET_DEFAULT:
                    if (targetNew == null) {
                        throw new RuntimeException(String.format("目标对象创建失败，请检查类“%s”是否包含空参构造函数", target.getClass()));
                    } else if (sourceValue != null && targetValue == null) {
                        result.add(fieldName);
                    } else if (sourceValue != null && !sourceValue.equals(targetValue) && targetValue.equals(getFieldValue(targetNew, fieldName))) {
                        result.add(fieldName);
                    }
                    break;
                case DIFF_TARGET_DEFAULT:
                    if (targetNew == null) {
                        throw new RuntimeException(String.format("目标对象创建失败，请检查类“%s”是否包含空参构造函数", target.getClass()));
                    } else if (targetValue != null && targetValue.equals(getFieldValue(targetNew, fieldName)) && targetValue.equals(sourceValue)) {
                        result.add(fieldName);
                    } else if (targetValue == null && getFieldValue(targetNew, fieldName) == null && sourceValue != null) {
                        result.add(fieldName);
                    }
                    break;
                case DIFF:
                    boolean is = (sourceValue == null && targetValue != null) ||
                            (sourceValue != null && targetValue == null) ||
                            (sourceValue != null && !source.equals(targetValue));
                    if (is) {
                        result.add(fieldName);
                    }
                    break;
                case SAME:
                    if (sourceValue != null && (sourceValue.equals(targetValue))) {
                        result.add(fieldName);
                    }
                    break;
                default:
            }
        }
        return result;
    }

    /**
     * 获取同名属性
     *
     * @param source 比较对象
     * @param target 比较对象
     * @return 同名属性集合
     */
    private static Set<String> getSameField(Object source, Object target) {
        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();
        Set<String> result = new HashSet<>();
        if (sourceClass == targetClass) {
            Set<Field> sourceFields = ClassUtil.getAllField(sourceClass);
            for (Field field : sourceFields) {
                result.add(field.getName());
            }
        } else {
            Set<Field> sourceFields = ClassUtil.getAllField(sourceClass);
            for (Field field : sourceFields) {
                String name = field.getName();
                Field targetField = ClassUtil.getField(targetClass, name);
                if (targetField != null) {
                    result.add(name);
                }
            }
        }
        return result;
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
        copyProperties(source, target, Constant.RegularAbout.BLANK, Constant.RegularAbout.BLANK, arguments, containOrExclude);
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
        copyProperties(source, target, prefix, suffix, new String[]{}, ContainOrExclude.INCLUDE);
    }

    /**
     * 复制对象属性
     * 仅拷贝属性值不相同属性
     *
     * @param source 原对象
     * @param target 新对象
     */
    public static void copyProperties(Object source, Object target) {
        copyProperties(source, target, Compare.DIFF);
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
    public static void copyProperties(Object source, Object target, String prefix, String suffix, String[] arguments, ContainOrExclude containOrExclude) {
        if (ObjectUtils.isEmpty(source) || ObjectUtils.isEmpty(target)) {
            return;
        }

        Set<Field> targetFields = ClassUtil.getAllField(target.getClass());
        for (Field field : targetFields) {
            field.setAccessible(true);
            String propertyName = field.getName();

            propertyName = StringUtils.isBlank(prefix) ? propertyName : prefix + StringUtil.toUpperName(propertyName);
            propertyName = StringUtils.isBlank(suffix) ? propertyName : propertyName + StringUtil.toUpperName(suffix);

            Field sourceProperty = ClassUtil.getField(source.getClass(), propertyName);
            if (sourceProperty == null) {
                continue;
            }
            if (arguments == null) {
                continue;
            }

            switch (containOrExclude) {
                case EXCLUDE:
                    if (ArrayUtils.contains(arguments, sourceProperty.getName())) {
                        continue;
                    }
                    break;
                case INCLUDE:
                    if (!ArrayUtils.contains(arguments, sourceProperty.getName())) {
                        continue;
                    }
                    break;
                default:
            }

            try {
                sourceProperty.setAccessible(true);
                Object value = sourceProperty.get(source);

                if (value != null) {
                    Type type = field.getGenericType();
                    TypeReference<Object> typeReference = new TypeReference<>(type);
                    value = to(value, typeReference);
                }

                field.setAccessible(true);
                setValue(target, field, value);

            } catch (Exception ignored) {

            }
        }
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
        try {
            String getMethodName = "get" + StringUtil.toUpperName(field.getName());
            Method getMethod = o.getClass().getMethod(getMethodName);
            getMethod.setAccessible(true);
            return getMethod.invoke(o);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            try {
                field.setAccessible(true);
                return field.get(o);
            } catch (IllegalAccessException ex) {
                return null;
            }
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
                fields.forEach(field -> {
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
            Set<Field> haveValueFields = ClassUtil.getAllField(clazz).stream().filter(field -> {
                field.setAccessible(true);
                try {
                    if (SERIAL_VERSION_UID.equals(field.getName())) {
                        return false;
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
                field.setAccessible(true);
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
            for (Field field : fields) {
                field.setAccessible(true);
                String key = StringUtil.toUnderline(field.getName());
                try {
                    result.put(key, field.get(o));
                } catch (IllegalAccessException ignored) {
                }
            }
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
        Set<Field> fields = ClassUtil.getAllField(sourceClass);
        for (Field field : fields) {
            field.setAccessible(true);
            if (excludeProperty != null && ArrayUtils.contains(excludeProperty, field.getName())) {
                continue;
            }
            Object sourceValue = field.get(sourceObject);
            Object targetValue = field.get(targetObject);
            if (compare(sourceValue, targetValue)) {
                continue;
            }

            result.add(new Different(field.getName(), field.getType().getTypeName(), String.valueOf(targetValue), String.valueOf(sourceValue)));
        }
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
            field.setAccessible(true);
            if (SERIAL_VERSION_UID.equals(field.getName())) {
                continue;
            }
            try {
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