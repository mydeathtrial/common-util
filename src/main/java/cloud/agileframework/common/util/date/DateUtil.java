package cloud.agileframework.common.util.date;

import cloud.agileframework.common.util.pattern.PatternUtil;
import org.apache.commons.lang3.ObjectUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * @author 佟盟
 * 日期 2019/10/24 11:30
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class DateUtil {

    /**
     * 日期正则
     */
    private static final String DATE_REGEX = "((?<year0>([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3}))(?<split1>[\\D]+)(((?<month0>([13578]|1[02]))(?<split2>[\\D]+)(?<date0>([12][0-9]|3[01]|[1-9])))|((?<month1>([469]|11))(?<split3>[\\D]+)(?<date1>([12][0-9]|30|[1-9])))|((?<month2>2)(?<split4>[\\D]+)(?<date2>([1][0-9]|2[0-8]|[1-9])))))|((?<year1>(([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|1[26]|[2468][048]|[3579][26])00)))(?<split5>[\\D]+)(?<month3>2)(?<split6>[\\D]+)(?<date3>29))";
    /**
     * 补零日期正则
     */
    private static final String ZERO_FILL_DATE_REGEX = "((?<year0>([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3}))(?<split1>[\\D]*)(((?<month0>(0[13578]|1[02]))(?<split2>[\\D]*)(?<date0>([12][0-9]|3[01]|0[1-9])))|((?<month1>(0[469]|11))(?<split3>[\\D]*)(?<date1>([12][0-9]|30|0[1-9])))|((?<month2>02)(?<split4>[\\D]*)(?<date2>([1][0-9]|2[0-8]|0[1-9])))))|((?<year1>(([0-9]{2})(0[48]|[2468][048]|[13579][26])|((0[48]|1[26]|[2468][048]|[3579][26])00)))(?<split5>[\\D]*)(?<month3>02)(?<split6>[\\D]*)(?<date3>29))";
    /**
     * 补零时间正则
     */
    private static final String ZERO_FILL_TIME_REGEX = "(?<hour>(0[0-9]|1[0-9]|2[0-4]|[0-9]))(((:|时|点|-|/)(?<minute>([0-5][0-9]|[0-9]))(((:|分|-|/)(?<second>([0-5][0-9]|[0-9])))?))|((:|时|点)))";
    /**
     * 时间正则
     */
    private static final String TIME_REGEX = "(?<hour>(1[0-9]|2[0-3]|[1-9]))(((:|时|点|-|/)(?<minute>[1-5][0-9]|[0-9])(((:|分|-|/)(?<second>[1-5][0-9]|[0-9]))?))|(:|时|点))";
    /**
     * 简单日期正则
     */
    private static final String DATE_SIMPLE_REGEX = "(?<year0>([0-9]{3}[1-9]|[0-9]{2}[1-9][0-9]{1}|[0-9]{1}[1-9][0-9]{2}|[1-9][0-9]{3}))(?<split1>[年|/|-]+)(((?<month0>(1[012]|0?[1-9])))?)(?<split2>[月|/|-]+)(((?<date0>([12][0-9]|3[01]|0?[1-9])))?)";

    /**
     * 时间戳正则
     */
    private static final String TIME_MILLIS_FORMAT = "(1[\\d]{12})|([\\d]{9,12})";

    /**
     * 下午标识
     */
    private static final String PM_FORMAT = "pm|PM|p.m|P.M|下午|[Aa]fternoon";

    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String SECOND = "second";
    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DATE = "date";

    /**
     * 从字符串中提取日期
     *
     * @param source 包含日期信息的字符串
     * @return JDK日历对象，可通过getTime()转换为Date对象
     */
    public static GregorianCalendar parse(String source) {
        String[] step = source.split("[\\s]");
        GregorianCalendar gregorianCalendar;
        if (step.length == 1) {
            if (PatternUtil.matches(TIME_MILLIS_FORMAT, source)) {
                gregorianCalendar = new GregorianCalendar();
                gregorianCalendar.setTimeInMillis(Long.parseLong(source));
            } else {
                gregorianCalendar = parseDate(source);

                if (gregorianCalendar == null) {
                    GregorianCalendar time = parseTime(source);
                    parsePM(time, source);
                    return time;
                } else {
                    List<String> list = PatternUtil.getMatched(ZERO_FILL_DATE_REGEX, source);
                    if (ObjectUtils.isEmpty(list)) {
                        list = PatternUtil.getMatched(DATE_REGEX, source);
                    }
                    if (ObjectUtils.isEmpty(list)) {
                        list = PatternUtil.getMatched(DATE_SIMPLE_REGEX, source);
                    }
                    for (String node : list) {
                        source = source.replace(node, "");
                    }
                    GregorianCalendar time = parseTime(source);

                    if (time != null) {
                        gregorianCalendar.set(gregorianCalendar.get(Calendar.YEAR),
                                gregorianCalendar.get(Calendar.MONTH),
                                gregorianCalendar.get(Calendar.DAY_OF_MONTH),
                                time.get(Calendar.HOUR_OF_DAY),
                                time.get(Calendar.MINUTE),
                                time.get(Calendar.SECOND));
                    }

                }
            }
        } else if (step.length > 1) {

            gregorianCalendar = new GregorianCalendar();

            boolean haveDate = false;
            for (String s : step) {
                if (!haveDate) {
                    GregorianCalendar date = parseDate(s);
                    if (date != null) {
                        gregorianCalendar.set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
                        haveDate = true;
                        continue;
                    }
                }
                GregorianCalendar time = parseTime(s);
                if (time != null) {
                    gregorianCalendar.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
                    gregorianCalendar.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
                    gregorianCalendar.set(Calendar.SECOND, time.get(Calendar.SECOND));
                }
            }
        } else {
            return null;
        }
        parsePM(gregorianCalendar, source);
        return gregorianCalendar;
    }

    /**
     * 从字符串中提取时间
     *
     * @param source 包含时间的字符串
     * @return JDK日历对象，可通过getTime()转换为Date对象
     */
    public static GregorianCalendar parseTime(String source) {
        Map<String, String> list = PatternUtil.getGroups(ZERO_FILL_TIME_REGEX, source);
        if (list == null) {
            list = PatternUtil.getGroups(TIME_REGEX, source);
        }
        if (list != null) {
            int hourOfDay = 0;
            int minute = 0;
            int second = 0;

            for (Map.Entry<String, String> entry : list.entrySet()) {
                if (entry.getValue() == null) {
                    continue;
                }
                if (entry.getKey().startsWith(HOUR)) {
                    hourOfDay = Integer.parseInt(entry.getValue());
                } else if (entry.getKey().startsWith(MINUTE)) {
                    minute = Integer.parseInt(entry.getValue());
                } else if (entry.getKey().startsWith(SECOND)) {
                    second = Integer.parseInt(entry.getValue());
                }
            }

            return new GregorianCalendar(0, 0, 0, hourOfDay, minute, second);
        }
        return null;
    }

    /**
     * 分析上下午
     *
     * @param gregorianCalendar 时间
     * @param source            源字符串
     */
    private static void parsePM(GregorianCalendar gregorianCalendar, String source) {
        if (gregorianCalendar == null) {
            return;
        }
        int hourOfDay = gregorianCalendar.get(Calendar.HOUR_OF_DAY);
        if (hourOfDay <= 12 && PatternUtil.find(PM_FORMAT, source)) {
            gregorianCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay + 12);
        }
    }

    /**
     * 从字符串中提取日期
     *
     * @param source 包含日期的字符串
     * @return JDK日历对象，可通过getTime()转换为Date对象
     */
    public static GregorianCalendar parseDate(String source) {
        Map<String, String> list = PatternUtil.getGroups(ZERO_FILL_DATE_REGEX, source);
        if (list == null) {
            list = PatternUtil.getGroups(DATE_REGEX, source);
        }
        if (list == null) {
            list = PatternUtil.getGroups(DATE_SIMPLE_REGEX, source);
        }
        if (list != null) {
            GregorianCalendar gregorianCalendar = new GregorianCalendar();
            gregorianCalendar.set(Calendar.MONTH, 0);
            gregorianCalendar.set(Calendar.DATE, 1);
            Stream<Map.Entry<String, String>> stream = list.entrySet().stream().filter(e -> e.getValue() != null);

            stream.forEach(e -> {
                if (e.getKey().startsWith(YEAR)) {
                    gregorianCalendar.set(Calendar.YEAR, Integer.parseInt(e.getValue()));
                } else if (e.getKey().startsWith(MONTH)) {
                    gregorianCalendar.set(Calendar.MONTH, Integer.parseInt(e.getValue()) - 1);
                } else if (e.getKey().startsWith(DATE)) {
                    gregorianCalendar.set(Calendar.DATE, Integer.parseInt(e.getValue()));
                }
            });
            return gregorianCalendar;
        }
        return null;
    }

    private static final int DATE_UNIT = 60;
    private static final int MIN_UNIT = 1000;
    private static final int HOUR_UNIT = 24;

    /**
     * 获取时间戳字符串
     */
    public static Date getCurrentDate() {
        return new Date(System.currentTimeMillis());
    }

    /**
     * 字符串转日期
     *
     * @param date   日期字符串
     * @param format 格式
     */
    public static Date toDateByFormat(String date, String format) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.parse(date);
    }

    public static String toFormatByDate(Date date, String format) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(format);
        return dateFormat.format(date);
    }

    /**
     * 比较两个Date类型时间是否相同
     *
     * @param date1 时间1
     * @param date2 时间2
     * @return true:相同
     */
    public static boolean isSame(Date date1, Date date2) {
        return date1.getTime() == date2.getTime();
    }

    /**
     * 计算两个日期之间相差的天数
     *
     * @param date1 时间1
     * @param date2 时间2
     * @return 相差天数
     */
    public static long getInterval(Date date1, Date date2, int unit) {
        long interval;
        switch (unit) {
            case Calendar.YEAR:
                interval = Math.abs(date1.getTime() - date2.getTime()) / (MIN_UNIT * DATE_UNIT * DATE_UNIT * HOUR_UNIT * 365);
                break;
            case Calendar.MONTH:
                interval = Math.abs(date1.getTime() - date2.getTime()) / (MIN_UNIT * DATE_UNIT * DATE_UNIT * HOUR_UNIT * 30);
                break;
            case Calendar.DATE:
                interval = Math.abs(date1.getTime() - date2.getTime()) / (MIN_UNIT * DATE_UNIT * DATE_UNIT * HOUR_UNIT);
                break;
            case Calendar.HOUR_OF_DAY:
                interval = Math.abs(date1.getTime() - date2.getTime()) / (MIN_UNIT * DATE_UNIT * DATE_UNIT);
                break;
            case Calendar.MINUTE:
                interval = Math.abs(date1.getTime() - date2.getTime()) / (MIN_UNIT * DATE_UNIT);
                break;
            case Calendar.SECOND:
                interval = Math.abs(date1.getTime() - date2.getTime()) / MIN_UNIT;
                break;
            case Calendar.MILLISECOND:
                interval = Math.abs(date1.getTime() - date2.getTime());
                break;
            default:
                throw new RuntimeException("Undefined variable " + unit);
        }
        return interval;
    }

    /**
     * 获取指定日期后时间（指定加算年月日信息）
     *
     * @param date     指定日期
     * @param duration 时间间隔
     * @return 加算后日期
     */
    public static Date add(Date date, Duration duration) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(date.getTime() + duration.toMillis());
        return cal.getTime();
    }
}
