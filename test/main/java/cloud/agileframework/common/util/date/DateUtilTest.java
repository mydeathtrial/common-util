package cloud.agileframework.common.util.date;

import junit.framework.TestCase;

import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;

public class DateUtilTest extends TestCase {

    public void testParse() {
    }

    public void testParseTime() {

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        GregorianCalendar date;
//        date = DateUtil.parse("19900905 11:12 下午");
//        System.out.println(format.format(date.getTime()));
//
//        date = DateUtil.parse("19900905 pm 11:12");
//        System.out.println(format.format(date.getTime()));
//
//        date = DateUtil.parse("pm 1990-9-5 11:12");
//        System.out.println(format.format(date.getTime()));
//
//        date = DateUtil.parse("11:12 1990/09/05");
//        System.out.println(format.format(date.getTime()));
//
//        date = DateUtil.parse("11:2:03 1990年9月05");
//        System.out.println(format.format(date.getTime()));
//
//        date = DateUtil.parse("11:02:33 1990-09/05");
//        System.out.println(format.format(date.getTime()));
//
//        date = DateUtil.parse("32493509091000");
//        System.out.println(format.format(date.getTime()));
//
//        date = DateUtil.parse("-32493509");
//        System.out.println(format.format(date.getTime()));

        date = DateUtil.parse("1990 9 5");
        System.out.println(format.format(date.getTime()));
    }

    public void testParseDate() {
    }
}