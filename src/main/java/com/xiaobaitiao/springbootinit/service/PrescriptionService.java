package com.xiaobaitiao.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaobaitiao.springbootinit.model.dto.prescription.PrescriptionQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.Prescription;
import com.xiaobaitiao.springbootinit.model.vo.PrescriptionVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 处方表服务
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
public interface PrescriptionService extends IService<Prescription> {

    /**
     * 校验数据
     *
     * @param prescription
     * @param add 对创建的数据进行校验
     */
    void validPrescription(Prescription prescription, boolean add);

    /**
     * 获取查询条件
     *
     * @param prescriptionQueryRequest
     * @return
     */
    QueryWrapper<Prescription> getQueryWrapper(PrescriptionQueryRequest prescriptionQueryRequest);
    
    /**
     * 获取处方表封装
     *
     * @param prescription
     * @param request
     * @return
     */
    PrescriptionVO getPrescriptionVO(Prescription prescription, HttpServletRequest request);

    /**
     * 分页获取处方表封装
     *
     * @param prescriptionPage
     * @param request
     * @return
     */
    Page<PrescriptionVO> getPrescriptionVOPage(Page<Prescription> prescriptionPage, HttpServletRequest request);
}
