package com.xiaobaitiao.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.constant.CommonConstant;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.mapper.DoctorScheduleMapper;
import com.xiaobaitiao.springbootinit.model.dto.doctorSchedule.DoctorScheduleQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.DoctorSchedule;
import com.xiaobaitiao.springbootinit.model.entity.DoctorScheduleFavour;
import com.xiaobaitiao.springbootinit.model.entity.DoctorScheduleThumb;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.DoctorScheduleVO;
import com.xiaobaitiao.springbootinit.model.vo.UserVO;
import com.xiaobaitiao.springbootinit.service.DoctorScheduleService;
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
 * 医生排班表服务实现
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Service
@Slf4j
public class DoctorScheduleServiceImpl extends ServiceImpl<DoctorScheduleMapper, DoctorSchedule> implements DoctorScheduleService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param doctorSchedule
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validDoctorSchedule(DoctorSchedule doctorSchedule, boolean add) {
        ThrowUtils.throwIf(doctorSchedule == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = doctorSchedule.getTitle();
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
     * @param doctorScheduleQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<DoctorSchedule> getQueryWrapper(DoctorScheduleQueryRequest doctorScheduleQueryRequest) {
        QueryWrapper<DoctorSchedule> queryWrapper = new QueryWrapper<>();
        if (doctorScheduleQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = doctorScheduleQueryRequest.getId();
        Long notId = doctorScheduleQueryRequest.getNotId();
        String title = doctorScheduleQueryRequest.getTitle();
        String content = doctorScheduleQueryRequest.getContent();
        String searchText = doctorScheduleQueryRequest.getSearchText();
        String sortField = doctorScheduleQueryRequest.getSortField();
        String sortOrder = doctorScheduleQueryRequest.getSortOrder();
        List<String> tagList = doctorScheduleQueryRequest.getTags();
        Long userId = doctorScheduleQueryRequest.getUserId();
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
     * 获取医生排班表封装
     *
     * @param doctorSchedule
     * @param request
     * @return
     */
    @Override
    public DoctorScheduleVO getDoctorScheduleVO(DoctorSchedule doctorSchedule, HttpServletRequest request) {
        // 对象转封装类
        DoctorScheduleVO doctorScheduleVO = DoctorScheduleVO.objToVo(doctorSchedule);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = doctorSchedule.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        doctorScheduleVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long doctorScheduleId = doctorSchedule.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<DoctorScheduleThumb> doctorScheduleThumbQueryWrapper = new QueryWrapper<>();
            doctorScheduleThumbQueryWrapper.in("doctorScheduleId", doctorScheduleId);
            doctorScheduleThumbQueryWrapper.eq("userId", loginUser.getId());
            DoctorScheduleThumb doctorScheduleThumb = doctorScheduleThumbMapper.selectOne(doctorScheduleThumbQueryWrapper);
            doctorScheduleVO.setHasThumb(doctorScheduleThumb != null);
            // 获取收藏
            QueryWrapper<DoctorScheduleFavour> doctorScheduleFavourQueryWrapper = new QueryWrapper<>();
            doctorScheduleFavourQueryWrapper.in("doctorScheduleId", doctorScheduleId);
            doctorScheduleFavourQueryWrapper.eq("userId", loginUser.getId());
            DoctorScheduleFavour doctorScheduleFavour = doctorScheduleFavourMapper.selectOne(doctorScheduleFavourQueryWrapper);
            doctorScheduleVO.setHasFavour(doctorScheduleFavour != null);
        }
        // endregion

        return doctorScheduleVO;
    }

    /**
     * 分页获取医生排班表封装
     *
     * @param doctorSchedulePage
     * @param request
     * @return
     */
    @Override
    public Page<DoctorScheduleVO> getDoctorScheduleVOPage(Page<DoctorSchedule> doctorSchedulePage, HttpServletRequest request) {
        List<DoctorSchedule> doctorScheduleList = doctorSchedulePage.getRecords();
        Page<DoctorScheduleVO> doctorScheduleVOPage = new Page<>(doctorSchedulePage.getCurrent(), doctorSchedulePage.getSize(), doctorSchedulePage.getTotal());
        if (CollUtil.isEmpty(doctorScheduleList)) {
            return doctorScheduleVOPage;
        }
        // 对象列表 => 封装对象列表
        List<DoctorScheduleVO> doctorScheduleVOList = doctorScheduleList.stream().map(doctorSchedule -> {
            return DoctorScheduleVO.objToVo(doctorSchedule);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = doctorScheduleList.stream().map(DoctorSchedule::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> doctorScheduleIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> doctorScheduleIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> doctorScheduleIdSet = doctorScheduleList.stream().map(DoctorSchedule::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<DoctorScheduleThumb> doctorScheduleThumbQueryWrapper = new QueryWrapper<>();
            doctorScheduleThumbQueryWrapper.in("doctorScheduleId", doctorScheduleIdSet);
            doctorScheduleThumbQueryWrapper.eq("userId", loginUser.getId());
            List<DoctorScheduleThumb> doctorScheduleDoctorScheduleThumbList = doctorScheduleThumbMapper.selectList(doctorScheduleThumbQueryWrapper);
            doctorScheduleDoctorScheduleThumbList.forEach(doctorScheduleDoctorScheduleThumb -> doctorScheduleIdHasThumbMap.put(doctorScheduleDoctorScheduleThumb.getDoctorScheduleId(), true));
            // 获取收藏
            QueryWrapper<DoctorScheduleFavour> doctorScheduleFavourQueryWrapper = new QueryWrapper<>();
            doctorScheduleFavourQueryWrapper.in("doctorScheduleId", doctorScheduleIdSet);
            doctorScheduleFavourQueryWrapper.eq("userId", loginUser.getId());
            List<DoctorScheduleFavour> doctorScheduleFavourList = doctorScheduleFavourMapper.selectList(doctorScheduleFavourQueryWrapper);
            doctorScheduleFavourList.forEach(doctorScheduleFavour -> doctorScheduleIdHasFavourMap.put(doctorScheduleFavour.getDoctorScheduleId(), true));
        }
        // 填充信息
        doctorScheduleVOList.forEach(doctorScheduleVO -> {
            Long userId = doctorScheduleVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            doctorScheduleVO.setUser(userService.getUserVO(user));
            doctorScheduleVO.setHasThumb(doctorScheduleIdHasThumbMap.getOrDefault(doctorScheduleVO.getId(), false));
            doctorScheduleVO.setHasFavour(doctorScheduleIdHasFavourMap.getOrDefault(doctorScheduleVO.getId(), false));
        });
        // endregion

        doctorScheduleVOPage.setRecords(doctorScheduleVOList);
        return doctorScheduleVOPage;
    }

}
