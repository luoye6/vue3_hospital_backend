package com.xiaobaitiao.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName doctor_schedule
 */
@TableName(value ="doctor_schedule")
@Data
public class DoctorSchedule implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 医生id
     */
    private Long doctorId;

    /**
     * 排班日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date scheduleDate;

    /**
     * 时间段（上午、下午、晚上）
     */
    private String timeSlot;

    /**
     * 最大预约数
     */
    private Integer maxAppointment;

    /**
     * 已经预约数
     */
    private Integer alreadyAppointment;

    /**
     * 是否启用 0 未启用 1已启用
     */
    private Integer isEnabled;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}