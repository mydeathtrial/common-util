package cloud.agileframework.common.util.file.poi;

import cloud.agileframework.common.util.clazz.TypeReference;
import cloud.agileframework.common.util.collection.CollectionsUtil;
import cloud.agileframework.common.util.file.FileUtil;
import cloud.agileframework.common.util.object.ObjectUtil;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.*;
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
        Workbook excel = parsing(file);
        return readExcel(typeReference, columns, excel);

    }

    protected static <T> List<T> readExcel(TypeReference<T> typeReference, List<CellInfo> columns, Workbook excel) {
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

    protected static List<Map<String, Object>> readSheet(List<CellInfo> columns, Sheet sheet) {
        return readSheet(new TypeReference<Map<String, Object>>() {
        }, columns, sheet);
    }

    protected static <T> List<T> readSheet(Class<T> clazz, List<CellInfo> columns, Sheet sheet) {
        return readSheet(new TypeReference<T>(clazz), columns, sheet);
    }

    /**
     * 读取sheet页
     *
     * @param columns 映射字段
     * @param sheet   sheet页
     */
    protected static <T> List<T> readSheet(TypeReference<T> typeReference, List<CellInfo> columns, Sheet sheet) {
        List<T> list = Lists.newArrayList();

        Map<String, String> columnMapping = columns.stream().collect(Collectors.toMap(CellInfo::getShowName, CellInfo::getKey));
        if (columnMapping.isEmpty()) {
            return list;
        }
        Iterator<Row> rows = sheet.rowIterator();
        List<String> columnInfo = Lists.newLinkedList();
        int rowNum = 0;
        while (rows.hasNext()) {
            int cellNum = 0;

            LinkedHashMap<String, Object> rowData = new LinkedHashMap<>();
            Iterator<Cell> cells = rows.next().cellIterator();
            while (cells.hasNext()) {
                Cell cell = cells.next();
                if (rowNum == 0) {
                    columnInfo.add(columnMapping.get(cell.getStringCellValue()));
                } else if (rowNum > 0) {
                    rowData.put(columnInfo.get(cellNum++), getValue(cell));
                }
            }
            rowNum++;
            
            T row = ObjectUtil.to(rowData, typeReference);
            if (row == null) {
                continue;
            }
            list.add(row);
            
        }

        return list;
    }

    private static Object getValue(Cell cell) {
        Object value = null;
        try {
            value = cell.getStringCellValue();
        } catch (Exception ignored) {
        }

        try {
            value = cell.getBooleanCellValue();
        } catch (Exception ignored) {
        }

        try {
            value = cell.getNumericCellValue();
        } catch (Exception ignored) {
        }

        try {
            value = cell.getDateCellValue();
        } catch (Exception ignored) {
        }

        try {
            value = cell.getErrorCellValue();
        } catch (Exception ignored) {
        }
        return value;
    }

    private static Workbook parsing(Object file) {
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
                if (xls.equals(suffix)) {
                    result = new HSSFWorkbook(new FileInputStream((File) file));
                } else {
                    result = new XSSFWorkbook(new FileInputStream((File) file));
                }
            } catch (Exception e) {
                result = null;
            }
        }

        return result;
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
