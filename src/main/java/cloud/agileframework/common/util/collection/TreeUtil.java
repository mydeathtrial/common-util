package cloud.agileframework.common.util.collection;

import cloud.agileframework.common.constant.Constant;
import cloud.agileframework.common.util.clazz.ClassInfo;
import cloud.agileframework.common.util.clazz.ClassUtil;
import cloud.agileframework.common.util.object.ObjectUtil;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
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

    public static <T> List<T> createTree(List<T> list, String key, String parentKey, String childrenKey, String rootValue) throws IllegalAccessException {
        return createTree(list, key, parentKey, childrenKey, null, rootValue, Constant.RegularAbout.SPOT);
    }

    /**
     * 构建树形结构
     *
     * @param list 构建源数据
     * @return 树形结构数据集
     */
    public static <T> List<T> createTree(List<T> list, String key, String parentKey, String childrenKey, String sortKey, String rootValue, String splitChar, String... fullFields) throws IllegalAccessException {
        if (!list.isEmpty()) {
            T entity = list.get(0);
            Class<T> tClass = (Class<T>) entity.getClass();
            ClassInfo<T> tClassInfo = ClassInfo.getCache(tClass);


            Field keyField = tClassInfo.getField(key);
            Field parentKeyField = tClassInfo.getField(parentKey);
            Field childrenKeyField = tClassInfo.getField(childrenKey);

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
            ObjectUtil.setValue(parent, keyField, rootValue);

            return createChildren(parent, new ArrayList<>(list), keyField, parentKeyField, childrenKeyField, sortKey, splitChar, fullFieldSet);
        }

        return new ArrayList<>();
    }

    private static <T> List<T> createChildren(T parentNode, List<T> list, Field keyField, Field parentKeyField, Field childrenKeyField, String sortKey, String splitChar, Set<Field> fullFieldSet) throws IllegalAccessException {
        List<T> children = new ArrayList<>();
        Object parentNodeKeyValue = keyField.get(parentNode);

        Collections.synchronizedCollection(list).parallelStream().forEach(currentNode -> {
            try {
                Object currentNodeParentKeyValue = ObjectUtil.getFieldValue(currentNode, parentKeyField);
                final boolean isChild = parentNodeKeyValue == null && currentNodeParentKeyValue == null
                        || (parentNodeKeyValue != null && parentNodeKeyValue.equals(currentNodeParentKeyValue));
                if (isChild) {
                    children.add(currentNode);

                    for (Field field : fullFieldSet) {
                        Object parentValue = ObjectUtil.getFieldValue(parentNode, field);
                        if (parentValue == null) {
                            continue;
                        }
                        field.set(currentNode, parentValue + splitChar + ObjectUtil.getFieldValue(currentNode, field));
                    }
                }
            } catch (Exception ignored) {
            }
        });

        list.removeIf(children::equals);

        Collections.synchronizedCollection(children).parallelStream().forEach(currentNode -> {
            try {
                childrenKeyField.set(currentNode,
                        createChildren(currentNode, new ArrayList<>(list),
                                keyField,
                                parentKeyField,
                                childrenKeyField,
                                sortKey,
                                splitChar,
                                fullFieldSet));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        if (StringUtils.isBlank(sortKey) || children.isEmpty()) {
            return children;
        }
        CollectionsUtil.sort(children, sortKey);
        return children;
    }
}
