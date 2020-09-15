package cloud.agileframework.common.util;

import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.date.DateUtil;
import cloud.agileframework.common.util.object.ObjectUtil;
import com.agile.common.data.DemoA;
import com.agile.common.data.DemoC;
import org.junit.Test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * @author 佟盟
 * 日期 2019/10/31 9:52
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class ObjectUtilTest {

    /**
     * 测试对象转Map
     */
    @Test
    public void objectToMap() {
        long currentTime = System.currentTimeMillis();
        ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        System.out.println(System.currentTimeMillis()-currentTime);currentTime = System.currentTimeMillis();
        ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        System.out.println(System.currentTimeMillis()-currentTime);currentTime = System.currentTimeMillis();
        ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        System.out.println(System.currentTimeMillis()-currentTime);currentTime = System.currentTimeMillis();
        ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        System.out.println(System.currentTimeMillis()-currentTime);currentTime = System.currentTimeMillis();
        ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        System.out.println(System.currentTimeMillis()-currentTime);currentTime = System.currentTimeMillis();
        ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        System.out.println(System.currentTimeMillis()-currentTime);currentTime = System.currentTimeMillis();
        ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        System.out.println(System.currentTimeMillis()-currentTime);currentTime = System.currentTimeMillis();
        ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        System.out.println(System.currentTimeMillis()-currentTime);currentTime = System.currentTimeMillis();
        ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        System.out.println(System.currentTimeMillis()-currentTime);currentTime = System.currentTimeMillis();
        ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        System.out.println(System.currentTimeMillis()-currentTime);currentTime = System.currentTimeMillis();
        ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        System.out.println(System.currentTimeMillis()-currentTime);currentTime = System.currentTimeMillis();
        ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        System.out.println(System.currentTimeMillis()-currentTime);currentTime = System.currentTimeMillis();
        ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        System.out.println(System.currentTimeMillis()-currentTime);currentTime = System.currentTimeMillis();

    }

    /**
     * 测试对象转对象
     */
    @Test
    public void objectToObject() {
        DemoA demoA = ObjectUtil.to(DemoA.testData(), new TypeReference<DemoA>() {
        });
        ObjectUtil.to(demoA, new TypeReference<DemoC>() {
        });
    }

    /**
     * 测试Map转对象
     */
    @Test
    public void parseDate() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        GregorianCalendar date;
        date = DateUtil.parse("19900905 11:12 下午");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("19900905 pm 11:12");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("pm 1990-9-5 11:12");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("11:12 1990/09/05");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("11:2:03 1990年9月05");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("11:02:33 1990-09/05");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("1596782907410");
        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("6782907410");
        System.out.println(format.format(date.getTime()));
    }

    @Test
    public void parseDate2() {
        ObjectUtil.to(new ArrayList<String>() {{
            add("123");
        }}, new TypeReference<List>() {
        });
    }
}
