package cloud.agileframework.common.util.file.poi;

import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.collection.CollectionsUtil;
import cloud.agileframework.common.util.file.FileUtil;
import cloud.agileframework.common.util.object.ObjectUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author 佟盟 on 2018/10/16
 */
public class POIUtil {
    private static final String SORT_FIELD_NAME = "sort";
    public static final DataFormatter DATA_FORMATTER = new DataFormatter();

    /**
     * 创建excel
     *
     * @param version   excel版本
     * @param sheetData sheet页数据
     * @return POI WorkBook对象
     */
    public static Workbook creatExcel(VERSION version, SheetData... sheetData) {
        Workbook excel = null;

        //判断excel版本
        switch (version) {
            case V2003:
                excel = new HSSFWorkbook();
                break;
            case V2007:
                excel = new XSSFWorkbook();
                break;
            case V2008:
                excel = new SXSSFWorkbook();
                break;
            default:
        }

        //遍历sheet页
        for (SheetData sheetDatum : sheetData) {
            creatSheet(excel, sheetDatum);
        }
        return excel;
    }

    /**
     * 创建sheet页对象
     *
     * @param excel     excel对象
     * @param sheetData sheet数据
     */
    private static void creatSheet(Workbook excel, SheetData sheetData) {
        Sheet sheet = excel.createSheet(sheetData.getName());
        if (sheet instanceof SXSSFSheet) {
            ((SXSSFSheet) sheet).trackAllColumnsForAutoSizing();
        }
        int currentRowIndex = 0;
        //创建字段头
        List<CellInfo> headerColumns = sheetData.getCells();

        final List<?> data = sheetData.getData();
        if (!CollectionUtils.isEmpty(headerColumns)) {
            //对excel表头进行排序
            CollectionsUtil.sort(headerColumns, SORT_FIELD_NAME);

            //创建表头
            Row row = sheet.createRow(currentRowIndex++);
            for (int i = 0; i < headerColumns.size(); i++) {
                row.createCell(i).setCellValue(headerColumns.get(i).getName());
            }

            //逐行创建表数据
            if (data == null) {
                return;
            }
            for (Object datum : data) {
                createRow(sheet, datum, currentRowIndex++, headerColumns);
            }
        } else {
            if (data == null) {
                return;
            }
            for (Object datum : data) {
                createRow(sheet, datum, currentRowIndex++);
            }
        }
    }

    /**
     * 无表头excel
     */
    private static void createRow(Sheet sheet, Object rowData, int rowIndex) {
        Row row = sheet.createRow(rowIndex);
        int currentColumnIndex = 0;
        if (rowData == null) {
            return;
        }
        Map<String, Object> data = ObjectUtil.to(rowData, new TypeReference<Map<String, Object>>() {
        });

        for (Object cell : data.values()) {
            row.createCell(currentColumnIndex++).setCellValue(ObjectUtil.to(cell, new TypeReference<String>() {
            }));
        }
    }

    /**
     * 创建行数据
     *
     * @param sheet         Sheet页对象
     * @param rowData       行数据
     * @param rowIndex      行号
     * @param headerColumns 表头
     */
    private static void createRow(Sheet sheet, Object rowData, int rowIndex, List<CellInfo> headerColumns) {
        Row row = sheet.createRow(rowIndex);
        int currentColumnIndex = 0;
        for (CellInfo cell : headerColumns) {
            Object currentCellData = null;
            if (rowData instanceof Map) {
                currentCellData = ((Map) rowData).get(cell.getKey());
            } else if (rowData instanceof List) {
                Object o = ((List) rowData).get(currentColumnIndex);
                if (o instanceof CellInfo) {
                    currentCellData = ((CellInfo) o).getName();
                } else if (o instanceof String) {
                    currentCellData = o;
                }
            } else {
                if (rowData != null) {
                    try {
                        Field field = rowData.getClass().getDeclaredField(cell.getKey());
                        field.setAccessible(true);
                        currentCellData = field.get(rowData);
                    } catch (IllegalAccessException | NoSuchFieldException ignored) {
                    }
                }
            }
            Object value = cell.getSerialize().apply(currentCellData);
            if (value instanceof Date) {
                row.createCell(currentColumnIndex++).setCellValue((Date) value);
            } else if (value instanceof Double) {
                row.createCell(currentColumnIndex++).setCellValue((Double) value);
            } else if (value instanceof String) {
                row.createCell(currentColumnIndex++).setCellValue((String) value);
            } else if (value instanceof Boolean) {
                row.createCell(currentColumnIndex++).setCellValue((Boolean) value);
            } else {
                row.createCell(currentColumnIndex++).setCellValue("");
            }

        }
    }

