package cloud.agileframework.common.util.collection;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.clazz.ClassUtil;
import cloud.agileframework.common.util.object.ObjectUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author 佟盟
 * @version 1.0
 * @since 1.0
 */
public class TreeUtil {

    public static <T> List<T> createTree(List<T> list, String key, String parentKey, String childrenKey, String rootValue) throws NoSuchFieldException, IllegalAccessException {
        return createTree(list, key, parentKey, childrenKey, null, Constant.RegularAbout.SPOT, rootValue);
    }

    /**
     * 构建树形结构
     *
     * @param list 构建源数据
     * @return 树形结构数据集
     */
    public static <T> List<T> createTree(List<T> list, String key, String parentKey, String childrenKey, String sortKey, String rootValue, String splitChar, String... fullFields) throws NoSuchFieldException, IllegalAccessException {
        List<T> roots = new ArrayList<>();

        if (!list.isEmpty()) {
            T entity = list.get(0);
            Class<T> tClass = (Class<T>) entity.getClass();

            Field keyField = tClass.getDeclaredField(key);
            keyField.setAccessible(true);
            Field parentKeyField = tClass.getDeclaredField(parentKey);
            parentKeyField.setAccessible(true);
            Field childrenKeyField = tClass.getDeclaredField(childrenKey);
            childrenKeyField.setAccessible(true);

            Set<Field> fullFieldSet = Stream.of(fullFields).map(fieldName -> {
                Field fullField;
                try {
                    fullField = ClassUtil.getField(tClass,fieldName);
                } catch (Exception e) {
                    return null;
                }
                fullField.setAccessible(true);
                return fullField;
            }).filter(Objects::nonNull).collect(Collectors.toSet());

            // 创建虚拟根节点
            T parent = ClassUtil.newInstance(tClass);
            keyField.set(parent, rootValue);

            return createChildren(parent, list, keyField, parentKeyField, childrenKeyField, sortKey, splitChar, fullFieldSet);
        }

        return roots;
    }

    private static <T> List<T> createChildren(T parentNode, List<T> list, Field keyField, Field parentKeyField, Field childrenKeyField, String sortKey, String splitChar, Set<Field> fullFieldSet) throws IllegalAccessException {
        List<T> children = new ArrayList<>();
        Object parentNodeKeyValue = keyField.get(parentNode);
        for (T currentNode : list) {
            Object currentNodeParentKeyValue = ObjectUtil.getFieldValue(currentNode,parentKeyField);
            final boolean isChild = parentNodeKeyValue == null && currentNodeParentKeyValue == null
                    || (parentNodeKeyValue != null && parentNodeKeyValue.equals(currentNodeParentKeyValue));
            if (isChild) {
                children.add(currentNode);

                for (Field field : fullFieldSet) {
                    Object parentValue = ObjectUtil.getFieldValue(parentNode,field);
                    if (parentValue == null) {
                        continue;
                    }
                    field.set(currentNode, parentValue + splitChar + ObjectUtil.getFieldValue(currentNode,field));
                }
                childrenKeyField.set(currentNode, createChildren(currentNode, list, keyField, parentKeyField, childrenKeyField, sortKey, splitChar, fullFieldSet));
            }
        }
        if (StringUtils.isBlank(sortKey) || children.isEmpty()) {
            return children;
        }
        CollectionsUtil.sort(children, sortKey);
        return children;
    }

}
