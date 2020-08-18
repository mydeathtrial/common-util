package cloud.agileframework.common.util.file.poi;

import java.util.ArrayList;
import java.util.List;

/**
 * @author on 2018/10/16
 * @author 佟盟
 * sheet页信息
 */
public class SheetData {
    private String name;
    private List<Cell> cells;
    private List data;

    public SheetData() {
    }

    public SheetData(String name, List<Cell> cells, List data) {
        this.name = name;
        this.cells = cells;
        this.data = data;
    }

    private SheetData(Builder builder) {
        this.name = builder.name;
        this.cells = builder.cells;
        this.data = builder.data;
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List getData() {
        return data;
    }

    public void setData(List data) {
        this.data = data;
    }

    public List<Cell> getCells() {
        return cells;
    }

    public void setCells(List<Cell> cells) {
        this.cells = cells;
    }

    public SheetData addCell(Cell cell) {
        if (cells == null) {
            cells = new ArrayList<>();
        }
        this.cells.add(cell);
        return this;
    }

    /**
     * 建造者
     */
    public static class Builder {
        private String name;
        private List<Cell> cells;
        private List data;

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setCells(List<Cell> cells) {
            this.cells = cells;
            return this;
        }

        public Builder setData(List data) {
            this.data = data;
            return this;
        }

        public SheetData build() {
            return new SheetData(this);
        }
    }
}
