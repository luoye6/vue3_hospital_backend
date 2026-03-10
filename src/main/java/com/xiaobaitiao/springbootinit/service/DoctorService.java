package com.xiaobaitiao.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaobaitiao.springbootinit.model.dto.doctor.DoctorQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.Doctor;
import com.xiaobaitiao.springbootinit.model.vo.DoctorVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 医生表服务
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
public interface DoctorService extends IService<Doctor> {

    /**
     * 校验数据
     *
     * @param doctor
     * @param add 对创建的数据进行校验
     */
    void validDoctor(Doctor doctor, boolean add);

    /**
     * 获取查询条件
     *
     * @param doctorQueryRequest
     * @return
     */
    QueryWrapper<Doctor> getQueryWrapper(DoctorQueryRequest doctorQueryRequest);
    
    /**
     * 获取医生表封装
     *
     * @param doctor
     * @param request
     * @return
     */
    DoctorVO getDoctorVO(Doctor doctor, HttpServletRequest request);

    /**
     * 分页获取医生表封装
     *
     * @param doctorPage
     * @param request
     * @return
     */
    Page<DoctorVO> getDoctorVOPage(Page<Doctor> doctorPage, HttpServletRequest request);
}
