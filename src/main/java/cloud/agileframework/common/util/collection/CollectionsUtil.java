package cloud.agileframework.common.util.collection;

import cloud.agileframework.common.util.number.NumberUtil;
import cloud.agileframework.common.util.object.ObjectUtil;
import org.apache.commons.lang3.math.NumberUtils;

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
            } catch (Exception e) {
                e.printStackTrace();
            }
            return v;
        });
    }

    private static <T> int compare(T o1, T o2, String property, boolean sort) {
        Object o1Value = getValue(o1, property);
        Object o2Value = getValue(o2, property);
        int result = compare(o1Value, o2Value);
        return sort ? result : -result;
    }

    private static Object getValue(Object o1, String property) {
        if (o1 == null) {
            return null;
        }
        if (Map.class.isAssignableFrom(o1.getClass())) {
            return ((Map) o1).get(property);
        } else {
            return ObjectUtil.getFieldValue(o1, property);
        }
    }

    private static int compare(Object o1, Object o2) {
        if (o1 == null && o2 == null) {
            return 0;
        } else if (o1 == null) {
            return -1;
        } else if (o2 == null) {
            return 1;
        }

        if (o1 == o2) {
            return 0;
        }

        if (NumberUtil.isNumber(o1.getClass()) && NumberUtil.isNumber(o2.getClass())) {
            return NumberUtils.createDouble(o1.toString())
                    .compareTo(NumberUtils.createDouble(o2.toString()));
        } else {
            return o1.toString().compareTo(o2.toString());
        }
    }
}
