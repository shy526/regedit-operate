package com.github.shy526.regedit.obj;

public enum RegTypeEnum {

    REG_SZ,
    REG_MULTI_SZ,
    REG_EXPAND_SZ,
    REG_BINARY,
    REG_DWORD,
    REG_QWORD,
    ;

    public RegValue of(String name, String value) {
        return new RegValue(name, value, this);
    }

    public static RegTypeEnum find(String str) {
        for (RegTypeEnum item : values()) {
            if (str.equals(item.name())) {
                return item;
            }
        }
        return null;
    }
}
