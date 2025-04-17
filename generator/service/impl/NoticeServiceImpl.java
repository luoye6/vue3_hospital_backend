package com.xiaobaitiao.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.constant.CommonConstant;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.mapper.NoticeMapper;
import com.xiaobaitiao.springbootinit.model.dto.notice.NoticeQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.Notice;
import com.xiaobaitiao.springbootinit.model.entity.NoticeFavour;
import com.xiaobaitiao.springbootinit.model.entity.NoticeThumb;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.NoticeVO;
import com.xiaobaitiao.springbootinit.model.vo.UserVO;
import com.xiaobaitiao.springbootinit.service.NoticeService;
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
 * 公告服务实现
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Service
@Slf4j
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param notice
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validNotice(Notice notice, boolean add) {
        ThrowUtils.throwIf(notice == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = notice.getTitle();
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
     * @param noticeQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Notice> getQueryWrapper(NoticeQueryRequest noticeQueryRequest) {
        QueryWrapper<Notice> queryWrapper = new QueryWrapper<>();
        if (noticeQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = noticeQueryRequest.getId();
        Long notId = noticeQueryRequest.getNotId();
        String title = noticeQueryRequest.getTitle();
        String content = noticeQueryRequest.getContent();
        String searchText = noticeQueryRequest.getSearchText();
        String sortField = noticeQueryRequest.getSortField();
        String sortOrder = noticeQueryRequest.getSortOrder();
        List<String> tagList = noticeQueryRequest.getTags();
        Long userId = noticeQueryRequest.getUserId();
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
     * 获取公告封装
     *
     * @param notice
     * @param request
     * @return
     */
    @Override
    public NoticeVO getNoticeVO(Notice notice, HttpServletRequest request) {
        // 对象转封装类
        NoticeVO noticeVO = NoticeVO.objToVo(notice);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = notice.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        noticeVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long noticeId = notice.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<NoticeThumb> noticeThumbQueryWrapper = new QueryWrapper<>();
            noticeThumbQueryWrapper.in("noticeId", noticeId);
            noticeThumbQueryWrapper.eq("userId", loginUser.getId());
            NoticeThumb noticeThumb = noticeThumbMapper.selectOne(noticeThumbQueryWrapper);
            noticeVO.setHasThumb(noticeThumb != null);
            // 获取收藏
            QueryWrapper<NoticeFavour> noticeFavourQueryWrapper = new QueryWrapper<>();
            noticeFavourQueryWrapper.in("noticeId", noticeId);
            noticeFavourQueryWrapper.eq("userId", loginUser.getId());
            NoticeFavour noticeFavour = noticeFavourMapper.selectOne(noticeFavourQueryWrapper);
            noticeVO.setHasFavour(noticeFavour != null);
        }
        // endregion

        return noticeVO;
    }

    /**
     * 分页获取公告封装
     *
     * @param noticePage
     * @param request
     * @return
     */
    @Override
    public Page<NoticeVO> getNoticeVOPage(Page<Notice> noticePage, HttpServletRequest request) {
        List<Notice> noticeList = noticePage.getRecords();
        Page<NoticeVO> noticeVOPage = new Page<>(noticePage.getCurrent(), noticePage.getSize(), noticePage.getTotal());
        if (CollUtil.isEmpty(noticeList)) {
            return noticeVOPage;
        }
        // 对象列表 => 封装对象列表
        List<NoticeVO> noticeVOList = noticeList.stream().map(notice -> {
            return NoticeVO.objToVo(notice);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = noticeList.stream().map(Notice::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> noticeIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> noticeIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> noticeIdSet = noticeList.stream().map(Notice::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<NoticeThumb> noticeThumbQueryWrapper = new QueryWrapper<>();
            noticeThumbQueryWrapper.in("noticeId", noticeIdSet);
            noticeThumbQueryWrapper.eq("userId", loginUser.getId());
            List<NoticeThumb> noticeNoticeThumbList = noticeThumbMapper.selectList(noticeThumbQueryWrapper);
            noticeNoticeThumbList.forEach(noticeNoticeThumb -> noticeIdHasThumbMap.put(noticeNoticeThumb.getNoticeId(), true));
            // 获取收藏
            QueryWrapper<NoticeFavour> noticeFavourQueryWrapper = new QueryWrapper<>();
            noticeFavourQueryWrapper.in("noticeId", noticeIdSet);
            noticeFavourQueryWrapper.eq("userId", loginUser.getId());
            List<NoticeFavour> noticeFavourList = noticeFavourMapper.selectList(noticeFavourQueryWrapper);
            noticeFavourList.forEach(noticeFavour -> noticeIdHasFavourMap.put(noticeFavour.getNoticeId(), true));
        }
        // 填充信息
        noticeVOList.forEach(noticeVO -> {
            Long userId = noticeVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            noticeVO.setUser(userService.getUserVO(user));
            noticeVO.setHasThumb(noticeIdHasThumbMap.getOrDefault(noticeVO.getId(), false));
            noticeVO.setHasFavour(noticeIdHasFavourMap.getOrDefault(noticeVO.getId(), false));
        });
        // endregion

        noticeVOPage.setRecords(noticeVOList);
        return noticeVOPage;
    }

}
