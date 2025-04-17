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
import com.xiaobaitiao.springbootinit.model.dto.userQuestionFavorites.UserQuestionFavoritesAddRequest;
import com.xiaobaitiao.springbootinit.model.dto.userQuestionFavorites.UserQuestionFavoritesEditRequest;
import com.xiaobaitiao.springbootinit.model.dto.userQuestionFavorites.UserQuestionFavoritesQueryRequest;
import com.xiaobaitiao.springbootinit.model.dto.userQuestionFavorites.UserQuestionFavoritesUpdateRequest;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 用户题目收藏关联接口
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@RestController
@RequestMapping("/userQuestionFavorites")
@Slf4j
public class UserQuestionFavoritesController {

    @Resource
    private UserQuestionFavoritesService userQuestionFavoritesService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建用户题目收藏关联
     *
     * @param userQuestionFavoritesAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Long> addUserQuestionFavorites(@RequestBody UserQuestionFavoritesAddRequest userQuestionFavoritesAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userQuestionFavoritesAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        UserQuestionFavorites userQuestionFavorites = new UserQuestionFavorites();
        BeanUtils.copyProperties(userQuestionFavoritesAddRequest, userQuestionFavorites);
        // 数据校验
        userQuestionFavoritesService.validUserQuestionFavorites(userQuestionFavorites, true);
        // todo 填充默认值
        User loginUser = userService.getLoginUser(request);
        userQuestionFavorites.setUserId(loginUser.getId());
        // 写入数据库
        boolean result = userQuestionFavoritesService.save(userQuestionFavorites);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newUserQuestionFavoritesId = userQuestionFavorites.getId();
        return ResultUtils.success(newUserQuestionFavoritesId);
    }

    /**
     * 删除用户题目收藏关联
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUserQuestionFavorites(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        UserQuestionFavorites oldUserQuestionFavorites = userQuestionFavoritesService.getById(id);
        ThrowUtils.throwIf(oldUserQuestionFavorites == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldUserQuestionFavorites.getUserId().equals(user.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = userQuestionFavoritesService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新用户题目收藏关联（仅管理员可用）
     *
     * @param userQuestionFavoritesUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUserQuestionFavorites(@RequestBody UserQuestionFavoritesUpdateRequest userQuestionFavoritesUpdateRequest) {
        if (userQuestionFavoritesUpdateRequest == null || userQuestionFavoritesUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        UserQuestionFavorites userQuestionFavorites = new UserQuestionFavorites();
        BeanUtils.copyProperties(userQuestionFavoritesUpdateRequest, userQuestionFavorites);
        // 数据校验
        userQuestionFavoritesService.validUserQuestionFavorites(userQuestionFavorites, false);
        // 判断是否存在
        long id = userQuestionFavoritesUpdateRequest.getId();
        UserQuestionFavorites oldUserQuestionFavorites = userQuestionFavoritesService.getById(id);
        ThrowUtils.throwIf(oldUserQuestionFavorites == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = userQuestionFavoritesService.updateById(userQuestionFavorites);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户题目收藏关联（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserQuestionFavoritesVO> getUserQuestionFavoritesVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        UserQuestionFavorites userQuestionFavorites = userQuestionFavoritesService.getById(id);
        ThrowUtils.throwIf(userQuestionFavorites == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(userQuestionFavoritesService.getUserQuestionFavoritesVO(userQuestionFavorites, request));
    }

    /**
     * 分页获取用户题目收藏关联列表（仅管理员可用）
     *
     * @param userQuestionFavoritesQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserQuestionFavorites>> listUserQuestionFavoritesByPage(@RequestBody UserQuestionFavoritesQueryRequest userQuestionFavoritesQueryRequest) {
        long current = userQuestionFavoritesQueryRequest.getCurrent();
        long size = userQuestionFavoritesQueryRequest.getPageSize();
        // 查询数据库
        Page<UserQuestionFavorites> userQuestionFavoritesPage = userQuestionFavoritesService.page(new Page<>(current, size),
                userQuestionFavoritesService.getQueryWrapper(userQuestionFavoritesQueryRequest));
        return ResultUtils.success(userQuestionFavoritesPage);
    }

    /**
     * 分页获取用户题目收藏关联列表（封装类）
     *
     * @param userQuestionFavoritesQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserQuestionFavoritesVO>> listUserQuestionFavoritesVOByPage(@RequestBody UserQuestionFavoritesQueryRequest userQuestionFavoritesQueryRequest,
                                                               HttpServletRequest request) {
        long current = userQuestionFavoritesQueryRequest.getCurrent();
        long size = userQuestionFavoritesQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserQuestionFavorites> userQuestionFavoritesPage = userQuestionFavoritesService.page(new Page<>(current, size),
                userQuestionFavoritesService.getQueryWrapper(userQuestionFavoritesQueryRequest));
        // 获取封装类
        return ResultUtils.success(userQuestionFavoritesService.getUserQuestionFavoritesVOPage(userQuestionFavoritesPage, request));
    }

    /**
     * 分页获取当前登录用户创建的用户题目收藏关联列表
     *
     * @param userQuestionFavoritesQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<UserQuestionFavoritesVO>> listMyUserQuestionFavoritesVOByPage(@RequestBody UserQuestionFavoritesQueryRequest userQuestionFavoritesQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(userQuestionFavoritesQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        User loginUser = userService.getLoginUser(request);
        userQuestionFavoritesQueryRequest.setUserId(loginUser.getId());
        long current = userQuestionFavoritesQueryRequest.getCurrent();
        long size = userQuestionFavoritesQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<UserQuestionFavorites> userQuestionFavoritesPage = userQuestionFavoritesService.page(new Page<>(current, size),
                userQuestionFavoritesService.getQueryWrapper(userQuestionFavoritesQueryRequest));
        // 获取封装类
        return ResultUtils.success(userQuestionFavoritesService.getUserQuestionFavoritesVOPage(userQuestionFavoritesPage, request));
    }

    /**
     * 编辑用户题目收藏关联（给用户使用）
     *
     * @param userQuestionFavoritesEditRequest
     * @param request
     * @return
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editUserQuestionFavorites(@RequestBody UserQuestionFavoritesEditRequest userQuestionFavoritesEditRequest, HttpServletRequest request) {
        if (userQuestionFavoritesEditRequest == null || userQuestionFavoritesEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        UserQuestionFavorites userQuestionFavorites = new UserQuestionFavorites();
        BeanUtils.copyProperties(userQuestionFavoritesEditRequest, userQuestionFavorites);
        // 数据校验
        userQuestionFavoritesService.validUserQuestionFavorites(userQuestionFavorites, false);
        User loginUser = userService.getLoginUser(request);
        // 判断是否存在
        long id = userQuestionFavoritesEditRequest.getId();
        UserQuestionFavorites oldUserQuestionFavorites = userQuestionFavoritesService.getById(id);
        ThrowUtils.throwIf(oldUserQuestionFavorites == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可编辑
        if (!oldUserQuestionFavorites.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = userQuestionFavoritesService.updateById(userQuestionFavorites);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    // endregion
}
