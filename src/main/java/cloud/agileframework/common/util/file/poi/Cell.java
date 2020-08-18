package cloud.agileframework.common.util.file.poi;

/**
 * @author on 2018/10/17
 * @author 佟盟
 * 为POI生成excel工具提供的辅助类，该类主要负责存放表头信息
 */
public final class Cell {
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
    private String showName;

    private Cell(Builder builder) {
        this.sort = builder.sort;
        this.key = builder.key;
        this.showName = builder.showName;
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

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    /**
     * 建造者
     */
    public static class Builder {
        private int sort;
        private String key;
        private String showName;

        public Builder setSort(int sort) {
            this.sort = sort;
            return this;
        }

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public Builder setShowName(String showName) {
            this.showName = showName;
            return this;
        }

        public Cell build() {
            return new Cell(this);
        }
    }
}
