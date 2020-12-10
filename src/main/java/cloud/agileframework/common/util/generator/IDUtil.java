package cloud.agileframework.common.util.generator;

import cloud.agileframework.common.constant.Constant;

/**
 * @author 佟盟
 * 日期 2020/8/00014 16:11
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class IDUtil {
    private static SnowflakeIdWorker snowflakeIdWorker;

    public static Long generatorId() {
        return getSnowflakeIdWorker().nextId();
    }

    public static synchronized SnowflakeIdWorker getSnowflakeIdWorker() {

        if (snowflakeIdWorker == null) {
            snowflakeIdWorker = new SnowflakeIdWorker(Constant.NumberAbout.ONE, Constant.NumberAbout.ONE);
        }
        return snowflakeIdWorker;
    }
}
