package com.agile.common.util.pattern;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author 佟盟
 * 日期 2019/10/29 17:12
 * 描述 正则表达式工具
 * @version 1.0
 * @since 1.0
 */
public class PatternUtil {
    /**
     * 根据字符串与正则表达式匹配模式，获取匹配信息对象
     *
     * @param text    字符串
     * @param compile 正则表达式匹配模式
     * @return 匹配信息对象 Matcher
     */
    public static Matcher getMatcher(String text, Pattern compile) {
        return compile.matcher(text);
    }

    /**
     * 完全匹配
     *
     * @param regex 正则表达式
     * @param text  正文
     * @param flags 匹配模式
     * @return 匹配的字符串
     */
    public static boolean matches(String regex, String text, int flags) {
        Matcher matcher = getMatcher(text, Pattern.compile(regex, flags));
        return matcher.matches();
    }

    /**
     * 完全匹配
     *
     * @param regex 正则表达式
     * @param text  正文
     * @return 匹配的字符串
     */
    public static boolean matches(String regex, String text) {
        Matcher matcher = getMatcher(text, Pattern.compile(regex));
        return matcher.matches();
    }

    /**
     * 部分匹配
     *
     * @param regex 正则表达式
     * @param text  正文
     * @return 匹配的字符串
     */
    public static boolean find(String regex, String text, int flags) {
        Matcher matcher = getMatcher(text, Pattern.compile(regex, flags));
        return matcher.find();
    }

    /**
     * 部分匹配
     *
     * @param regex 正则表达式
     * @param text  正文
     * @return 匹配的字符串
     */
    public static boolean find(String regex, String text) {
        Matcher matcher = getMatcher(text, Pattern.compile(regex));
        return matcher.find();
    }

    /**
     * 获取字符串中匹配正则表达式的部分
     *
     * @param regex 正则表达式
     * @param text  正文
     * @return 匹配的字符串
     */
    public static List<String> getMatched(String regex, String text, int flags) {
        Matcher matcher = getMatcher(text, Pattern.compile(regex, flags));
        ArrayList<String> list = Lists.newArrayList();
        while (matcher.find()) {
            list.add(matcher.group());
        }
        return list;
    }

    /**
     * 获取字符串中匹配正则表达式的部分
     *
     * @param regex 正则表达式
     * @param text  正文
     * @return 匹配的字符串
     */
    public static List<String> getMatched(String regex, String text) {
        return getMatched(regex, text, 0);
    }


    /**
     * 根据正则查找字符串中包含的group形式参数
     *
     * @param regex 正则
     * @param text  目标串
     * @return map形式参数集合
     */
    public static Map<String, String> getGroups(String regex, String text) {
        List<String> groupNames = getMatched("(?<=<)[\\w]+(?=>)", regex);

        Matcher matcher = getMatcher(text, Pattern.compile(regex));
        if (matcher.find() && groupNames.size() > 0) {
            Map<String, String> result = Maps.newHashMapWithExpectedSize(groupNames.size());
            for (String key : groupNames) {
                result.put(key, matcher.group(key));
            }
            return result;
        }
        return null;
    }
}
