package cloud.agileframework.common.util.object;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author 佟盟
 * 日期 2021-05-17 17:15
 * 描述 对象属性比较的属性信息
 * @version 1.0
 * @since 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class DifferentField implements Serializable {
    /**
     * 属性名
     */
    private String fieldName;
    /**
     * 备注
     */
    private String fieldRemark;
    /**
     * 属性类型
     */
    private Class<?> fieldType;

    public static class LogFieldIgnoreException extends Exception {
        static final LogFieldIgnoreException
                LOG_FIELD_IGNORE_EXCEPTION = new LogFieldIgnoreException();
    }

    /**
     * 描述
     *
     * @return 文字描述
     */
    public abstract String describe();
}
