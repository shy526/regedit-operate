package com.github.shy526.regedit;

import com.github.shy526.regedit.obj.RegRootEnum;
import com.github.shy526.regedit.shell.CommonShellWin;
import com.github.shy526.regedit.shell.ShellClient;
import lombok.Getter;

@Getter
public abstract class AbsRegOperate implements RegOperate {

    public static final String SYS_ENVIRONMENT = "SYSTEM\\CurrentControlSet\\Control\\Session Manager\\Environment";
    public static final String USER_ENVIRONMENT = "Environment";
    private final static String FIND_EXPLORER = String.format(CommonShellWin.CMD_SHELL_FIND_PROCESS_FORMAT, "explorer");
    private final static String RESTART_EXPLORER = String.format(CommonShellWin.POWER_SHELL_STOP__PROCESS_FORMAT, "explorer");
    private static final String SEPARATE = "\\";
    private final RegRootEnum rootEnum;
    private final String keyName;
    private final String rootKey;


    public AbsRegOperate(RegRootEnum rootEnum, String keyName) {
        this.rootKey = join(rootEnum.name(), keyName);
        this.rootEnum = rootEnum;
        this.keyName = keyName;
    }

    public String join(String... params) {
        StringBuilder result = new StringBuilder();
        for (String param : params) {
            result.append(param).append(SEPARATE);
        }
        result.deleteCharAt(result.length() - 1);
        return result.toString();
    }

    @Override
    public void refreshEnvironment() {
        int exec = ShellClient.exec(FIND_EXPLORER, result -> {
            if (!"".equals(result)) {
                ShellClient.exec(RESTART_EXPLORER);
            }
        });
    }


}
