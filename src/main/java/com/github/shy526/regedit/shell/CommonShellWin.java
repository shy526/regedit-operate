package com.github.shy526.regedit.shell;

/**
 * 一些常用的windows命令
 */
public class CommonShellWin {
    public static final String POWER_SHELL_RESTART_EXPLORER= "powershell.exe Stop-Process -processname explorer";
    public static final String[] CMD_SHELL_RESTART_EXPLORER=new String[]{"cmd","/c","taskkill", "/f", "/im", "explorer.exe","&","start","explorer.exe"};
}
