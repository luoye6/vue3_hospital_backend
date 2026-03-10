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
import com.xiaobaitiao.springbootinit.model.dto.doctorSchedule.DoctorScheduleAddRequest;
import com.xiaobaitiao.springbootinit.model.dto.doctorSchedule.DoctorScheduleEditRequest;
import com.xiaobaitiao.springbootinit.model.dto.doctorSchedule.DoctorScheduleQueryRequest;
import com.xiaobaitiao.springbootinit.model.dto.doctorSchedule.DoctorScheduleUpdateRequest;
import com.xiaobaitiao.springbootinit.model.entity.DoctorSchedule;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.DoctorScheduleVO;
import com.xiaobaitiao.springbootinit.service.DoctorScheduleService;
import com.xiaobaitiao.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 医生排班表接口
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@RestController
@RequestMapping("/doctorSchedule")
@Slf4j
public class DoctorScheduleController {

    @Resource
    private DoctorScheduleService doctorScheduleService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建医生排班表
     *
     * @param doctorScheduleAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addDoctorSchedule(@RequestBody DoctorScheduleAddRequest doctorScheduleAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(doctorScheduleAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        DoctorSchedule doctorSchedule = new DoctorSchedule();
        BeanUtils.copyProperties(doctorScheduleAddRequest, doctorSchedule);
        // 数据校验
        doctorScheduleService.validDoctorSchedule(doctorSchedule, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        doctorSchedule.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = doctorScheduleService.save(doctorSchedule);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newDoctorScheduleId = doctorSchedule.getId();
        return ResultUtils.success(newDoctorScheduleId);
    }

    /**
     * 删除医生排班表
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteDoctorSchedule(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        DoctorSchedule oldDoctorSchedule = doctorScheduleService.getById(id);
        ThrowUtils.throwIf(oldDoctorSchedule == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldDoctorSchedule.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = doctorScheduleService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新医生排班表（仅管理员可用）
     *
     * @param doctorScheduleUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateDoctorSchedule(@RequestBody DoctorScheduleUpdateRequest doctorScheduleUpdateRequest) {
        if (doctorScheduleUpdateRequest == null || doctorScheduleUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        DoctorSchedule doctorSchedule = new DoctorSchedule();
        BeanUtils.copyProperties(doctorScheduleUpdateRequest, doctorSchedule);
        // 数据校验
        doctorScheduleService.validDoctorSchedule(doctorSchedule, false);
        // 判断是否存在
        long id = doctorScheduleUpdateRequest.getId();
        DoctorSchedule oldDoctorSchedule = doctorScheduleService.getById(id);
        ThrowUtils.throwIf(oldDoctorSchedule == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = doctorScheduleService.updateById(doctorSchedule);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取医生排班表（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<DoctorScheduleVO> getDoctorScheduleVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        DoctorSchedule doctorSchedule = doctorScheduleService.getById(id);
        ThrowUtils.throwIf(doctorSchedule == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(doctorScheduleService.getDoctorScheduleVO(doctorSchedule, request));
    }

    /**
     * 分页获取医生排班表列表（仅管理员可用）
     *
     * @param doctorScheduleQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<DoctorSchedule>> listDoctorScheduleByPage(@RequestBody DoctorScheduleQueryRequest doctorScheduleQueryRequest) {
        long current = doctorScheduleQueryRequest.getCurrent();
        long size = doctorScheduleQueryRequest.getPageSize();
        // 查询数据库
        Page<DoctorSchedule> doctorSchedulePage = doctorScheduleService.page(new Page<>(current, size),
                doctorScheduleService.getQueryWrapper(doctorScheduleQueryRequest));
        return ResultUtils.success(doctorSchedulePage);
    }

    /**
     * 分页获取医生排班表列表（封装类）
     *
     * @param doctorScheduleQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<DoctorScheduleVO>> listDoctorScheduleVOByPage(@RequestBody DoctorScheduleQueryRequest doctorScheduleQueryRequest,
                                                               HttpServletRequest request) {
        long current = doctorScheduleQueryRequest.getCurrent();
        long size = doctorScheduleQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<DoctorSchedule> doctorSchedulePage = doctorScheduleService.page(new Page<>(current, size),
                doctorScheduleService.getQueryWrapper(doctorScheduleQueryRequest));
        // 获取封装类
        return ResultUtils.success(doctorScheduleService.getDoctorScheduleVOPage(doctorSchedulePage, request));
    }

    /**
     * 分页获取当前登录用户创建的医生排班表列表
     *
     * @param doctorScheduleQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<DoctorScheduleVO>> listMyDoctorScheduleVOByPage(@RequestBody DoctorScheduleQueryRequest doctorScheduleQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(doctorScheduleQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        doctorScheduleQueryRequest.setUserId(loginUser.getId());
        long current = doctorScheduleQueryRequest.getCurrent();
        long size = doctorScheduleQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<DoctorSchedule> doctorSchedulePage = doctorScheduleService.page(new Page<>(current, size),
                doctorScheduleService.getQueryWrapper(doctorScheduleQueryRequest));
        // 获取封装类
        return ResultUtils.success(doctorScheduleService.getDoctorScheduleVOPage(doctorSchedulePage, request));
    }

    /**
     * 编辑医生排班表（给用户使用）
     *
     * @param doctorScheduleEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editDoctorSchedule(@RequestBody DoctorScheduleEditRequest doctorScheduleEditRequest, HttpServletRequest request) {
        if (doctorScheduleEditRequest == null || doctorScheduleEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        DoctorSchedule doctorSchedule = new DoctorSchedule();
        BeanUtils.copyProperties(doctorScheduleEditRequest, doctorSchedule);
        // 数据校验
        doctorScheduleService.validDoctorSchedule(doctorSchedule, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = doctorScheduleEditRequest.getId();
        DoctorSchedule oldDoctorSchedule = doctorScheduleService.getById(id);
        ThrowUtils.throwIf(oldDoctorSchedule == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldDoctorSchedule.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = doctorScheduleService.updateById(doctorSchedule);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
