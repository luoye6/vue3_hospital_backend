package com.xiaobaitiao.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.constant.CommonConstant;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.mapper.DepartmentMapper;
import com.xiaobaitiao.springbootinit.model.dto.department.DepartmentQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.Department;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.DepartmentVO;
import com.xiaobaitiao.springbootinit.model.vo.UserVO;
import com.xiaobaitiao.springbootinit.service.DepartmentService;
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
 * 科室表服务实现
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Service
@Slf4j
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param department
     * @param add        对创建的数据进行校验
     */
    @Override
    public void validDepartment(Department department, boolean add) {
        ThrowUtils.throwIf(department == null, ErrorCode.PARAMS_ERROR);
        String departmentName = department.getDepartmentName();
        String departmentDescription = department.getDepartmentDescription();
        ThrowUtils.throwIf(StringUtils.isBlank(departmentName), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StringUtils.isBlank(departmentDescription), ErrorCode.PARAMS_ERROR);
    }

    /**
     * 获取查询条件
     *
     * @param departmentQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Department> getQueryWrapper(DepartmentQueryRequest departmentQueryRequest) {
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        if (departmentQueryRequest == null) {
            return queryWrapper;
        }
        Long id = departmentQueryRequest.getId();
        String departmentName = departmentQueryRequest.getDepartmentName();
        String departmentDescription = departmentQueryRequest.getDepartmentDescription();
        Integer isEnabled = departmentQueryRequest.getIsEnabled();
        String sortField = departmentQueryRequest.getSortField();
        String sortOrder = departmentQueryRequest.getSortOrder();
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(departmentName), "departmentName", departmentName);
        queryWrapper.like(StringUtils.isNotBlank(departmentDescription), "departmentDescription", departmentDescription);
        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(isEnabled), "isEnabled", isEnabled);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取科室表封装
     *
     * @param department
     * @param request
     * @return
     */
    @Override
    public DepartmentVO getDepartmentVO(Department department, HttpServletRequest request) {
        // 对象转封装类
        return DepartmentVO.objToVo(department);
    }

    /**
     * 分页获取科室表封装
     *
     * @param departmentPage
     * @param request
     * @return
     */
    @Override
    public Page<DepartmentVO> getDepartmentVOPage(Page<Department> departmentPage, HttpServletRequest request) {
        List<Department> departmentList = departmentPage.getRecords();
        Page<DepartmentVO> departmentVOPage = new Page<>(departmentPage.getCurrent(), departmentPage.getSize(), departmentPage.getTotal());
        if (CollUtil.isEmpty(departmentList)) {
            return departmentVOPage;
        }
        // 对象列表 => 封装对象列表
        List<DepartmentVO> departmentVOList = departmentList.stream().map(DepartmentVO::objToVo).collect(Collectors.toList());
        departmentVOPage.setRecords(departmentVOList);
        return departmentVOPage;
    }

}
