package cloud.agileframework.common.util.file.poi;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * @author 佟盟 on 2018/10/17
 */
public class ExcelFile {
    private String fileName;
    private Workbook workbook;

    public ExcelFile(String fileName, Workbook workbook) {
        this.workbook = workbook;

        String suffix;
        if (workbook instanceof HSSFWorkbook) {
            suffix = "xls";
        } else {
            suffix = "xlsx";
        }

        this.fileName = String.format("%s.%s", fileName, suffix);
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Workbook getWorkbook() {
        return workbook;
    }

    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

}
