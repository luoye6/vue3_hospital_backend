package com.xiaobaitiao.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaobaitiao.springbootinit.model.dto.doctorSchedule.DoctorScheduleQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.DoctorSchedule;
import com.xiaobaitiao.springbootinit.model.vo.DoctorScheduleVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 医生排班表服务
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
public interface DoctorScheduleService extends IService<DoctorSchedule> {

    /**
     * 校验数据
     *
     * @param doctorSchedule
     * @param add 对创建的数据进行校验
     */
    void validDoctorSchedule(DoctorSchedule doctorSchedule, boolean add);

    /**
     * 获取查询条件
     *
     * @param doctorScheduleQueryRequest
     * @return
     */
    QueryWrapper<DoctorSchedule> getQueryWrapper(DoctorScheduleQueryRequest doctorScheduleQueryRequest);
    
    /**
     * 获取医生排班表封装
     *
     * @param doctorSchedule
     * @param request
     * @return
     */
    DoctorScheduleVO getDoctorScheduleVO(DoctorSchedule doctorSchedule, HttpServletRequest request);

    /**
     * 分页获取医生排班表封装
     *
     * @param doctorSchedulePage
     * @param request
     * @return
     */
    Page<DoctorScheduleVO> getDoctorScheduleVOPage(Page<DoctorSchedule> doctorSchedulePage, HttpServletRequest request);
}
