package cloud.agileframework.common.util.collection;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.clazz.ClassUtil;
import cloud.agileframework.common.util.object.ObjectUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
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
     *
     * 借助引用，快速构建树形结构，避免了递归的消耗
     *
     * @param nodes 构建源数据
     * @return 树形结构数据集
     */
    public static <A, T extends TreeBase<A>> SortedSet<T> createTree(Collection<T> nodes, A rootValue, String splitChar, Set<Field> fullFieldSet) {
        Map<A, List<T>> children = nodes.parallelStream().filter(node -> !Objects.equals(node.getParentId(), rootValue)).collect(Collectors.groupingBy(TreeBase::getParentId));

        nodes.parallelStream().forEach(node -> {
            final List<T> child = children.get(node.getId());
            if (child == null) {
                return;
            }
            node.setChildren(Sets.newTreeSet(child));
        });

        //计算full属性值，借助ParentWrapper包裹，通过引用实现快速计算
        if (fullFieldSet != null && !fullFieldSet.isEmpty()) {
            List<ParentWrapper<A>> wrapperList = nodes.stream().map(ParentWrapper::new).collect(Collectors.toList());
            Map<A, ParentWrapper<A>> map = Maps.newConcurrentMap();
            wrapperList.parallelStream().forEach(a -> map.put(a.getCurrent().getId(), a));
            wrapperList.parallelStream().forEach(a -> {
                Object parentId = a.getCurrent().getParentId();
                if (parentId == null) {
                    return;
                }
                ParentWrapper<A> parentWrapper = map.get(parentId);
                if (parentWrapper == null) {
                    return;
                }
                a.setParent(parentWrapper);
            });

            Map<Object, T> cache = Maps.newConcurrentMap();
            fullFieldSet.forEach(b -> {
                wrapperList.parallelStream().forEach(a -> {
                    Object v = a.getFull(b, splitChar);
                    cache.put(v, (T) a.getCurrent());
                });
                cache.entrySet().parallelStream().forEach(e -> ObjectUtil.setValue(e.getValue(), b, e.getKey()));
                cache.clear();
            });
        }
        return nodes.parallelStream().filter(node -> Objects.equals(node.getParentId(), rootValue)).collect(Collectors.toCollection(Sets::newTreeSet));
    }

    public static <A, T extends TreeBase<A>> SortedSet<T> createTree(Collection<T> list, A rootValue, String splitChar, String... fullFields) {
        if (!list.isEmpty()) {
            T entity = list.stream().findAny().get();
            Class<T> tClass = (Class<T>) entity.getClass();

            Set<Field> fullFieldSet = Stream.of(fullFields)
                    .map(fieldName -> ClassUtil.getField(tClass, fieldName))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            return createTree(list, rootValue, splitChar, fullFieldSet);
        }

        return new TreeSet<>();
    }

    public static <A, T extends TreeBase<A>> SortedSet<T> createTree(Collection<T> list, A rootValue) {
        return createTree(list, rootValue, Constant.RegularAbout.SPOT);
    }
}