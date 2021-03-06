package cloud.agileframework.common.util.clazz;

import cloud.agileframework.common.util.string.StringUtil;
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
    public static Set<Field> getAllField(Class<?> clazz) {
        ClassInfo<?> classInfo = ClassInfo.getCache(clazz);
        return classInfo.getAllField();
    }

    /**
     * 获取所有类方法，包括继承、私有、公有等
     *
     * @param clazz 目标类型
     * @return 方法集合
     */
    public static Set<Method> getAllMethod(Class<?> clazz) {
        ClassInfo<?> classInfo = ClassInfo.getCache(clazz);
        return classInfo.getAllMethod();
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
        ClassInfo<?> classInfo = ClassInfo.getCache(clazz);
        return classInfo.getMethod(methodName, paramTypes);
    }

    /**
     * 根据属性名字模糊匹配获取对应属性
     *
     * @param clazz     类型
     * @param fieldName 属性名
     * @return 属性
     */
    public static Field getField(Class<?> clazz, String fieldName) {
        ClassInfo<?> classInfo = ClassInfo.getCache(clazz);
        return classInfo.getField(fieldName);
    }

    /**
     * 判断是否为java基本类型或基本类型的包装类
     *
     * @param clazz 类
     * @return 是否
     */
    public static boolean isWrapOrPrimitive(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            return true;
        }
        try {
            return ((Class<?>) clazz.getDeclaredField("TYPE").get(null)).isPrimitive();
        } catch (NoSuchFieldException | IllegalAccessException ignored) {
            return false;
        }
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
        ClassInfo<T> classInfo = (ClassInfo<T>) ClassInfo.getCache(clazz);
        return classInfo.getConstructor(parameterTypes);
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

        // 取私有构造器构造对象
        ClassInfo<T> classInfo = (ClassInfo<T>) ClassInfo.getCache(clazz);
        Constructor<T> privateConstructor = classInfo.getPrivateConstructor();
        if (privateConstructor == null) {
            try {
                Method method = Class.class.getDeclaredMethod("privateGetDeclaredConstructors", boolean.class);
                method.setAccessible(true);
                Constructor<T>[] constructors = (Constructor<T>[]) method.invoke(clazz, false);
                for (Constructor<T> constructor : constructors) {
                    constructor.setAccessible(true);
                    if (constructor.getParameterCount() == 0) {
                        privateConstructor = constructor;
                        classInfo.setPrivateConstructor(privateConstructor);
                    }
                }
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            }
        }

        if (privateConstructor != null) {
            try {
                return privateConstructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                return null;
            }
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
        ClassInfo<?> classInfo = ClassInfo.getCache(clazz);
        return classInfo.getAllFieldAnnotation(annotationClass);
    }

    public static <A extends Annotation> Set<Target<A>> getAllMethodAnnotation(Class<?> clazz, Class<A> annotationClass) {
        ClassInfo<?> classInfo = ClassInfo.getCache(clazz);
        return classInfo.getAllMethodAnnotation(annotationClass);
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
                fieldAnnotation.add(new Target<>(targetField, target.getAnnotation()));
            }
        }
        return fieldAnnotation;
    }

    /**
     * 比较两个对象是否属于同一个类
     *
     * @param source 源对象
     * @param target 目标对象
     * @return 是否相同
     */
    public static Boolean compareClass(Object source, Object target) {
        return ObjectUtils.isEmpty(source) ? ObjectUtils.isEmpty(target) : (!ObjectUtils.isEmpty(target) && source.getClass() == (target.getClass()));
    }
}
