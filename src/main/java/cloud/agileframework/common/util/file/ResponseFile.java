package cloud.agileframework.common.util.file;

import cloud.agileframework.common.util.stream.ThrowingConsumer;
import com.google.common.collect.Maps;
import org.apache.commons.io.IOUtils;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Map;

/**
 * @author 佟盟 on 2018/11/6
 */
public class ResponseFile {
    private String fileName;
    private ThrowingConsumer<HttpServletResponse> write;
    private static final MimetypesFileTypeMap MIMETYPES_FILE_TYPE_MAP = new MimetypesFileTypeMap();

    public ResponseFile(String fileName,
                        String contentType,
                        String characterEncoding,
                        Map<String, String> head,
                        ThrowingConsumer<HttpServletResponse> write) {
        this.fileName = fileName;
        this.write = response -> {
            write.accept(response);
            if (head!=null && !head.isEmpty()) {
                for (Map.Entry<String, String> entry : head.entrySet()) {
                    response.setHeader(entry.getKey(), entry.getValue());
                }
            }
            response.setCharacterEncoding(characterEncoding == null ? StandardCharsets.UTF_8.displayName() : characterEncoding);
            response.setContentType(contentType == null ? "application/octet-stream" : contentType);
        };
    }

    public ResponseFile(String fileName, String contentType, String characterEncoding, Map<String, String> head, InputStream inputStream) {
        this(fileName, contentType, characterEncoding, head, (ThrowingConsumer<HttpServletResponse>) null);
     
        this.write = response -> {
           int contentLength = IOUtils.copy(inputStream, response.getOutputStream());
           response.setContentLength(contentLength);
        };
    }

    public ResponseFile(String fileName, String contentType, String characterEncoding, InputStream inputStream) {
        this(fileName, contentType, characterEncoding, Maps.newHashMap(), inputStream);
    }

    public ResponseFile(String fileName, Map<String, String> head, InputStream inputStream) {
        this(fileName, null, null, head, inputStream);
    }

    public ResponseFile(String fileName, InputStream inputStream) {
        this(fileName, null, null, Maps.newHashMap(), inputStream);
    }

    public ResponseFile(String fileName,String contentType, InputStream inputStream) {
        this(fileName, contentType, null, Maps.newHashMap(), inputStream);
    }

    public ResponseFile(String fileName, String contentType, String characterEncoding, File file) throws IOException {
        this(fileName, contentType, characterEncoding, Files.newInputStream(file.toPath()));
    }

    public ResponseFile(String fileName, String characterEncoding, File file) throws IOException {
        this(fileName, MIMETYPES_FILE_TYPE_MAP.getContentType(file), characterEncoding, Files.newInputStream(file.toPath()));
    }

    public ResponseFile(String fileName, File file) throws IOException {
        this(fileName, MIMETYPES_FILE_TYPE_MAP.getContentType(file), null, Files.newInputStream(file.toPath()));
    }

    public ResponseFile(File file) throws IOException {
        this(file.getName(), MIMETYPES_FILE_TYPE_MAP.getContentType(file), null, Files.newInputStream(file.toPath()));
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void write(HttpServletResponse response) {
        write.accept(response);
    }
}
