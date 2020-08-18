package cloud.agileframework.common.util.clazz;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.pattern.PatternUtil;
import cloud.agileframework.common.util.string.StringUtil;
import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

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
            if (PatternUtil.matches(targetFieldName, field.getName(), Pattern.CASE_INSENSITIVE)) {
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
        try {
            Method method = Class.class.getDeclaredMethod("privateGetDeclaredConstructors", boolean.class);
            method.setAccessible(true);
            Constructor<T>[] constructors = (Constructor<T>[]) method.invoke(clazz, false);
            if (constructors.length > 0) {
                Constructor<T> privateConstructor = constructors[0];
                privateConstructor.setAccessible(true);
                return privateConstructor.newInstance();
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
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

    public static Method getMethod(Class clazz, String fieldName) {
        Set<Method> methods = getAllMethod(clazz);
        for (Method method : methods) {
            if (method.getName().equals(fieldName)) {
                return method;
            }
        }
        return null;
    }

    /**
     * 目标
     *
     * @param <A> 注解类型
     */
    @Data
    @EqualsAndHashCode
    @AllArgsConstructor
    public static class Target<A extends Annotation> {
        private Member member;
        private A annotation;
    }

    public static <A extends Annotation> Set<Target<A>> getAllFieldAnnotation(Class<?> clazz, Class<A> annotationClass) {
        Set<Field> fields = getAllField(clazz);
        Set<Target<A>> set = new HashSet<>();
        for (Field field : fields) {
            A annotation = field.getAnnotation(annotationClass);
            if (annotation != null) {
                set.add(new Target<>(field, annotation));
            }
        }
        return set;
    }

    public static <A extends Annotation> Set<Target<A>> getAllMethodAnnotation(Class<?> clazz, Class<A> annotationClass) {
        Set<Method> fields = getAllMethod(clazz);
        Set<Target<A>> set = new HashSet<>();
        for (Method method : fields) {
            A annotation = method.getAnnotation(annotationClass);
            if (annotation != null) {
                set.add(new Target<>(method, annotation));
            }
        }
        return set;
    }

    /**
     * 获取所有字段注解
     *
     * @param clazz 类
     * @return 注解结果集
     */
    public static <A extends Annotation> Set<Target<A>> getAllEntityAnnotation(Class<?> clazz, Class<A> annotation) {
        Set<Target<A>> fieldAnnotation = getAllFieldAnnotation(clazz, annotation);
        Set<Target<A>> methodAnnotation = getAllMethodAnnotation(clazz, annotation);
        for (Target<A> target : methodAnnotation) {
            String name = target.getMember().getName();
            if (name.startsWith("get")) {
                final int length = 3;
                Field targetField = getField(clazz, StringUtil.toLowerName(name.substring(length)));
                fieldAnnotation.add(new Target<A>(targetField, target.getAnnotation()));
            }
        }
        return fieldAnnotation;
    }

    /**
     * 比较两个对象是否继承于同一个类
     *
     * @param source 源对象
     * @param target 目标对象
     * @return 是否相同
     */
    public static Boolean compareClass(Object source, Object target) {
        return ObjectUtils.isEmpty(source) ? ObjectUtils.isEmpty(target) : (!ObjectUtils.isEmpty(target) && source.getClass() == (target.getClass()));
    }
}
