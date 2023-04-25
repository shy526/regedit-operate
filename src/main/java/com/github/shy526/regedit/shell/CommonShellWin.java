package com.github.shy526.regedit.shell;

/**
 * 一些常用的windows命令
 */
public class CommonShellWin {
    /**
     * 查询莫个线程
     */
    public static final String CMD_SHELL_FIND_PROCESS_FORMAT="cmd /c tasklist |findstr \"%s\"";
    public static final String POWER_SHELL_STOP__PROCESS_FORMAT= "powershell.exe Stop-Process -processname %s";
    public static final String[] CMD_SHELL_RESTART_EXPLORER=new String[]{"cmd","/c","taskkill", "/f", "/im", "explorer.exe","&","start","explorer.exe"};
}
