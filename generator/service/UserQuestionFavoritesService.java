package com.xiaobaitiao.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaobaitiao.springbootinit.model.dto.userQuestionFavorites.UserQuestionFavoritesQueryRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户题目收藏关联服务
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
public interface UserQuestionFavoritesService extends IService<UserQuestionFavorites> {

    /**
     * 校验数据
     *
     * @param userQuestionFavorites
     * @param add 对创建的数据进行校验
     */
    void validUserQuestionFavorites(UserQuestionFavorites userQuestionFavorites, boolean add);

    /**
     * 获取查询条件
     *
     * @param userQuestionFavoritesQueryRequest
     * @return
     */
    QueryWrapper<UserQuestionFavorites> getQueryWrapper(UserQuestionFavoritesQueryRequest userQuestionFavoritesQueryRequest);
    
    /**
     * 获取用户题目收藏关联封装
     *
     * @param userQuestionFavorites
     * @param request
     * @return
     */
    UserQuestionFavoritesVO getUserQuestionFavoritesVO(UserQuestionFavorites userQuestionFavorites, HttpServletRequest request);

    /**
     * 分页获取用户题目收藏关联封装
     *
     * @param userQuestionFavoritesPage
     * @param request
     * @return
     */
    Page<UserQuestionFavoritesVO> getUserQuestionFavoritesVOPage(Page<UserQuestionFavorites> userQuestionFavoritesPage, HttpServletRequest request);
}
