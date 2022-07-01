package cloud.agileframework.common.util.collection;

import lombok.Builder;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.util.Objects;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * @author 佟盟
 * 日期 2021-01-04 15:04
 * 描述 树元素
 * @version 1.0
 * @since 1.0
 */
@SuperBuilder(toBuilder = true)
public class TreeBase<I extends Serializable, A extends TreeBase<I, A>> implements Comparable<TreeBase<I, A>>, Serializable {
    private I id;
    private I parentId;
    private Integer sort;
    @Builder.Default
    private SortedSet<A> children = new ConcurrentSkipListSet<>();

    public TreeBase() {
        children = new ConcurrentSkipListSet<>();
    }

    /**
     * 获取跟的parentId
     *
     * @return 跟节点的parentId值
     */
    public static <C extends Serializable> C rootParentId() {
        return null;
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
        return Objects.equals(getId(), treeBase.getId())
                && Objects.equals(getParentId(), treeBase.getParentId())
                && Objects.equals(getSort(), treeBase.getSort());
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
        if (Objects.equals(this, o)) {
            return 0;
        }
        if (o == null) {
            return 1;
        }
        int a = o.getSort() == null ? 0 : o.getSort();
        int b = sort == null ? 0 : sort;
        final int i = b - a;
        return i == 0 ? 1 : i;
    }

    public void setChildren(SortedSet<A> children) {
        if (children == null) {
            return;
        }
        this.children = children;
    }

    public I getId() {
        return id;
    }

    public void setId(I id) {
        this.id = id;
    }

    public I getParentId() {
        return parentId;
    }

    public void setParentId(I parentId) {
        this.parentId = parentId;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public SortedSet<A> getChildren() {
        return children;
    }
}
