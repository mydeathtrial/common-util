package cloud.agileframework.common.util.collection;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.clazz.ClassUtil;
import cloud.agileframework.common.util.object.ObjectUtil;
import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
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

    public static <A, T extends TreeBase<A>> SortedSet<T> createTree(Collection<T> list, A rootValue) {
        return createTree(list, rootValue, Constant.RegularAbout.SPOT);
    }

    /**
     * 构建树形结构
     *
     * @param list 构建源数据
     * @return 树形结构数据集
     */
    public static <A, T extends TreeBase<A>> SortedSet<T> createTree(Collection<T> list, A rootValue, String splitChar, String... fullFields) {
        if (!list.isEmpty()) {
            T entity = list.stream().findAny().get();
            Class<T> tClass = (Class<T>) entity.getClass();

            Set<Field> fullFieldSet = Stream.of(fullFields).map(fieldName -> {
                Field fullField;
                try {
                    fullField = ClassUtil.getField(tClass, fieldName);
                } catch (Exception e) {
                    return null;
                }
                return fullField;
            }).filter(Objects::nonNull).collect(Collectors.toSet());

            // 创建虚拟根节点
            T parent = ClassUtil.newInstance(tClass);
            if (parent == null) {
                throw new RuntimeException("fail to create object of " + tClass);
            }
            parent.setId(rootValue);

            return createChildren(parent, list, splitChar, fullFieldSet);
        }

        return new TreeSet<>();
    }

    private static <A, T extends TreeBase<A>> SortedSet<T> createChildren(T parentNode, Collection<T> collection, String splitChar, Set<Field> fullFieldSet) {
        SortedSet<T> children = new TreeSet<>();
        Object parentNodeKeyValue = parentNode.getId();

        Collection<T> all = Collections.synchronizedCollection(collection);
        synchronized (all){
            all.parallelStream().forEach(currentNode -> {
                try {
                    Object currentNodeParentKeyValue = currentNode.getParentId();
                    final boolean isChild = parentNodeKeyValue == null && currentNodeParentKeyValue == null
                            || (parentNodeKeyValue != null && String.valueOf(parentNodeKeyValue).equals(String.valueOf(currentNodeParentKeyValue)));
                    if (isChild) {
                        children.add(currentNode);

                        for (Field field : fullFieldSet) {
                            Object parentValue = ObjectUtil.getFieldValue(parentNode, field);
                            if (parentValue == null) {
                                continue;
                            }
                            final Object currentFullValue = ObjectUtil.getFieldValue(currentNode, field);
                            ObjectUtil.setValue(currentNode, field, parentValue + splitChar + currentFullValue);
                        }
                    }
                } catch (Exception ignored) {
                }
            });

            collection.removeIf(children::equals);
        }


        Collection<T> cc = Collections.synchronizedCollection(children);
        synchronized (cc){
            cc.forEach(currentNode -> {
                SortedSet<T> c = createChildren(currentNode, collection,
                        splitChar,
                        fullFieldSet);
                currentNode.setChildren(c);
            });
        }

        return children;
    }
}
