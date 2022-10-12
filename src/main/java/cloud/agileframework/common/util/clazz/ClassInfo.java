package cloud.agileframework.common.util.clazz;

import cloud.agileframework.common.util.object.ObjectUtil;
import cloud.agileframework.common.util.pattern.PatternUtil;
import cloud.agileframework.common.util.string.StringUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.Lists;
import org.apache.commons.lang3.reflect.TypeUtils;
import sun.reflect.generics.repository.FieldRepository;
import sun.reflect.generics.repository.MethodRepository;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author 佟盟
 * 日期 2020-09-14 14:24
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@Slf4j
public class ClassInfo<T> {
    private static final Map<String, ClassInfo<?>> CACHE = Maps.newConcurrentMap();
    private final Class<T> clazz;
    private final Map<String, Type> typeVariableClassMap = Maps.newConcurrentMap();
    private Map<String, Constructor<T>> constructors;
    private Constructor<T> privateConstructor;
    private Set<Field> allField;
    private Set<Method> allMethod;
    private Map<String, Field> fieldMap;
    private Map<String, Method> methodMap;
    private Map<Class<? extends Annotation>, Set<ClassUtil.Target<?>>> fieldAnnotations;
    private Map<Class<? extends Annotation>, Set<ClassUtil.Target<?>>> methodAnnotations;
    private Map<Field, FieldInfo> fieldInfoCache;
    private boolean parsed = false;

