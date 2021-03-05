package cloud.agileframework.common.util.collection;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 佟盟
 * 日期 2021-01-04 15:04
 * 描述 树元素
 * @version 1.0
 * @since 1.0
 */
@Data
public class TreeBase<I> {
    private I id;
    private I parentId;
    private Integer sort;
    private List<? extends TreeBase<I>> children = new ArrayList<>();
}
