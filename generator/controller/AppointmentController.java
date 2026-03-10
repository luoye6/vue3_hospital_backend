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
import com.xiaobaitiao.springbootinit.model.dto.appointment.AppointmentAddRequest;
import com.xiaobaitiao.springbootinit.model.dto.appointment.AppointmentEditRequest;
import com.xiaobaitiao.springbootinit.model.dto.appointment.AppointmentQueryRequest;
import com.xiaobaitiao.springbootinit.model.dto.appointment.AppointmentUpdateRequest;
import com.xiaobaitiao.springbootinit.model.entity.Appointment;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.AppointmentVO;
import com.xiaobaitiao.springbootinit.service.AppointmentService;
import com.xiaobaitiao.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 预约挂号表接口
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@RestController
@RequestMapping("/appointment")
@Slf4j
public class AppointmentController {

    @Resource
    private AppointmentService appointmentService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建预约挂号表
     *
     * @param appointmentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addAppointment(@RequestBody AppointmentAddRequest appointmentAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appointmentAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(appointmentAddRequest, appointment);
        // 数据校验
        appointmentService.validAppointment(appointment, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        appointment.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = appointmentService.save(appointment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newAppointmentId = appointment.getId();
        return ResultUtils.success(newAppointmentId);
    }

    /**
     * 删除预约挂号表
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteAppointment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Appointment oldAppointment = appointmentService.getById(id);
        ThrowUtils.throwIf(oldAppointment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldAppointment.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = appointmentService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新预约挂号表（仅管理员可用）
     *
     * @param appointmentUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppointment(@RequestBody AppointmentUpdateRequest appointmentUpdateRequest) {
        if (appointmentUpdateRequest == null || appointmentUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(appointmentUpdateRequest, appointment);
        // 数据校验
        appointmentService.validAppointment(appointment, false);
        // 判断是否存在
        long id = appointmentUpdateRequest.getId();
        Appointment oldAppointment = appointmentService.getById(id);
        ThrowUtils.throwIf(oldAppointment == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = appointmentService.updateById(appointment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取预约挂号表（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppointmentVO> getAppointmentVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Appointment appointment = appointmentService.getById(id);
        ThrowUtils.throwIf(appointment == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(appointmentService.getAppointmentVO(appointment, request));
    }

    /**
     * 分页获取预约挂号表列表（仅管理员可用）
     *
     * @param appointmentQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Appointment>> listAppointmentByPage(@RequestBody AppointmentQueryRequest appointmentQueryRequest) {
        long current = appointmentQueryRequest.getCurrent();
        long size = appointmentQueryRequest.getPageSize();
        // 查询数据库
        Page<Appointment> appointmentPage = appointmentService.page(new Page<>(current, size),
                appointmentService.getQueryWrapper(appointmentQueryRequest));
        return ResultUtils.success(appointmentPage);
    }

    /**
     * 分页获取预约挂号表列表（封装类）
     *
     * @param appointmentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<AppointmentVO>> listAppointmentVOByPage(@RequestBody AppointmentQueryRequest appointmentQueryRequest,
                                                               HttpServletRequest request) {
        long current = appointmentQueryRequest.getCurrent();
        long size = appointmentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Appointment> appointmentPage = appointmentService.page(new Page<>(current, size),
                appointmentService.getQueryWrapper(appointmentQueryRequest));
        // 获取封装类
        return ResultUtils.success(appointmentService.getAppointmentVOPage(appointmentPage, request));
    }

    /**
     * 分页获取当前登录用户创建的预约挂号表列表
     *
     * @param appointmentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppointmentVO>> listMyAppointmentVOByPage(@RequestBody AppointmentQueryRequest appointmentQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(appointmentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        appointmentQueryRequest.setUserId(loginUser.getId());
        long current = appointmentQueryRequest.getCurrent();
        long size = appointmentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Appointment> appointmentPage = appointmentService.page(new Page<>(current, size),
                appointmentService.getQueryWrapper(appointmentQueryRequest));
        // 获取封装类
        return ResultUtils.success(appointmentService.getAppointmentVOPage(appointmentPage, request));
    }

    /**
     * 编辑预约挂号表（给用户使用）
     *
     * @param appointmentEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editAppointment(@RequestBody AppointmentEditRequest appointmentEditRequest, HttpServletRequest request) {
        if (appointmentEditRequest == null || appointmentEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(appointmentEditRequest, appointment);
        // 数据校验
        appointmentService.validAppointment(appointment, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = appointmentEditRequest.getId();
        Appointment oldAppointment = appointmentService.getById(id);
        ThrowUtils.throwIf(oldAppointment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldAppointment.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = appointmentService.updateById(appointment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
