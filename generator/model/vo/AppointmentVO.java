package com.xiaobaitiao.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.xiaobaitiao.springbootinit.model.entity.Appointment;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
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
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 创建用户信息
     */
    private UserVO user;

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
        List<String> tagList = appointmentVO.getTagList();
        appointment.setTags(JSONUtil.toJsonStr(tagList));
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
        appointmentVO.setTagList(JSONUtil.toList(appointment.getTags(), String.class));
        return appointmentVO;
    }
}
