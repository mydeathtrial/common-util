package cloud.agileframework.common.util.generator;

import cloud.agileframework.common.util.array.ArrayUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.UUID;

/**
 * @author 佟盟 on 2017/7/13
 */
public class RandomStringUtil extends RandomStringUtils {

    private static final String[] lettersOfLowers = {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "a", "s", "d", "f", "g", "h", "j", "k", "l", "z", "x", "c", "v", "b", "n", "m"};
    private static final String[] lettersOfUppers = {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "A", "S", "D", "F", "G", "H", "J", "K", "L", "Z", "X", "C", "V", "B", "N", "M"};
    private static final String[] symbols = {"!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "+", "{", "}", "|", "}", "[", "]", "\\", ":", "\"", ";", "'", "<", ">", ",", ".", "?", "/", "~", "`"};
    private static final String[] numbers = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};

    private static final int DIGIT = 32;

    /**
     * 获取随机码
     *
     * @param digit     位数
     * @param group     多少位一分组
     * @param delimiter 分组分隔符
     * @param pre       前缀
     * @param suffix    后缀
     * @param random    随机类型
     */
    public static String getRandom(int digit, int group, String delimiter, String pre, String suffix, Random random) {
        pre = StringUtils.isEmpty(pre) ? "" : pre;
        suffix = StringUtils.isEmpty(suffix) ? "" : suffix;
        int count = digit - pre.length() - suffix.length();
        if (count < 0) {
            return null;
        }
        StringBuilder result = new StringBuilder(pre);
        Object[] temp = null;
        switch (random) {
            case NUMBER:
                temp = numbers;
                break;
            case LETTER:
                temp = ArrayUtils.addAll(lettersOfLowers, lettersOfUppers);
                break;
            case LETTER_LOWER:
                temp = lettersOfLowers;
                break;
            case LETTER_UPPER:
                temp = lettersOfUppers;
                break;
            case MIX_1:
                temp = ArrayUtil.addAll(lettersOfLowers, lettersOfUppers, numbers);
                break;
            case MIX_2:
                temp = ArrayUtil.addAll(lettersOfLowers, lettersOfUppers, symbols, numbers);
                break;
            case ROUTINE:
                return routine(Boolean.TRUE);
            case ROUTINE_NO_LINE:
                return routine(Boolean.FALSE);
            default:
        }
        if (!ArrayUtil.isEmpty(temp) && temp.length > 0) {
            for (int i = 0; i < count; i++) {
                if (group > 0 && i > 0 && i % group == 0) {
                    result.append(delimiter);
                }
                result.append(temp[(int) (Math.random() * (temp.length - 1))]);
            }
        }

        result.append(suffix);
        return result.toString();
    }

    public static String getRandom(int digit, int group, String delimiter, Random random) {
        return getRandom(digit, group, delimiter, null, null, random);
    }

    public static String getRandom(int digit, int group, String delimiter, String pre, Random random) {
        return getRandom(digit, group, delimiter, pre, null, random);
    }

    public static String getRandom(int digit, String pre, String suffix, Random random) {
        return getRandom(digit, 0, null, pre, suffix, random);
    }

    public static String getRandom(int digit, String pre, Random random) {
        return getRandom(digit, 0, null, pre, null, random);
    }

    public static String getRandom(int digit, Random random) {
        return getRandom(digit, 0, null, null, null, random);
    }

    public static String getRandom(Random random) {
        return getRandom(DIGIT, 0, null, null, null, random);
    }

    /**
     * 获取常规随机数
     *
     * @param haveLine 是否包含中划线
     */
    public static String routine(boolean haveLine) {
        return haveLine ? UUID.randomUUID().toString() : UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 随机种类
     */
    public enum Random {
        /**
         * 数字
         */
        NUMBER,
        /**
         * 字母
         */
        LETTER,
        /**
         * 小写字母
         */
        LETTER_LOWER,
        /**
         * 大写字母
         */
        LETTER_UPPER,
        /**
         * 小写字母+大写字母+数字
         */
        MIX_1,
        /**
         * 小写字母+大写字母+数字+符号
         */
        MIX_2,
        /**
         * 包含中划线随机
         */
        ROUTINE,
        /**
         * 不包含中划线随机
         */
        ROUTINE_NO_LINE
    }

}
