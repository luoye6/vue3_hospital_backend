package com.xiaobaitiao.springbootinit.model.dto.doctorSchedule;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaobaitiao.springbootinit.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 查询医生排班表请求
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class DoctorScheduleQueryRequest extends PageRequest implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 医生id
     */
    private Long doctorId;

    /**
     * 排班日期
     */
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

    private static final long serialVersionUID = 1L;
}