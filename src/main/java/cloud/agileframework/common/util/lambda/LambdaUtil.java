package cloud.agileframework.common.util.lambda;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author 佟盟
 * 日期 2019/6/28 13:36
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public class LambdaUtil {

    /**
     * 运用lambda表达式中，反复试运行若干段程序段场景
     *
     * @param consumer 试运行的程序段
     * @param <I>      原始参数
     * @return 试运行后的包装结果
     */
    public static <I> Function<ModifiedResult<I>, ModifiedResult<I>> test(Consumer<ModifiedResult<I>> consumer) {
        return modifiedResult -> {
            if (modifiedResult.isSuccess()) {
                return modifiedResult;
            }
            try {
                consumer.accept(modifiedResult);
                return ModifiedResult.success(modifiedResult.getSource());
            } catch (Exception e) {
                return ModifiedResult.init(modifiedResult.getSource());
            }
        };
    }
}
