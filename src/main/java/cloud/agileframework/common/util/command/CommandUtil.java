package cloud.agileframework.common.util.command;


import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author 佟盟
 * 日期 2020/1/12 14:45
 * 描述 操作系统
 * @version 1.0
 * @since 1.0
 */
public class CommandUtil {
    private static Log log = LogFactory.getLog(CommandUtil.class);

    public static boolean extractProcess(Process p) throws IOException {
        Reader input = new InputStreamReader(p.getInputStream());
        Reader errors = new InputStreamReader(p.getErrorStream());

        for (String line : IOUtils.readLines(input)) {
            if (line.startsWith("[ERROR]")) {
                log.error(line);
            } else if (line.startsWith("[WARNING]")) {
                log.warn(line);
            } else {
                log.info(line);
            }
        }

        for (String line : IOUtils.readLines(errors)) {
            if (line.startsWith("[ERROR]")) {
                log.error(line);
            } else if (line.startsWith("[WARNING]")) {
                log.warn(line);
            } else {
                log.info(line);
            }
        }

        p.getOutputStream().close();

        try {
            if (p.waitFor() != 0) {
                log.warn("The command did not complete successfully");
                return false;
            }
            return true;
        } catch (final InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

//    public static void main(String[] args) throws IOException {
//        extractProcess(Runtime.getRuntime().exec("java -version"));
//    }
}
