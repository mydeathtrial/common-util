package cloud.agileframework.common.util.file.poi;

import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.collection.CollectionsUtil;
import cloud.agileframework.common.util.file.FileUtil;
import cloud.agileframework.common.util.object.ObjectUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
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
            createRow(sheet, headerColumns, currentRowIndex++, headerColumns);

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
            String currentCellData = null;
            if (rowData instanceof Map) {
                currentCellData = ObjectUtil.to(((Map) rowData).get(cell.getKey()), new TypeReference<String>() {
                });
            } else if (rowData instanceof List) {
                Object o = ((List) rowData).get(currentColumnIndex);
                if (o instanceof CellInfo) {
                    currentCellData = ((CellInfo) o).getShowName();
                } else if (o instanceof String) {
                    currentCellData = (String) o;
                }
            } else {
                if (rowData != null) {
                    try {
                        Field field = rowData.getClass().getDeclaredField(cell.getKey());
                        field.setAccessible(true);
                        currentCellData = ObjectUtil.to(field.get(rowData), new TypeReference<String>() {
                        });
                    } catch (IllegalAccessException | NoSuchFieldException e) {
                        currentCellData = null;
                    }
                }
            }
            row.createCell(currentColumnIndex++).setCellValue(currentCellData);
        }
    }

    /**
     * 读取excel文件成list-map形式
     *
     * @param file excel文件
     * @return 格式化结果
     */
    public static <T> List<T> readExcel(Class<T> clazz, File file) {
        return readExcel(new TypeReference<T>(clazz), file, null);
    }

    /**
     * 读取excel文件成list-map形式，并且map-key值对应columns内容
     *
     * @param file    文件
     * @param columns map-key对应字段
     * @return 格式化结果
     */
    public static List<Map<String, Object>> readExcel(File file, List<CellInfo> columns) {
        return readExcel(new TypeReference<Map<String, Object>>() {
        }, file, columns);
    }


    public static <T> List<T> readExcel(TypeReference<T> typeReference, File file, List<CellInfo> columns) {
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
            list.addAll(readSheet(typeReference, columns, sheets.next()));
        }
        return list;
    }

    public static List<Map<String, Object>> readSheet(List<CellInfo> columns, Sheet sheet) {
        return readSheet(new TypeReference<Map<String, Object>>() {
        }, columns, sheet);
    }

    public static <T> List<T> readSheet(Class<T> clazz, List<CellInfo> columns, Sheet sheet) {
        return readSheet(new TypeReference<T>(clazz), columns, sheet);
    }

    /**
     * 按顺序提取每一列字段对应的code列表
     *
     * @param columns 字段信息
     * @param sheet   sheet页
     * @return 字段信息列表
     */
    public static List<String> readColumnInfo(List<CellInfo> columns, Sheet sheet) {
        Map<String, String> columnMapping = columns.stream().collect(Collectors.toMap(CellInfo::getShowName, CellInfo::getKey));
        if (columnMapping.isEmpty()) {
            return Lists.newArrayList();
        }
        Iterator<Row> rows = sheet.rowIterator();
        List<String> columnInfo = Lists.newLinkedList();
        if (!rows.hasNext()) {
            return Lists.newArrayList();
        }
        Row row = rows.next();

        Iterator<Cell> cells = row.cellIterator();
        while (cells.hasNext()) {
            Cell cell = cells.next();
            columnInfo.add(columnMapping.get(cell.getStringCellValue()));
        }
        return columnInfo;
    }

    /**
     * 读取sheet页
     *
     * @param columns 映射字段
     * @param sheet   sheet页
     */
    public static <T> List<T> readSheet(TypeReference<T> typeReference, List<CellInfo> columns, Sheet sheet) {
        List<T> list = Lists.newArrayList();
        List<String> columnInfo = readColumnInfo(columns, sheet);
        if (columnInfo.isEmpty()) {
            return list;
        }

        Iterator<Row> rows = sheet.rowIterator();
        int rowNum = 0;
        while (rows.hasNext()) {
            if (rowNum != 0) {
                Row row = rows.next();
                T result = readRow(typeReference, columnInfo, row);
                if (result == null) continue;
                list.add(result);
            }
            rowNum++;
        }

        return list;
    }

    public static <T> T readRow(TypeReference<T> typeReference, List<String> columnInfo, Row row) {
        LinkedHashMap<String, Object> rowData = new LinkedHashMap<>();
        int cellNum = 0;
        Iterator<Cell> cells = row.cellIterator();
        while (cells.hasNext()) {
            Cell cell = cells.next();

            int dataCellIndex = cell.getColumnIndex();
            for (int i = 0; i < dataCellIndex - cellNum; i++) {
                rowData.put(columnInfo.get(cellNum++), null);
            }
            rowData.put(columnInfo.get(cellNum++), getValue(cell));
        }
        return ObjectUtil.to(rowData, typeReference);
    }

    public static Object getValue(Cell cell) {
        try {
            return cell.getStringCellValue();
        } catch (Exception ignored) {
        }

        try {
            return cell.getBooleanCellValue();
        } catch (Exception ignored) {
        }

        try {
            return cell.getNumericCellValue();
        } catch (Exception ignored) {
        }

        try {
            return cell.getDateCellValue();
        } catch (Exception ignored) {
        }

        try {
            return cell.getErrorCellValue();
        } catch (Exception ignored) {
        }
        return null;
    }

    public static Workbook readFile(Object file) {
        Workbook result = null;
        if (file instanceof File) {
            String[] s = ((File) file).getName().split("[.]");
            String suffix;
            if (s.length > 1) {
                suffix = s[s.length - 1];
            } else {
                suffix = Objects.requireNonNull(FileUtil.getFormat((File) file)).toLowerCase();
            }
            try {
                final String xls = "xls";
                Path path = ((File) file).toPath();
                if (xls.equals(suffix)) {
                    result = new HSSFWorkbook(Files.newInputStream(path));
                } else {
                    result = new XSSFWorkbook(Files.newInputStream(path));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
