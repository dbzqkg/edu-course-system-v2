package com.lzh.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SelectionSourceType {
    AUTO(1, "置课"),
    ROBBING(2, "抢课"),
    INTENT(3, "意向");

    private final int code;
    private final String desc;

}