package cloud.agileframework.common.util.array;

import junit.framework.TestCase;

public class ArrayUtilTest extends TestCase {

    public void testLast() {
    }

    public void testAsList() {
    }

    public void testAddAll() {
        String[] a = new String[]{"1", "2", "3"};
        String[] b = new String[]{"4", "5", "6"};
        String[] c = new String[]{"7", "8", "9"};
        String[] d = ArrayUtil.addAll(a, b, c);
        System.out.println(d);
    }
}