    /**
     * 读取excel文件成list-map形式
     *
     * @param file excel文件
     * @return 格式化结果
     */
    public static <T> List<T> readExcel(Class<T> clazz, File file) throws IOException {
        return readExcel(new TypeReference<T>(clazz), file, null);
    }

    /**
     * 读取excel文件成list-map形式，并且map-key值对应columns内容
     *
     * @param file    文件
     * @param columns map-key对应字段
     * @return 格式化结果
     */
    public static List<Map<String, Object>> readExcel(File file, List<CellInfo> columns) throws IOException {
        return readExcel(new TypeReference<Map<String, Object>>() {
        }, file, columns);
    }


    public static <T> List<T> readExcel(TypeReference<T> typeReference, File file, List<CellInfo> columns) throws IOException {
        Workbook excel = readFile(file);
        return readExcel(typeReference, columns, excel);

    }

    public static <T> List<T> readExcel(TypeReference<T> typeReference, List<CellInfo> columns, Workbook excel) {
        if (excel == null) {
            return Lists.newArrayList();
        }
        List<T> list = Lists.newArrayList();
        Iterator<Sheet> sheets = excel.sheetIterator();
        while (sheets.hasNext()) {
            list.addAll(readSheet(typeReference, columns, sheets.next(), excel));
        }
        return list;
    }

    public static List<Map<String, Object>> readSheet(List<CellInfo> columns, Sheet sheet, Workbook workbook) {
        return readSheet(new TypeReference<Map<String, Object>>() {
        }, columns, sheet, workbook);
    }

    public static <T> List<T> readSheet(Class<T> clazz, List<CellInfo> columns, Sheet sheet, Workbook workbook) {
        return readSheet(new TypeReference<T>(clazz), columns, sheet, workbook);
    }

    /**
     * excel按准许翻译每一列的CellInfo信息
     *
     * @param columns 字段信息
     * @param sheet   sheet页
     * @return 字段信息列表
     */
    public static List<CellInfo> readColumnInfo(List<CellInfo> columns, Sheet sheet) {
        Map<String, CellInfo> columnMapping = columns.stream().collect(Collectors.toMap(a -> a.getName().trim(), a -> a));
        if (columnMapping.isEmpty()) {
            return Lists.newArrayList();
        }
        Iterator<Row> rows = sheet.rowIterator();
        List<CellInfo> columnInfo = Lists.newLinkedList();
        if (!rows.hasNext()) {
            return Lists.newArrayList();
        }
        Row row = rows.next();

        Iterator<Cell> cells = row.cellIterator();
        int cellIndex = 1;
        while (cells.hasNext()) {
            Cell cell = cells.next();
            String cellName = cell.getStringCellValue().trim();
            if (StringUtils.isBlank(cellName)) {
                cellName = cellIndex + "";
            }
            CellInfo cellInfo = columnMapping.get(cellName);
            if (cellInfo == null) {
                columnInfo.add(CellInfo.builder().name(cellName).key(cellIndex + "").build());
            } else {
                columnInfo.add(cellInfo);
            }
            cellIndex++;
        }
        return columnInfo;
    }

    /**
     * 读取sheet页
     *
     * @param columns 映射字段
     * @param sheet   sheet页
     */
    public static <T> List<T> readSheet(TypeReference<T> typeReference, List<CellInfo> columns, Sheet sheet, Workbook workbook) {
        List<T> list = Lists.newArrayList();
        List<CellInfo> columnInfo = readColumnInfo(columns, sheet);
        if (columnInfo.isEmpty()) {
            return list;
        }

        Iterator<Row> rows = sheet.rowIterator();
        int rowNum = 0;
        while (rows.hasNext()) {
            if (rowNum != 0) {
                Row row = rows.next();
                T result = readRow(typeReference, columnInfo, row, workbook);
                if (result == null) continue;
                list.add(result);
            }
            rowNum++;
        }

        return list;
    }

