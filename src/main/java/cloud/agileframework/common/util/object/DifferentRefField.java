package cloud.agileframework.common.util.object;

import cloud.agileframework.common.constant.Constant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.apache.commons.compress.utils.Lists;

import java.util.List;
import java.util.stream.Collectors;

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
public class DifferentRefField extends DifferentField {
    private List<DifferentField> ref;
    private boolean ignoreParentName;
    private boolean ignoreParentRemark;

    public DifferentRefField(String propertyName, String propertyRemark, Class<?> propertyType, List<DifferentField> ref, boolean ignoreParentName, boolean ignoreParentRemark) {
        super(propertyName, propertyRemark, propertyType);
        this.ref = ref;
        this.ignoreParentName = ignoreParentName;
        this.ignoreParentRemark = ignoreParentRemark;
    }

    /**
     * 提取引用差异
     *
     * @return 引用差异集合
     */
    public List<DifferentField> extractRef() {
        if (ref == null) {
            return Lists.newArrayList();
        }
        if (ref.isEmpty()) {
            return ref;
        }
        return ref.stream().map(r -> {
            if (!ignoreParentName) {
                String parentFieldName = getFieldName();

                String currentFieldName = r.getFieldName();

                r.setFieldName(parentFieldName + Constant.RegularAbout.SPOT + currentFieldName);
            }

            if (!ignoreParentRemark) {
                String parentFieldRemark = getFieldRemark();
                if (parentFieldRemark == null) {
                    parentFieldRemark = getFieldName();
                }
                String currentFieldRemark = r.getFieldRemark();
                if (currentFieldRemark == null) {
                    currentFieldRemark = r.getFieldName();
                }

                r.setFieldRemark(parentFieldRemark + Constant.RegularAbout.SPOT + currentFieldRemark);
            }

            return r;
        }).collect(Collectors.toList());
    }
}
