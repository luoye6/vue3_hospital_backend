package com.xiaobaitiao.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaobaitiao.springbootinit.model.entity.DoctorSchedule;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 医生排班表视图
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class DoctorScheduleVO implements Serializable {

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



    private static final long serialVersionUID = 1L;

    /**
     * 封装类转对象
     *
     * @param doctorScheduleVO
     * @return
     */
    public static DoctorSchedule voToObj(DoctorScheduleVO doctorScheduleVO) {
        if (doctorScheduleVO == null) {
            return null;
        }
        DoctorSchedule doctorSchedule = new DoctorSchedule();
        BeanUtils.copyProperties(doctorScheduleVO, doctorSchedule);
        return doctorSchedule;
    }

    /**
     * 对象转封装类
     *
     * @param doctorSchedule
     * @return
     */
    public static DoctorScheduleVO objToVo(DoctorSchedule doctorSchedule) {
        if (doctorSchedule == null) {
            return null;
        }
        DoctorScheduleVO doctorScheduleVO = new DoctorScheduleVO();
        BeanUtils.copyProperties(doctorSchedule, doctorScheduleVO);
        return doctorScheduleVO;
    }
}
