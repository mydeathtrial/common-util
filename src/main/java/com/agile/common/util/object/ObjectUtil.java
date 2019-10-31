package com.agile.common.util.object;

import com.agile.common.constant.Constant;
import com.agile.common.util.array.ArrayUtil;
import com.agile.common.util.clazz.ClassUtil;
import com.agile.common.util.clazz.TypeReference;
import com.agile.common.util.date.DateUtil;
import com.agile.common.util.map.MapUtil;
import com.agile.common.util.number.NumberUtil;
import com.agile.common.util.string.StringUtil;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.ObjectUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 佟盟
 * 日期 2019/10/21 15:51
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class ObjectUtil extends ObjectUtils {


    public static <T> T to(Object from, TypeReference<T> toClass) {
        T result;
        if (from == null) {
            return null;
        }
        if (toClass.isExtendsFrom(Collection.class)) {
            result = toCollection(from, toClass);
        } else if (toClass.isExtendsFrom(Map.class)) {
            result = toMap(from, toClass);
        } else if (toClass.isWrapOrPrimitive()) {
            // 基本类型转换
            result = (T) to(from, toClass.getWrapperClass());
        } else if (toClass.isExtendsFrom(Date.class)) {
            // 日期类型转换
            result = (T) DateUtil.parse(from.toString()).getTime();
        } else if (toClass.isAssignableFrom(from.getClass())) {
            // 类型相同，直接返回
            result = (T) from;
        } else {
            // POJO类型转换
            result = toPOJO(from, (Class<T>) toClass.getType());
        }

        if (result == null) {
            try {
                Constructor<T> construct = toClass.getConstruct(from.getClass());
                if (construct == null) {
                    construct = toClass.getConstruct(String.class);
                    result = construct.newInstance(from.toString());
                }
                result = construct.newInstance(from);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }
        return result;
    }

    private static <T> T toMap(Object from, TypeReference<T> toClass) {
        if (toClass.isExtendsFrom(Map.class)) {
            if (ClassUtil.isExtendsFrom(from.getClass(), Collection.class) || from.getClass().isArray()) {
                return null;
            }
            Map map = MapUtil.parse(from);
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
        if (from.getClass() == toClass.getType()) {
            return (T) from;
        }
        if (toClass.isExtendsFrom(Collection.class)) {

            if (!ClassUtil.isExtendsFrom(from.getClass(), Collection.class) && !from.getClass().isArray()) {
                return null;
            }
            Type nodeType = toClass.getParameterizedType(0);
            if (!(nodeType instanceof Class)) {
                return null;
            }

            Collection<?> collection = null;
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
                    for (Object o : (Collection) from) {
                        collection.add(to(o, new TypeReference<>(nodeType)));
                    }
                } else if (from.getClass().isArray()) {
                    for (Object o : (Object[]) from) {
                        collection.add(to(o, new TypeReference<>(nodeType)));
                    }
                }
            }
            return (T) collection;
        }

        return null;
    }

    /**
     * 转换成POJO
     *
     * @param from    被转换对象
     * @param toClass 转换的目标POJO类型
     * @param <T>     泛型
     * @return 转换后的POJO
     */
    private static <T> T toPOJO(Object from, Class<? extends T> toClass) {

        if (from == null || toClass.isAssignableFrom(from.getClass())) {
            return (T) from;
        }
        final Class sourceClass = from.getClass();
        if (sourceClass.isPrimitive() || Iterable.class.isAssignableFrom(sourceClass)) {
            throw new ParseException();
        }
        if (ClassUtil.isExtendsFrom(sourceClass, Map.class)) {
            Map<String, Object> map = (Map<String, Object>) from;

            T object = ClassUtil.newInstance(toClass);
            if (object != null) {
                Set<Field> fields = ClassUtil.getAllField(toClass);
                fields.stream().forEach(field -> {
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
    public static <T> T to(Object from, Class<T> toClass) {
        if (from == null) {
            return null;
        }

        Object temp = null;

        String valueStr = from.toString();
        if (NumberUtil.isNumber(toClass)) {
            Number number = NumberUtil.createNumber(valueStr);
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
            if (NumberUtil.isParsable(valueStr)) {
                temp = NumberUtil.createNumber(valueStr).intValue() > 0;
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
     * 判断对象属性是否全空
     */
    public static boolean isNotChange(Object object) {
        if (object == null) {
            return true;
        }
        Class<?> clazz = object.getClass();
        final String serialVersionUid = "serialVersionUID";
        try {
            Object newObject = clazz.newInstance();
            Set<Field> haveValueFields = ClassUtil.getAllField(clazz).parallelStream().filter(field -> {
                field.setAccessible(true);
                try {
                    if (serialVersionUid.equals(field.getName())) {
                        return false;
                    }
                    Object currentValue = field.get(object);
                    Object initValue = field.get(newObject);
                    return currentValue != null && !Objects.deepEquals(currentValue, initValue);
                } catch (Exception e) {
                    return false;
                }

            }).collect(Collectors.toSet());

            return haveValueFields.size() == 0;
        } catch (InstantiationException | IllegalAccessException e) {
            return false;
        }
    }

    /**
     * 为object对象的field字段设置value值
     *
     * @param object 对象
     * @param field  字段
     * @param value  值
     */
    public static void setValue(Object object, Field field, Object value) {
        Optional.ofNullable(object).orElseThrow(IllegalArgumentException::new);

        field.setAccessible(true);
        Class objectClass = object.getClass();
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
                setMethod = ClassUtil.getMethod(objectClass, setMethodName, (Class<?>) fieldType);
                invokeMethodIfParamNotNull(object, setMethod, value);
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
                        invokeMethodIfParamNotNull(object, method, value);
                        if (field.get(object) != initValue) {
                            return;
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            //将来值类型与属性类型相同时，直接设置
            setValueIfNotNull(object, field, value);
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
     * 如果值转换不为空情况下，强设置值
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
     * @param compare 拷贝方式
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
        if (ObjectUtil.isEmpty(source) || ObjectUtil.isEmpty(target)) {
            return result;
        }
        Set<String> sameField = getSameField(source, target);
        if (sameField.size() == 0) {
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
                    if (sourceValue == null && targetValue != null) {
                        result.add(fieldName);
                    } else if (sourceValue != null && targetValue == null) {
                        result.add(fieldName);
                    } else if (sourceValue != null && !source.equals(targetValue)) {
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

    public static void copyProperties(Object source, Object target, String[] arguments, ContainOrExclude containOrExclude) {
        copyProperties(source, target, Constant.RegularAbout.BLANK, Constant.RegularAbout.BLANK, arguments, containOrExclude);
    }

    public static void copyProperties(Object source, Object target, String prefix, String suffix) {
        copyProperties(source, target, prefix, suffix, new String[]{}, ContainOrExclude.INCLUDE);
    }

    /**
     * 复制对象属性
     *
     * @param source 原对象
     * @param target 新对象
     */
    public static void copyProperties(Object source, Object target) {
        copyProperties(source, target, Compare.DIFF);
    }

    /**
     * 复制对象中哪些属性
     *
     * @param source           原对象
     * @param target           新对象
     * @param arguments        属性列表
     * @param containOrExclude 包含或排除
     */
    public static void copyProperties(Object source, Object target, String prefix, String suffix, String[] arguments, ContainOrExclude containOrExclude) {
        if (ObjectUtil.isEmpty(source) || ObjectUtil.isEmpty(target)) {
            return;
        }

        Set<Field> targetFields = ClassUtil.getAllField(target.getClass());
        for (Field field : targetFields) {
            field.setAccessible(true);
            String propertyName = field.getName();

            propertyName = StringUtil.isBlank(prefix) ? propertyName : prefix + StringUtil.toUpperName(propertyName);
            propertyName = StringUtil.isBlank(suffix) ? propertyName : propertyName + StringUtil.toUpperName(suffix);

            Field sourceProperty = ClassUtil.getField(source.getClass(), propertyName);
            if (sourceProperty == null) {
                continue;
            }
            if (arguments == null) {
                continue;
            }

            switch (containOrExclude) {
                case EXCLUDE:
                    if (ArrayUtil.contains(arguments, sourceProperty.getName())) {
                        continue;
                    }
                    break;
                case INCLUDE:
                    if (!ArrayUtil.contains(arguments, sourceProperty.getName())) {
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
                    if (!(typeReference.getWrapperClass()).isAssignableFrom(value.getClass())) {
                        value = to(value, typeReference);
                    }
                }

                field.setAccessible(true);
                field.set(target, value);

            } catch (IllegalAccessException ignored) {

            }
        }
    }

    /**
     * 取属性值
     *
     * @param o         对象
     * @param fieldName 属性名
     * @return 值
     */
    public static Object getFieldValue(Object o, String fieldName) {
        Field field = ClassUtil.getField(o.getClass(), fieldName);
        if (field == null) {
            return null;
        }
        try {
            return field.get(o);
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    public static class A {
        private String userName;
        private int code;
        private A a;
        private Queue<A> queue;
        private Set<Integer> set;
        private List<Integer> list;
        private Map<StringBuilder, Integer> map;
    }


    public static void main(String[] args) {
        Map<String, Object> map0 = Maps.newHashMap();
        map0.put("user_name", "333");
        map0.put("code", "333");
        map0.put("set", new String[]{"33", "44"});
        map0.put("list", new String[]{"55", "66"});

        Map<String, Object> map = Maps.newHashMap();
        map.put("user_name", "222");
        map.put("code", "222");
//        map.put("a", map0);

        Map<String, Object> map1 = Maps.newHashMap();
        map1.put("user_name", "1111");
        map1.put("code", "1111");
        map1.put("a", map);
        map1.put("queue", new Map[]{map, map});
        map1.put("map", map);

        to(map1, new TypeReference<A>() {
        });
    }
}
