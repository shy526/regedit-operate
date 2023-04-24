package com.github.shy526.regedit.shell;


import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class ShellClient {

    //region 返回码
    public static final int CODE_TIME_OUT = 1;
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FAIL = 2;
    //endregion
    private static final int DEFAULT_TIME_OUT = 1000;



    /**
     * 执行命令
     *
     * @param cmd     命令数组的集合
     * @param success 成功调用后返回的结果集
     * @param fail    错误返回的结果集
     * @param timeOut 超时事件
     * @return 错误码
     */
    public static int exec(String[] cmd, Consumer<String> success, Consumer<String> fail, int timeOut) {

        return process(cmd, success, fail, timeOut);

    }

    /**
     * 执行命令
     *
     * @param cmd     命令数组的集合
     * @param success 成功调用后返回的结果集
     * @param fail    错误返回的结果集
     * @return 错误码
     */
    public static int exec(String[] cmd, Consumer<String> success, Consumer<String> fail) {
        return process(cmd, success, fail, null);
    }

    /**
     * 执行命令
     *
     * @param cmd     命令数组的集合
     * @param success 成功调用后返回的结果集
     * @return 错误码
     */
    public static int exec(String[] cmd, Consumer<String> success) {
        return process(cmd, success, null, null);
    }

    /**
     * 执行命令
     *
     * @param cmd 命令数组的集合
     * @return 错误码
     */
    public static int exec(String[] cmd) {
        return process(cmd, null, null, null);
    }

    /**
     * 执行命令
     *
     * @param cmd     命令数组的集合
     * @param success 成功调用后返回的结果集
     * @param fail    错误返回的结果集
     * @param timeOut 超时事件
     * @return 错误码
     */
    public static int exec(String cmd, Consumer<String> success, Consumer<String> fail, int timeOut) {
        return process(cmd, success, fail, timeOut);
    }

    /**
     * 执行命令
     *
     * @param cmd     命令字符串
     * @param success 成功调用后返回的结果集
     * @param fail    错误返回的结果集
     * @return 错误码
     */
    public static int exec(String cmd, Consumer<String> success, Consumer<String> fail) {
        return process(cmd, success, null, null);
    }

    /**
     * 执行命令
     *
     * @param cmd     命令字符串
     * @param success 成功调用后返回的结果集
     * @return 错误码
     */
    public static int exec(String cmd, Consumer<String> success) {
        return process(cmd, success, null, null);
    }

    /**
     * 执行命令
     *
     * @param cmd 命令字符串
     * @return 错误码
     */
    public static int exec(String cmd) {
        return process(cmd, null, null, null);
    }

    /**
     * 执行命令
     *
     * @param cmd     命令数组的集合或字符串
     * @param success 成功调用后返回的结果集
     * @param fail    错误返回的结果集
     * @param timeOut 超时事件
     * @return 错误码
     */
    private static int process(Object cmd, Consumer<String> success, Consumer<String> fail, Integer timeOut) {
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            if (cmd instanceof String) {
                process = runtime.exec((String) cmd);
            } else if (cmd instanceof String[]) {
                process = runtime.exec((String[]) cmd);
            } else {
                return CODE_FAIL;
            }

            FutureTask<String> errorTask = getStreamData(process.getErrorStream());
            FutureTask<String> resultTask = getStreamData(process.getInputStream());
            timeOut = timeOut == null ? DEFAULT_TIME_OUT : timeOut;
            process.waitFor(timeOut, TimeUnit.MILLISECONDS);
            String error = errorTask.get(timeOut, TimeUnit.MILLISECONDS);
            if (fail != null) {
                fail.accept(error);
            }
            if (!"".equals(error)) {
                return CODE_FAIL;
            }
            if (success != null) {
                success.accept(resultTask.get(timeOut, TimeUnit.MILLISECONDS));
            }
        } catch (Exception e) {
            return e instanceof TimeoutException ? CODE_TIME_OUT : CODE_FAIL;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return CODE_SUCCESS;
    }

    /**
     * 异步读取流
     *
     * @param inputStream 流
     * @return FutureTask<String>
     */
    private static FutureTask<String> getStreamData(InputStream inputStream) {
        FutureTask<String> task = new FutureTask<>(() -> {
            StringBuilder result = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "GBK"))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    result.append(line).append("\r\n");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return result.toString();
        });
        new Thread(task).start();
        return task;
    }

}
