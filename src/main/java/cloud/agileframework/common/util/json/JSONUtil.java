package cloud.agileframework.common.util.json;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.clazz.ClassUtil;
import cloud.agileframework.common.util.number.NumberUtil;
import cloud.agileframework.common.util.string.StringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 佟盟
 * 日期 2019/11/28 19:50
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class JSONUtil extends JSON {
    /**
     * java对象转json
     *
     * @param javaObject java对象
     * @return json对象
     */
    public static JSON toJSON(Object javaObject) {
        if (javaObject == null) {
            return null;
        }
        return (JSON)JSON.toJSON(javaObject);
    }

    /**
     * json-lib转List Map结构
     *
     * @param json json数据
     * @return List Map结构
     */
    public static Object toMapOrList(Object json) {
        if (json == null) {
            return null;
        }
        if (!(json instanceof JSON)) {
            json = toJSON(json);
        }
        if (JSONObject.class.isAssignableFrom(json.getClass())) {
            return jsonObjectCoverMap((JSONObject) json);
        } else if (JSONArray.class.isAssignableFrom(json.getClass())) {
            return jsonArrayCoverArray((JSONArray) json);
        } else {
            return json.toString();
        }
    }

    /**
     * json-lib转List Map结构
     *
     * @param json JSONObject数据
     * @return List Map结构
     */
    private static Map<String, Object> jsonObjectCoverMap(JSONObject json) {

        if (json == null) {
            return null;
        }
        Map<String, Object> result = new HashMap<>(json.size());

        Set<String> keySet = json.keySet();
        for (String key : keySet) {
            Object o = json.get(key);
            if (o != null && JSONObject.class.isAssignableFrom(o.getClass())) {
                result.put(key, jsonObjectCoverMap((JSONObject) o));
            } else if (o != null && JSONArray.class.isAssignableFrom(o.getClass())) {
                result.put(key, jsonArrayCoverArray((JSONArray) o));
            } else {
                result.put(key, o);
            }
        }

        return result;
    }

    /**
     * json-lib转List Map结构
     *
     * @param jsonArray JSONArray数据
     * @return List Map结构
     */
    private static List<Object> jsonArrayCoverArray(JSONArray jsonArray) {
        if (jsonArray == null) {
            return null;
        }
        List<Object> result = new ArrayList<>();
        for (Object o : jsonArray) {
            if (o != null && JSON.class.isAssignableFrom(o.getClass())) {
                if (JSONObject.class.isAssignableFrom(o.getClass())) {
                    result.add(jsonObjectCoverMap((JSONObject) o));
                } else if (JSONArray.class.isAssignableFrom(o.getClass())) {
                    result.add(jsonArrayCoverArray((JSONArray) o));
                }
            } else if (o != null) {
                result.add(o);
            }
        }
        return result;
    }

    /**
     * 若干层次路径查找取参
     *
     * @param key a.b.c...
     * @param o   list/map结构
     * @return 值
     */
    public static Object pathGet(String key, Object o) {
        if (o == null) {
            return null;
        }
        Object result;
        if (key.contains(Constant.RegularAbout.SPOT)) {
            String parentKey = StringUtil.getSplitAtomic(key, "[.]", Constant.NumberAbout.ZERO);
            Object parentValue = getValue(parentKey, o);
            result = pathGet(key.replaceFirst(parentKey + Constant.RegularAbout.SPOT, Constant.RegularAbout.BLANK), parentValue);
        } else {
            result = getValue(key, o);
        }
        return result;
    }

    private static Object getValue(String key, Object o) {
        final String all = "all";
        Object result = null;
        if (Map.class.isAssignableFrom(o.getClass())) {
            result = ((Map) o).get(key);
        } else if (List.class.isAssignableFrom(o.getClass())) {
            if (NumberUtil.isCreatable(key) && ((List) o).size() > Integer.parseInt(key)) {
                result = ((List) o).get(Integer.parseInt(key));
            } else if (all.equals(key)) {
                List<Object> cache = new ArrayList<>();
                for (Object node : (List) o) {
                    if (List.class.isAssignableFrom(node.getClass())) {
                        cache.addAll(((List) node));
                    } else {
                        cache.add(node);
                    }
                }
                if (cache.size() > 0) {
                    result = cache;
                }
            } else if (key.contains(Constant.RegularAbout.COMMA)) {
                List<Object> cache = new ArrayList<>();
                String[] indexes = key.split(Constant.RegularAbout.COMMA);

                for (String index : indexes) {
                    if (NumberUtil.isCreatable(index)) {
                        int number = Integer.parseInt(index);
                        if (((List) o).size() > number) {
                            cache.add(((List) o).get(number));
                        }
                    }
                }
                if (cache.size() > 0) {
                    result = cache;
                }
            } else {
                List<Object> cache = new ArrayList<>();
                for (Object node : (List) o) {
                    Object cacheNode = pathGet(key, node);
                    if (cacheNode != null) {
                        cache.add(cacheNode);
                    }
                }
                if (cache.size() > 0) {
                    result = cache;
                }
            }
        } else {
            try {
                Field field = ClassUtil.getField(o.getClass(), key);
                if (field != null) {
                    result = field.get(o);
                }
            } catch (IllegalAccessException ignored) {

            }
        }
        return result;
    }

}
