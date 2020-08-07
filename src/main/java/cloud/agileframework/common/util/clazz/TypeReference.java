package cloud.agileframework.common.util.clazz;

import cloud.agileframework.common.util.array.ArrayUtil;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;


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
    private final Type type;
    /**
     * 泛型<T>的子泛型
     */
    private Type[] typeArguments;

    public TypeReference() {
        Type genType = getClass().getGenericSuperclass();
        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        this.type = params[0];
        if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
            typeArguments = ((ParameterizedType) type).getActualTypeArguments();
        } else {
            typeArguments = null;
        }
    }

    public TypeReference(Type type) {
        this.type = type;
        if (ParameterizedType.class.isAssignableFrom(type.getClass())) {
            typeArguments = ((ParameterizedType) type).getActualTypeArguments();
        } else {
            typeArguments = null;
        }
    }

    /**
     * 判断泛型<T>是否存在子泛型
     *
     * @return 是否
     */
    public boolean hasParameterizedType() {
        return ParameterizedType.class.isAssignableFrom(type.getClass());
    }

    /**
     * 取泛型<T>的子泛型
     *
     * @param index 子泛型角标
     * @return 子泛型内容
     */
    public Type getParameterizedType(int index) {
        if (typeArguments != null && typeArguments.length > index) {
            return typeArguments[index];
        }
        return null;
    }

    /**
     * 添加类的ParameterizedType
     *
     * @param type
     */
    public void addParameterizedType(Type type) {
        typeArguments = ArrayUtil.add(typeArguments, type);
    }

    /**
     * 替换子类
     * @param index 索引
     * @param type 子类型
     */
    public void replaceParameterizedType(int index, Type type) {
        typeArguments = ArrayUtil.insert(index, ArrayUtil.remove(typeArguments, index), type);
    }

    /**
     * 判断clazz是否是type子类或实现类
     *
     * @param clazz 目标类
     * @return 是否
     */
    public boolean isAssignableFrom(Class clazz) {
        Class<?> sourceClass = getWrapperClass();
        return (sourceClass).isAssignableFrom(clazz);
    }

    /**
     * 判断是否继承自clazz类
     *
     * @param clazz 判断目标类
     * @return 是否
     */
    public boolean isExtendsFrom(Class<?> clazz) {
        Class sourceClass = getWrapperClass();
        return clazz.isAssignableFrom(sourceClass);
    }

    /**
     * 判断是否是枚举
     *
     * @return 是否
     */
    public boolean isEnum() {
        Class sourceClass = getWrapperClass();
        return sourceClass.isEnum();
    }

    /**
     * 判断是否是数组
     *
     * @return 是否
     */
    public boolean isArray() {
        Class sourceClass = getWrapperClass();
        return sourceClass.isArray();
    }

    /**
     * 判断是否为基础类型
     *
     * @return 是否
     */
    public boolean isWrapOrPrimitive() {
        Class sourceClass = getWrapperClass();
        return ClassUtil.isWrapOrPrimitive(sourceClass);
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
     * 取最外层的包裹类
     *
     * @return 包裹类
     */
    public Class getWrapperClass() {
        if (type instanceof Class) {
            return (Class) type;
        } else if (type instanceof ParameterizedTypeImpl) {
            return ((ParameterizedTypeImpl) type).getRawType();
        } else {
            return Object.class;
        }
    }

    /**
     * 取构造方法
     *
     * @param parameterTypes 参数
     * @return 构造方法
     */
    public Constructor<T> getConstruct(Class<?>... parameterTypes) {
        return ClassUtil.getConstructor(getWrapperClass(), parameterTypes);
    }

}
