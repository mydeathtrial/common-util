package com.agile.common.util.clazz;

import com.agile.common.constant.Constant;
import com.agile.common.util.pattern.PatternUtil;
import com.agile.common.util.string.StringUtil;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.ClassUtils;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author 佟盟
 * 日期 2019/10/30 11:43
 * 描述 反射相关工具
 * @version 1.0
 * @since 1.0
 */
public class ClassUtil extends ClassUtils {
    /**
     * 获取所有类属性，包括继承、私有、公有等
     *
     * @param clazz 目标类型
     * @return 属性集合
     */
    public static Set<Field> getAllField(Class clazz) {
        Set<Field> set = Sets.newHashSet();
        extractFieldRecursion(clazz, set);
        set.forEach(field -> field.setAccessible(true));
        return set;
    }

    /**
     * 递归获取所有类属性，包括继承、私有、公有等
     *
     * @param clazz 目标类型
     * @param set   属性集合
     */
    private static void extractFieldRecursion(Class clazz, Set<Field> set) {
        Field[] selfFields = clazz.getDeclaredFields();
        Field[] extendFields = clazz.getFields();
        set.addAll(Arrays.asList(selfFields));
        set.addAll(Arrays.asList(extendFields));

        Class superClass = clazz.getSuperclass();
        if (superClass != null) {
            extractFieldRecursion(superClass, set);
        }
    }

    /**
     * 获取所有类方法，包括继承、私有、公有等
     *
     * @param clazz 目标类型
     * @return 方法集合
     */
    public static Set<Method> getAllMethod(Class clazz) {
        Set<Method> set = Sets.newHashSet();
        extractMethodRecursion(clazz, set);
        set.forEach(method -> method.setAccessible(true));
        return set;
    }

    /**
     * 递归获取所有类方法，包括继承、私有、公有等
     *
     * @param clazz 目标类型
     * @param set   方法结合
     */
    private static void extractMethodRecursion(Class clazz, Set<Method> set) {
        Method[] selfMethods = clazz.getDeclaredMethods();
        Method[] extendMethods = clazz.getMethods();
        set.addAll(Arrays.asList(selfMethods));
        set.addAll(Arrays.asList(extendMethods));

        Class superClass = clazz.getSuperclass();
        if (superClass != null) {
            extractMethodRecursion(superClass, set);
        }
    }

    /**
     * 取类方法
     *
     * @param clazz      类
     * @param methodName 方法名
     * @param paramTypes 参数
     * @return 方法
     */
    public static Method getMethod(Class<?> clazz, String methodName, @Nullable Class<?>... paramTypes) {
        if (paramTypes != null) {
            try {
                return clazz.getMethod(methodName, paramTypes);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException("Expected method not found: " + ex);
            }
        } else {
            Set<Method> candidates = new HashSet<>(1);
            Method[] methods = clazz.getMethods();
            for (Method method : methods) {
                if (methodName.equals(method.getName())) {
                    candidates.add(method);
                }
            }
            if (candidates.size() == 1) {
                return candidates.iterator().next();
            } else if (candidates.isEmpty()) {
                throw new IllegalStateException("Expected method not found: " + clazz.getName() + '.' + methodName);
            } else {
                throw new IllegalStateException("No unique method found: " + clazz.getName() + '.' + methodName);
            }
        }
    }

    /**
     * 根据属性名字模糊匹配获取对应属性
     *
     * @param clazz     类型
     * @param fieldName 属性名
     * @return 属性
     */
    public static Field getField(Class clazz, String fieldName) {
        Set<Field> fields = ClassUtil.getAllField(clazz);
        Map<String, Field> targetFields = new HashMap<>(Constant.NumberAbout.ONE);
        String targetFieldName = StringUtil.camelToMatchesRegex(fieldName);
        for (Field field : fields) {
            if (PatternUtil.find(targetFieldName, field.getName())) {
                field.setAccessible(true);
                targetFields.put(field.getName(), field);
            }
        }
        if (targetFields.size() == 0) {
            return null;
        }
        if (targetFields.containsKey(fieldName)) {
            return targetFields.get(fieldName);
        } else {
            return targetFields.values().iterator().next();
        }
    }

    /**
     * 判断是否为java基本类型或基本类型的包装类
     *
     * @param clazz 类
     * @return 是否
     */
    public static boolean isWrapOrPrimitive(Class clazz) {
        if (clazz.isPrimitive()) {
            return true;
        }
        try {
            if (((Class) clazz.getDeclaredField("TYPE").get(null)).isPrimitive()) {
                return true;
            }
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
        }
        return false;
    }

    /**
     * 取指定类型构造方法
     *
     * @param clazz          类型
     * @param parameterTypes 参数
     * @param <T>            泛型
     * @return 构造方法
     */
    public static <T> Constructor<T> getConstructor(Class<T> clazz, Class<?>... parameterTypes) {
        Constructor<T> constructor = null;
        try {
            if (parameterTypes.length > 0) {
                constructor = clazz.getConstructor(parameterTypes);
            } else {
                constructor = clazz.getConstructor();
            }
            constructor.setAccessible(true);
        } catch (NoSuchMethodException ignored) {
        }
        return constructor;
    }

    /**
     * 创建clazz对象
     *
     * @param clazz 目标类
     * @param <T>   泛型
     * @return 对象
     */
    public static <T> T newInstance(Class<T> clazz) {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException ignored) {
        }
        return null;
    }

    /**
     * 判断other是否是type子类或实现类
     *
     * @param other 目标类
     * @return 是否
     */
    public static boolean isAssignableFrom(Class<?> clazz, Class<?> other) {
        return clazz.isAssignableFrom(other);
    }

    /**
     * 判断是否继承自other类
     *
     * @param other 判断目标类
     * @return 是否
     */
    public static boolean isExtendsFrom(Class<?> clazz, Class<?> other) {
        return other.isAssignableFrom(clazz);
    }

    /**
     * 判断是否为基础类型
     *
     * @return 是否
     */
    public static boolean isPrimitive(Class<?> clazz) {
        return clazz.isPrimitive();
    }
}