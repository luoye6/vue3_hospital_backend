package com.xiaobaitiao.springbootinit.model.dto.appointment;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 更新预约挂号表请求
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class AppointmentUpdateRequest implements Serializable {
    /**
     * id
     */
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


    private static final long serialVersionUID = 1L;
}