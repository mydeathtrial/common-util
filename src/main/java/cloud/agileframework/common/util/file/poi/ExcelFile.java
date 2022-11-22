package cloud.agileframework.common.util.file.poi;

import cloud.agileframework.common.util.file.ResponseFile;
import org.apache.poi.hpsf.ClassIDPredefined;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookType;

import java.util.Map;

/**
 * @author 佟盟 on 2018/10/17
 */
public class ExcelFile extends ResponseFile {
    private static final String S_S = "%s.%s";
    private final Workbook workbook;

    public ExcelFile(String fileName, Workbook workbook) {
        this(fileName, null, null, null, workbook);
    }

    public ExcelFile(String fileName, String contentType, String characterEncoding, Map<String, String> head, Workbook workbook) {
        super(fileName, contentType, characterEncoding, head, response -> {
            if (contentType == null) {
                response.setContentType(ClassIDPredefined.EXCEL_V3.getContentType());
                if (workbook instanceof HSSFWorkbook) {
                    response.setContentType("application/vnd.ms-excel");
                } else if (workbook instanceof XSSFWorkbook || workbook instanceof SXSSFWorkbook) {
                    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                } 
            } else {
                response.setContentType(contentType);
            }
            workbook.write(response.getOutputStream());
        });
        if (workbook instanceof HSSFWorkbook) {
            ClassIDPredefined classIDPredefined = ClassIDPredefined.lookup(((HSSFWorkbook) workbook).getDirectory().getStorageClsid());
            setFileName(String.format(S_S, fileName, classIDPredefined.getFileExtension()));
        } else if (workbook instanceof XSSFWorkbook) {
            XSSFWorkbookType workbookType = ((XSSFWorkbook) workbook).getWorkbookType();
            setFileName(String.format(S_S, fileName, workbookType.getExtension()));
        } else if (workbook instanceof SXSSFWorkbook) {
            setFileName(String.format(S_S, fileName, ClassIDPredefined.EXCEL_V14.getFileExtension()));
        }
        this.workbook = workbook;

    }

    public Workbook getWorkbook() {
        return workbook;
    }
}
