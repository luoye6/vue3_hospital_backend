package com.xiaobaitiao.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.constant.CommonConstant;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.mapper.DoctorMapper;
import com.xiaobaitiao.springbootinit.model.dto.doctor.DoctorQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.Doctor;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.DoctorVO;
import com.xiaobaitiao.springbootinit.model.vo.UserVO;
import com.xiaobaitiao.springbootinit.service.DoctorService;
import com.xiaobaitiao.springbootinit.service.UserService;
import com.xiaobaitiao.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 医生表服务实现
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Service
@Slf4j
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param doctor
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validDoctor(Doctor doctor, boolean add) {
        ThrowUtils.throwIf(doctor == null, ErrorCode.PARAMS_ERROR);
        Long departmentId = doctor.getDepartmentId();
        String doctorName = doctor.getDoctorName();
        String doctorSex = doctor.getDoctorSex();
        String doctorTitle = doctor.getDoctorTitle();
        String doctorDescription = doctor.getDoctorDescription();
        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isBlank(doctorName), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(StringUtils.isBlank(doctorSex), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(StringUtils.isBlank(doctorTitle), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(StringUtils.isBlank(doctorDescription), ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(departmentId == null || departmentId  <=0, ErrorCode.PARAMS_ERROR);
        }

    }

    /**
     * 获取查询条件
     *
     * @param doctorQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Doctor> getQueryWrapper(DoctorQueryRequest doctorQueryRequest) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        if (doctorQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = doctorQueryRequest.getId();
        Long departmentId = doctorQueryRequest.getDepartmentId();
        String doctorName = doctorQueryRequest.getDoctorName();
        String doctorTitle = doctorQueryRequest.getDoctorTitle();
        String doctorDescription = doctorQueryRequest.getDoctorDescription();
        Integer isEnabled = doctorQueryRequest.getIsEnabled();
        String sortField = doctorQueryRequest.getSortField();
        String sortOrder = doctorQueryRequest.getSortOrder();
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(doctorName), "doctorName", doctorName);
        queryWrapper.like(StringUtils.isNotBlank(doctorTitle), "doctorTitle", doctorTitle);
        queryWrapper.like(StringUtils.isNotBlank(doctorDescription), "doctorDescription", doctorDescription);
        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(departmentId), "departmentId", departmentId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(isEnabled), "isEnabled", isEnabled);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取医生表封装
     *
     * @param doctor
     * @param request
     * @return
     */
    @Override
    public DoctorVO getDoctorVO(Doctor doctor, HttpServletRequest request) {
        // 对象转封装类
        return DoctorVO.objToVo(doctor);
    }

    /**
     * 分页获取医生表封装
     *
     * @param doctorPage
     * @param request
     * @return
     */
    @Override
    public Page<DoctorVO> getDoctorVOPage(Page<Doctor> doctorPage, HttpServletRequest request) {
        List<Doctor> doctorList = doctorPage.getRecords();
        Page<DoctorVO> doctorVOPage = new Page<>(doctorPage.getCurrent(), doctorPage.getSize(), doctorPage.getTotal());
        if (CollUtil.isEmpty(doctorList)) {
            return doctorVOPage;
        }
        // 对象列表 => 封装对象列表
        List<DoctorVO> doctorVOList = doctorList.stream().map(DoctorVO::objToVo).collect(Collectors.toList());

        doctorVOPage.setRecords(doctorVOList);
        return doctorVOPage;
    }

}
