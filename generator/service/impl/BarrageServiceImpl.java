package com.xiaobaitiao.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.constant.CommonConstant;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.mapper.BarrageMapper;
import com.xiaobaitiao.springbootinit.model.dto.barrage.BarrageQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.Barrage;
import com.xiaobaitiao.springbootinit.model.entity.BarrageFavour;
import com.xiaobaitiao.springbootinit.model.entity.BarrageThumb;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.BarrageVO;
import com.xiaobaitiao.springbootinit.model.vo.UserVO;
import com.xiaobaitiao.springbootinit.service.BarrageService;
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
 * 弹幕服务实现
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Service
@Slf4j
public class BarrageServiceImpl extends ServiceImpl<BarrageMapper, Barrage> implements BarrageService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param barrage
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validBarrage(Barrage barrage, boolean add) {
        ThrowUtils.throwIf(barrage == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = barrage.getTitle();
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
     * @param barrageQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Barrage> getQueryWrapper(BarrageQueryRequest barrageQueryRequest) {
        QueryWrapper<Barrage> queryWrapper = new QueryWrapper<>();
        if (barrageQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = barrageQueryRequest.getId();
        Long notId = barrageQueryRequest.getNotId();
        String title = barrageQueryRequest.getTitle();
        String content = barrageQueryRequest.getContent();
        String searchText = barrageQueryRequest.getSearchText();
        String sortField = barrageQueryRequest.getSortField();
        String sortOrder = barrageQueryRequest.getSortOrder();
        List<String> tagList = barrageQueryRequest.getTags();
        Long userId = barrageQueryRequest.getUserId();
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
     * 获取弹幕封装
     *
     * @param barrage
     * @param request
     * @return
     */
    @Override
    public BarrageVO getBarrageVO(Barrage barrage, HttpServletRequest request) {
        // 对象转封装类
        BarrageVO barrageVO = BarrageVO.objToVo(barrage);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = barrage.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        barrageVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long barrageId = barrage.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<BarrageThumb> barrageThumbQueryWrapper = new QueryWrapper<>();
            barrageThumbQueryWrapper.in("barrageId", barrageId);
            barrageThumbQueryWrapper.eq("userId", loginUser.getId());
            BarrageThumb barrageThumb = barrageThumbMapper.selectOne(barrageThumbQueryWrapper);
            barrageVO.setHasThumb(barrageThumb != null);
            // 获取收藏
            QueryWrapper<BarrageFavour> barrageFavourQueryWrapper = new QueryWrapper<>();
            barrageFavourQueryWrapper.in("barrageId", barrageId);
            barrageFavourQueryWrapper.eq("userId", loginUser.getId());
            BarrageFavour barrageFavour = barrageFavourMapper.selectOne(barrageFavourQueryWrapper);
            barrageVO.setHasFavour(barrageFavour != null);
        }
        // endregion

        return barrageVO;
    }

    /**
     * 分页获取弹幕封装
     *
     * @param barragePage
     * @param request
     * @return
     */
    @Override
    public Page<BarrageVO> getBarrageVOPage(Page<Barrage> barragePage, HttpServletRequest request) {
        List<Barrage> barrageList = barragePage.getRecords();
        Page<BarrageVO> barrageVOPage = new Page<>(barragePage.getCurrent(), barragePage.getSize(), barragePage.getTotal());
        if (CollUtil.isEmpty(barrageList)) {
            return barrageVOPage;
        }
        // 对象列表 => 封装对象列表
        List<BarrageVO> barrageVOList = barrageList.stream().map(barrage -> {
            return BarrageVO.objToVo(barrage);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = barrageList.stream().map(Barrage::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> barrageIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> barrageIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> barrageIdSet = barrageList.stream().map(Barrage::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<BarrageThumb> barrageThumbQueryWrapper = new QueryWrapper<>();
            barrageThumbQueryWrapper.in("barrageId", barrageIdSet);
            barrageThumbQueryWrapper.eq("userId", loginUser.getId());
            List<BarrageThumb> barrageBarrageThumbList = barrageThumbMapper.selectList(barrageThumbQueryWrapper);
            barrageBarrageThumbList.forEach(barrageBarrageThumb -> barrageIdHasThumbMap.put(barrageBarrageThumb.getBarrageId(), true));
            // 获取收藏
            QueryWrapper<BarrageFavour> barrageFavourQueryWrapper = new QueryWrapper<>();
            barrageFavourQueryWrapper.in("barrageId", barrageIdSet);
            barrageFavourQueryWrapper.eq("userId", loginUser.getId());
            List<BarrageFavour> barrageFavourList = barrageFavourMapper.selectList(barrageFavourQueryWrapper);
            barrageFavourList.forEach(barrageFavour -> barrageIdHasFavourMap.put(barrageFavour.getBarrageId(), true));
        }
        // 填充信息
        barrageVOList.forEach(barrageVO -> {
            Long userId = barrageVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            barrageVO.setUser(userService.getUserVO(user));
            barrageVO.setHasThumb(barrageIdHasThumbMap.getOrDefault(barrageVO.getId(), false));
            barrageVO.setHasFavour(barrageIdHasFavourMap.getOrDefault(barrageVO.getId(), false));
        });
        // endregion

        barrageVOPage.setRecords(barrageVOList);
        return barrageVOPage;
    }

}
