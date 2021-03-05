package cloud.agileframework.common.util.collection;

import cloud.agileframework.common.util.object.ObjectUtil;

import java.util.List;
import java.util.Map;

/**
 * @author 佟盟 on 2017/12/11
 */
public class CollectionsUtil {
    private CollectionsUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static <T> void sort(List<T> list, String property) {
        sort(list, SortInfo.builder().property(property).sort(true).build());
    }

    public static <T> void sort(List<T> list, SortInfo... sortInfos) {
        if (sortInfos == null || sortInfos.length == 0) {
            return;
        }
        list.sort((o1, o2) -> {
            int v = 0;
            try {
                for (SortInfo sort : sortInfos) {
                    final String property = sort.getProperty();
                    v = compare(o1, o2, property, sort.isSort());
                    if (v != 0) {
                        break;
                    }
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return v;
        });
    }

    private static <T> int compare(T o1, T o2, String property, boolean sort) throws IllegalAccessException {
        int result = 0;
        if (Map.class.isAssignableFrom(o1.getClass())) {
            result = String.valueOf(((Map) o1).get(property)).compareTo(String.valueOf(((Map) o2).get(property)));
        } else {
            result = String.valueOf(ObjectUtil.getFieldValue(o1, property))
                    .compareTo(String.valueOf(ObjectUtil.getFieldValue(o2, property)));
        }
        return sort ? result : -result;
    }
}
