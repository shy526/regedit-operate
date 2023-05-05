package com.github.shy526.regedit;

import com.github.shy526.regedit.obj.RegRootEnum;
import com.github.shy526.regedit.obj.RegTypeEnum;
import com.github.shy526.regedit.obj.RegValue;
import com.github.shy526.regedit.shell.ShellClient;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * wmic win7以上系统
 * 和 reg 都无法立即生效
 */

public class CmdRegOperate extends AbsRegOperate {
    //region 使用到的指令合集
    private final static String GET_REG_VAL_LIST_CMD = "REG QUERY \"%s\"";
    private final static String GET_REG_VAL_CMD = "REG QUERY \"%s\" /v %s";
    private final static String SET_REG_VAL_CMD = "REG ADD \"%s\" /v %s /t %s /d \"%s\" /f";
    private final static String ADD_NODE_CMD = "REG ADD \"%s\" /f";
    private final static String DELETE_REG_VAL_CMD = "REG DELETE \"%s\"  /v %s /f";
    private final static String DELETE_NODE_CMD = "REG DELETE \"%s\" /f";
    //endregion

    //region 解析value用到的所用
    private static final int REG_VAL_KEY_INDEX = 0;
    private static final int REG_VAL_TYPE_INDEX = 1;
    private static final int REG_VAL_INDEX = 2;
    private static final int REG_VAL_LENGTH = 3;
    private static final int REG_NODE_LENGTH = 1;
    private static final String SEPARATOR = "\\s{4}";
    //endregion

    public CmdRegOperate(RegRootEnum rootEnum, String keyName) {
        super(rootEnum, keyName);


    }


    @Override
    public Set<String> getNodes() {
        String cmd = String.format(GET_REG_VAL_LIST_CMD, getRootKey());
        List<String> result = shellParseLine(cmd, line -> {
            String[] temp = line.split(SEPARATOR);
            if (temp.length == REG_NODE_LENGTH) {
                if (!line.equals( getRootKey())) {
                    return line;
                }
            }
            return null;
        });
        return new HashSet<>(result);
    }

    @Override
    public boolean deleteNode(String name) {
        String newKey = join(getRootKey(),name);
        String cmd = String.format(DELETE_NODE_CMD, newKey);
        return ShellClient.exec(cmd) == ShellClient.CODE_SUCCESS;
    }

    @Override
    public String getNode(String name) {
        String newKey =join(getRootKey(),name);
        String cmd = String.format(GET_REG_VAL_LIST_CMD, newKey);
        return ShellClient.exec(cmd) == ShellClient.CODE_SUCCESS ? newKey : null;
    }

    @Override
    public boolean createNode(String name) {
        String newKey = join(getRootKey(),name);
        String cmd = String.format(ADD_NODE_CMD, newKey);
        return ShellClient.exec(cmd) == ShellClient.CODE_SUCCESS;
    }

    private <T> List<T> shellParseLine(String cmd, Function<String, T> function) {
        List<T> result = new ArrayList<>();
        ShellClient.exec(cmd, str -> {
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
        });
        return result;
    }

    @Override
    public List<RegValue> getRegValue() {
        String cmd = String.format(GET_REG_VAL_LIST_CMD,  getRootKey());
        return shellParseLine(cmd, this::parseRegValue);
    }

    @Override
    public RegValue getRegValue(String regValueName) {
        String cmd = String.format(GET_REG_VAL_CMD,  getRootKey(), regValueName);
        List<RegValue> regValues = shellParseLine(cmd, this::parseRegValue);
        if (regValues.isEmpty()) {
            return null;
        }
        return regValues.get(0);
    }

    private RegValue parseRegValue(String line) {
        String[] temp = line.split(SEPARATOR);
        if (temp.length == REG_VAL_LENGTH) {
            RegTypeEnum regTypeEnum = RegTypeEnum.find(temp[REG_VAL_TYPE_INDEX].trim());
            return regTypeEnum.of(temp[REG_VAL_KEY_INDEX].trim(), temp[REG_VAL_INDEX].trim());
        }
        return null;
    }

    @Override
    public boolean deleteRegValue(String regValueName) {
        String cmd = String.format(DELETE_REG_VAL_CMD,  getRootKey(), regValueName);
        return ShellClient.exec(cmd) == ShellClient.CODE_SUCCESS;
    }

    @Override
    public boolean setRegValue(RegValue regValue) {
        RegTypeEnum type = regValue.getType();
        String value = regValue.getValue();
/*        if (type.equals(RegTypeEnum.REG_EXPAND_SZ)) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < value.length(); i++) {
                char ch = value.charAt(i);
                if (ch == '%') {
                    sb.append("^");
                }
                sb.append(ch);
            }
            value = sb.toString();
        }*/
        String cmd = String.format(SET_REG_VAL_CMD,  getRootKey(), regValue.getName(), type, value);

        return  ShellClient.exec(cmd,System.out::println,System.out::println) == ShellClient.CODE_SUCCESS;
    }



}
