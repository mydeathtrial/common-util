package cloud.agileframework.common.util.collection;

import cloud.agileframework.common.util.object.ObjectUtil;
import lombok.Data;

import java.lang.reflect.Field;

/**
 * @author 佟盟
 * 日期 2021-04-09 14:33
 * 描述 处理创建树过程中，计算full属性时的包裹类型
 * @version 1.0
 * @since 1.0
 */
@Data
public class ParentWrapper<I> {
    private ParentWrapper<I> parent;
    private final TreeBase<I> current;

    public ParentWrapper(TreeBase<I> current) {
        this.current = current;
    }

    /**
     * 递归获取full属性值
     * @param field 属性
     * @param split 分隔符
     * @return full属性值
     */
    public Object getFull(Field field, String split) {
        if (parent == null) {
            return ObjectUtil.getFieldValue(current, field);
        }
        return parent.getFull(field, split) + split + ObjectUtil.getFieldValue(current, field);
    }
}
