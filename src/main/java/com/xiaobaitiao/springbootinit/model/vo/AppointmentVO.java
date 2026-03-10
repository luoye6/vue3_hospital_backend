package com.xiaobaitiao.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaobaitiao.springbootinit.model.entity.Appointment;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 预约挂号表视图
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class AppointmentVO implements Serializable {
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

    private static final long serialVersionUID = 1L;
    /**
     * 封装类转对象
     *
     * @param appointmentVO
     * @return
     */
    public static Appointment voToObj(AppointmentVO appointmentVO) {
        if (appointmentVO == null) {
            return null;
        }
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(appointmentVO, appointment);
        return appointment;
    }

    /**
     * 对象转封装类
     *
     * @param appointment
     * @return
     */
    public static AppointmentVO objToVo(Appointment appointment) {
        if (appointment == null) {
            return null;
        }
        AppointmentVO appointmentVO = new AppointmentVO();
        BeanUtils.copyProperties(appointment, appointmentVO);
        return appointmentVO;
    }
}
