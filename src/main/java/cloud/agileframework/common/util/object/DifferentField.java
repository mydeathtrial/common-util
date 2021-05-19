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
public class DifferentField implements Serializable {
    public static final DifferentField EQUAL_FIELD = new DifferentField();
    /**
     * 属性名
     */
    private String propertyName;
    /**
     * 备注
     */
    private String propertyRemark;
    /**
     * 属性类型
     */
    private Class<?> propertyType;

    public static class LogFieldIgnoreException extends Exception{
        static final LogFieldIgnoreException
                LOG_FIELD_IGNORE_EXCEPTION = new LogFieldIgnoreException();
    }
}
