package cloud.agileframework.common.util.map;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.clazz.ClassUtil;
import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.object.ObjectUtil;
import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

/**
 * @author 佟盟
 * 日期 2019/10/28 18:26
 * 描述 Map工具
 * @version 1.0
 * @since 1.0
 */
public class MapUtil {
    /**
     * 根据前后缀过滤key值
     *
     * @param sourceMap 要处理的Map容器
     * @param prefix    前缀
     * @param suffix    后缀
     * @return 新Map
     */
    public static Map<String, Object> keyFilter(Map<String, Object> sourceMap, String prefix, String suffix) {
        // 构造返回的Map容器
        Map<String, Object> result = Maps.newHashMapWithExpectedSize(sourceMap.size());

        // 如果没有赋予前后缀，则返回sourceMap的拷贝
        if (prefix == null && suffix == null) {
            result.putAll(sourceMap);
            return result;
        }

        // 如果至少存在前缀或后缀，则进行key值筛选，并装填到result容器
        getEntryStream(sourceMap, prefix, suffix).forEach(e -> result.put(e.getKey(), e.getValue()));

        return result;
    }

    /**
     * 根据前后缀过滤key值，并切割key值去掉前后缀
     *
     * @param sourceMap 要处理的Map容器
     * @param prefix    前缀
     * @param suffix    后缀
     * @return 新Map
     */
    public static Map<String, Object> keyFilterAndCut(Map<String, Object> sourceMap, String prefix, String suffix) {
        // 构造返回的Map容器
        Map<String, Object> result = Maps.newHashMapWithExpectedSize(sourceMap.size());

        // 如果没有赋予前后缀，则返回sourceMap的拷贝
        if (prefix == null && suffix == null) {
            result.putAll(sourceMap);
            return result;
        }

        // 如果至少存在前缀或后缀，则进行key值筛选，并装填到result容器
        getEntryStream(sourceMap, prefix, suffix).forEach(e -> {
            String key = e.getKey();
            Object value = e.getValue();
            int start = prefix != null ? prefix.length() : 0;
            int end = suffix != null ? key.lastIndexOf(suffix) : key.length();
            result.put(key.substring(start, end), value);
        });


        return result;
    }

    /**
     * 根据前后缀过滤key值
     *
     * @param sourceMap 要处理的Map容器
     * @param prefix    前缀
     * @param suffix    后缀
     * @return 原Map的Stream流
     */
    public static Stream<Map.Entry<String, Object>> getEntryStream(Map<String, Object> sourceMap, String prefix, String suffix) {
        return sourceMap.entrySet()
                .stream()
                .filter(e -> {
                    String key = e.getKey();
                    if (prefix != null && suffix != null) {
                        return key.startsWith(prefix) && key.endsWith(suffix);
                    } else if (prefix != null) {
                        return key.startsWith(prefix);
                    } else {
                        return key.endsWith(suffix);
                    }
                });
    }

    public static <T> T to(Map<String, Object> from, TypeReference<? extends T> clazz) {
        return to(from, clazz, null, null);
    }

    public static <T> T to(Map<String, Object> from, TypeReference<? extends T> toClass, String prefix) {
        return to(from, toClass, prefix, null);
    }

    public static <T> T to(Map<String, Object> from, TypeReference<? extends T> toClass, String prefix, String suffix) {
        return ObjectUtil.to(keyFilterAndCut(from, prefix, suffix), toClass);
    }

    /**
     * Map转Map
     *
     * @param from    要转换的map
     * @param toClass 要转换成的类型
     * @param <K>     Key的类型
     * @param <V>     Value的类型
     * @return 转换后的新Map
     */
    public static <K, V, K1, V1> Map<K, V> toMap(Map<K1, V1> from, TypeReference<Map<K, V>> toClass) {
        Type mapType = toClass.getType();
        mapType = mapType(mapType);
        if (mapType == null) {
            return (Map<K, V>) from;
        }
        ParameterizedType parameterizedType = (ParameterizedType) mapType;
        Class<?> wrapperClass = ClassUtil.getWrapper(toClass.getType());

        Map<K, V> result;

        if (wrapperClass.isInterface()) {
            if (wrapperClass == ConcurrentMap.class) {
                result = Maps.newConcurrentMap();
            } else if (wrapperClass == SortedMap.class) {
                result = new TreeMap<>();
            } else {
                result = Maps.newHashMapWithExpectedSize(from.size());
            }
        } else {
            result = (Map<K, V>) ClassUtil.newInstance((Class) wrapperClass);
        }

        if (result == null) {
            return null;
        }
        Type[] arguments = parameterizedType.getActualTypeArguments();
        Type keyClass = arguments[0];
        Type valueClass = arguments[1];
        for (Map.Entry<K1, V1> entry : from.entrySet()) {
            K key = ObjectUtil.to(entry.getKey(), new TypeReference<K>(keyClass) {
            });
            V value = ObjectUtil.to(entry.getValue(), new TypeReference<V>(valueClass) {
            });
            result.put(key, value);
        }
        return result;
    }

