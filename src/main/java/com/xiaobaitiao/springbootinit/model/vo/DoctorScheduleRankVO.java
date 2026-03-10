package com.xiaobaitiao.springbootinit.model.vo;

import lombok.Data;

@Data
public class DoctorScheduleRankVO {
    private Long doctorId;
    private String doctorName;  // 新增医生姓名
    private Integer scheduleCount;
}