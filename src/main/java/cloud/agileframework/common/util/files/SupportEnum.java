package cloud.agileframework.common.util.files;

import java.util.stream.Stream;

/**
 * @author 佟盟
 * 日期 2019/11/29 9:32
 * 描述 配置文件加载支持的文件类型
 * @version 1.0
 * @since 1.0
 */
public enum SupportEnum {
    /**
     * yml
     */
    yml,
    yaml,
    json,
    ftl,
    xml,
    py,
    jar,
    properties;

    /**
     * 判断文件名是否是支持的文件类型
     * @param fileName 文件名
     * @return 是否
     */
    public static boolean isSupport(String fileName) {
        return Stream.of(values()).anyMatch(node -> fileName.endsWith(node.name()));
    }
}
