package com.xiaobaitiao.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.constant.CommonConstant;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.model.dto.userQuestionFavorites.UserQuestionFavoritesQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.UserQuestionFavoritesFavour;
import com.xiaobaitiao.springbootinit.model.entity.UserQuestionFavoritesThumb;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.UserVO;
import com.xiaobaitiao.springbootinit.service.UserService;
import com.xiaobaitiao.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户题目收藏关联服务实现
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Service
@Slf4j
public class UserQuestionFavoritesServiceImpl extends ServiceImpl<UserQuestionFavoritesMapper, UserQuestionFavorites> implements UserQuestionFavoritesService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param userQuestionFavorites
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validUserQuestionFavorites(UserQuestionFavorites userQuestionFavorites, boolean add) {
        ThrowUtils.throwIf(userQuestionFavorites == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = userQuestionFavorites.getTitle();
        // 创建数据时，参数不能为空
        if (add) {
            // todo 补充校验规则
            ThrowUtils.throwIf(StringUtils.isBlank(title), ErrorCode.PARAMS_ERROR);
        }
        // 修改数据时，有参数则校验
        // todo 补充校验规则
        if (StringUtils.isNotBlank(title)) {
            ThrowUtils.throwIf(title.length() > 80, ErrorCode.PARAMS_ERROR, "标题过长");
        }
    }

    /**
     * 获取查询条件
     *
     * @param userQuestionFavoritesQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<UserQuestionFavorites> getQueryWrapper(UserQuestionFavoritesQueryRequest userQuestionFavoritesQueryRequest) {
        QueryWrapper<UserQuestionFavorites> queryWrapper = new QueryWrapper<>();
        if (userQuestionFavoritesQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = userQuestionFavoritesQueryRequest.getId();
        Long notId = userQuestionFavoritesQueryRequest.getNotId();
        String title = userQuestionFavoritesQueryRequest.getTitle();
        String content = userQuestionFavoritesQueryRequest.getContent();
        String searchText = userQuestionFavoritesQueryRequest.getSearchText();
        String sortField = userQuestionFavoritesQueryRequest.getSortField();
        String sortOrder = userQuestionFavoritesQueryRequest.getSortOrder();
        List<String> tagList = userQuestionFavoritesQueryRequest.getTags();
        Long userId = userQuestionFavoritesQueryRequest.getUserId();
        // todo 补充需要的查询条件
        // 从多字段中搜索
        if (StringUtils.isNotBlank(searchText)) {
            // 需要拼接查询条件
            queryWrapper.and(qw -> qw.like("title", searchText).or().like("content", searchText));
        }
        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        // JSON 数组查询
        if (CollUtil.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        // 精确查询
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        // 排序规则
        queryWrapper.orderBy(SqlUtils.validSortField(sortField),
                sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    /**
     * 获取用户题目收藏关联封装
     *
     * @param userQuestionFavorites
     * @param request
     * @return
     */
    @Override
    public UserQuestionFavoritesVO getUserQuestionFavoritesVO(UserQuestionFavorites userQuestionFavorites, HttpServletRequest request) {
        // 对象转封装类
        UserQuestionFavoritesVO userQuestionFavoritesVO = UserQuestionFavoritesVO.objToVo(userQuestionFavorites);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = userQuestionFavorites.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        userQuestionFavoritesVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long userQuestionFavoritesId = userQuestionFavorites.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<UserQuestionFavoritesThumb> userQuestionFavoritesThumbQueryWrapper = new QueryWrapper<>();
            userQuestionFavoritesThumbQueryWrapper.in("userQuestionFavoritesId", userQuestionFavoritesId);
            userQuestionFavoritesThumbQueryWrapper.eq("userId", loginUser.getId());
            UserQuestionFavoritesThumb userQuestionFavoritesThumb = userQuestionFavoritesThumbMapper.selectOne(userQuestionFavoritesThumbQueryWrapper);
            userQuestionFavoritesVO.setHasThumb(userQuestionFavoritesThumb != null);
            // 获取收藏
            QueryWrapper<UserQuestionFavoritesFavour> userQuestionFavoritesFavourQueryWrapper = new QueryWrapper<>();
            userQuestionFavoritesFavourQueryWrapper.in("userQuestionFavoritesId", userQuestionFavoritesId);
            userQuestionFavoritesFavourQueryWrapper.eq("userId", loginUser.getId());
            UserQuestionFavoritesFavour userQuestionFavoritesFavour = userQuestionFavoritesFavourMapper.selectOne(userQuestionFavoritesFavourQueryWrapper);
            userQuestionFavoritesVO.setHasFavour(userQuestionFavoritesFavour != null);
        }
        // endregion

        return userQuestionFavoritesVO;
    }

    /**
     * 分页获取用户题目收藏关联封装
     *
     * @param userQuestionFavoritesPage
     * @param request
     * @return
     */
    @Override
    public Page<UserQuestionFavoritesVO> getUserQuestionFavoritesVOPage(Page<UserQuestionFavorites> userQuestionFavoritesPage, HttpServletRequest request) {
        List<UserQuestionFavorites> userQuestionFavoritesList = userQuestionFavoritesPage.getRecords();
        Page<UserQuestionFavoritesVO> userQuestionFavoritesVOPage = new Page<>(userQuestionFavoritesPage.getCurrent(), userQuestionFavoritesPage.getSize(), userQuestionFavoritesPage.getTotal());
        if (CollUtil.isEmpty(userQuestionFavoritesList)) {
            return userQuestionFavoritesVOPage;
        }
        // 对象列表 => 封装对象列表
        List<UserQuestionFavoritesVO> userQuestionFavoritesVOList = userQuestionFavoritesList.stream().map(userQuestionFavorites -> {
            return UserQuestionFavoritesVO.objToVo(userQuestionFavorites);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = userQuestionFavoritesList.stream().map(UserQuestionFavorites::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> userQuestionFavoritesIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> userQuestionFavoritesIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> userQuestionFavoritesIdSet = userQuestionFavoritesList.stream().map(UserQuestionFavorites::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<UserQuestionFavoritesThumb> userQuestionFavoritesThumbQueryWrapper = new QueryWrapper<>();
            userQuestionFavoritesThumbQueryWrapper.in("userQuestionFavoritesId", userQuestionFavoritesIdSet);
            userQuestionFavoritesThumbQueryWrapper.eq("userId", loginUser.getId());
            List<UserQuestionFavoritesThumb> userQuestionFavoritesUserQuestionFavoritesThumbList = userQuestionFavoritesThumbMapper.selectList(userQuestionFavoritesThumbQueryWrapper);
            userQuestionFavoritesUserQuestionFavoritesThumbList.forEach(userQuestionFavoritesUserQuestionFavoritesThumb -> userQuestionFavoritesIdHasThumbMap.put(userQuestionFavoritesUserQuestionFavoritesThumb.getUserQuestionFavoritesId(), true));
            // 获取收藏
            QueryWrapper<UserQuestionFavoritesFavour> userQuestionFavoritesFavourQueryWrapper = new QueryWrapper<>();
            userQuestionFavoritesFavourQueryWrapper.in("userQuestionFavoritesId", userQuestionFavoritesIdSet);
            userQuestionFavoritesFavourQueryWrapper.eq("userId", loginUser.getId());
            List<UserQuestionFavoritesFavour> userQuestionFavoritesFavourList = userQuestionFavoritesFavourMapper.selectList(userQuestionFavoritesFavourQueryWrapper);
            userQuestionFavoritesFavourList.forEach(userQuestionFavoritesFavour -> userQuestionFavoritesIdHasFavourMap.put(userQuestionFavoritesFavour.getUserQuestionFavoritesId(), true));
        }
        // 填充信息
        userQuestionFavoritesVOList.forEach(userQuestionFavoritesVO -> {
            Long userId = userQuestionFavoritesVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            userQuestionFavoritesVO.setUser(userService.getUserVO(user));
            userQuestionFavoritesVO.setHasThumb(userQuestionFavoritesIdHasThumbMap.getOrDefault(userQuestionFavoritesVO.getId(), false));
            userQuestionFavoritesVO.setHasFavour(userQuestionFavoritesIdHasFavourMap.getOrDefault(userQuestionFavoritesVO.getId(), false));
        });
        // endregion

        userQuestionFavoritesVOPage.setRecords(userQuestionFavoritesVOList);
        return userQuestionFavoritesVOPage;
    }

}