    public ClassInfo(Type type) {

        if (type instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) type).getRawType();
            if (!(rawType instanceof Class)) {
                throw new IllegalArgumentException(type + "Unable to get complete class information");
            }
            this.clazz = (Class<T>) rawType;
        } else if (type instanceof Class) {
            this.clazz = (Class<T>) type;
        } else {
            throw new IllegalArgumentException(type + "Unable to get complete class information");
        }
        getTypeParameterName(type);
    }

    /**
     * 取类的缓存信息
     *
     * @param type 类型
     * @param <A>  泛型
     * @return ClassInfo
     */
    public static <A extends Type> ClassInfo<A> getCache(A type) {
        ClassInfo<?> target = CACHE.get(type.toString());
        if (target == null) {
            target = new ClassInfo<>(type);
            CACHE.put(type.toString(), target);
        }
        return (ClassInfo<A>) target;
    }

    /**
     * 递归获取所有类属性，包括继承、私有、公有等
     *
     * @param clazz 目标类型
     * @param set   属性集合
     */
    private static void extractFieldRecursion(Class<?> clazz, Set<Field> set) {
        Field[] selfFields = clazz.getDeclaredFields();
        Field[] extendFields = clazz.getFields();
        set.addAll(Arrays.asList(selfFields));
        set.addAll(Arrays.asList(extendFields));

        Class<?> superClass = clazz.getSuperclass();
        if (superClass == Object.class || superClass == null) {
            return;
        }
        extractFieldRecursion(superClass, set);
    }

    /**
     * 递归获取所有类方法，包括继承、私有、公有等
     *
     * @param clazz 目标类型
     * @param set   方法结合
     */
    private static void extractMethodRecursion(Class<?> clazz, Set<Method> set) {
        Method[] selfMethods = clazz.getDeclaredMethods();
        Method[] extendMethods = clazz.getMethods();
        Set<Method> set1 = new HashSet<>();
        for (Method selfMethod : selfMethods) {
            if (selfMethod.getDeclaringClass() != Object.class) {
                set1.add(selfMethod);
            }
        }
        set.addAll(set1);
        Set<Method> result = new HashSet<>();
        for (Method c : extendMethods) {
            if (c.getDeclaringClass() != Object.class) {
                result.add(c);
            }
        }
        set.addAll(result);

        Class<?> superClass = clazz.getSuperclass();
        if (superClass == Object.class || superClass == null) {
            return;
        }
        extractMethodRecursion(superClass, set);
    }

    /**
     * 处理参数化类型
     *
     * @param currentType 要处理的类型
     */
    private void getTypeParameterName(Type currentType) {
        if (currentType instanceof ParameterizedType) {
            Type rawType = ((ParameterizedType) currentType).getRawType();
            getTypeParameterName(rawType);
            return;
        }
        if (currentType == Object.class) {
            return;
        }
        if (currentType instanceof Class) {
            Type supper = ((Class<?>) currentType).getGenericSuperclass();
            getTypeParameterName(supper);
            if (supper instanceof ParameterizedType) {
                final Class<?> superclass = ((Class<?>) currentType).getSuperclass();
                //参数化泛型
                TypeVariable<? extends Class<?>>[] superClassTypeParameters = superclass.getTypeParameters();
                //具体参数化类
                Type[] typeParameters = ((ParameterizedType) supper).getActualTypeArguments();

                for (int i = 0; i < superClassTypeParameters.length; i++) {
                    final TypeVariable<? extends Class<?>> key = superClassTypeParameters[i];
                    Type value = typeParameters[i];
                    if (value instanceof TypeVariable) {
                        value = typeVariableClassMap.get(((TypeVariable<?>) value).getName());
                    }
                    if (value == null) {
                        continue;
                    }
                    typeVariableClassMap.put(key.getName(), value);
                }
            }
        }
    }

    /**
     * 获取所有具备指定注解的属性集合
     *
     * @param annotationClass 注解类
     * @param <A>             泛型
     * @return ClassUtil.Target集合
     */
    public <A extends Annotation> Set<ClassUtil.Target<A>> getAllFieldAnnotation(Class<A> annotationClass) {

        Set<ClassUtil.Target<?>> set = null;
        if (fieldAnnotations != null) {
            set = fieldAnnotations.get(annotationClass);
        }

        if (set == null) {
            Set<Field> fields = getAllField();
            set = Sets.newConcurrentHashSet();

            for (Field field : fields) {
                A annotation = field.getAnnotation(annotationClass);
                if (annotation != null) {
                    set.add(new ClassUtil.Target<>(field, annotation));
                }
            }
            if (fieldAnnotations == null) {
                fieldAnnotations = Maps.newConcurrentMap();
            }
            fieldAnnotations.put(annotationClass, set);
        }

        Set<ClassUtil.Target<A>> result = new HashSet<>();
        for (ClassUtil.Target<?> r : set) {
            ClassUtil.Target<A> aTarget = (ClassUtil.Target<A>) r;
            result.add(aTarget);
        }
        return result;
    }

    public <A extends Annotation> Set<ClassUtil.Target<A>> getAllMethodAnnotation(Class<A> annotationClass) {

        Set<ClassUtil.Target<?>> set = null;
        if (methodAnnotations != null) {
            set = methodAnnotations.get(annotationClass);
        }
        if (set == null) {
            Set<Method> methods = getAllMethod();
            set = Sets.newConcurrentHashSet();
            for (Method method : methods) {
                A annotation = method.getAnnotation(annotationClass);
                if (annotation != null) {
                    set.add(new ClassUtil.Target<>(method, annotation));
                }
            }
            if (methodAnnotations == null) {
                methodAnnotations = Maps.newConcurrentMap();
            }
            methodAnnotations.put(annotationClass, set);
        }
        Set<ClassUtil.Target<A>> result = new HashSet<>();
        for (ClassUtil.Target<?> r : set) {
            ClassUtil.Target<A> aTarget = (ClassUtil.Target<A>) r;
            result.add(aTarget);
        }
        return result;
    }

    public Constructor<T> getPrivateConstructor() {
        return privateConstructor;
    }

    public void setPrivateConstructor(Constructor<T> privateConstructor) {
        this.privateConstructor = privateConstructor;
    }

    /**
     * 取指定类型构造方法
     *
     * @param parameterTypes 参数
     * @return 构造方法
     */
    public Constructor<T> getConstructor(Class<?>... parameterTypes) {
        StringBuilder sb = new StringBuilder();
        for (Class<?> parameterType : parameterTypes) {
            String canonicalName = parameterType.getCanonicalName();
            sb.append(canonicalName);
        }
        final String cacheKey = sb.toString();

        Constructor<T> constructor = null;
        if (constructors != null) {
            constructor = constructors.get(cacheKey);
        }
        if (constructor == null) {
            try {
                if (parameterTypes.length > 0) {
                    for(Constructor<?> c :clazz.getConstructors()){
                        if(c.getParameterCount() == parameterTypes.length){
                            Class<?>[] ps = c.getParameterTypes();
                            boolean isTrue = true;
                            for (int i = 0; i < ps.length; i++) {
                                Class<?> p = ps[i];
                                boolean same = ClassUtil.isAssignableFrom(p, parameterTypes[i]);
                                if (!same) {
                                    isTrue = false;
                                    break;
                                }
                            }
                            if(isTrue){
                                constructor = (Constructor<T>)c;
                                break;
                            }
                        }

                    }
                } else {
                    constructor = clazz.getConstructor();
                }
                constructor.setAccessible(true);
            } catch (NoSuchMethodException ignored) {
            }
            if (constructors == null) {
                constructors = Maps.newConcurrentMap();
            }
            constructors.put(cacheKey, constructor);
        }
        return constructor;
    }

    public Field getField(String key) {
        Field targetField = null;
        if (fieldMap != null) {
            targetField = fieldMap.get(key);
        }

        if (targetField == null) {
            Set<Field> fields = getAllField();
            Map<String, Field> targetFields = Maps.newConcurrentMap();
            String targetFieldName = StringUtil.camelToMatchesRegex(key);
            for (Field field : fields) {
                if (PatternUtil.matches(targetFieldName, field.getName(), Pattern.CASE_INSENSITIVE)) {
                    targetFields.put(field.getName(), field);
                }
            }

            if (targetFields.containsKey(key)) {
                targetField = targetFields.get(key);
            } else if (targetFields.isEmpty()) {
                targetField = null;
            } else {
                targetField = targetFields.values().iterator().next();
            }

            if (targetField != null) {
                if (!targetField.isAccessible()) {
                    targetField.setAccessible(true);
                }
                if (fieldMap == null) {
                    fieldMap = Maps.newConcurrentMap();
                }
                fieldMap.put(key, targetField);
            }

        }

        return targetField;
    }

    /**
     * 处理属性泛型
     *
     * @param targetField 属性
     */
    public void parsingGeneric(Field targetField) {
        if (targetField == null) {
            return;
        }

        try {
            Method getGenericInfoMethod = Field.class.getDeclaredMethod("getGenericInfo");
            getGenericInfoMethod.setAccessible(true);
            getGenericInfoMethod.invoke(targetField);
        } catch (Exception ignored) {
        }

        Object genericInfo = ObjectUtil.getFieldValue(targetField, "genericInfo");
        if (genericInfo == null) {
            return;
        }
        final Type genericType = targetField.getGenericType();
        try {
            Type value = switchParseType(genericType);
            final Field genericTypeField = ClassUtil.getField(FieldRepository.class, "genericType");
            genericTypeField.setAccessible(true);
            ObjectUtil.setValue(genericInfo, genericTypeField, value);

            final Field typeField = ClassUtil.getField(Field.class, "type");
            typeField.setAccessible(true);
            ObjectUtil.setValue(targetField, typeField, value);
        } catch (Exception ignored) {
        }
    }

    /**
     * 泛型
     *
     * @param typeVariable 泛型
     * @return
     */
    private Type parseType(TypeVariable<?> typeVariable) {
        final Type value = typeVariableClassMap.get(typeVariable.getName());
        if (value == null) {
            return typeVariable;
        }
        return value;
    }

    /**
     * 参数化类型
     *
     * @param parameterizedType 参数化类型
     * @return
     */
    private Type parseType(ParameterizedType parameterizedType) {
        List<Type> list = new ArrayList<>();
        for (Type type : parameterizedType.getActualTypeArguments()) {
            Type switchParseType = switchParseType(type);
            list.add(switchParseType);
        }
        Type[] upperBounds = list.toArray(new Type[0]);
        return TypeUtils.parameterizeWithOwner(parameterizedType.getOwnerType(), (Class<?>) parameterizedType.getRawType(), upperBounds);
    }

    /**
     * 泛型数组
     *
     * @param parameterizedType 泛型数组
     * @return
     */
    private Type parseType(GenericArrayType parameterizedType) {
        return switchParseType(parameterizedType.getGenericComponentType());
    }

    /**
     * 通配符类型
     *
     * @param wildcardType 通配符类型
     * @return
     */
    private Type parseType(WildcardType wildcardType) {
        List<Type> list = new ArrayList<>();
        for (Type type : wildcardType.getUpperBounds()) {
            Type switchParseType = switchParseType(type);
            list.add(switchParseType);
        }
        Type[] upperBounds = list.toArray(new Type[0]);


        List<Type> result = new ArrayList<>();
        for (Type type : wildcardType.getLowerBounds()) {
            Type switchParseType = switchParseType(type);
            result.add(switchParseType);
        }
        Type[] lowerBounds = result.toArray(new Type[0]);

        return TypeUtils.wildcardType().withUpperBounds(upperBounds).withLowerBounds(lowerBounds).build();
    }

    private Type switchParseType(Type upperBound) {
        if (upperBound instanceof TypeVariable) {
            return parseType((TypeVariable<?>) upperBound);
        } else if (upperBound instanceof WildcardType) {
            return parseType((WildcardType) upperBound);
        } else if (upperBound instanceof ParameterizedType) {
            return parseType((ParameterizedType) upperBound);
        } else if (upperBound instanceof GenericArrayType) {
            return parseType((GenericArrayType) upperBound);
        } else {
            return upperBound;
        }
    }

    public Method getMethod(String methodName, Class<?>... paramTypes) {
        StringBuilder sb = new StringBuilder();
        for (Class<?> paramType : paramTypes) {
            String canonicalName = paramType.getCanonicalName();
            sb.append(canonicalName);
        }
        final String cacheKey = methodName + sb;
        Method targetMethod = null;
        if (methodMap != null) {
            targetMethod = methodMap.get(cacheKey);
        }

        if (targetMethod != null) {
            return targetMethod;
        }
        Set<Method> candidates = new HashSet<>(1);
        Method[] methods = clazz.getMethods();
        for (Method method : methods) {
            if (methodName.equals(method.getName()) && Arrays.equals(method.getParameterTypes(), paramTypes)) {
                candidates.add(method);
            }
        }
        if (candidates.size() == 1) {
            targetMethod = candidates.iterator().next();
        } else if (candidates.isEmpty()) {
            log.debug("Expected method not found: " + clazz.getName() + '.' + methodName);
            return null;
        } else {
            Optional<Method> any = Optional.empty();
            for (Method c : candidates) {
                if (c.getDeclaringClass() == clazz) {
                    any = Optional.of(c);
                    break;
                }
            }
            targetMethod = any.orElseGet(() -> candidates.iterator().next());
        }
        if (!targetMethod.isAccessible()) {
            targetMethod.setAccessible(true);
        }

        if (methodMap == null) {
            methodMap = Maps.newConcurrentMap();
        }

        methodMap.put(cacheKey, targetMethod);
        return targetMethod;
    }

    /**
     * 处理方法泛型
     *
     * @param targetMethod 方法
     */
    public void parsingGeneric(Method targetMethod) {
        try {
            Method getGenericInfoMethod = Method.class.getDeclaredMethod("getGenericInfo");
            getGenericInfoMethod.setAccessible(true);
            getGenericInfoMethod.invoke(targetMethod);
        } catch (Exception ignored) {
        }

        try {
            setParameterTypes(targetMethod);

            setReturnType(targetMethod);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 设置返回类型
     *
     * @param targetMethod 方法
     * @throws NoSuchFieldException   没这个字段
     * @throws IllegalAccessException 访问限制
     */
    private void setReturnType(Method targetMethod) throws NoSuchFieldException, IllegalAccessException {
        Field genericInfoField = Method.class.getDeclaredField("genericInfo");
        genericInfoField.setAccessible(true);
        Object genericInfo = genericInfoField.get(targetMethod);
        if (genericInfo == null) {
            return;
        }

        Type genericReturnType = targetMethod.getGenericReturnType();
        try {
            final Type v = switchParseType(genericReturnType);

            final Field returnType = MethodRepository.class.getDeclaredField("returnType");
            returnType.setAccessible(true);
            returnType.set(genericInfo, v);

            final Field returnTypeField = Method.class.getDeclaredField("returnType");
            returnTypeField.setAccessible(true);
            returnTypeField.set(targetMethod, v);
        } catch (Exception ignored) {
        }
    }

    /**
     * 设置方法入参类型
     *
     * @param targetMethod 方法
     * @throws NoSuchFieldException   没这个字段
     * @throws IllegalAccessException 访问限制
     */
    private void setParameterTypes(Method targetMethod) throws NoSuchFieldException, IllegalAccessException {
        Type[] genericParameterTypes = targetMethod.getGenericParameterTypes();
        Type[] types = new Type[genericParameterTypes.length];
        for(int i = 0;i<types.length;i++){
            Type t = genericParameterTypes[i];
            Type v = switchParseType(t);
            v = v == null ? t : v;
            types[i] = v;
        }

        int size = types.length;
        if (size == 0) {
            return;
        }

        Field genericInfoField = Method.class.getDeclaredField("genericInfo");
        genericInfoField.setAccessible(true);
        Object genericInfo = genericInfoField.get(targetMethod);
        if (genericInfo == null) {
            return;
        }

        Field parameterTypes;
        try {
            parameterTypes = MethodRepository.class.getSuperclass().getDeclaredField("paramTypes");
        } catch (NoSuchFieldException e) {
            parameterTypes = MethodRepository.class.getSuperclass().getDeclaredField("parameterTypes");
        }

        parameterTypes.setAccessible(true);
        parameterTypes.set(genericInfo, types);

        final Field methodParameterTypes = Method.class.getDeclaredField("parameterTypes");
        methodParameterTypes.setAccessible(true);

        Class<?>[] classes = new Class[size];
        for (int i = 0; i < size; i++) {
            Type type = types[i];
            if (!(type instanceof Class)) {
                return;
            }
            classes[i] = (Class<?>) type;
        }
        methodParameterTypes.set(targetMethod, classes);
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Set<Field> getAllField() {
        if (allField == null) {
            allField = Sets.newConcurrentHashSet();
        }
        if (allField.isEmpty()) {
            extractFieldRecursion(clazz, allField);
            for (Field field : allField) {
                field.setAccessible(true);
                parsingGeneric(field);
            }
        }
        return allField;
    }

    public synchronized Set<Method> getAllMethod() {
        if (allMethod == null) {
            allMethod = Sets.newConcurrentHashSet();
        }
        if (allMethod.isEmpty()) {
            extractMethodRecursion(clazz, allMethod);

        }
        if (!parsed) {
            allMethod.forEach(method -> {
                method.setAccessible(true);
                parsingGeneric(method);
            });
            parsed = true;
        }
        return allMethod;
    }

    public FieldInfo getFieldInfo(Field field) {
        if (fieldInfoCache == null) {
            fieldInfoCache = Maps.newConcurrentMap();
        }
        FieldInfo fieldInfo = fieldInfoCache.get(field);
        if (fieldInfo == null) {
            fieldInfo = new FieldInfo();
            fieldInfoCache.put(field, fieldInfo);
        }
        return fieldInfo;
    }
}
