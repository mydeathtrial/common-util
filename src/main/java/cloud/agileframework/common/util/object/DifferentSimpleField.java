package cloud.agileframework.common.util.object;

import cloud.agileframework.common.constant.Constant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Objects;

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
public class DifferentSimpleField extends DifferentField {
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

    @Override
    public String describe() {
        StringBuilder desc = new StringBuilder(getFieldRemark() == null ? getFieldName() : getFieldRemark()).append(Constant.RegularAbout.COLON);
        if (!Objects.deepEquals(newValue, oldValue)) {
            if (oldValue == null) {
                desc.append(String.format("设置成了%s", newValue));
            } else if (newValue == null) {
                desc.append(String.format("%s被删除了", oldValue));
            } else {
                desc.append(String.format("由%s变成%s", oldValue, newValue));
            }
        }
        return desc.toString();
    }
}
