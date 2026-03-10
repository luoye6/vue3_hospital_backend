package com.xiaobaitiao.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.constant.CommonConstant;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.mapper.PrescriptionMapper;
import com.xiaobaitiao.springbootinit.model.dto.prescription.PrescriptionQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.Prescription;
import com.xiaobaitiao.springbootinit.model.vo.PrescriptionVO;
import com.xiaobaitiao.springbootinit.service.PrescriptionService;
import com.xiaobaitiao.springbootinit.service.UserService;
import com.xiaobaitiao.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 处方表服务实现
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Service
@Slf4j
public class PrescriptionServiceImpl extends ServiceImpl<PrescriptionMapper, Prescription> implements PrescriptionService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param prescription
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validPrescription(Prescription prescription, boolean add) {
        ThrowUtils.throwIf(prescription == null, ErrorCode.PARAMS_ERROR);
        Long patientId = prescription.getPatientId();
        Long appointmentId = prescription.getAppointmentId();
        String diagnosticResult = prescription.getDiagnosticResult();
        String prescriptionContent = prescription.getPrescriptionContent();

        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(patientId == null || patientId <= 0, ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(appointmentId == null || appointmentId <= 0, ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(StringUtils.isBlank(diagnosticResult), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(StringUtils.isBlank(prescriptionContent), ErrorCode.PARAMS_ERROR);
        }
    }

    /**
     * 获取查询条件
     *
     * @param prescriptionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Prescription> getQueryWrapper(PrescriptionQueryRequest prescriptionQueryRequest) {
        QueryWrapper<Prescription> queryWrapper = new QueryWrapper<>();
        if (prescriptionQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = prescriptionQueryRequest.getId();
        Long patientId = prescriptionQueryRequest.getPatientId();
        Long appointmentId = prescriptionQueryRequest.getAppointmentId();
        String diagnosticResult = prescriptionQueryRequest.getDiagnosticResult();
        String prescriptionContent = prescriptionQueryRequest.getPrescriptionContent();
        String precautions = prescriptionQueryRequest.getPrecautions();
        String sortField = prescriptionQueryRequest.getSortField();
        String sortOrder = prescriptionQueryRequest.getSortOrder();

        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(diagnosticResult), "diagnosticResult", diagnosticResult);
        queryWrapper.like(StringUtils.isNotBlank(prescriptionContent), "prescriptionContent", prescriptionContent);
        queryWrapper.like(StringUtils.isNotBlank(precautions), "precautions", precautions);

        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(patientId), "patientId", patientId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appointmentId), "appointmentId", appointmentId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取处方表封装
     *
     * @param prescription
     * @param request
     * @return
     */
    @Override
    public PrescriptionVO getPrescriptionVO(Prescription prescription, HttpServletRequest request) {
        // 对象转封装类


        return PrescriptionVO.objToVo(prescription);
    }

    /**
     * 分页获取处方表封装
     *
     * @param prescriptionPage
     * @param request
     * @return
     */
    @Override
    public Page<PrescriptionVO> getPrescriptionVOPage(Page<Prescription> prescriptionPage, HttpServletRequest request) {
        List<Prescription> prescriptionList = prescriptionPage.getRecords();
        Page<PrescriptionVO> prescriptionVOPage = new Page<>(prescriptionPage.getCurrent(), prescriptionPage.getSize(), prescriptionPage.getTotal());
        if (CollUtil.isEmpty(prescriptionList)) {
            return prescriptionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PrescriptionVO> prescriptionVOList = prescriptionList.stream().map(prescription -> {
            return PrescriptionVO.objToVo(prescription);
        }).collect(Collectors.toList());

        prescriptionVOPage.setRecords(prescriptionVOList);
        return prescriptionVOPage;
    }

}
