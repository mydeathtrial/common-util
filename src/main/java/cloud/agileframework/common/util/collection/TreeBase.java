package cloud.agileframework.common.util.collection;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author 佟盟
 * 日期 2021-01-04 15:04
 * 描述 树元素
 * @version 1.0
 * @since 1.0
 */
@EqualsAndHashCode
@Data
public class TreeBase<I> implements Comparable<TreeBase<I>>, Serializable {
    private I id;
    private I parentId;
    private Integer sort;
    private SortedSet<? extends TreeBase<I>> children = new TreeSet<>();


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TreeBase)) {
            return false;
        }
        TreeBase<?> treeBase = (TreeBase<?>) o;
        return Objects.equals(getId(), treeBase.getId()) && Objects.equals(getParentId(), treeBase.getParentId()) && Objects.equals(getSort(), treeBase.getSort());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getParentId(), getSort());
    }

    @Override
    public int compareTo(TreeBase<I> o) {
        if (o == null) {
            return 1;
        }
        int a = o.getSort() == null ? 0 : o.getSort();
        int b = sort == null ? 0 : sort;
        final int i = b - a;
        return i == 0 ? 1 : i;
    }
}
