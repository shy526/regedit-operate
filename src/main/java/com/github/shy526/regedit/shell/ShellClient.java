package com.github.shy526.regedit.shell;


import com.github.shy526.regedit.obj.RegTypeEnum;
import com.github.shy526.regedit.obj.RegValue;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ShellClient {

    //region 返回码
    public static final int CODE_TIME_OUT = -2;
    public static final int CODE_SUCCESS = 0;
    public static final int CODE_FAIL = -1;
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

        return process(cmd, success, fail, timeOut, false);

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
        return process(cmd, success, fail, null, false);
    }

    /**
     * 执行命令
     *
     * @param cmd     命令数组的集合
     * @param success 成功调用后返回的结果集
     * @return 错误码
     */
    public static int exec(String[] cmd, Consumer<String> success) {
        return process(cmd, success, null, null, false);
    }

    /**
     * 执行命令
     *
     * @param cmd 命令数组的集合
     * @return 错误码
     */
    public static int exec(String[] cmd) {
        return process(cmd, null, null, null, false);
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
        return process(cmd, success, fail, timeOut, false);
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
        return process(cmd, success, fail, null, false);
    }

    /**
     * 执行命令
     *
     * @param cmd     命令字符串
     * @param success 成功调用后返回的结果集
     * @return 错误码
     */
    public static int exec(String cmd, Consumer<String> success) {
        return process(cmd, success, null, null, false);
    }

    /**
     * 执行命令
     *
     * @param cmd 命令字符串
     * @return 错误码
     */
    public static int exec(String cmd) {
        return process(cmd, null, null, null, false);
    }

    /**
     * 执行命令
     *
     * @param cmd 命令字符串
     * @return 错误码
     */
    public static int exec(String cmd, boolean envRefresh) {
        return process(cmd, null, null, null, envRefresh);
    }


    /**
     * 执行命令
     *
     * @param cmd     命令字符串
     * @param success 成功调用后返回的结果集
     * @return 错误码
     */
    public static int exec(String cmd, Consumer<String> success, boolean envRefresh) {
        return process(cmd, success, null, null, envRefresh);
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

    private static int process(Object cmd, Consumer<String> success, Consumer<String> fail, Integer timeOut, boolean envRefresh) {

        Process process = null;
        try {
            List<String> command = parseCommandStr(cmd);
            if (command.isEmpty()) {
                return CODE_FAIL;
            }

            ProcessBuilder pb = new ProcessBuilder(command);
            Map<String, String> oldEnv = pb.environment();
            if (envRefresh) {
                Map<String, RegValue> newEnv = new HashMap<>();
                exec("REG QUERY \"HKEY_LOCAL_MACHINE\\SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment\"", result -> {
                    String[] lines = result.split("\r\n");
                    for (String line : lines) {
                        line = line.trim();
                        if ("".equals(line)) {
                            continue;
                        }
                        String[] temp = line.split("\\s{4}");
                        if (temp.length != 3) {
                            continue;
                        }
                        String key = temp[0];
                        String val = temp[2];
                        String type = temp[1];
                        RegTypeEnum regTypeEnum = RegTypeEnum.find(type.trim());
                        RegValue of = regTypeEnum.of(key.trim(), val.trim());
                        newEnv.put(key, of);
                    }
                });
                for (Map.Entry<String, RegValue> item : newEnv.entrySet()) {
                    String key = item.getKey();
                    RegValue val = fillEnv(newEnv, item.getValue(), oldEnv);
                    oldEnv.put(key, val.getValue());
                }
            }
            process = pb.start();
            FutureTask<String> errorTask = getStreamData(process.getErrorStream());
            FutureTask<String> resultTask = getStreamData(process.getInputStream());
            timeOut = timeOut == null ? DEFAULT_TIME_OUT : timeOut;
            process.waitFor(timeOut, TimeUnit.MILLISECONDS);
            String error = "";
            try {
                error = errorTask.get(timeOut, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error(cmd + " ->errorStream timeOut");
            }
            if (fail != null) {
                fail.accept(error);
            }
            if (!"".equals(error)) {
                log.debug(cmd + ":" + error);
                return process.exitValue();
            }
            String successStr = "";
            try {
                successStr = resultTask.get(timeOut, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error(cmd + " ->inputStream timeOut");
            }
            log.debug(cmd + ":" + successStr);
            if (success != null) {
                success.accept(successStr);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return CODE_FAIL;
        } finally {
            if (process != null) {
                process.destroy();
            }
        }
        return process.exitValue();
    }

    private static RegValue fillEnv(Map<String, RegValue> newEnv, RegValue val, Map<String, String> oldEnv) {
        if (!RegTypeEnum.REG_EXPAND_SZ.equals(val.getType())) {
            return val;
        }
        Pattern compile = Pattern.compile("%(\\w+)%");
        Matcher matcher = compile.matcher(val.getValue());
        while (matcher.find()) {
            String group = matcher.group(1);
            RegValue temp = newEnv.get(group);
            if (temp != null) {
                fillEnv(newEnv, temp, oldEnv);
                val.setValue(val.getValue().replace(matcher.group(), temp.getValue()));
            } else {
                String str = oldEnv.get(group);
                if (null != str) {
                    val.setValue(val.getValue().replace(matcher.group(), str));
                } else {
                    log.error(val.getName() + ":" + group + "-> is null");
                    System.out.println(val.getName() + ":" + group + "-> is null");
                }
            }
        }
        return val;
    }

    /**
     * 解析指令
     *
     * @param cmd cmd
     * @return List<String>
     */
    private static List<String> parseCommandStr(Object cmd) {
        List<String> result = new ArrayList<>();
        if (cmd instanceof String) {
            List<String> command = new ArrayList<>();
            StringBuilder commandItem = new StringBuilder();
            StringBuilder sb = new StringBuilder((String) cmd);
            boolean flag = true;
            for (int i = 0; i < sb.length(); i++) {
                char c = sb.charAt(i);
                if (' ' == c) {
                    if (flag) {
                        command.add(commandItem.toString());
                        commandItem = new StringBuilder();
                    } else {
                        commandItem.append(c);
                    }
                } else if ('"' == c) {
                    flag = !flag;
                    commandItem.append(c);
                } else {
                    commandItem.append(c);
                }
            }
            command.add(commandItem.toString());
            result = command;
        } else if (cmd instanceof String[]) {
            result = new ArrayList<>();
        } else if (cmd instanceof List) {
            return (List<String>) cmd;
        }
        return result;
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
                log.error(e.getMessage(), e);
            }
            return result.toString();
        });
        new Thread(task).start();
        return task;
    }

}
