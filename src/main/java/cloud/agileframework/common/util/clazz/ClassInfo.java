package cloud.agileframework.common.util.clazz;

import cloud.agileframework.common.util.object.ObjectUtil;
import cloud.agileframework.common.util.pattern.PatternUtil;
import cloud.agileframework.common.util.string.StringUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;
import sun.reflect.generics.reflectiveObjects.TypeVariableImpl;
import sun.reflect.generics.repository.FieldRepository;
import sun.reflect.generics.repository.MethodRepository;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author 佟盟
 * 日期 2020-09-14 14:24
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class ClassInfo<T> {
    private static final Map<String, ClassInfo<?>> CACHE = Maps.newConcurrentMap();
    private final Class<T> clazz;
    private Map<String, Constructor<T>> constructors;
    private Constructor<T> privateConstructor;
    private Set<Field> allField;
    private Set<Method> allMethod;
    private Map<String, Field> fieldMap;
    private Map<String, Method> methodMap;
    private Map<Class<? extends Annotation>, Set<ClassUtil.Target<?>>> fieldAnnotations;
    private Map<Class<? extends Annotation>, Set<ClassUtil.Target<?>>> methodAnnotations;
    private Map<Field, FieldInfo> fieldInfoCache;
    private final Map<String, Type> typeVariableClassMap = Maps.newConcurrentMap();
    private boolean parsed = false;

    public ClassInfo(Type type) {

        if (type instanceof ParameterizedTypeImpl) {
            this.clazz = (Class<T>) ((ParameterizedTypeImpl) type).getRawType();
        } else if (type instanceof Class) {
            this.clazz = (Class<T>) type;
        } else {
            this.clazz = null;
        }
        getTypeParameterName(type);
    }

    private void getTypeParameterName(Type currentType) {
        Class<?> clazz = null;
        if (currentType instanceof ParameterizedTypeImpl) {
            clazz = ((ParameterizedTypeImpl) currentType).getRawType();
        } else if (currentType instanceof Class) {
            clazz = (Class<?>) currentType;
        }
        Class<?> superClass = clazz.getSuperclass();

        if (superClass == Object.class) {
            return;
        }

        getTypeParameterName(superClass);

        Type supper = clazz.getGenericSuperclass();
        if (supper instanceof ParameterizedTypeImpl) {
            TypeVariable<? extends Class<?>>[] superClassTypeParameters = superClass.getTypeParameters();

            Type[] typeParameters = ((ParameterizedTypeImpl) supper).getActualTypeArguments();

            for (int i = 0; i < superClassTypeParameters.length; i++) {
                final TypeVariable<? extends Class<?>> key = superClassTypeParameters[i];
                Type value = typeParameters[i];
                if (value instanceof TypeVariableImpl) {
                    value = typeVariableClassMap.get(((TypeVariableImpl<?>) value).getName());
                }
                if (value == null) {
                    continue;
                }
                typeVariableClassMap.put(key.getName(), value);
            }
        }


    }

    public static <A extends Type> ClassInfo<A> getCache(A type) {
        ClassInfo<?> target = CACHE.get(type.toString());
        if (target == null) {
            target = new ClassInfo<>((Class<A>) type);
            CACHE.put(type.toString(), target);
        }
        return (ClassInfo<A>) target;
    }

    public <A extends Annotation> Set<ClassUtil.Target<A>> getAllFieldAnnotation(Class<A> annotationClass) {
        Set<ClassUtil.Target<?>> set = null;
        if (fieldAnnotations != null) {
            set = fieldAnnotations.get(annotationClass);
        }

        if (set == null) {
            Set<Field> fields = getAllField();
            set = Sets.newHashSetWithExpectedSize(fields.size());

            for (Field field : fields) {
                A annotation = field.getAnnotation(annotationClass);
                if (annotation != null) {
                    set.add(new ClassUtil.Target<>(field, annotation));
                }
            }
            if (fieldAnnotations == null) {
                fieldAnnotations = Maps.newHashMap();
            }
            fieldAnnotations.put(annotationClass, set);
        }

        return set.stream().map(r -> (ClassUtil.Target<A>) r).collect(Collectors.toSet());
    }

    public <A extends Annotation> Set<ClassUtil.Target<A>> getAllMethodAnnotation(Class<A> annotationClass) {

        Set<ClassUtil.Target<?>> set = null;
        if (methodAnnotations != null) {
            set = methodAnnotations.get(annotationClass);
        }
        if (set == null) {
            Set<Method> methods = getAllMethod();
            set = Sets.newHashSetWithExpectedSize(methods.size());
            for (Method method : methods) {
                A annotation = method.getAnnotation(annotationClass);
                if (annotation != null) {
                    set.add(new ClassUtil.Target<>(method, annotation));
                }
            }
            if (methodAnnotations == null) {
                methodAnnotations = Maps.newHashMap();
            }
            methodAnnotations.put(annotationClass, set);
        }
        return set.stream().map(r -> (ClassUtil.Target<A>) r).collect(Collectors.toSet());
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
        final String cacheKey = Arrays.stream(parameterTypes).map(Class::getCanonicalName).collect(Collectors.joining());

        Constructor<T> constructor = null;
        if (constructors != null) {
            constructor = constructors.get(cacheKey);
        }
        if (constructor == null) {
            try {
                if (parameterTypes.length > 0) {
                    constructor = clazz.getConstructor(parameterTypes);
                } else {
                    constructor = clazz.getConstructor();
                }
                constructor.setAccessible(true);
            } catch (NoSuchMethodException ignored) {
            }
            if (constructors == null) {
                constructors = Maps.newHashMap();
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
            Map<String, Field> targetFields = Maps.newHashMap();
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
                    fieldMap = Maps.newHashMap();
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
        if (genericType instanceof TypeVariableImpl) {
            try {
                final Type value = typeVariableClassMap.get(((TypeVariableImpl<?>) genericType).getName());
                if (value == null) {
                    return;
                }

                final Field genericTypeField = ClassUtil.getField(FieldRepository.class, "genericType");
                genericTypeField.setAccessible(true);
                ObjectUtil.setValue(genericInfo, genericTypeField, value);

                final Field typeField = ClassUtil.getField(Field.class, "type");
                typeField.setAccessible(true);
                ObjectUtil.setValue(targetField, typeField, value);
            } catch (Exception ignored) {
            }
        }
    }

    public Method getMethod(String methodName, Class<?>... paramTypes) {
        final String cacheKey = methodName + Arrays.stream(paramTypes).map(Class::getCanonicalName).collect(Collectors.joining());
        Method targetMethod = null;
        if (methodMap != null) {
            targetMethod = methodMap.get(cacheKey);
        }

        if (targetMethod != null) {
            return targetMethod;
        }
        if (paramTypes != null) {
            try {
                targetMethod = clazz.getMethod(methodName, paramTypes);
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
                targetMethod = candidates.iterator().next();
            } else if (candidates.isEmpty()) {
                throw new IllegalStateException("Expected method not found: " + clazz.getName() + '.' + methodName);
            } else {
                throw new IllegalStateException("No unique method found: " + clazz.getName() + '.' + methodName);
            }
        }
        if (!targetMethod.isAccessible()) {
            targetMethod.setAccessible(true);
        }

        if (methodMap == null) {
            methodMap = Maps.newHashMap();
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
        if (genericReturnType instanceof TypeVariableImpl) {
            final Type v = typeVariableClassMap.get(genericReturnType.getTypeName());

            final Field returnType = MethodRepository.class.getDeclaredField("returnType");
            returnType.setAccessible(true);
            returnType.set(genericInfo, v);

            final Field returnTypeField = Method.class.getDeclaredField("returnType");
            returnTypeField.setAccessible(true);
            returnTypeField.set(targetMethod, v);
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
        final Type[] types = Arrays.stream(targetMethod.getGenericParameterTypes())
                .map(t -> {
                    Type v = typeVariableClassMap.get(t.getTypeName());
                    return v == null ? t : v;
                })
                .collect(Collectors.toList())
                .toArray(new Type[]{});

        if (types.length == 0) {
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

        Class<?>[] classes = new Class[types.length];
        for (int i = 0; i < types.length; i++) {
            if (!(types[i] instanceof Class)) {
                return;
            }
            classes[i] = (Class<?>) types[i];
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
            allField.parallelStream().forEach(field -> {
                field.setAccessible(true);
                parsingGeneric(field);
            });
        }
        return allField;
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

    /**
     * 递归获取所有类方法，包括继承、私有、公有等
     *
     * @param clazz 目标类型
     * @param set   方法结合
     */
    private static void extractMethodRecursion(Class<?> clazz, Set<Method> set) {
        Method[] selfMethods = clazz.getDeclaredMethods();
        Method[] extendMethods = clazz.getMethods();
        set.addAll(Arrays.asList(selfMethods));
        set.addAll(Arrays.asList(extendMethods));

        Class<?> superClass = clazz.getSuperclass();
        if (superClass == Object.class || superClass == null) {
            return;
        }
        extractMethodRecursion(superClass, set);
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
