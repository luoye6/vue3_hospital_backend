package com.xiaobaitiao.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 
 * @TableName appointment
 */
@TableName(value ="appointment")
@Data
public class Appointment implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 患者id
     */
    private Long patientId;

    /**
     * 医生排班表id
     */
    private Long doctorScheduleId;

    /**
     * 预约号(第几个被叫号)
     */
    private Integer appointmentNumber;

    /**
     * 预约状态（0 已预约 1 已完成 2 已取消）
     */
    private Integer appointmentStatus;

    /**
     * 症状描述
     */
    private String symptomDescription;

    /**
     * 取消预约原因
     */
    private String cancelReason;

    /**
     * 支付状态(0 未支付 1已支付）
     */
    private Integer payStatus;

    /**
     * 支付金额
     */
    private BigDecimal paymentAmount;

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