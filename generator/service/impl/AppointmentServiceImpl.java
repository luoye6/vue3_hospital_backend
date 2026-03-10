package com.xiaobaitiao.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.constant.CommonConstant;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.mapper.AppointmentMapper;
import com.xiaobaitiao.springbootinit.model.dto.appointment.AppointmentQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.Appointment;
import com.xiaobaitiao.springbootinit.model.entity.AppointmentFavour;
import com.xiaobaitiao.springbootinit.model.entity.AppointmentThumb;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.AppointmentVO;
import com.xiaobaitiao.springbootinit.model.vo.UserVO;
import com.xiaobaitiao.springbootinit.service.AppointmentService;
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
 * 预约挂号表服务实现
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Service
@Slf4j
public class AppointmentServiceImpl extends ServiceImpl<AppointmentMapper, Appointment> implements AppointmentService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param appointment
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validAppointment(Appointment appointment, boolean add) {
        ThrowUtils.throwIf(appointment == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = appointment.getTitle();
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
     * @param appointmentQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Appointment> getQueryWrapper(AppointmentQueryRequest appointmentQueryRequest) {
        QueryWrapper<Appointment> queryWrapper = new QueryWrapper<>();
        if (appointmentQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = appointmentQueryRequest.getId();
        Long notId = appointmentQueryRequest.getNotId();
        String title = appointmentQueryRequest.getTitle();
        String content = appointmentQueryRequest.getContent();
        String searchText = appointmentQueryRequest.getSearchText();
        String sortField = appointmentQueryRequest.getSortField();
        String sortOrder = appointmentQueryRequest.getSortOrder();
        List<String> tagList = appointmentQueryRequest.getTags();
        Long userId = appointmentQueryRequest.getUserId();
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
     * 获取预约挂号表封装
     *
     * @param appointment
     * @param request
     * @return
     */
    @Override
    public AppointmentVO getAppointmentVO(Appointment appointment, HttpServletRequest request) {
        // 对象转封装类
        AppointmentVO appointmentVO = AppointmentVO.objToVo(appointment);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = appointment.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        appointmentVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long appointmentId = appointment.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<AppointmentThumb> appointmentThumbQueryWrapper = new QueryWrapper<>();
            appointmentThumbQueryWrapper.in("appointmentId", appointmentId);
            appointmentThumbQueryWrapper.eq("userId", loginUser.getId());
            AppointmentThumb appointmentThumb = appointmentThumbMapper.selectOne(appointmentThumbQueryWrapper);
            appointmentVO.setHasThumb(appointmentThumb != null);
            // 获取收藏
            QueryWrapper<AppointmentFavour> appointmentFavourQueryWrapper = new QueryWrapper<>();
            appointmentFavourQueryWrapper.in("appointmentId", appointmentId);
            appointmentFavourQueryWrapper.eq("userId", loginUser.getId());
            AppointmentFavour appointmentFavour = appointmentFavourMapper.selectOne(appointmentFavourQueryWrapper);
            appointmentVO.setHasFavour(appointmentFavour != null);
        }
        // endregion

        return appointmentVO;
    }

    /**
     * 分页获取预约挂号表封装
     *
     * @param appointmentPage
     * @param request
     * @return
     */
    @Override
    public Page<AppointmentVO> getAppointmentVOPage(Page<Appointment> appointmentPage, HttpServletRequest request) {
        List<Appointment> appointmentList = appointmentPage.getRecords();
        Page<AppointmentVO> appointmentVOPage = new Page<>(appointmentPage.getCurrent(), appointmentPage.getSize(), appointmentPage.getTotal());
        if (CollUtil.isEmpty(appointmentList)) {
            return appointmentVOPage;
        }
        // 对象列表 => 封装对象列表
        List<AppointmentVO> appointmentVOList = appointmentList.stream().map(appointment -> {
            return AppointmentVO.objToVo(appointment);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = appointmentList.stream().map(Appointment::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> appointmentIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> appointmentIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> appointmentIdSet = appointmentList.stream().map(Appointment::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<AppointmentThumb> appointmentThumbQueryWrapper = new QueryWrapper<>();
            appointmentThumbQueryWrapper.in("appointmentId", appointmentIdSet);
            appointmentThumbQueryWrapper.eq("userId", loginUser.getId());
            List<AppointmentThumb> appointmentAppointmentThumbList = appointmentThumbMapper.selectList(appointmentThumbQueryWrapper);
            appointmentAppointmentThumbList.forEach(appointmentAppointmentThumb -> appointmentIdHasThumbMap.put(appointmentAppointmentThumb.getAppointmentId(), true));
            // 获取收藏
            QueryWrapper<AppointmentFavour> appointmentFavourQueryWrapper = new QueryWrapper<>();
            appointmentFavourQueryWrapper.in("appointmentId", appointmentIdSet);
            appointmentFavourQueryWrapper.eq("userId", loginUser.getId());
            List<AppointmentFavour> appointmentFavourList = appointmentFavourMapper.selectList(appointmentFavourQueryWrapper);
            appointmentFavourList.forEach(appointmentFavour -> appointmentIdHasFavourMap.put(appointmentFavour.getAppointmentId(), true));
        }
        // 填充信息
        appointmentVOList.forEach(appointmentVO -> {
            Long userId = appointmentVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            appointmentVO.setUser(userService.getUserVO(user));
            appointmentVO.setHasThumb(appointmentIdHasThumbMap.getOrDefault(appointmentVO.getId(), false));
            appointmentVO.setHasFavour(appointmentIdHasFavourMap.getOrDefault(appointmentVO.getId(), false));
        });
        // endregion

        appointmentVOPage.setRecords(appointmentVOList);
        return appointmentVOPage;
    }

}
