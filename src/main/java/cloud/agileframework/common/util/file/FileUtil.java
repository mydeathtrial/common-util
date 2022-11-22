package cloud.agileframework.common.util.file;

import cloud.agileframework.common.util.stream.ThrowingConsumer;
import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author 佟盟 on 2017/12/21
 */
public class FileUtil {

    public static void downloadFile(String fileName, ThrowingConsumer<HttpServletResponse> write, HttpServletRequest request,HttpServletResponse response) throws IOException {
        String characterEncoding = request.getCharacterEncoding();
        if (characterEncoding != null) {
            response.setCharacterEncoding(characterEncoding);
        }
        response.setHeader("Content-disposition",
                "attachment;" +
                        " filename=" + URLEncoder.encode(fileName, characterEncoding) + ";" +
                        " filename*=" + characterEncoding + "''" + URLEncoder.encode(fileName, characterEncoding) + ";");
        write.accept(response);
    }

    public static void downloadFile(String fileName, File file,HttpServletRequest request, HttpServletResponse response) throws IOException {
        fileName = fileName == null ? file.getName() : fileName;
        downloadFile(fileName, response1 -> IOUtils.copy(Files.newInputStream(file.toPath()), response1.getOutputStream()),request, response);

    }

    public static void downloadFile(File file, HttpServletRequest request, HttpServletResponse response) throws IOException {
        downloadFile(file.getName(), file,request, response);
    }

    public static File createZip(String dir, String zipFileName, File... files) throws IOException {
        if (!zipFileName.toLowerCase().endsWith(".zip")) {
            zipFileName = zipFileName + ".zip";
        }
        File zip = createFile(dir, zipFileName);
        addFileToZip(zip, files);
        return zip;
    }

    public static File createFile(String path, String fileName) throws IOException {
        File dir = new File(path);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("create dir " + path + " fail");

        }
        File file = new File(dir.getPath() + File.separator + fileName);
        if (!file.exists() && !file.createNewFile()) {
            throw new IOException("create file " + path + fileName + " fail");
        }
        return file;
    }

    public static void addFileToZip(File zipFile, String... filepath) throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
            for (String path : filepath) {
                //文件读取到文件流中
                URL url = new URL(path);
                URLConnection connection = url.openConnection();
                InputStream fileInputStream = connection.getInputStream();

                //压缩文件名称
                String fileName = File.separatorChar + (path.substring(path.lastIndexOf(File.separatorChar) + 1));
                out.putNextEntry(new ZipEntry(fileName));
                //把流中文件写到压缩包
                IOUtils.copy(fileInputStream, out);
            }
            out.flush();
        }
    }

    public static void addFileToZip(File zipFile, File... files) throws IOException {
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(zipFile.toPath()))) {
            for (File fileObject : files) {
                out.putNextEntry(new ZipEntry(fileObject.getName()));
                try (InputStream inputStream = Files.newInputStream(fileObject.toPath())) {
                    IOUtils.copy(inputStream, out);
                }
            }
            out.flush();
        }
    }

    /**
     * 判断文件夹名是否非法
     *
     * @param dirName 文件夹名字
     * @return 是（非法）否（合法）
     */
    public static boolean isIllegalDirName(String dirName) {
        return dirName.matches("(.*)([\\\\/.~:*?\"<>|])(.*)");
    }

    /**
     * 统一路径中的斜杠
     *
     * @param str 路径
     * @return 处理后的合法路径
     */
    public static String parseFilePath(String str) {
        String url = str.replaceAll("[\\\\/]+", Matcher.quoteReplacement(File.separator));
        if (!url.endsWith(File.separator)) {
            url += File.separator;
        }
        return url;
    }

    /**
     * 统一路径中的斜杠
     *
     * @param str 路径
     * @return 处理后的合法路径
     */
    public static String parseClassPath(String str) {
        String prefix = "/";
        String url = str.replaceAll("[\\\\/]+", prefix);
        if (!url.startsWith(prefix)) {
            url = prefix + url;
        }
        return url;
    }
}
