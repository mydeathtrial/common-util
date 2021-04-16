package cloud.agileframework.common.util.clazz;

import java.lang.reflect.Constructor;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;


/**
 * @author 佟盟
 * 日期 2019/10/21 16:58
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class TypeReference<T> {
    /**
     * 泛型<T>
     */
    private Type type;

    public TypeReference() {
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        this.type = params[0];
    }

    public TypeReference(Type type) {
        this.type = type;
    }

    /**
     * 判断类型
     *
     * @return 转枚举，用于判断
     */
    public TypeEnum toEnum() {
        if (type instanceof TypeVariable) {
            return TypeEnum.TypeVariable;
        } else if (type instanceof ParameterizedType) {
            return TypeEnum.ParameterizedType;
        } else if (type instanceof GenericArrayType) {
            return TypeEnum.GenericArrayType;
        } else if (type instanceof WildcardType) {
            return TypeEnum.WildcardType;
        } else {
            return TypeEnum.Class;
        }
    }

    /**
     * 替换
     *
     * @param type 新的类型
     */
    public void replace(Type type) {
        this.type = type;
    }

    /**
     * 判断clazz是否是type子类或实现类
     *
     * @param clazz 目标类
     * @return 是否
     */
    public boolean isAssignableFrom(Class<?> clazz) {
        return ClassUtil.isAssignableFrom(type, clazz, true);
    }

    /**
     * 判断是否继承自clazz类
     *
     * @param clazz 判断目标类
     * @return 是否
     */
    public boolean isExtendsFrom(Class<?> clazz) {
        return ClassUtil.isAssignableFrom(type, clazz, false);
    }

    /**
     * 判断是否是枚举
     *
     * @return 是否
     */
    public Class<Enum> extractEnum() {
        if (type instanceof Class && ((Class<?>) type).isEnum()) {
            return (Class<Enum>) type;
        } else {
            return null;
        }
    }

    /**
     * 判断是否是枚举
     *
     * @return 是否
     */
    public boolean isEnum() {
        Class<?> enumClass = extractEnum();
        return enumClass != null;
    }

    /**
     * 判断是否是数组
     *
     * @return 是否
     */
    public static Class<?> extractArray(Type type) {
        if (type instanceof ParameterizedType) {
            return null;
        } else if (type instanceof GenericArrayType) {
            return extractArray(((GenericArrayType) type).getGenericComponentType());
        } else if (type instanceof TypeVariable) {
            return null;
        } else if (type instanceof WildcardType) {
            return null;
        } else {
            return ((Class<?>) type).getComponentType();
        }
    }

    public Class<?> extractArray() {
        return extractArray(type);
    }

    /**
     * 判断是否是数组
     *
     * @return 是否
     */
    public boolean isArray() {
        Class<?> t = extractArray(type);
        return t != null;
    }


    /**
     * 判断是否为基础类型
     *
     * @return 是否
     */
    public boolean isWrapOrPrimitive() {
        Class<?> c = ClassUtil.isWrapOrPrimitive(type);
        return c != null;
    }

    /**
     * 提取基础类型
     *
     * @return 是否
     */
    public Class<?> extractWrapOrPrimitive() {
        return ClassUtil.isWrapOrPrimitive(type);
    }

    /**
     * 取泛型<T>
     *
     * @return 泛型内容
     */
    public Type getType() {
        return type;
    }


    /**
     * 取构造方法
     *
     * @param parameterTypes 参数
     * @return 构造方法
     */
    public Constructor<T> getConstruct(Class<?>... parameterTypes) {
        return (Constructor<T>) ClassUtil.getConstruct(type, parameterTypes);
    }
}