    public static <T> T readRow(TypeReference<T> typeReference, List<CellInfo> columnInfo, Row row, Workbook workbook) {
        LinkedHashMap<String, Object> rowData = new LinkedHashMap<>();
        Iterator<Cell> cells = row.cellIterator();
        while (cells.hasNext()) {
            Cell cell = cells.next();
            int dataCellIndex = cell.getColumnIndex();
            Object value = getValue(cell, workbook);
            CellInfo cellInfo = columnInfo.get(dataCellIndex);
            Object v = ObjectUtil.to(cellInfo.getDeserialize().apply(value), new TypeReference<>(cellInfo.getType()));
            rowData.put(cellInfo.getKey(), v);
        }
        return ObjectUtil.to(rowData, typeReference);
    }

    public static Object getValue(Cell cell, Workbook workbook) {
        Object result = null;
        FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
        switch (cell.getCellType()) {
            case STRING:
                String value = cell.getStringCellValue();
                if (!StringUtils.isBlank(value)) {
                    result = value.trim();
                }
                break;
            case ERROR:
                result = cell.getErrorCellValue();
                break;
            case BOOLEAN:
                result = cell.getBooleanCellValue();
                break;
            case FORMULA:
            case NUMERIC:
                result = DATA_FORMATTER.formatCellValue(cell, evaluator);
                break;
            case BLANK:
            case _NONE:
                break;
        }
        return result;
    }

    public static Workbook readFile(File file) throws IOException {
        return readFile(file.getName(),new FileInputStream(file));
    }

    public static Workbook readFile(String fileName, InputStream inputStream) throws IOException {
        Workbook result;
        String[] s = fileName.split("[.]");
        String suffix = null;
        if (s.length > 1) {
            suffix = s[s.length - 1];
        }
        final String xls = "xls";
        if (xls.equals(suffix)) {
            result = new HSSFWorkbook(inputStream);
        } else {
            result = new XSSFWorkbook(inputStream);
        }
        return result;
    }

    public static void removeRow(Sheet sheet, int rowIndex) {
        int lastRowNum = sheet.getLastRowNum();
        if (rowIndex >= 0 && rowIndex < lastRowNum) {
            sheet.shiftRows(rowIndex + 1, lastRowNum, -1);
        }
        if (rowIndex == lastRowNum) {
            Row removingRow = sheet.getRow(rowIndex);
            if (removingRow != null) {
                sheet.removeRow(removingRow);
            }
        }
    }

    /**
     * 单元格赋值，携带字体
     *
     * @param workbook 表格
     * @param cell     单元格
     * @param value    值
     * @param font     字体
     */
    public static void addCellValue(Workbook workbook, Cell cell, String value, Font font) {
        RichTextString text = workbook.getCreationHelper().createRichTextString(value);
        text.applyFont(font);
        cell.setCellValue(text);
    }

    /**
     * 给Cell添加批注
     *
     * @param cell     单元格
     * @param value    批注内容
     * @param workbook 表格
     */
    public static void addComment(Workbook workbook, Cell cell, String value) {
        Sheet sheet = cell.getSheet();
        cell.removeCellComment();
        ClientAnchor anchor = workbook.getCreationHelper().createClientAnchor();
        RichTextString text = workbook.getCreationHelper().createRichTextString(value);
        anchor.setDx1(0);
        anchor.setDx2(0);
        anchor.setDy1(0);
        anchor.setDy2(0);
        anchor.setCol1(cell.getColumnIndex());
        anchor.setRow1(cell.getRowIndex());
        anchor.setCol2(cell.getColumnIndex() + 2);
        anchor.setRow2(cell.getRowIndex() + 2);
        // 结束
        Drawing<?> drawing = sheet.createDrawingPatriarch();
        Comment comment = drawing.createCellComment(anchor);
        // 输入批注信息
        comment.setString(text);
        // 将批注添加到单元格对象中
        cell.setCellComment(comment);
    }

    /**
     * Excel版本信息
     */
    public enum VERSION {
        /**
         * 版本
         */
        V2003,
        V2007,
        V2008
    }
}
