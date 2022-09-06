package cloud.agileframework.common.util.file.poi;

import java.util.function.UnaryOperator;

/**
 * @author on 2018/10/17
 * @author 佟盟
 * 为POI生成excel工具提供的辅助类，该类主要负责存放表头信息
 */
public class CellInfo {
    /**
     * 排位
     */
    private int sort;
    /**
     * 列索引值
     */
    private String key;
    /**
     * 显示名
     */
    private String name;

    private Class<?> type;
    
    private UnaryOperator<Object> deserialize;

    private UnaryOperator<Object> serialize;

    private CellInfo(Builder builder) {
        this.sort = builder.sort;
        this.key = builder.key;
        this.name = builder.name;
        this.type = builder.type;
        this.serialize = builder.serialize;
        this.deserialize = builder.deserialize;
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public UnaryOperator<Object> getDeserialize() {
        return deserialize;
    }

    public void setDeserialize(UnaryOperator<Object> deserialize) {
        this.deserialize = deserialize;
    }

    public UnaryOperator<Object> getSerialize() {
        return serialize;
    }

    public void setSerialize(UnaryOperator<Object> serialize) {
        this.serialize = serialize;
    }

    /**
     * 建造者
     */
    public static class Builder {
        private int sort;
        private String key;
        private String name;
        private Class<?> type = Object.class;

        private UnaryOperator<Object> deserialize = a -> a;

        private UnaryOperator<Object> serialize = a -> a;

        public Builder sort(int sort) {
            this.sort = sort;
            return this;
        }

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(Class<?> type) {
            this.type = type;
            return this;
        }

        public Builder deserialize(UnaryOperator<Object> deserialize) {
            this.deserialize = deserialize;
            return this;
        }

        public Builder serialize(UnaryOperator<Object> serialize) {
            this.serialize = serialize;
            return this;
        }

        public CellInfo build() {
            return new CellInfo(this);
        }
    }
}
