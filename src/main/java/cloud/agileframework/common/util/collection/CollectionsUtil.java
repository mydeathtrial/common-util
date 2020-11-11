package cloud.agileframework.common.util.collection;

import cloud.agileframework.common.util.clazz.ClassInfo;

import java.lang.reflect.Field;
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
        list.sort((o1, o2) -> {
            try {
                if (Map.class.isAssignableFrom(o1.getClass())) {
                    return String.valueOf(((Map) o1).get(property)).compareTo(String.valueOf(((Map) o2).get(property)));
                } else {
                    Class<?> clazz = o1.getClass();
                    Field field = ClassInfo.getCache(clazz).getField(property);
                    return String.valueOf(field.get(o1)).compareTo(String.valueOf(field.get(o2)));
                }

            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return 0;
        });
    }
}
