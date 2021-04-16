package cloud.agileframework.common.util.collection;

import lombok.Data;

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
@Data
public class TreeBase<I extends Serializable, A extends TreeBase<I, A>> implements Comparable<TreeBase<I, A>>, Serializable {
    private I id;
    private I parentId;
    private Integer sort;
    private SortedSet<A> children = new TreeSet<>();

    public <B extends A> void setChildren(SortedSet<B> children) {
        this.children.addAll(children);
    }

    /**
     * 重写比较方法，规避children比较
     *
     * @param o 比较对象
     * @return 是否相等
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TreeBase)) {
            return false;
        }
        TreeBase<?, ?> treeBase = (TreeBase<?, ?>) o;
        return Objects.equals(getId(), treeBase.getId()) && Objects.equals(getParentId(), treeBase.getParentId()) && Objects.equals(getSort(), treeBase.getSort());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getParentId(), getSort());
    }

    /**
     * 处理排序，规避TreeSet比较时返回0导致数据丢失问题
     *
     * @param o 比较对象
     * @return 比较结果
     */
    @Override
    public int compareTo(TreeBase<I, A> o) {
        if (o == null) {
            return 1;
        }
        int a = o.getSort() == null ? 0 : o.getSort();
        int b = sort == null ? 0 : sort;
        final int i = b - a;
        return i == 0 ? 1 : i;
    }
}
