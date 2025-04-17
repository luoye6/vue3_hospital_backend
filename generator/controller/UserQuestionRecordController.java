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
import com.xiaobaitiao.springbootinit.model.dto.userQuestionRecord.UserQuestionRecordAddRequest;
import com.xiaobaitiao.springbootinit.model.dto.userQuestionRecord.UserQuestionRecordEditRequest;
import com.xiaobaitiao.springbootinit.model.dto.userQuestionRecord.UserQuestionRecordQueryRequest;
import com.xiaobaitiao.springbootinit.model.dto.userQuestionRecord.UserQuestionRecordUpdateRequest;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户刷题记录表接口
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@RestController
@RequestMapping("/userQuestionRecord")
@Slf4j
public class UserQuestionRecordController {

    @Resource
    private UserQuestionRecordService userQuestionRecordService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建用户刷题记录表
     *
     * @param userQuestionRecordAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserQuestionRecord(@RequestBody UserQuestionRecordAddRequest userQuestionRecordAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userQuestionRecordAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        UserQuestionRecord userQuestionRecord = new UserQuestionRecord();
        BeanUtils.copyProperties(userQuestionRecordAddRequest, userQuestionRecord);
        // 数据校验
        userQuestionRecordService.validUserQuestionRecord(userQuestionRecord, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        userQuestionRecord.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = userQuestionRecordService.save(userQuestionRecord);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newUserQuestionRecordId = userQuestionRecord.getId();
        return ResultUtils.success(newUserQuestionRecordId);
    }

    /**
     * 删除用户刷题记录表
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserQuestionRecord(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserQuestionRecord oldUserQuestionRecord = userQuestionRecordService.getById(id);
        ThrowUtils.throwIf(oldUserQuestionRecord == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserQuestionRecord.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = userQuestionRecordService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户刷题记录表（仅管理员可用）
     *
     * @param userQuestionRecordUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserQuestionRecord(@RequestBody UserQuestionRecordUpdateRequest userQuestionRecordUpdateRequest) {
        if (userQuestionRecordUpdateRequest == null || userQuestionRecordUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        UserQuestionRecord userQuestionRecord = new UserQuestionRecord();
        BeanUtils.copyProperties(userQuestionRecordUpdateRequest, userQuestionRecord);
        // 数据校验
        userQuestionRecordService.validUserQuestionRecord(userQuestionRecord, false);
        // 判断是否存在
        long id = userQuestionRecordUpdateRequest.getId();
        UserQuestionRecord oldUserQuestionRecord = userQuestionRecordService.getById(id);
        ThrowUtils.throwIf(oldUserQuestionRecord == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = userQuestionRecordService.updateById(userQuestionRecord);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户刷题记录表（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserQuestionRecordVO> getUserQuestionRecordVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        UserQuestionRecord userQuestionRecord = userQuestionRecordService.getById(id);
        ThrowUtils.throwIf(userQuestionRecord == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(userQuestionRecordService.getUserQuestionRecordVO(userQuestionRecord, request));
    }

    /**
     * 分页获取用户刷题记录表列表（仅管理员可用）
     *
     * @param userQuestionRecordQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserQuestionRecord>> listUserQuestionRecordByPage(@RequestBody UserQuestionRecordQueryRequest userQuestionRecordQueryRequest) {
        long current = userQuestionRecordQueryRequest.getCurrent();
        long size = userQuestionRecordQueryRequest.getPageSize();
        // 查询数据库
        Page<UserQuestionRecord> userQuestionRecordPage = userQuestionRecordService.page(new Page<>(current, size),
                userQuestionRecordService.getQueryWrapper(userQuestionRecordQueryRequest));
        return ResultUtils.success(userQuestionRecordPage);
    }

    /**
     * 分页获取用户刷题记录表列表（封装类）
     *
     * @param userQuestionRecordQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserQuestionRecordVO>> listUserQuestionRecordVOByPage(@RequestBody UserQuestionRecordQueryRequest userQuestionRecordQueryRequest,
                                                               HttpServletRequest request) {
        long current = userQuestionRecordQueryRequest.getCurrent();
        long size = userQuestionRecordQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserQuestionRecord> userQuestionRecordPage = userQuestionRecordService.page(new Page<>(current, size),
                userQuestionRecordService.getQueryWrapper(userQuestionRecordQueryRequest));
        // 获取封装类
        return ResultUtils.success(userQuestionRecordService.getUserQuestionRecordVOPage(userQuestionRecordPage, request));
    }

    /**
     * 分页获取当前登录用户创建的用户刷题记录表列表
     *
     * @param userQuestionRecordQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserQuestionRecordVO>> listMyUserQuestionRecordVOByPage(@RequestBody UserQuestionRecordQueryRequest userQuestionRecordQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(userQuestionRecordQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        userQuestionRecordQueryRequest.setUserId(loginUser.getId());
        long current = userQuestionRecordQueryRequest.getCurrent();
        long size = userQuestionRecordQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserQuestionRecord> userQuestionRecordPage = userQuestionRecordService.page(new Page<>(current, size),
                userQuestionRecordService.getQueryWrapper(userQuestionRecordQueryRequest));
        // 获取封装类
        return ResultUtils.success(userQuestionRecordService.getUserQuestionRecordVOPage(userQuestionRecordPage, request));
    }

    /**
     * 编辑用户刷题记录表（给用户使用）
     *
     * @param userQuestionRecordEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUserQuestionRecord(@RequestBody UserQuestionRecordEditRequest userQuestionRecordEditRequest, HttpServletRequest request) {
        if (userQuestionRecordEditRequest == null || userQuestionRecordEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        UserQuestionRecord userQuestionRecord = new UserQuestionRecord();
        BeanUtils.copyProperties(userQuestionRecordEditRequest, userQuestionRecord);
        // 数据校验
        userQuestionRecordService.validUserQuestionRecord(userQuestionRecord, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = userQuestionRecordEditRequest.getId();
        UserQuestionRecord oldUserQuestionRecord = userQuestionRecordService.getById(id);
        ThrowUtils.throwIf(oldUserQuestionRecord == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldUserQuestionRecord.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = userQuestionRecordService.updateById(userQuestionRecord);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
