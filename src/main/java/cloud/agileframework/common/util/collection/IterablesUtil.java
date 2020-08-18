package cloud.agileframework.common.util.collection;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * @author 佟盟
 * 日期 2020/3/23 16:12
 * 描述 迭代器工具
 * @version 1.0
 * @since 1.0
 */
public class IterablesUtil {
    /**
     * 流转换
     *
     * @param startIndex 起始遍历索引
     * @param node       元素
     * @param action     动作
     * @param <T>        入参
     * @param <R>        出参
     * @return 出参流
     */
    public static <T, R> Stream<R> map(Integer startIndex, Iterable<T> node, BiFunction<Integer, T, R> action) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(action);
        ArrayList<R> list = Lists.newArrayList();
        for (T element : node) {
            list.add(action.apply(startIndex++, element));
        }
        return list.stream();
    }

    /**
     * 遍历并且返回流
     *
     * @param startIndex 起始遍历索引
     * @param node       元素
     * @param action     动作
     * @param <T>        入参
     */
    public static <T> void forEach(Integer startIndex, Iterable<T> node, BiConsumer<Integer, T> action) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(action);
        for (T element : node) {
            action.accept(startIndex++, element);
        }
    }
}
