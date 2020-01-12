package com.agile.common.util.string;

import com.agile.common.constant.Constant;
import com.agile.common.util.pattern.PatternUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;
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

    public static String parsingPlaceholder(String openToken, String closeToken, String text, Map args) {
        return parsingPlaceholder(openToken, closeToken, ":-", text, args, null);
    }

    public static String parsingPlaceholder(String openToken, String closeToken, String equalToken, String text, Map args) {
        return parsingPlaceholder(openToken, closeToken, equalToken, text, args, null);
    }

    /**
     * 将字符串text中由openToken和closeToken组成的占位符依次替换为args数组中的值
     *
     * @param openToken   开始符号
     * @param closeToken  结束符号
     * @param text        转换原文
     * @param args        替换内容集合
     * @param replaceNull 代替空参占位
     * @return
     */
    public static String parsingPlaceholder(String openToken, String closeToken, String equalToken, String text, Map args, String replaceNull) {
        if (args == null || args.size() <= 0) {
            if (replaceNull != null) {
                args = new HashMap(0);
            } else {
                return text;
            }
        }

        if (text == null || text.isEmpty()) {
            return "";
        }
        char[] src = text.toCharArray();
        int offset = 0;
        // search open token
        int start = text.indexOf(openToken, offset);
        if (start == -1) {
            return text;
        }
        final StringBuilder builder = new StringBuilder();
        StringBuilder expression = null;
        while (start > -1) {
            if (start > 0 && src[start - 1] == '\\') {
                // this open token is escaped. remove the backslash and continue.
                builder.append(src, offset, start - offset - 1).append(openToken);
                offset = start + openToken.length();
            } else {
                // found open token. let's search close token.
                if (expression == null) {
                    expression = new StringBuilder();
                } else {
                    expression.setLength(0);
                }
                builder.append(src, offset, start - offset);
                offset = start + openToken.length();
                int end = text.indexOf(closeToken, offset);
                while (end > -1) {
                    if (end > offset && src[end - 1] == '\\') {
                        // this close token is escaped. remove the backslash and continue.
                        expression.append(src, offset, end - offset - 1).append(closeToken);
                        offset = end + closeToken.length();
                        end = text.indexOf(closeToken, offset);
                    } else {
                        expression.append(src, offset, end - offset);
                        offset = end + closeToken.length();
                        break;
                    }
                }
                if (end == -1) {
                    // close token was not found.
                    builder.append(src, start, src.length - start);
                    offset = src.length;
                } else {
                    String key = expression.toString();
                    String[] keyObj = key.split(equalToken);
                    Object o;
                    String value;
                    //判断是否有配置了默认值(:-)  by nhApis 2018.12.27
                    if (keyObj.length > 0) {
                        //配置了默认值,使用key获取当前环境变量中是否已经配置  by nhApis 2018.12.27
                        o = args.get(keyObj[0]);
                    } else {
                        o = args.get(key);
                    }

                    if (o == null) {
                        if (key.contains(equalToken)) {
                            //获取不到使用默认值   by nhApis 2018.12.24
                            value = keyObj[1].trim();
                        } else if (replaceNull != null) {
                            //获取不到环境变量时,返回原表达式 by nhApis 2018.12.24
                            value = replaceNull;
                        } else {
                            value = openToken + key + closeToken;
                        }
                    } else {
                        value = String.valueOf(o);
                    }
                    builder.append(value);
                    offset = end + closeToken.length();
                }
            }
            start = text.indexOf(openToken, offset);
        }
        if (offset < src.length) {
            builder.append(src, offset, src.length - offset);
        }
        return builder.toString();
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
