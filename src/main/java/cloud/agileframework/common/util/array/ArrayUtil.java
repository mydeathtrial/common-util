package cloud.agileframework.common.util.array;

import org.apache.commons.lang3.ArrayUtils;

import java.lang.reflect.Array;
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

    /**
     * 取数组最后一个
     *
     * @param array 数组
     * @return 最后一个元素
     */
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

    /**
     * 数组融合
     *
     * @param array1 数组
     * @param array2 数组
     * @param <T>    数组元素类型
     * @return 融合后的新数组
     */
    public static <T> T[] addAll(T[] array1, T[]... array2) {
        Class<?> type1 = array1.getClass().getComponentType();

        //构造容器
        int length = array1.length;
        for (T[] array : array2) {
            length += array.length;
        }
        T[] joinedArray = (T[]) (Array.newInstance(type1, length));

        //逐个拷贝
        try {
            System.arraycopy(array1, 0, joinedArray, 0, array1.length);
            int index = array1.length;
            for (T[] array : array2) {
                System.arraycopy(array, 0, joinedArray, index, array.length);
                index += array.length;
            }
            return joinedArray;
        } catch (ArrayStoreException var6) {
            Class<?> type2 = array2.getClass().getComponentType();
            if (!type1.isAssignableFrom(type2)) {
                throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of " + type1.getName(), var6);
            } else {
                throw var6;
            }
        }
    }
}
