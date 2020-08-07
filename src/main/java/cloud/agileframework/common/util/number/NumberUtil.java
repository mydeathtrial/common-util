package cloud.agileframework.common.util.number;

import org.apache.commons.lang3.math.NumberUtils;

/**
 * @author 佟盟
 * 日期 2019/10/30 10:50
 * 描述 数字工具
 * @version 1.0
 * @since 1.0
 */
public class NumberUtil extends NumberUtils {
    /**
     * 判断是否为数字类型
     *
     * @param clazz 类型
     * @return 是否
     */
    public static boolean isNumber(Class clazz) {
        if (Number.class.isAssignableFrom(clazz)) {
            return true;
        } else {
            return clazz == short.class || clazz == int.class || clazz == long.class || clazz == double.class || clazz == float.class;
        }
    }

    public static void main(String[] args) {
        Long.class.isAssignableFrom(long.class);
    }
}
