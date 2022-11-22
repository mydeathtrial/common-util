package cloud.agileframework.common.util.file.poi;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author on 2018/10/16
 * @author 佟盟
 * sheet页信息
 */
public class SheetData<T> {
    //sheet 页名
    private String name;
    //字段头信息
    private List<CellInfo> cells;
    //所有行数据
    private List<T> data;

    public SheetData(String name, List<CellInfo> cells, List<T> data) {
        this.name = name;
        this.cells = cells;
        this.data = data;
    }

    private SheetData(Builder builder) {
        this.name = builder.name;
        this.cells = builder.cells;
        this.data = (List<T>) builder.data;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        if (name == null) {
            return "数据";
        }
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public List<CellInfo> getCells() {
        return cells;
    }

    public void setCells(List<CellInfo> cells) {
        this.cells = cells;
    }

    public SheetData<T> addCell(CellInfo cell) {
        if (cells == null) {
            cells = Lists.newCopyOnWriteArrayList();
        }
        this.cells.add(cell);
        return this;
    }

    /**
     * 建造者
     */
    public static class Builder {
        private String name;
        private List<CellInfo> cells;
        private List<?> data;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setCells(List<CellInfo> cells) {
            this.cells = cells;
            return this;
        }

        public <T>Builder setData(List<T> data) {
            this.data = data;
            return this;
        }

        public SheetData build() {
            return new SheetData<>(this);
        }
    }
}
