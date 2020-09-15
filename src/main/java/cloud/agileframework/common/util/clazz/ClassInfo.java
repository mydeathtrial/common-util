package cloud.agileframework.common.util.clazz;

import cloud.agileframework.common.util.pattern.PatternUtil;
import cloud.agileframework.common.util.string.StringUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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
    private static final Map<Class<?>, ClassInfo<?>> CACHE = Maps.newHashMap();
    private final Class<T> clazz;
    private Map<String, Constructor<T>> constructors;
    private Constructor<T> privateConstructor;
    private Set<Field> allField;
    private Set<Method> allMethod;
    private Map<String, Field> fieldMap;
    private Map<String, Method> methodMap;
    private Map<Class<? extends Annotation>, Set<ClassUtil.Target<?>>> fieldAnnotations;
    private Map<Class<? extends Annotation>, Set<ClassUtil.Target<?>>> methodAnnotations;

    public ClassInfo(Class<T> clazz) {
        this.clazz = clazz;
    }

    public static <A> ClassInfo<A> getCache(Class<A> clazz) {
        ClassInfo<?> target = CACHE.get(clazz);
        if (target == null) {
            target = new ClassInfo<>(clazz);
            CACHE.put(clazz, target);
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

    public Class<?> getClazz() {
        return clazz;
    }

    public Set<Field> getAllField() {
        if (allField == null) {
            allField = Sets.newConcurrentHashSet();
        }
        if (allField.isEmpty()) {
            extractFieldRecursion(clazz, allField);
            allField.parallelStream().forEach(field -> field.setAccessible(true));
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

    public Set<Method> getAllMethod() {
        if (allMethod == null) {
            allMethod = Sets.newConcurrentHashSet();
        }
        if (allMethod.isEmpty()) {
            extractMethodRecursion(clazz, allMethod);
            allMethod.parallelStream().forEach(method -> method.setAccessible(true));
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
}
