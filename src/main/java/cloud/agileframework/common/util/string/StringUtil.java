package cloud.agileframework.common.util.string;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.array.ArrayUtil;
import cloud.agileframework.common.util.json.JSONUtil;
import cloud.agileframework.common.util.pattern.PatternUtil;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.io.FilenameUtils.EXTENSION_SEPARATOR;

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
    public static String vagueMatches(String source, Collection<String> targets) {
        if (targets.contains(source)) {
            return source;
        }

        String underlineSource = toUnderline(source).toLowerCase();
        if (targets.contains(underlineSource)) {
            return underlineSource;
        }

        String camelSource = toCamel(source);
        if (targets.contains(camelSource)) {
            return camelSource;
        }

        // 根据source构建模糊匹配正则
        String fuzzyMatching = camelToMatchesRegex(source);

        return targets.parallelStream()
                .filter(target -> target.equalsIgnoreCase(source) ||
                        PatternUtil.matches(fuzzyMatching, target, Pattern.CASE_INSENSITIVE) ||
                        target.equalsIgnoreCase(camelSource) ||
                        target.equalsIgnoreCase(underlineSource))
                .findFirst().orElse(null);
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
    public static String parsingPlaceholder(String openToken, String closeToken, String equalToken, String text, Object args, String replaceNull) {
        if (args == null) {
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
                    //判断是否有配置了默认值(:)
                    if (keyObj.length > 0) {
                        //配置了默认值,使用key获取当前环境变量中是否已经配置
                        o = JSONUtil.pathGet(keyObj[0].trim(), args);
                    } else {
                        o = JSONUtil.pathGet(key.trim(), args);
                    }

                    if (o == null || ObjectUtils.isEmpty(o)) {
                        if (key.contains(equalToken)) {
                            //获取不到使用默认值
                            value = keyObj[1].trim();
                        } else if (replaceNull != null) {
                            //获取不到环境变量时,返回原表达式
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

    /**
     * 删除扩展名
     *
     * @param str 字符串
     * @return 删除扩展名以后的字符串
     */
    public static String removeExtension(String str) {
        if (!isEmpty(str) && str.contains(Constant.RegularAbout.SPOT)) {
            return str.substring(0, str.lastIndexOf(Constant.RegularAbout.SPOT));
        }
        return str;
    }

    /**
     * 切割字符串成原子数组并取出指定下标下的原子
     *
     * @return 原子
     */
    public static String getSplitAtomic(String source, String regex, int index) {
        if (source != null) {
            String[] atomics = source.split(regex);
            if (atomics.length > index) {
                return atomics[index];
            }
        }
        return null;
    }

    /**
     * 切割字符串成原子数组并取出最后下标下的原子
     *
     * @return 原子
     */
    public static String getSplitLastAtomic(String source, String regex) {
        if (source != null) {
            String[] atomics = source.split(regex);
            if (atomics.length > 0) {
                return atomics[atomics.length - 1];
            }
        }
        return null;
    }

    /**
     * 切割字符串成原子数组并取出最后下标下的原子
     *
     * @return 原子
     */
    public static String getSplitByStrLastAtomic(String source, String regex) {
        if (source != null) {
            String[] atomics = StringUtils.split(source, regex);
            if (atomics.length > 0) {
                return atomics[atomics.length - 1];
            }
        }
        return null;
    }

    /**
     * 从el表达式中获取key、value，如{key:value}
     *
     * @param el        需要处理的字符串
     * @param startChar 开始字符串如{
     * @param endChar   结束字符串如}
     * @param equalChar 中间字符串如：
     * @return 返回处理后的map集合
     */
    public static Map<String, String> getGroupByStartEnd(String el, String startChar, String endChar, String equalChar) {
        Map<String, String> map = new LinkedHashMap<>();
        int index = el.indexOf(startChar);
        if (index == -1) {
            return map;
        }

        String last = el;
        while (index > -1 && index < el.length()) {
            int end;
            int first = last.indexOf(startChar);
            last = last.substring(first + startChar.length());
            index += (first + startChar.length());
            end = last.indexOf(endChar);
            if (end == -1) {
                return map;
            }
            String mapV = last.substring(0, end);

            int consume = end + endChar.length();
            index += consume;

            last = last.substring(consume);

            String[] mapVs = mapV.split(equalChar);
            map.put(mapVs[0], mapVs.length > 1 ? mapVs[1] : null);
        }
        return map;
    }

    public static Map<String, String> getParamFromMapping(String url, String mapUrl) {
        final String left = "{";
        final String right = "}";
        final String equal = ":";
        final int minLength = 2;
        if (StringUtil.isEmpty(mapUrl) || mapUrl.length() <= minLength) {
            return null;
        }
        if (!mapUrl.contains(left) || !mapUrl.contains(right)) {
            return null;
        }
        Map<String, String> result = new LinkedHashMap<>();
        Map<String, String> map;
        if (mapUrl.contains(equal)) {
            map = getGroupByStartEnd(mapUrl, left, right, equal);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String value = PatternUtil.getMatchedString(entry.getValue(), url, 0);
                if (isBlank(value)) {
                    return null;
                }
                result.put(entry.getKey(), value);
                int start = url.indexOf(value);
                url = url.substring(start + value.length());
            }
        } else {
            result.put(mapUrl.substring(1, mapUrl.length() - 1), url);
        }


        return result;
    }

    /**
     * 比较长短
     */
    public static boolean compareTo(String resource, String target) {
        return resource.length() > target.length();
    }

    /**
     * 字符数组转16进制字符串
     */
    public static String coverToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        if (ArrayUtil.isEmpty(bytes)) {
            return null;
        }

        final int length = 0xFF;
        final int two = 2;
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & length;
            String hv = Integer.toHexString(v);
            if (hv.length() < two) {
                result.append(0);
            }
            result.append(hv);
        }
        return result.toString().toUpperCase();
    }

    /**
     * 异常转字符串
     *
     * @param e 异常
     * @return 字符串
     */
    public static String exceptionToString(Throwable e) {
        StringWriter writer = new StringWriter();
        try (PrintWriter pw = new PrintWriter(writer);) {
            e.printStackTrace(pw);
        }
        return writer.toString();
    }

    public static void main(String[] args) {
//        camelToMatchesRegex("asdDsa");
        toUnderline("asdDsa");
//        toSeparator("asdDsa", ":");
//        Set<String> set = Sets.newHashSet();
//        set.add("user_name");
//        set.add("User_name");
//        set.add("usernamE");
//        set.add("_usernamE");getGroupByStartEnd
//        vagueMatches("userName", set);
        System.out.println(removeExtension("s.s"));
    }

    public static String getFilenameExtension(String path) {
        if (path == null) {
            return null;
        }

        int extIndex = path.lastIndexOf(EXTENSION_SEPARATOR);
        if (extIndex == -1) {
            return null;
        }

        int folderIndex = path.lastIndexOf(Constant.RegularAbout.SLASH);
        if (folderIndex > extIndex) {
            return null;
        }

        return path.substring(extIndex + 1);
    }
}
