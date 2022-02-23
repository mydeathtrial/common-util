package cloud.agileframework.common.util.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author 佟盟 on 2018/11/6
 */
public class ResponseFile {
    private String fileName;
    private String contentType;
    private InputStream inputStream;
    private boolean isDownload = true;

    public ResponseFile(String fileName, String contentType, InputStream inputStream, boolean isDownload) {
        this.fileName = fileName;
        this.contentType = contentType;
        this.inputStream = inputStream;
        this.isDownload = isDownload;
    }

    public ResponseFile(String fileName, String contentType, InputStream inputStream) {
        this(fileName, contentType, inputStream, true);
    }

    public ResponseFile(String fileName, String contentType, File file) throws FileNotFoundException {
        this(fileName, contentType, new FileInputStream(file));
    }

    public ResponseFile(String fileName, File file) throws FileNotFoundException {
        this(fileName, null, new FileInputStream(file));
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public boolean isDownload() {
        return isDownload;
    }

    public void setDownload(boolean download) {
        isDownload = download;
    }
}
