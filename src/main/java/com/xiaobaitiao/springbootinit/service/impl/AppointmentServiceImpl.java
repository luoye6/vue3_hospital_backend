package com.xiaobaitiao.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.constant.CommonConstant;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.mapper.AppointmentMapper;
import com.xiaobaitiao.springbootinit.model.dto.appointment.AppointmentQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.Appointment;
import com.xiaobaitiao.springbootinit.model.vo.AppointmentVO;
import com.xiaobaitiao.springbootinit.service.AppointmentService;
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
 * 预约挂号表服务实现
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Service
@Slf4j
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param appointment
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validAppointment(Appointment appointment, boolean add) {
        ThrowUtils.throwIf(appointment == null, ErrorCode.PARAMS_ERROR);
        Long patientId = appointment.getPatientId();
        Long doctorScheduleId = appointment.getDoctorScheduleId();
        Integer appointmentNumber = appointment.getAppointmentNumber();
        String symptomDescription = appointment.getSymptomDescription();

        if (add) {
            ThrowUtils.throwIf(patientId == null ||patientId <=0, ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(doctorScheduleId == null ||doctorScheduleId <=0, ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(appointmentNumber == null ||appointmentNumber <=0, ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(StringUtils.isBlank(symptomDescription), ErrorCode.PARAMS_ERROR);
        }

    }

    /**
     * 获取查询条件
     *
     * @param appointmentQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Appointment> getQueryWrapper(AppointmentQueryRequest appointmentQueryRequest) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        if (appointmentQueryRequest == null) {
            return queryWrapper;
        }
        Long id = appointmentQueryRequest.getId();
        Long patientId = appointmentQueryRequest.getPatientId();
        Long doctorScheduleId = appointmentQueryRequest.getDoctorScheduleId();
        Integer appointmentStatus = appointmentQueryRequest.getAppointmentStatus();
        String symptomDescription = appointmentQueryRequest.getSymptomDescription();
        String sortField = appointmentQueryRequest.getSortField();
        String sortOrder = appointmentQueryRequest.getSortOrder();
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(symptomDescription), "symptomDescription", symptomDescription);
        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(patientId), "patientId", patientId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(doctorScheduleId), "doctorScheduleId", doctorScheduleId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(appointmentStatus), "appointmentStatus", appointmentStatus);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取预约挂号表封装
     *
     * @param appointment
     * @param request
     * @return
     */
    @Override
    public AppointmentVO getAppointmentVO(Appointment appointment, HttpServletRequest request) {
        // 对象转封装类
        AppointmentVO appointmentVO = AppointmentVO.objToVo(appointment);


        return appointmentVO;
    }

    /**
     * 分页获取预约挂号表封装
     *
     * @param appointmentPage
     * @param request
     * @return
     */
    @Override
    public Page<AppointmentVO> getAppointmentVOPage(Page<Appointment> appointmentPage, HttpServletRequest request) {
        List<Appointment> appointmentList = appointmentPage.getRecords();
        Page<AppointmentVO> appointmentVOPage = new Page<>(appointmentPage.getCurrent(), appointmentPage.getSize(), appointmentPage.getTotal());
        if (CollUtil.isEmpty(appointmentList)) {
            return appointmentVOPage;
        }
        // 对象列表 => 封装对象列表
        List<AppointmentVO> appointmentVOList = appointmentList.stream().map(appointment -> {
            return AppointmentVO.objToVo(appointment);
        }).collect(Collectors.toList());

        appointmentVOPage.setRecords(appointmentVOList);
        return appointmentVOPage;
    }

}
