package com.agile.common.util.shell;

import com.agile.common.util.stream.StreamUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author 佟盟
 * 日期 2020/5/7 9:39
 * 描述 命令执行工具
 * @version 1.0
 * @since 1.0
 */
public class ShellUtil {
    private ShellUtil() {
    }

    private static final Log log = LogFactory.getLog(ShellUtil.class);
    private static final String ERROR_LOG = "执行命令时发生异常";
    /**
     * @value 换行
     */
    public static final String NEW_LINE = "\n";

    /**
     * 执行单条命令
     *
     * @param command 单条命令
     * @return 执行结果
     */
    public static Result execOut(String command) {
        return execOut(null, null, command);
    }

    /**
     * 执行单条命令
     *
     * @param commands 若干命令，最后合并为一条
     * @return 执行结果
     */
    public static Result execOut(String... commands) {
        return execOut(null, null, commands);
    }

    /**
     * 执行单条命令
     *
     * @param envp     环境变量
     * @param commands 若干命令，最后合并为一条
     * @return 执行结果
     */
    public static Result execOut(String[] envp, String... commands) {
        return execOut(envp, null, commands);
    }

    /**
     * 执行单条命令
     *
     * @param envp    环境变量
     * @param dir     之形目录
     * @param command 单条命令
     * @return 执行结果
     */
    public static Result execOut(String[] envp, File dir, String command) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command, envp, dir);
            return getResult(process);
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
            log.error(ERROR_LOG, e);
            return new Result(false, e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 执行单条命令
     *
     * @param envp     环境变量
     * @param dir      之形目录
     * @param commands 若干命令，最后合并为一条
     * @return 执行结果
     */
    public static Result execOut(String[] envp, File dir, String[] commands) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(commands, envp, dir);
            return getResult(process);
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
            log.error(ERROR_LOG, e);
            return new Result(false, e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 执行多条命令
     *
     * @param commands 命令集合，每个元素将成为一条命令
     * @return 执行结果
     */
    public static Result batchExecOut(String... commands) {
        return batchExecOut(null, null, commands);
    }

    /**
     * 执行多条命令
     *
     * @param envp     环境变量
     * @param commands 命令集合，每个元素将成为一条命令
     * @return 执行结果
     */
    public static Result batchExecOut(String[] envp, String... commands) {
        return batchExecOut(envp, null, commands);
    }

    /**
     * 执行多条命令
     *
     * @param envp     环境变量
     * @param dir      执行目录
     * @param commands 命令集合，每个元素将成为一条命令
     * @return 执行结果
     */
    public static Result batchExecOut(String[] envp, File dir, String[] commands) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(commands[0], envp, dir);

            OutputStream outputStream = process.getOutputStream();
            for (int i = 1; i < commands.length; i++) {
                if (commands[i] == null) {
                    continue;
                }
                outputStream.write(commands[i].getBytes());
                outputStream.write(NEW_LINE.getBytes());

            }
            // 不调用时终端无法判断是否完成写入，输入流读取时会造成堵塞
            outputStream.flush();
            outputStream.close();

            return getResult(process);
        } catch (InterruptedException | IOException e) {
            Thread.currentThread().interrupt();
            log.error(ERROR_LOG, e);
            return new Result(false, e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    /**
     * 获取Process中的正常或者异常流
     *
     * @param process Process对象
     * @return 结果
     * @throws InterruptedException         终端
     * @throws UnsupportedEncodingException 不支持的编码
     */
    private static Result getResult(Process process) throws InterruptedException, UnsupportedEncodingException {
        String successLog = StreamUtil.toString(process.getInputStream());
        String errorLog = StreamUtil.toString(process.getErrorStream());
        String log;
        final boolean result = process.waitFor() == 0;

        if (result) {
            log = successLog;
        } else {
            log = errorLog;
        }
        return new Result(result, log);
    }

    /**
     * 执行单条命令
     *
     * @param command 命令
     * @return 结果
     */
    public static boolean exec(String command) {
        return exec(null, null, command);
    }

    /**
     * 执行单条命令
     *
     * @param commands 最终合并为一条命令
     * @return 结果
     */
    public static boolean exec(String... commands) {
        return exec(null, null, commands);
    }

    /**
     * 执行单条命令
     *
     * @param envp    环境变量
     * @param command 命令
     * @return 结果
     */
    public static boolean exec(String[] envp, String command) {
        return exec(envp, null, command);
    }

    /**
     * 执行单条命令
     *
     * @param envp    环境变量
     * @param dir     之形目录
     * @param command 命令
     * @return 结果
     */
    public static boolean exec(String[] envp, File dir, String command) {
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

    /**
     * 执行单条命令
     *
     * @param envp     环境变量
     * @param dir      之形目录
     * @param commands 最终合并为一条命令
     * @return 结果
     */
    public static boolean exec(String[] envp, File dir, String[] commands) {
        try {
            Process process = Runtime.getRuntime().exec(commands, envp, dir);
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

    /**
     * 批量执行命令
     *
     * @param commands 命令集合，每个元素视为一条命令
     * @return 结果
     */
    public static boolean batchExec(String... commands) {
        return batchExec(null, null, commands);
    }

    /**
     * 批量执行命令
     *
     * @param envp     环境变量
     * @param commands 命令集合，每个元素视为一条命令
     * @return 结果
     */
    public static boolean batchExec(String[] envp, String... commands) {
        return batchExec(envp, null, commands);
    }

    /**
     * 批量执行命令
     *
     * @param envp     环境变量
     * @param dir      执行目录
     * @param commands 命令集合，每个元素视为一条命令
     * @return 结果
     */
    public static boolean batchExec(String[] envp, File dir, String[] commands) {
        try {
            Process process = Runtime.getRuntime().exec(commands[0], envp, dir);
            OutputStream outputStream = process.getOutputStream();
            for (int i = 1; i < commands.length; i++) {
                if (commands[i] == null) {
                    continue;
                }

                outputStream.write(commands[i].getBytes());
                outputStream.write(NEW_LINE.getBytes());
            }
            outputStream.flush();
            outputStream.close();

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

    /**
     * 结果
     */
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
