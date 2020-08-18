package cloud.agileframework.common.util.lambda;

/**
 * @param <I> 参数
 * @author 佟盟
 * 日期 2019/9/17 11:24
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
public final class ModifiedResult<I> {
    private final I source;
    private final boolean isSuccess;

    private ModifiedResult(I source, boolean isSuccess) {
        this.source = source;
        this.isSuccess = isSuccess;
    }

    public I getSource() {
        return source;
    }

    public boolean isSuccess() {
        return isSuccess;
    }

    public boolean haveInParam() {
        return source != null;
    }

    public static <I> ModifiedResult<I> init(I inParam) {
        return new ModifiedResult<>(inParam, false);
    }

    public static <I> ModifiedResult<I> success(I inParam) {
        return new ModifiedResult<>(inParam, true);
    }
}
