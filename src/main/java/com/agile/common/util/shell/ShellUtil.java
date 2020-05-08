package com.agile.common.util.shell;

import com.agile.common.constant.Constant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.Charset;

/**
 * @author 佟盟
 * 日期 2020/5/7 9:39
 * 描述 命令执行工具
 * @version 1.0
 * @since 1.0
 */
public class ShellUtil {
    private static final Log log = LogFactory.getLog(ShellUtil.class);
    private static final String ERROR_LOG = "执行命令时发生异常";

    public static Result execOut(String command) {
        return execOut(command, null, null);
    }

    public static Result execOut(String command, String[] envp) {
        return execOut(command, envp, null);
    }

    public static Result execOut(String command, String[] envp, File dir) {

        try {
            Process process = Runtime.getRuntime().exec(command, envp, dir);
            InputStream stream;
            final boolean result = process.waitFor() == 0;
            if (result) {
                stream = process.getInputStream();
            } else {
                stream = process.getErrorStream();
            }
            InputStreamReader reader = new InputStreamReader(stream, Charset.forName(System.getProperty("sun.jnu.encoding")));
            LineNumberReader lineNumberReader = new LineNumberReader(reader);
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = lineNumberReader.readLine()) != null) {
                builder.append(line).append(Constant.RegularAbout.NEW_LINE);
            }
            return new Result(result, builder.toString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(ERROR_LOG, e);
        } catch (IOException e) {
            log.error(ERROR_LOG, e);
        }
        return null;
    }

    public static boolean exec(String command, String[] envp, File dir) {
        try {
            Process process = Runtime.getRuntime().exec(command, envp, dir);
            if (process.waitFor() == 0) {
                return true;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(ERROR_LOG, e);
        } catch (IOException e) {
            log.error(ERROR_LOG, e);
        }
        return false;
    }

    public static class Result {
        private final boolean success;
        private final String log;

        public Result(boolean success, String log) {
            this.success = success;
            this.log = log;
        }

        public boolean success() {
            return success;
        }

        public String log() {
            return log;
        }
    }
}
