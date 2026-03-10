package com.xiaobaitiao.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaobaitiao.springbootinit.model.dto.appointment.AppointmentQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.Appointment;
import com.xiaobaitiao.springbootinit.model.vo.AppointmentVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 预约挂号表服务
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
public interface AppointmentService extends IService<Appointment> {

    /**
     * 校验数据
     *
     * @param appointment
     * @param add 对创建的数据进行校验
     */
    void validAppointment(Appointment appointment, boolean add);

    /**
     * 获取查询条件
     *
     * @param appointmentQueryRequest
     * @return
     */
    QueryWrapper<Appointment> getQueryWrapper(AppointmentQueryRequest appointmentQueryRequest);
    
    /**
     * 获取预约挂号表封装
     *
     * @param appointment
     * @param request
     * @return
     */
    AppointmentVO getAppointmentVO(Appointment appointment, HttpServletRequest request);

    /**
     * 分页获取预约挂号表封装
     *
     * @param appointmentPage
     * @param request
     * @return
     */
    Page<AppointmentVO> getAppointmentVOPage(Page<Appointment> appointmentPage, HttpServletRequest request);
}
