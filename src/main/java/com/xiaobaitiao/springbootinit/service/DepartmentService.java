package com.xiaobaitiao.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaobaitiao.springbootinit.model.dto.department.DepartmentQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.Department;
import com.xiaobaitiao.springbootinit.model.vo.DepartmentVO;

import javax.servlet.http.HttpServletRequest;

/**
 * 科室表服务
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
public interface DepartmentService extends IService<Department> {

    /**
     * 校验数据
     *
     * @param department
     * @param add 对创建的数据进行校验
     */
    void validDepartment(Department department, boolean add);

    /**
     * 获取查询条件
     *
     * @param departmentQueryRequest
     * @return
     */
    QueryWrapper<Department> getQueryWrapper(DepartmentQueryRequest departmentQueryRequest);
    
    /**
     * 获取科室表封装
     *
     * @param department
     * @param request
     * @return
     */
    DepartmentVO getDepartmentVO(Department department, HttpServletRequest request);

    /**
     * 分页获取科室表封装
     *
     * @param departmentPage
     * @param request
     * @return
     */
    Page<DepartmentVO> getDepartmentVOPage(Page<Department> departmentPage, HttpServletRequest request);
}
