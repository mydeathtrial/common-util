package cloud.agileframework.common.util.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author 佟盟
 * 日期 2021-05-17 17:16
 * 描述 简单类型属性比较心细
 * @version 1.0
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class DifferentSimpleField extends DifferentField{
    /**
     * 新值
     */
    private Object newValue;
    /**
     * 旧值
     */
    private Object oldValue;

    public DifferentSimpleField(String propertyName, String propertyRemark, Class<?> propertyType) {
        super(propertyName, propertyRemark, propertyType);
    }
}
