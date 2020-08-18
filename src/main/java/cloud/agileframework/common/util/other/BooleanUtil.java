package cloud.agileframework.common.util.other;


import org.apache.commons.lang3.ObjectUtils;

/**
 * @author mydeathtrial on 2017/4/19
 */
public class BooleanUtil {
    public static boolean toBoolean(String resource) {
        return "yes".equalsIgnoreCase(resource) || "true".equalsIgnoreCase(resource) || "1".equals(resource);
    }

    public static boolean toBoolean(Object resource) {
        return !ObjectUtils.isEmpty(resource) && Boolean.parseBoolean(resource.toString());
    }
}
