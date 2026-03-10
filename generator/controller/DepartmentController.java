package com.xiaobaitiao.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaobaitiao.springbootinit.annotation.AuthCheck;
import com.xiaobaitiao.springbootinit.common.BaseResponse;
import com.xiaobaitiao.springbootinit.common.DeleteRequest;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.common.ResultUtils;
import com.xiaobaitiao.springbootinit.constant.UserConstant;
import com.xiaobaitiao.springbootinit.exception.BusinessException;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.model.dto.department.DepartmentAddRequest;
import com.xiaobaitiao.springbootinit.model.dto.department.DepartmentEditRequest;
import com.xiaobaitiao.springbootinit.model.dto.department.DepartmentQueryRequest;
import com.xiaobaitiao.springbootinit.model.dto.department.DepartmentUpdateRequest;
import com.xiaobaitiao.springbootinit.model.entity.Department;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.DepartmentVO;
import com.xiaobaitiao.springbootinit.service.DepartmentService;
import com.xiaobaitiao.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 科室表接口
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@RestController
@RequestMapping("/department")
@Slf4j
public class DepartmentController {

    @Resource
    private DepartmentService departmentService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建科室表
     *
     * @param departmentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addDepartment(@RequestBody DepartmentAddRequest departmentAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(departmentAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        Department department = new Department();
        BeanUtils.copyProperties(departmentAddRequest, department);
        // 数据校验
        departmentService.validDepartment(department, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        department.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = departmentService.save(department);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newDepartmentId = department.getId();
        return ResultUtils.success(newDepartmentId);
    }

    /**
     * 删除科室表
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteDepartment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Department oldDepartment = departmentService.getById(id);
        ThrowUtils.throwIf(oldDepartment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldDepartment.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = departmentService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新科室表（仅管理员可用）
     *
     * @param departmentUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateDepartment(@RequestBody DepartmentUpdateRequest departmentUpdateRequest) {
        if (departmentUpdateRequest == null || departmentUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Department department = new Department();
        BeanUtils.copyProperties(departmentUpdateRequest, department);
        // 数据校验
        departmentService.validDepartment(department, false);
        // 判断是否存在
        long id = departmentUpdateRequest.getId();
        Department oldDepartment = departmentService.getById(id);
        ThrowUtils.throwIf(oldDepartment == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = departmentService.updateById(department);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取科室表（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<DepartmentVO> getDepartmentVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Department department = departmentService.getById(id);
        ThrowUtils.throwIf(department == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(departmentService.getDepartmentVO(department, request));
    }

    /**
     * 分页获取科室表列表（仅管理员可用）
     *
     * @param departmentQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Department>> listDepartmentByPage(@RequestBody DepartmentQueryRequest departmentQueryRequest) {
        long current = departmentQueryRequest.getCurrent();
        long size = departmentQueryRequest.getPageSize();
        // 查询数据库
        Page<Department> departmentPage = departmentService.page(new Page<>(current, size),
                departmentService.getQueryWrapper(departmentQueryRequest));
        return ResultUtils.success(departmentPage);
    }

    /**
     * 分页获取科室表列表（封装类）
     *
     * @param departmentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<DepartmentVO>> listDepartmentVOByPage(@RequestBody DepartmentQueryRequest departmentQueryRequest,
                                                               HttpServletRequest request) {
        long current = departmentQueryRequest.getCurrent();
        long size = departmentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Department> departmentPage = departmentService.page(new Page<>(current, size),
                departmentService.getQueryWrapper(departmentQueryRequest));
        // 获取封装类
        return ResultUtils.success(departmentService.getDepartmentVOPage(departmentPage, request));
    }

    /**
     * 分页获取当前登录用户创建的科室表列表
     *
     * @param departmentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<DepartmentVO>> listMyDepartmentVOByPage(@RequestBody DepartmentQueryRequest departmentQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(departmentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        departmentQueryRequest.setUserId(loginUser.getId());
        long current = departmentQueryRequest.getCurrent();
        long size = departmentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Department> departmentPage = departmentService.page(new Page<>(current, size),
                departmentService.getQueryWrapper(departmentQueryRequest));
        // 获取封装类
        return ResultUtils.success(departmentService.getDepartmentVOPage(departmentPage, request));
    }

    /**
     * 编辑科室表（给用户使用）
     *
     * @param departmentEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editDepartment(@RequestBody DepartmentEditRequest departmentEditRequest, HttpServletRequest request) {
        if (departmentEditRequest == null || departmentEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Department department = new Department();
        BeanUtils.copyProperties(departmentEditRequest, department);
        // 数据校验
        departmentService.validDepartment(department, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = departmentEditRequest.getId();
        Department oldDepartment = departmentService.getById(id);
        ThrowUtils.throwIf(oldDepartment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldDepartment.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = departmentService.updateById(department);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
