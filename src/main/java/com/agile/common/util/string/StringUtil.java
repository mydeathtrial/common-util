package com.agile.common.util.string;

import com.agile.common.constant.Constant;
import com.agile.common.util.pattern.PatternUtil;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 佟盟
 * 日期 2019/10/29 16:55
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class StringUtil extends StringUtils {

    /**
     * 模糊匹配
     *
     * @param source  原字符串
     * @param targets 比对集
     * @return targets中与原字符串相似度最高的字符串
     */
    public static String vagueMatches(String source, Iterable<String> targets) {
        // 根据source构建模糊匹配正则
        String fuzzyMatching = camelToMatchesRegex(source);

        // 构建模糊匹配结果容器，装填匹配到的字符串
        Set<String> keys = Sets.newHashSetWithExpectedSize(2);
        targets.forEach(key -> {
            if (PatternUtil.matches(fuzzyMatching, key, Pattern.CASE_INSENSITIVE)) {
                keys.add(key);
            }
        });

        // 最终结果
        String result = null;

        if (keys.size() > 0) {
            if (keys.contains(source)) {
                result = source;
            } else {
                String camelToUnderlineKey = toUnderline(source);
                String camelToUnderlineKeyUpper = camelToUnderlineKey.toUpperCase();
                String camelToUnderlineKeyLower = camelToUnderlineKey.toLowerCase();

                if (keys.contains(camelToUnderlineKey)) {
                    result = camelToUnderlineKey;
                } else if (keys.contains(camelToUnderlineKeyUpper)) {
                    result = camelToUnderlineKeyUpper;
                } else if (keys.contains(camelToUnderlineKeyLower)) {
                    result = camelToUnderlineKeyLower;
                }
            }

            if (result == null) {
                result = keys.iterator().next();
            }
        }
        return result;
    }

    /**
     * 驼峰式转split分隔符
     *
     * @param text      任意字符串
     * @param separator 分隔符
     * @return 返回split分割字符串
     */
    public static String toSeparator(String text, String separator) {
        String regex = Constant.RegularAbout.UPER;
        if (!PatternUtil.find(regex, text)) {
            return text;
        }

        StringBuilder cacheStr = new StringBuilder(text);
        Matcher matcher = PatternUtil.getMatcher(text, Pattern.compile(regex));
        int i = 0;
        while (matcher.find()) {
            int position = matcher.start() + i;
            if (position >= 1 && !separator.equals(cacheStr.substring(position - 1, position))) {
                cacheStr.replace(position, position + 1, separator + cacheStr.substring(position, position + 1).toLowerCase());
                i++;
            }
        }
        return cacheStr.toString();
    }

    /**
     * 驼峰式转下划线
     *
     * @param text 任意字符串
     * @return 返回驼峰字符串
     */
    public static String toUnderline(String text) {
        return toSeparator(text, Constant.RegularAbout.UNDER_LINE);
    }

    /**
     * 转驼峰式
     *
     * @param text 任意字符串
     * @return 返回驼峰字符串
     */
    public static String toCamel(String text) {
        String regex = Constant.RegularAbout.HUMP;
        if (!PatternUtil.find(regex, text)) {
            return text;
        }

        StringBuilder cacheStr = new StringBuilder(text);
        Matcher matcher = Pattern.compile(regex).matcher(text);
        int i = 0;
        while (matcher.find()) {
            int position = matcher.end() - (i++);
            if (position + 1 <= cacheStr.length()) {
                cacheStr.replace(position - 1, position + 1, cacheStr.substring(position, position + 1).toUpperCase());
            } else {
                break;
            }
        }
        return cacheStr.toString();
    }

    /**
     * 驼峰式转下路径匹配
     *
     * @param text 任意字符串
     * @return 返回路径匹配正则
     */
    public static String camelToMatchesRegex(String text) {
        StringBuilder result = new StringBuilder();
        String[] steps = toUnderline(text).split("_");
        for (int i = 0; i < steps.length; i++) {
            String step = steps[i];
            String first = step.substring(0, 1);

            result.append(String.format("[%s]", first.toLowerCase() + first.toUpperCase())).append(step.substring(1));
            if (i == steps.length - 1) {
                continue;
            }
            result.append(Constant.RegularAbout.URL_REGEX);
        }
        return result.toString();
    }

    /**
     * 字符串转首字母大写驼峰名
     *
     * @param text 任意字符串
     * @return 返回首字母大写的驼峰字符串
     */
    public static String toUpperName(String text) {
        if (isEmpty(text)) {
            return "";
        }
        String camelString = toCamel(text);
        return camelString.substring(0, 1).toUpperCase() + camelString.substring(1);
    }

    /**
     * 字符串转首字母小写驼峰名
     *
     * @param text 任意字符串
     * @return 返回首字母小写的驼峰字符串
     */
    public static String toLowerName(String text) {
        if (isEmpty(text)) {
            return "";
        }
        String camelString = toCamel(text);
        return camelString.substring(0, 1).toLowerCase() + camelString.substring(1);
    }

    public static void main(String[] args) {
        camelToMatchesRegex("asdDsa");
        toUnderline("asdDsa");
        toSeparator("asdDsa", ":");
        Set<String> set = Sets.newHashSet();
        set.add("user_name");
        set.add("User_name");
        set.add("usernamE");
        set.add("_usernamE");
        vagueMatches("userName", set);
    }
}
