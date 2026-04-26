package com.lzh.context;

import com.lzh.vo.TimeBitmap;

public record StuContext (

    Long stuId,

    String name,

    Long majorId,

    Integer grade,

    String engLevel,

    TimeBitmap scheduleBitMap,

    Integer tuitionStatus,

    Integer evalStatus
){
}
