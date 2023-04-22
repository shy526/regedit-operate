package com.github.shy526.regedit.obj;

import lombok.Data;

@Data
public class RegValue {

    public RegValue(String name, String value, RegTypeEnum type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    private String name;
    private String value;
    private RegTypeEnum type;
    }
