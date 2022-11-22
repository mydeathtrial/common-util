package cloud.agileframework.common.util.file.poi;

import java.io.IOException;

public class ExcelFormatException extends IOException {
    public ExcelFormatException() {
        super();
    }

    public ExcelFormatException(String message) {
        super(message);
    }

    public ExcelFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public ExcelFormatException(Throwable cause) {
        super(cause);
    }
}
