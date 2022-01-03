package cloud.agileframework.common.util.clazz;

import cloud.agileframework.common.util.string.StringUtil;
import com.google.common.collect.Maps;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.ClassUtils;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
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
        if (paramTypes == null) {
            return classInfo.getMethod(methodName);
        }
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
     * 获取类的属性相关注解，支持通过get方法获取，以属性上的注解优先级高
     *
     * @param clazz           类型
     * @param fieldName       属性名
     * @param annotationClass 注解类型
     * @param <A>             注解类型
     * @return 注解
     */
    public static <A extends Annotation> A getFieldAnnotation(Class<?> clazz, String fieldName, Class<A> annotationClass) {
        A result = null;
        Field field = getField(clazz, fieldName);
        if (field == null) {
            return result;
        }
        result = field.getAnnotation(annotationClass);

        Method getMethod;
        if (result != null) {
            return result;
        }
        getMethod = getMethod(clazz, "get" + StringUtil.toUpperName(fieldName));
        if (getMethod == null && field.getType() == boolean.class) {
            getMethod = getMethod(clazz, "is" + StringUtil.toUpperName(fieldName));
        }
        if (getMethod != null) {
            result = getMethod.getAnnotation(annotationClass);
        }

        return result;
    }

    /**
     * 取类的所有属性与注解映射关系
     *
     * @param clazz           类型
     * @param annotationClass 注解类型
     * @param <A>             注解
     * @return 属性与注解映射信息
     */
    public static <A extends Annotation> Set<Target<A>> getAllFieldAnnotation(Class<?> clazz, Class<A> annotationClass) {
        ClassInfo<?> classInfo = ClassInfo.getCache(clazz);
        return classInfo.getAllFieldAnnotation(annotationClass);
    }

    /**
     * 取类的所有方法与注解映射关系
     *
     * @param clazz           类型
     * @param annotationClass 注解类型
     * @param <A>             注解
     * @return 方法与注解映射关系
     */
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

    public static Constructor<Type> getConstruct(Type type, Class<?>... parameterTypes) {
        if (type instanceof ParameterizedType) {
            return getConstruct((ParameterizedType) type, parameterTypes);
        } else if (type instanceof GenericArrayType) {
            return null;
        } else if (type instanceof TypeVariable) {
            return getConstruct((TypeVariable<?>) type, parameterTypes);
        } else if (type instanceof WildcardType) {
            return getConstruct((WildcardType) type, parameterTypes);
        } else {
            return (Constructor<Type>) getConstruct((Class<?>) type, parameterTypes);
        }
    }

    public static Constructor<Type> getConstruct(ParameterizedType parameterizedType, Class<?>[] parameterTypes) {
        return getConstruct(parameterizedType.getRawType(), parameterTypes);
    }

    public static Constructor<Type> getConstruct(TypeVariable<?> typeVariable, Class<?>[] parameterTypes) {
        return Arrays.stream(typeVariable.getBounds())
                .map(a -> getConstruct(a, parameterTypes))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static Constructor<Type> getConstruct(WildcardType wildcardType, Class<?>[] parameterTypes) {
        return Arrays.stream(wildcardType.getLowerBounds())
                .map(a -> getConstruct(a, parameterTypes))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static <F> Constructor<F> getConstruct(Class<F> clazz, Class<?>[] parameterTypes) {
        if (clazz.isInterface()) {
            return null;
        }
        return ClassUtil.getConstructor(clazz, parameterTypes);
    }

    public static Class<?> isWrapOrPrimitive(Type type) {
        if (type instanceof ParameterizedType) {
            return null;
        } else if (type instanceof GenericArrayType) {
            return null;
        } else if (type instanceof TypeVariable) {
            return isWrapOrPrimitive((TypeVariable<?>) type);
        } else if (type instanceof WildcardType) {
            return isWrapOrPrimitive((WildcardType) type);
        } else {
            if (ClassUtils.isPrimitiveOrWrapper((Class<?>) type)) {
                return (Class<?>) type;
            }
            return null;
        }
    }

    public static Class<?> isWrapOrPrimitive(TypeVariable<?> type) {
        return Arrays.stream((type)
                        .getBounds())
                .map(ClassUtil::isWrapOrPrimitive)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static Class<?> isWrapOrPrimitive(WildcardType type) {
        return Arrays.stream((type)
                        .getUpperBounds())
                .map(ClassUtil::isWrapOrPrimitive)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    public static boolean isAssignableFrom(Type type, Class<?> clazz, boolean positive) {
        if (type instanceof ParameterizedType) {
            return isAssignableFrom((ParameterizedType) type, clazz, positive);
        } else if (type instanceof GenericArrayType) {
            return isAssignableFrom((GenericArrayType) type, clazz, positive);
        } else if (type instanceof TypeVariable) {
            return isAssignableFrom((TypeVariable<?>) type, clazz, positive);
        } else if (type instanceof WildcardType) {
            return isAssignableFrom((WildcardType) type, clazz, positive);
        } else {
            return isAssignableFrom((Class<?>) type, (Type) clazz, positive);
        }
    }

    public static boolean isAssignableFrom(Class<?> type, Type clazz, boolean positive) {
        if (clazz instanceof ParameterizedType) {
            return isAssignableFrom((ParameterizedType) clazz, type, positive);
        } else if (clazz instanceof GenericArrayType) {
            return isAssignableFrom((GenericArrayType) clazz, type, positive);
        } else if (clazz instanceof TypeVariable) {
            return isAssignableFrom((TypeVariable<?>) clazz, type, positive);
        } else if (clazz instanceof WildcardType) {
            return isAssignableFrom((WildcardType) clazz, type, positive);
        } else {
            return positive ? type.isAssignableFrom(((Class<?>) clazz)) :
                    ((Class<?>) clazz).isAssignableFrom(type);
        }
    }

    /**
     * 参数化类型
     *
     * @param parameterizedType
     * @param clazz
     * @param positive
     * @return
     */
    public static boolean isAssignableFrom(ParameterizedType parameterizedType, Class<?> clazz, boolean positive) {
        Type rawType = parameterizedType.getRawType();
        if (rawType instanceof Class) {
            return positive ? ((Class<?>) rawType).isAssignableFrom(clazz) : clazz.isAssignableFrom(((Class<?>) rawType));
        } else {
            return isAssignableFrom(rawType, clazz, positive);
        }
    }

    /**
     * 泛型数组、参数化类型数组
     *
     * @param genericArrayType
     * @param clazz
     * @param positive
     * @return
     */
    public static boolean isAssignableFrom(GenericArrayType genericArrayType, Class<?> clazz, boolean positive) {
        Type genericComponentType = genericArrayType.getGenericComponentType();
        if (genericComponentType instanceof ParameterizedType) {
            return isAssignableFrom((ParameterizedType) genericComponentType, clazz, positive);
        } else if (genericComponentType instanceof TypeVariable) {
            return isAssignableFrom((TypeVariable<?>) genericComponentType, clazz, positive);
        }
        return false;
    }

    /**
     * 泛型
     *
     * @param typeVariable
     * @param clazz
     * @param positive
     * @return
     */
    public static boolean isAssignableFrom(TypeVariable<?> typeVariable, Class<?> clazz, boolean positive) {
        //如果判断clazz是不是typeVariable子类，由于只有上边界，所以无法比较
        if (positive) {
            return false;
        }
        Type[] bounds = typeVariable.getBounds();
        for (Type type : bounds) {
            boolean is = isAssignableFrom(type, clazz, false);
            if (is) {
                return true;
            }
        }
        return false;
    }

    /**
     * 通配符
     *
     * @param wildcardType 通配符类型
     * @param clazz        类型
     * @param positive
     * @return
     */
    public static boolean isAssignableFrom(WildcardType wildcardType, Class<?> clazz, boolean positive) {
        Type[] lowerBounds = wildcardType.getLowerBounds();
        Type[] upperBounds = wildcardType.getUpperBounds();
        if (positive && lowerBounds.length > 0) {
            //查看clazz是不是wildcardType子类时，以wildcardType下边界匹配
            for (Type type : lowerBounds) {
                if (!(type instanceof Class)) {
                    continue;
                }
                boolean is = isAssignableFrom(type, clazz, true);
                if (is) {
                    return true;
                }
            }
        } else //不存在上下边界说明匹配任意对象，所以是任意对象父类
            if (!positive && upperBounds.length > 0) {
                //查看clazz是不是wildcardType父类时，以wildcardType上边界匹配
                for (Type type : upperBounds) {
                    boolean is = isAssignableFrom(type, clazz, false);
                    if (is) {
                        return true;
                    }
                }
            } else return positive && upperBounds.length == 0;

        return false;
    }

    public static Class<?> getWrapper(Type type) {
        if (type instanceof ParameterizedType) {
            return getWrapper((ParameterizedType) type);
        } else if (type instanceof TypeVariable) {
            return getWrapper((TypeVariable<?>) type);
        } else if (type instanceof GenericArrayType) {
            return getWrapper((GenericArrayType) type);
        } else if (type instanceof WildcardType) {
            return getWrapper((WildcardType) type);
        } else {
            return (Class<?>) type;
        }
    }

    public static Class<?> getWrapper(ParameterizedType type) {
        return getWrapper(type.getRawType());
    }

    public static Class<?> getWrapper(TypeVariable<?> type) {
        return Arrays.stream(type.getBounds())
                .map(ClassUtil::getWrapper)
                .filter(Objects::nonNull).min((a, b) -> a.isInterface() ? -1 : 1)
                .orElse(null);
    }

    public static Class<?> getWrapper(GenericArrayType type) {
        Class<?> wrapper = getWrapper(type.getGenericComponentType());
        if (wrapper == null) {
            return null;
        }
        return Array.newInstance(wrapper, 0).getClass();
    }

    public static Class<?> getWrapper(WildcardType type) {
        Class<?> result = Arrays.stream(type.getUpperBounds())
                .map(ClassUtil::getWrapper)
                .filter(Objects::nonNull).min((a, b) -> a.isInterface() ? -1 : 1)
                .orElse(null);
        if (result == null) {
            result = Arrays.stream(type.getLowerBounds())
                    .map(ClassUtil::getWrapper)
                    .filter(Objects::nonNull).min((a, b) -> a.isInterface() ? -1 : 1)
                    .orElse(null);
        }
        return result;
    }

    public static Type getGeneric(Class<?> clazz, Class<?> supperOrInterface, int post) {
        Map<Type, Type> realTypeMapping = Maps.newConcurrentMap();
        extractParameterizedTypeMap(clazz, realTypeMapping);
        return getGeneric(clazz, supperOrInterface, post, realTypeMapping);
    }

    /**
     * 获取类的泛型
     *
     * @param clazz             从该类中获取泛型
     * @param parameterizedType clazz的祖先类或祖先接口，为参数化类型
     * @param post              获取supperOrInterface中第几个参数化类型
     * @return 泛型类型
     */
    public static Type getGeneric(Type clazz, Class<?> parameterizedType, int post, Map<Type, Type> realTypeMapping) {

        if (parameterizedType.getTypeParameters().length == 0) {
            throw new IllegalArgumentException(parameterizedType + "不是参数化类型");
        }
        if (clazz instanceof Class) {
            Type genericSuperclass = ((Class<?>) clazz).getGenericSuperclass();
            if (genericSuperclass != null && genericSuperclass != Object.class) {
                Type temp = getGeneric(genericSuperclass, parameterizedType, post, realTypeMapping);
                if (temp != null) return temp;
            }
            Type[] genericInterfaces = ((Class<?>) clazz).getGenericInterfaces();
            if (genericInterfaces.length > 0) {
                for (Type genericInterface : genericInterfaces) {
                    Type temp = getGeneric(genericInterface, parameterizedType, post, realTypeMapping);
                    if (temp != null) return temp;
                }
            }
        }
        if (clazz instanceof ParameterizedType) {
            if (((ParameterizedType) clazz).getRawType() == parameterizedType) {
                Type type = ((ParameterizedType) clazz).getActualTypeArguments()[post];
                if (type instanceof Class) {
                    return type;
                }
                return realTypeMapping.get(type);
            }

            return getGeneric(((ParameterizedType) clazz).getRawType(), parameterizedType, post, realTypeMapping);
        }

        return null;
    }

    /**
     * 提取类型type的参数化类型映射关系
     *
     * @param type                 被解析的类型
     * @param parameterizedTypeMap 参数化类型映射关系
     */
    private static void extractParameterizedTypeMap(Type type, Map<Type, Type> parameterizedTypeMap) {
        if (type instanceof Class) {
            extractParameterizedTypeMap(((Class<?>) type).getGenericSuperclass(), parameterizedTypeMap);
            for (Type genericInterface : ((Class<?>) type).getGenericInterfaces()) {
                extractParameterizedTypeMap(genericInterface, parameterizedTypeMap);
            }
            return;
        }
        //参数化类型
        if (type instanceof ParameterizedType) {
            Type[] realTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
            TypeVariable<?>[] typeArguments = ((ParameterizedTypeImpl) type).getRawType().getTypeParameters();
            for (int i = 0; i < realTypeArguments.length; i++) {
                if (realTypeArguments[i] instanceof TypeVariable) {
                    parameterizedTypeMap.put(typeArguments[i], parameterizedTypeMap.get(realTypeArguments[i]));
                    continue;
                }
                parameterizedTypeMap.put(typeArguments[i], realTypeArguments[i]);
            }
            extractParameterizedTypeMap(((ParameterizedTypeImpl) type).getRawType(), parameterizedTypeMap);
        }
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
}
