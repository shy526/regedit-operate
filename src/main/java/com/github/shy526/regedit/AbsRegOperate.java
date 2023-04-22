package com.github.shy526.regedit;

import com.github.shy526.regedit.obj.RegRootEnum;
import lombok.Getter;

@Getter
public abstract class AbsRegOperate implements RegOperate {

    private final RegRootEnum rootEnum;
    private final String keyName;

    public AbsRegOperate(RegRootEnum rootEnum, String keyName) {
        this.rootEnum = rootEnum;
        this.keyName = keyName;
    }
}
