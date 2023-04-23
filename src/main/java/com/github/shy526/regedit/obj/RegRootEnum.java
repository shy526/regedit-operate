package com.github.shy526.regedit.obj;


import lombok.Getter;

public enum RegRootEnum {
    HKEY_CLASSES_ROOT(Integer.MIN_VALUE),
    HKEY_CURRENT_USER(-2147483647),
    HKEY_LOCAL_MACHINE(-2147483646),
    HKEY_USERS(-2147483645),
    HKEY_CURRENT_CONFIG(-2147483643);

    RegRootEnum(Integer code) {
        this.code = code;
    }
    @Getter
    private final Integer code;


}
