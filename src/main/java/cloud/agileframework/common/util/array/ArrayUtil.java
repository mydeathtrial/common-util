package cloud.agileframework.common.util.array;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.List;

/**
 * @author 佟盟
 * 日期 2019/10/30 15:42
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class ArrayUtil extends ArrayUtils {

    public static Object last(Object[] array) {
        return array[array.length - 1];
    }

    /**
     * 数组转list
     *
     * @param array 数组
     */
    public static <T> List<T> asList(T... array) {
        return Arrays.asList(array);
    }
}
