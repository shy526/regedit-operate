package com.github.shy526.regedit.oo;


import lombok.Getter;
import com.sun.deploy.util.WinRegistry;
public enum RegRootEnum {
    HKEY_CLASSES_ROOT(WinRegistry.HKEY_CLASSES_ROOT),
    HKEY_CURRENT_USER(WinRegistry.HKEY_CURRENT_USER),
    HKEY_LOCAL_MACHINE(WinRegistry.HKEY_LOCAL_MACHINE),
    HKEY_USERS(WinRegistry.HKEY_USERS),
    HKEY_CURRENT_CONFIG(WinRegistry.HKEY_CURRENT_CONFIG);

    RegRootEnum(Integer code) {
        this.code = code;
    }
    @Getter
    private final Integer code;


}
