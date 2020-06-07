package com.agile.common.util.shell;

import com.agile.common.util.stream.StreamUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

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
    private static final ExecutorService POOL = new ThreadPoolExecutor(0, 10, 10, TimeUnit.SECONDS, new LinkedBlockingDeque<>(), new ThreadPoolExecutor.CallerRunsPolicy());

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
        return execOut(null, null, command, 1, TimeUnit.MINUTES);
    }

    /**
     * 执行单条命令
     *
     * @param commands 若干命令，最后合并为一条
     * @return 执行结果
     */
    public static Result execOut(String... commands) {
        return execOut(null, null, commands, 1, TimeUnit.MINUTES);
    }

    /**
     * 执行单条命令
     *
     * @param env      环境变量
     * @param commands 若干命令，最后合并为一条
     * @return 执行结果
     */
    public static Result execOut(String[] env, String... commands) {
        return execOut(env, null, commands, 1, TimeUnit.MINUTES);
    }

    /**
     * 执行单条命令
     *
     * @param env     环境变量
     * @param dir     之形目录
     * @param command 单条命令
     * @return 执行结果
     */
    public static Result execOut(String[] env, File dir, String command, long timout, TimeUnit unit) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(command, env, dir);
            return getResult(process, timout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(ERROR_LOG, e);
            return new Result(false, e.getMessage());
        } catch (IOException | ExecutionException e) {
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
     * @param env      环境变量
     * @param dir      之形目录
     * @param commands 若干命令，最后合并为一条
     * @return 执行结果
     */
    public static Result execOut(String[] env, File dir, String[] commands, long timout, TimeUnit unit) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(commands, env, dir);
            return getResult(process, timout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(ERROR_LOG, e);
            return new Result(false, e.getMessage());
        } catch (IOException | ExecutionException e) {
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
        return batchExecOut(null, null, commands, 1, TimeUnit.MINUTES);
    }

    /**
     * 执行多条命令
     *
     * @param env      环境变量
     * @param commands 命令集合，每个元素将成为一条命令
     * @return 执行结果
     */
    public static Result batchExecOut(String[] env, String... commands) {
        return batchExecOut(env, null, commands, 1, TimeUnit.MINUTES);
    }

    /**
     * 执行多条命令
     *
     * @param env      环境变量
     * @param dir      执行目录
     * @param commands 命令集合，每个元素将成为一条命令
     * @return 执行结果
     */
    public static Result batchExecOut(String[] env, File dir, String[] commands, long timeout, TimeUnit unit) {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(commands[0], env, dir);

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

            return getResult(process, timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(ERROR_LOG, e);
            return new Result(false, e.getMessage());
        } catch (IOException | ExecutionException e) {
            log.error(ERROR_LOG, e);
            return new Result(false, e.getMessage());
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
    }

    private static Future<?> read(Process process, AtomicReference<String> log, Function<Process, InputStream> function) {
        return POOL.submit(() -> {
            try {
                log.set(StreamUtil.toString(function.apply(process)));
            } catch (Exception e) {
                log.set(null);
            }
        });
    }

    /**
     * 获取Process中的正常或者异常流
     *
     * @param process Process对象
     * @return 结果
     * @throws InterruptedException 终端
     */
    private static Result getResult(Process process, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException {
        AtomicReference<String> successLog = new AtomicReference<>();
        AtomicReference<String> errorLog = new AtomicReference<>();

        Thread successF = new Thread(() -> successLog.set(StreamUtil.toString(process.getInputStream())));
        successF.start();
        Thread errorF = new Thread(() -> errorLog.set(StreamUtil.toString(process.getErrorStream())));
        errorF.start();
        Future<Boolean> waitFuture = POOL.submit(() -> {
            process.waitFor();
            return process.exitValue() == 0;
        });

        Result result;
        boolean waitResult = false;

        try {
            waitResult = waitFuture.get(timeout, unit);
        } catch (TimeoutException e) {
            waitFuture.cancel(true);
            successF.interrupt();
            errorF.interrupt();
        } finally {
            if (waitResult) {
                result = new Result(true, successLog.get());
            } else {
                result = new Result(false, errorLog.get());
            }
        }
        return result;
    }

    /**
     * 执行单条命令
     *
     * @param command 命令
     * @return 结果
     */
    public static boolean exec(String command) {
        return exec(null, null, command, 1, TimeUnit.MINUTES);
    }

    /**
     * 执行单条命令
     *
     * @param commands 最终合并为一条命令
     * @return 结果
     */
    public static boolean exec(String... commands) {
        return exec(null, null, commands, 1, TimeUnit.MINUTES);
    }

    /**
     * 执行单条命令
     *
     * @param env     环境变量
     * @param command 命令
     * @return 结果
     */
    public static boolean exec(String[] env, String command) {
        return exec(env, null, command, 1, TimeUnit.MINUTES);
    }

    /**
     * 执行单条命令
     *
     * @param env     环境变量
     * @param dir     之形目录
     * @param command 命令
     * @return 结果
     */
    public static boolean exec(String[] env, File dir, String command, long timeout, TimeUnit unit) {
        try {
            Process process = Runtime.getRuntime().exec(command, env, dir);

            Future<Boolean> waitFuture = POOL.submit(() -> {
                process.waitFor();
                return process.exitValue() == 0;
            });
            return waitFuture.get(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(ERROR_LOG, e);
        } catch (IOException | ExecutionException | TimeoutException e) {
            log.error(ERROR_LOG, e);
        }
        return false;
    }


    /**
     * 执行单条命令
     *
     * @param env      环境变量
     * @param dir      之形目录
     * @param commands 最终合并为一条命令
     * @return 结果
     */
    public static boolean exec(String[] env, File dir, String[] commands, long timeout, TimeUnit unit) {
        try {
            Process process = Runtime.getRuntime().exec(commands, env, dir);
            Future<Boolean> waitFuture = POOL.submit(() -> {
                process.waitFor();
                return process.exitValue() == 0;
            });
            return waitFuture.get(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(ERROR_LOG, e);
        } catch (IOException | ExecutionException | TimeoutException e) {
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
        return batchExec(null, null, commands, 1, TimeUnit.MINUTES);
    }

    /**
     * 批量执行命令
     *
     * @param env      环境变量
     * @param commands 命令集合，每个元素视为一条命令
     * @return 结果
     */
    public static boolean batchExec(String[] env, String... commands) {
        return batchExec(env, null, commands, 1, TimeUnit.MINUTES);
    }

    /**
     * 批量执行命令
     *
     * @param env      环境变量
     * @param dir      执行目录
     * @param commands 命令集合，每个元素视为一条命令
     * @return 结果
     */
    public static boolean batchExec(String[] env, File dir, String[] commands, long timeout, TimeUnit unit) {
        try {
            Process process = Runtime.getRuntime().exec(commands[0], env, dir);
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

            Future<Boolean> waitFuture = POOL.submit(() -> {
                process.waitFor();
                return process.exitValue() == 0;
            });
            return waitFuture.get(timeout, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error(ERROR_LOG, e);
        } catch (IOException | ExecutionException | TimeoutException e) {
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
