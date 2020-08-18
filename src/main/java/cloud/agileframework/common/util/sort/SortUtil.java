package cloud.agileframework.common.util.sort;

/**
 * @author 佟盟 on 2018/8/27
 */
public class SortUtil {
    public static int getMiddle(int[] numbers, int low, int high) {
        //数组的第一个作为中轴
        int temp = numbers[low];
        while (low < high) {
            while (low < high && numbers[high] >= temp) {
                high--;
            }
            //比中轴小的记录移到低端
            numbers[low] = numbers[high];
            while (low < high && numbers[low] <= temp) {
                low++;
            }
            //比中轴大的记录移到高端
            numbers[high] = numbers[low];
        }
        //中轴记录到尾
        numbers[low] = temp;
        // 返回中轴的位置
        return low;
    }

    /**
     * 快排
     *
     * @param array  数组
     * @param low    最低位
     * @param height 最高位
     */
    public static void fastSort(int[] array, int low, int height) {
        if (low < height) {
            int middle = getMiddle(array, low, height);
            fastSort(array, low, middle - 1);
            fastSort(array, middle + 1, height);
        }
    }

    public static void fastSort(int[] array) {
        fastSort(array, 0, array.length - 1);
    }


    public static void bubbleSort(int[] numbers) {
        int temp = 0;
        int size = numbers.length;
        for (int i = 0; i < size - 1; i++) {
            for (int j = 0; j < size - 1 - i; j++) {
                //交换两数位置
                if (numbers[j] > numbers[j + 1]) {
                    temp = numbers[j];
                    numbers[j] = numbers[j + 1];
                    numbers[j + 1] = temp;
                }
            }
        }
    }
}
