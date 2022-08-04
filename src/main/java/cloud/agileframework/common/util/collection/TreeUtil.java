package cloud.agileframework.common.util.collection;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.clazz.ClassUtil;
import cloud.agileframework.common.util.object.ObjectUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 佟盟
 * @version 1.0
 * @since 1.0
 */
public class TreeUtil {

    /**
     * 快速构建树形结构
     * <p>
     * 借助引用，快速构建树形结构，避免了递归的消耗
     *
     * @param nodes 构建源数据
     * @return 树形结构数据集
     */
    public static <A extends Serializable, T extends TreeBase<A, T>> SortedSet<T> createTree(Collection<? extends T> nodes, A rootValue, String splitChar, Set<Field> fullFieldSet) {
        Map<Optional<A>, List<T>> children = nodes.parallelStream().filter(node -> {
            node.getChildren().clear();
            return !Objects.equals(node.getParentId(), rootValue);
        }).collect(Collectors.groupingBy(n -> Optional.ofNullable(n.getParentId())));

        nodes.parallelStream().forEach(node -> {
            final List<T> child = children.get(Optional.ofNullable(node.getId()));
            if (child == null) {
                return;
            }
            node.setChildren(new ConcurrentSkipListSet<>(child));
        });

        //计算full属性值，借助ParentWrapper包裹，通过引用实现快速计算
        if (fullFieldSet != null && !fullFieldSet.isEmpty()) {
            List<ParentWrapper<A, T>> wrapperList = nodes.stream().map(ParentWrapper::new).collect(Collectors.toList());
            Map<A, ParentWrapper<A, T>> map = Maps.newConcurrentMap();
            wrapperList.parallelStream().forEach(a -> map.put(a.getCurrent().getId(), a));
            wrapperList.parallelStream().forEach(a -> {
                Object parentId = a.getCurrent().getParentId();
                if (parentId == null) {
                    return;
                }
                ParentWrapper<A, T> parentWrapper = map.get(parentId);
                if (parentWrapper == null) {
                    return;
                }
                a.setParent(parentWrapper);
            });

            Map<Object, T> cache = Maps.newConcurrentMap();
            fullFieldSet.forEach(b -> {
                wrapperList.parallelStream().forEach(a -> {
                    Object v = a.getFull(b, splitChar);
                    if (v == null) {
                        return;
                    }
                    cache.put(v, (T) a.getCurrent());
                });
                cache.entrySet().parallelStream().forEach(e -> ObjectUtil.setValue(e.getValue(), b, e.getKey()));
                cache.clear();
            });
        }
        return nodes.parallelStream().filter(node -> Objects.equals(node.getParentId(), rootValue)).collect(Collectors.toCollection(ConcurrentSkipListSet::new));
    }

    public static <A extends Serializable, T extends TreeBase<A, T>> SortedSet<T> createTree(Collection<? extends T> list, A rootValue, String splitChar, String... fullFields) {
        if (!list.isEmpty()) {
            T entity = list.stream().findAny().get();
            Class<T> tClass = (Class<T>) entity.getClass();

            Set<Field> fullFieldSet = Stream.of(fullFields)
                    .map(fieldName -> ClassUtil.getField(tClass, fieldName))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            return createTree(list, rootValue, splitChar, fullFieldSet);
        }

        return new ConcurrentSkipListSet<>();
    }

    public static <A extends Serializable, T extends TreeBase<A, T>> SortedSet<T> createTree(Collection<? extends T> list, A rootValue) {
        return createTree(list, rootValue, Constant.RegularAbout.SPOT);
    }

    public static <A extends Serializable, T extends TreeBase<A, T>> SortedSet<T> createTreeByNodeIds(Collection<? extends T> all, Collection<A> ids, A rootValue) {
        return createTreeByNodeIds(all, ids, rootValue, Constant.AgileAbout.DIC_SPLIT);
    }

    /**
     * 创建跟主键相关的树形结构，一般用于数据权限的使用场景
     *
     * @param all        完整树的全部数据
     * @param ids        想要查询相关节点的主键集合
     * @param rootValue  跟节点值
     * @param splitChar  分隔符
     * @param fullFields 累加字段
     * @param <A>        主键类型
     * @param <T>        树元素类型
     * @return 树
     */
    public static <A extends Serializable, T extends TreeBase<A, T>> SortedSet<T> createTreeByNodeIds(Collection<? extends T> all, Collection<A> ids, A rootValue, String splitChar, String... fullFields) {
        if (CollectionUtils.isEmpty(ids)) {
            all.clear();
            return Sets.newTreeSet();
        } else {
            List<TreeBaseProxy<A, T>> allWrapper = all.stream().map(TreeBaseProxy::new).collect(Collectors.toList());
            createTree(allWrapper, rootValue, splitChar, "fullId");

            Set<String> relevantNode = allWrapper
                    .stream()
                    .filter(m -> ids.contains(m.getId())).flatMap(m -> Arrays.stream(StringUtils.split(m.getFullId(), splitChar))).collect(Collectors.toSet());
            allWrapper.removeIf(m -> !relevantNode.contains(String.valueOf(m.getId())));
            List<T> allNodes = allWrapper.stream().map(TreeBaseProxy::getTreeBase).map(a -> (T) a).collect(Collectors.toList());
            return createTree(allNodes, rootValue, splitChar, fullFields);
        }
    }

    public static class TreeBaseProxy<A extends Serializable, T extends TreeBase<A, T>> extends TreeBase<A, TreeBaseProxy<A, T>> {
        private final T treeBase;

        public TreeBaseProxy(T treeBase) {
            this.treeBase = treeBase;
            this.fullId = String.valueOf(getId());
        }

        @Override
        public A getId() {
            return treeBase.getId();
        }

        @Override
        public A getParentId() {
            return treeBase.getParentId();
        }

        @Override
        public Integer getSort() {
            return treeBase.getSort();
        }

        private String fullId;

        public String getFullId() {
            return fullId;
        }

        public void setFullId(String fullId) {
            this.fullId = fullId;
        }

        public TreeBase<A, T> getTreeBase() {
            return treeBase;
        }
    }
}