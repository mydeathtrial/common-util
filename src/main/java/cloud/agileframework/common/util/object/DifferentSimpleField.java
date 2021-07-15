package cloud.agileframework.common.util.object;

import cloud.agileframework.common.constant.Constant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;

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
        Object newValueTemp = newValue;
        if (newValueTemp instanceof String && StringUtils.isBlank((String) newValueTemp)) {
            newValueTemp = null;
        }

        Object oldValueTemp = oldValue;
        if (oldValueTemp instanceof String && StringUtils.isBlank((String) oldValueTemp)) {
            oldValueTemp = null;
        }
        if (!Objects.deepEquals(newValueTemp, oldValueTemp)) {
            if (oldValueTemp == null) {
                desc.append(String.format("设置成了%s", newValueTemp));
            } else if (newValueTemp == null) {
                desc.append(String.format("%s被删除了", oldValueTemp));
            } else {
                desc.append(String.format("由%s变成%s", oldValueTemp, newValueTemp));
            }
        }
        return desc.toString();
    }
}
