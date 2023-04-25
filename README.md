# regedit-operate

提供了操作windows注册表的api

## com.github.shy526.regedit.CmdRegOperate 

1. 使用了`reg`指令来实现注册表的操作
2. 使用了`powershell`指令对系统环境变量进行刷新


## com.github.shy526.regedit.PrefsRegOperate

1. 对 `java.util.prefs.WindowsPreferences`进行反射来实现注册表的操作
2. 取消了大写的转换 `A`->`\A`
3. 使用了`powershell`指令对系统环境变量进行刷新
4. 由于`WindowsPreferences`的限制只能对REG_SZ就行操作