    /**
     * 对象属性转Map结构
     *
     * @param object 需要被转换的对象
     */
    public static Map parse(Object object) {
        if (ClassUtil.isExtendsFrom(object.getClass(), Map.class)) {
            return (Map) object;
        }
        if (object instanceof String) {
            try {
                return JSON.parseObject((String) object);
            } catch (Exception ignored) {
            }
        }
        Set<Field> fields = ClassUtil.getAllField(object.getClass());
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(fields.size());
        fields.forEach(field -> {
            try {
                field.setAccessible(true);
                Object value = field.get(object);
                map.put(field.getName(), value);
            } catch (Exception ignored) {
            }
        });
        return map;
    }

    /**
     * Map所有key添加前缀
     *
     * @param from   转换的map
     * @param prefix 前缀
     * @return 新Map
     */
    public static Map<String, Object> addPrefix(Map<String, Object> from, String prefix) {
        Map<String, Object> toMap = Maps.newHashMapWithExpectedSize(from.size());
        from.forEach((key, value) -> toMap.put(prefix + key, value));
        return toMap;
    }

    /**
     * map格式转url参数路径
     *
     * @param map 参数集合
     * @return url参数
     */
    public static String toUrl(Map<String, Object> map) {
        StringBuilder mapOfString = new StringBuilder(Constant.RegularAbout.BLANK);
        for (Map.Entry<String, Object> entity : map.entrySet()) {
            Object value = entity.getValue();
            if (value.getClass().isArray()) {
                for (Object v : (Object[]) value) {
                    mapOfString.append(Constant.RegularAbout.AND).append(entity.getKey());
                    mapOfString.append(Constant.RegularAbout.EQUAL).append(v);
                }
            } else {
                mapOfString.append(Constant.RegularAbout.AND).append(entity.getKey());
                mapOfString.append(Constant.RegularAbout.EQUAL).append(entity.getValue());
            }
        }
        String urlParam = mapOfString.toString();
        return urlParam.startsWith(Constant.RegularAbout.AND) ? urlParam.substring(1) : urlParam;
    }

    /**
     * 按照key值排序Map
     *
     * @param map
     * @return
     */
    public static Map<String, Object> sortByKey(Map<String, Object> map) {
        return sort(map, KeyOrValue.KEY);
    }

    /**
     * 按照value值排序Map
     *
     * @param map
     * @return
     */
    public static Map<String, Object> sortByValue(Map<String, Object> map) {
        return sort(map, KeyOrValue.VALUE);
    }

    /**
     * Map排序
     *
     * @param map        目标map
     * @param keyOrValue 排序key值还是排序value值
     * @return 排序好的Map
     */
    private static Map<String, Object> sort(Map<String, Object> map, KeyOrValue keyOrValue) {
        List<Map.Entry<String, Object>> list = new LinkedList<>(map.entrySet());
        switch (keyOrValue) {
            case KEY:
                list.sort(Map.Entry.comparingByKey());
                break;
            case VALUE:
                list.sort(Comparator.comparing(o -> String.valueOf(o.getValue())));
                break;
            default:
        }
        Map<String, Object> linkedHashMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : list) {
            linkedHashMap.put(entry.getKey(), entry.getValue());
        }
        return linkedHashMap;
    }

    public static void main(String[] args) {
        Map<String, Object> result = Maps.newHashMap();
        result.put("myname", "1");
        result.put("mycode", "2");
        result.put("mytudou", "3");
        keyFilter(result, "my", "e");
        keyFilterAndCut(result, "my", "e");
        toMap(result, new TypeReference<Map<StringBuilder, Integer>>() {
        });
    }

    private static Type mapType(Type clazz) {
        if (clazz instanceof ParameterizedType) {
            return clazz;
        }
        if (clazz instanceof Class) {
            return mapType(((Class) clazz).getGenericSuperclass());
        }
        return null;
    }


    /**
     * 辅助枚举
     */
    public enum KeyOrValue {
        /**
         * key
         */
        KEY,
        VALUE
    }
}
