package cloud.agileframework.common.util.collection;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author 佟盟
 * 日期 2021-04-09 10:16
 * 描述 TODO
 * @version 1.0
 * @since 1.0
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class TreeNode extends TreeBase<String> {
    private String full;

    public TreeNode() {
    }

    public TreeNode(String id, String parentId, int sort) {
        super();
        setId(id);
        setParentId(parentId);
        setSort(sort);
        setFull(id);
    }
}
