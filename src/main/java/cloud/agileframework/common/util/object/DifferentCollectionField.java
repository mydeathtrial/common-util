package cloud.agileframework.common.util.object;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author 佟盟
 * 日期 2021-05-17 17:17
 * 描述 集合属性比较信息
 * @version 1.0
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class DifferentCollectionField extends DifferentField{
    private List<Object> add;
    private List<Object> del;

    public DifferentCollectionField(String propertyName, String propertyRemark, Class<?> propertyType) {
        super(propertyName, propertyRemark, propertyType);
    }
}
