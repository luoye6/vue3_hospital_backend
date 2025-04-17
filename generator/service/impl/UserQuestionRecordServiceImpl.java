package com.xiaobaitiao.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.constant.CommonConstant;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.model.dto.userQuestionRecord.UserQuestionRecordQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.UserQuestionRecordFavour;
import com.xiaobaitiao.springbootinit.model.entity.UserQuestionRecordThumb;
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
 * 用户刷题记录表服务实现
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Service
@Slf4j
public class UserQuestionRecordServiceImpl extends ServiceImpl<UserQuestionRecordMapper, UserQuestionRecord> implements UserQuestionRecordService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param userQuestionRecord
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validUserQuestionRecord(UserQuestionRecord userQuestionRecord, boolean add) {
        ThrowUtils.throwIf(userQuestionRecord == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = userQuestionRecord.getTitle();
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
     * @param userQuestionRecordQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<UserQuestionRecord> getQueryWrapper(UserQuestionRecordQueryRequest userQuestionRecordQueryRequest) {
        QueryWrapper<UserQuestionRecord> queryWrapper = new QueryWrapper<>();
        if (userQuestionRecordQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = userQuestionRecordQueryRequest.getId();
        Long notId = userQuestionRecordQueryRequest.getNotId();
        String title = userQuestionRecordQueryRequest.getTitle();
        String content = userQuestionRecordQueryRequest.getContent();
        String searchText = userQuestionRecordQueryRequest.getSearchText();
        String sortField = userQuestionRecordQueryRequest.getSortField();
        String sortOrder = userQuestionRecordQueryRequest.getSortOrder();
        List<String> tagList = userQuestionRecordQueryRequest.getTags();
        Long userId = userQuestionRecordQueryRequest.getUserId();
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
     * 获取用户刷题记录表封装
     *
     * @param userQuestionRecord
     * @param request
     * @return
     */
    @Override
    public UserQuestionRecordVO getUserQuestionRecordVO(UserQuestionRecord userQuestionRecord, HttpServletRequest request) {
        // 对象转封装类
        UserQuestionRecordVO userQuestionRecordVO = UserQuestionRecordVO.objToVo(userQuestionRecord);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = userQuestionRecord.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        userQuestionRecordVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long userQuestionRecordId = userQuestionRecord.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<UserQuestionRecordThumb> userQuestionRecordThumbQueryWrapper = new QueryWrapper<>();
            userQuestionRecordThumbQueryWrapper.in("userQuestionRecordId", userQuestionRecordId);
            userQuestionRecordThumbQueryWrapper.eq("userId", loginUser.getId());
            UserQuestionRecordThumb userQuestionRecordThumb = userQuestionRecordThumbMapper.selectOne(userQuestionRecordThumbQueryWrapper);
            userQuestionRecordVO.setHasThumb(userQuestionRecordThumb != null);
            // 获取收藏
            QueryWrapper<UserQuestionRecordFavour> userQuestionRecordFavourQueryWrapper = new QueryWrapper<>();
            userQuestionRecordFavourQueryWrapper.in("userQuestionRecordId", userQuestionRecordId);
            userQuestionRecordFavourQueryWrapper.eq("userId", loginUser.getId());
            UserQuestionRecordFavour userQuestionRecordFavour = userQuestionRecordFavourMapper.selectOne(userQuestionRecordFavourQueryWrapper);
            userQuestionRecordVO.setHasFavour(userQuestionRecordFavour != null);
        }
        // endregion

        return userQuestionRecordVO;
    }

    /**
     * 分页获取用户刷题记录表封装
     *
     * @param userQuestionRecordPage
     * @param request
     * @return
     */
    @Override
    public Page<UserQuestionRecordVO> getUserQuestionRecordVOPage(Page<UserQuestionRecord> userQuestionRecordPage, HttpServletRequest request) {
        List<UserQuestionRecord> userQuestionRecordList = userQuestionRecordPage.getRecords();
        Page<UserQuestionRecordVO> userQuestionRecordVOPage = new Page<>(userQuestionRecordPage.getCurrent(), userQuestionRecordPage.getSize(), userQuestionRecordPage.getTotal());
        if (CollUtil.isEmpty(userQuestionRecordList)) {
            return userQuestionRecordVOPage;
        }
        // 对象列表 => 封装对象列表
        List<UserQuestionRecordVO> userQuestionRecordVOList = userQuestionRecordList.stream().map(userQuestionRecord -> {
            return UserQuestionRecordVO.objToVo(userQuestionRecord);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = userQuestionRecordList.stream().map(UserQuestionRecord::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> userQuestionRecordIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> userQuestionRecordIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> userQuestionRecordIdSet = userQuestionRecordList.stream().map(UserQuestionRecord::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<UserQuestionRecordThumb> userQuestionRecordThumbQueryWrapper = new QueryWrapper<>();
            userQuestionRecordThumbQueryWrapper.in("userQuestionRecordId", userQuestionRecordIdSet);
            userQuestionRecordThumbQueryWrapper.eq("userId", loginUser.getId());
            List<UserQuestionRecordThumb> userQuestionRecordUserQuestionRecordThumbList = userQuestionRecordThumbMapper.selectList(userQuestionRecordThumbQueryWrapper);
            userQuestionRecordUserQuestionRecordThumbList.forEach(userQuestionRecordUserQuestionRecordThumb -> userQuestionRecordIdHasThumbMap.put(userQuestionRecordUserQuestionRecordThumb.getUserQuestionRecordId(), true));
            // 获取收藏
            QueryWrapper<UserQuestionRecordFavour> userQuestionRecordFavourQueryWrapper = new QueryWrapper<>();
            userQuestionRecordFavourQueryWrapper.in("userQuestionRecordId", userQuestionRecordIdSet);
            userQuestionRecordFavourQueryWrapper.eq("userId", loginUser.getId());
            List<UserQuestionRecordFavour> userQuestionRecordFavourList = userQuestionRecordFavourMapper.selectList(userQuestionRecordFavourQueryWrapper);
            userQuestionRecordFavourList.forEach(userQuestionRecordFavour -> userQuestionRecordIdHasFavourMap.put(userQuestionRecordFavour.getUserQuestionRecordId(), true));
        }
        // 填充信息
        userQuestionRecordVOList.forEach(userQuestionRecordVO -> {
            Long userId = userQuestionRecordVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            userQuestionRecordVO.setUser(userService.getUserVO(user));
            userQuestionRecordVO.setHasThumb(userQuestionRecordIdHasThumbMap.getOrDefault(userQuestionRecordVO.getId(), false));
            userQuestionRecordVO.setHasFavour(userQuestionRecordIdHasFavourMap.getOrDefault(userQuestionRecordVO.getId(), false));
        });
        // endregion

        userQuestionRecordVOPage.setRecords(userQuestionRecordVOList);
        return userQuestionRecordVOPage;
    }

}
