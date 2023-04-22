package com.github.shy526.regedit;

import com.github.shy526.regedit.oo.RegRootEnum;
import com.github.shy526.regedit.oo.RegTypeEnum;
import com.github.shy526.regedit.oo.RegValue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class CmdRegOperate extends AbsRegOperate {

    private final static String GET_REG_VAL_LIST_CMD = "REG QUERY \"%s\"";
    private final static String GET_REG_VAL_CMD = "REG QUERY \"%s\" /v %s";
    private final static String SET_REG_VAL_CMD = "REG ADD \"%s\" /v %s /t %s /d %s /f";

    private final static String DELETE_REG_VAL_CMD = "REG DELETE \"%s\"  /v %s /f";


    //region 解析value用到的所用
    private static final int REG_VAL_KEY_INDEX = 0;
    private static final int REG_VAL_TYPE_INDEX = 1;
    private static final int REG_VAL_INDEX = 2;
    private static final int REG_VAL_LENGTH = 3;
    private static final int REG_NODE_LENGTH = 1;
    //endregion
    private final String rootKey;

    public CmdRegOperate(RegRootEnum rootEnum, String keyName) {
        super(rootEnum, keyName);
        this.rootKey = rootEnum + "\\" + keyName;

    }

    public String exe(String cmd) {
        Runtime runtime = Runtime.getRuntime();
        Process process = null;
        try {
            process = runtime.exec(cmd);
            FutureTask<String> errorTask = getStreamData(process.getErrorStream());
            FutureTask<String> resultTask = getStreamData(process.getInputStream());
            process.waitFor(1, TimeUnit.SECONDS);
            String error = errorTask.get(2, TimeUnit.SECONDS);
            if (!"".equals(error)) {
                throw new RuntimeException(cmd + ":" + error);
            }
            return resultTask.get(1, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (process != null) {
                process.destroy();
            }

        }
        return null;
    }

    FutureTask<String> getStreamData(InputStream inputStream) {
        FutureTask<String> task = new FutureTask<>(() -> {
            StringBuilder result = new StringBuilder();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "GBK"))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println("line = " + line);
                    result = result.append(line).append("\r\n");
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            return result.toString();
        });
        new Thread(task).start();
        return task;
    }

    @Override
    public Set<String> getNodes() {
        String cmd = String.format(GET_REG_VAL_LIST_CMD, rootKey);
        List<String> result = shellParseLine(cmd, line -> {
            String[] temp = line.split("    ");
            if (temp.length == REG_NODE_LENGTH) {
                if (!line.equals(rootKey)) {
                    return line;
                }
            }
            return null;
        });
        return new HashSet<>(result);
    }

    @Override
    public boolean deleteNode(String name) {
        return false;
    }

    @Override
    public String getNode(String name) {
        String newKey= rootKey + "\\" + name;
        String cmd = String.format(GET_REG_VAL_LIST_CMD,newKey);
        List<String> result = shellParseLine(cmd, (line) -> {
            String[] temp = line.split("    ");
            if (temp.length == REG_NODE_LENGTH) {
                if (line.equals(newKey)) {
                    return line;
                }
            }
            return null;
        });
        return result.isEmpty()?null:result.get(0);
    }

    @Override
    public boolean createNode(String name) {
        return false;
    }

    private <T> List<T> shellParseLine(String cmd, Function<String, T> function) {
        String str = exe(cmd);
        List<T> result = new ArrayList<>();
        String[] lines = str.split("\r\n");
        for (String line : lines) {
            line = line.trim();
            if ("".equals(line)) {
                continue;
            }
            T temp = function.apply(line);
            if (temp != null) {
                result.add(temp);
            }
        }
        return result;
    }

    @Override
    public List<RegValue> getRegValue() {
        String cmd = String.format(GET_REG_VAL_LIST_CMD, rootKey);
        return shellParseLine(cmd, this::parseRegValue);
    }

    @Override
    public RegValue getRegValue(String regValueName) {
        String cmd = String.format(GET_REG_VAL_CMD, rootKey, regValueName);
        List<RegValue> regValues = shellParseLine(cmd, this::parseRegValue);
        if (regValues.isEmpty()) {
            return RegTypeEnum.REG_SZ.of(regValueName, null);
        }
        return regValues.get(0);
    }

    private RegValue parseRegValue(String line) {
        String[] temp = line.split("    ");
        if (temp.length == REG_VAL_LENGTH) {
            RegTypeEnum regTypeEnum = RegTypeEnum.find(temp[REG_VAL_TYPE_INDEX].trim());
            return regTypeEnum.of(temp[REG_VAL_KEY_INDEX].trim(), temp[REG_VAL_INDEX].trim());
        }
        return null;
    }

    @Override
    public boolean deleteRegValue(String regValueName) {
        String cmd = String.format(DELETE_REG_VAL_CMD, rootKey, regValueName);
        String exe = exe(cmd);
        exe("taskkill /f /im explorer.exe");
        exe("cmd /c start explorer.exe");
        return !"".equals(exe);
    }

    @Override
    public boolean setRegValue(RegValue regValue) {
        RegTypeEnum type = regValue.getType();
        String value = regValue.getValue();
        if (type.equals(RegTypeEnum.REG_EXPAND_SZ)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                if (ch == '%') {
                    sb.append("^");
                }
                sb.append(ch);
            }
            value = sb.toString();
        }
        String cmd = String.format(SET_REG_VAL_CMD, rootKey, regValue.getName(), type, value);
        String result = exe(cmd);
        exe("taskkill /f /im explorer.exe");
        exe("cmd /c start explorer.exe");
        return !"".equals(result);
    }

    public static void main(String[] args) {
        CmdRegOperate cmdRegOperate = new CmdRegOperate(RegRootEnum.HKEY_LOCAL_MACHINE, "SYSTEM\\CurrentControlSet\\Control\\Session Manager");
        //    List<RegValue> regValue = cmdRegOperate.getRegValue();
        // RegValue javaHome = cmdRegOperate.getRegValue("JAVA_HOME");
        //RegValue test1 = RegTypeEnum.REG_SZ.of("test1", "1122");
        //  cmdRegOperate.setRegValue(test1);
        String kernel = cmdRegOperate.getNode("kernel1");
        System.out.println("kernel = " + kernel);
        //cmdRegOperate.deleteRegValue("test1");
    }
}
