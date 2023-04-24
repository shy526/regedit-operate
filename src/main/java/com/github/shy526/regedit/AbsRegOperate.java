package com.github.shy526.regedit;

import com.github.shy526.regedit.obj.RegRootEnum;
import com.github.shy526.regedit.shell.CommonShellWin;
import com.github.shy526.regedit.shell.ShellClient;
import lombok.Getter;

@Getter
public abstract class AbsRegOperate implements RegOperate {

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
        ShellClient.exec(CommonShellWin.POWER_SHELL_RESTART_EXPLORER);
    }


}
