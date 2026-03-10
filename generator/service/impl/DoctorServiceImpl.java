package com.xiaobaitiao.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.constant.CommonConstant;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.mapper.DoctorMapper;
import com.xiaobaitiao.springbootinit.model.dto.doctor.DoctorQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.Doctor;
import com.xiaobaitiao.springbootinit.model.entity.DoctorFavour;
import com.xiaobaitiao.springbootinit.model.entity.DoctorThumb;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.DoctorVO;
import com.xiaobaitiao.springbootinit.model.vo.UserVO;
import com.xiaobaitiao.springbootinit.service.DoctorService;
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
 * 医生表服务实现
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Service
@Slf4j
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param doctor
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validDoctor(Doctor doctor, boolean add) {
        ThrowUtils.throwIf(doctor == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = doctor.getTitle();
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
     * @param doctorQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Doctor> getQueryWrapper(DoctorQueryRequest doctorQueryRequest) {
        QueryWrapper<Doctor> queryWrapper = new QueryWrapper<>();
        if (doctorQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = doctorQueryRequest.getId();
        Long notId = doctorQueryRequest.getNotId();
        String title = doctorQueryRequest.getTitle();
        String content = doctorQueryRequest.getContent();
        String searchText = doctorQueryRequest.getSearchText();
        String sortField = doctorQueryRequest.getSortField();
        String sortOrder = doctorQueryRequest.getSortOrder();
        List<String> tagList = doctorQueryRequest.getTags();
        Long userId = doctorQueryRequest.getUserId();
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
     * 获取医生表封装
     *
     * @param doctor
     * @param request
     * @return
     */
    @Override
    public DoctorVO getDoctorVO(Doctor doctor, HttpServletRequest request) {
        // 对象转封装类
        DoctorVO doctorVO = DoctorVO.objToVo(doctor);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = doctor.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        doctorVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long doctorId = doctor.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<DoctorThumb> doctorThumbQueryWrapper = new QueryWrapper<>();
            doctorThumbQueryWrapper.in("doctorId", doctorId);
            doctorThumbQueryWrapper.eq("userId", loginUser.getId());
            DoctorThumb doctorThumb = doctorThumbMapper.selectOne(doctorThumbQueryWrapper);
            doctorVO.setHasThumb(doctorThumb != null);
            // 获取收藏
            QueryWrapper<DoctorFavour> doctorFavourQueryWrapper = new QueryWrapper<>();
            doctorFavourQueryWrapper.in("doctorId", doctorId);
            doctorFavourQueryWrapper.eq("userId", loginUser.getId());
            DoctorFavour doctorFavour = doctorFavourMapper.selectOne(doctorFavourQueryWrapper);
            doctorVO.setHasFavour(doctorFavour != null);
        }
        // endregion

        return doctorVO;
    }

    /**
     * 分页获取医生表封装
     *
     * @param doctorPage
     * @param request
     * @return
     */
    @Override
    public Page<DoctorVO> getDoctorVOPage(Page<Doctor> doctorPage, HttpServletRequest request) {
        List<Doctor> doctorList = doctorPage.getRecords();
        Page<DoctorVO> doctorVOPage = new Page<>(doctorPage.getCurrent(), doctorPage.getSize(), doctorPage.getTotal());
        if (CollUtil.isEmpty(doctorList)) {
            return doctorVOPage;
        }
        // 对象列表 => 封装对象列表
        List<DoctorVO> doctorVOList = doctorList.stream().map(doctor -> {
            return DoctorVO.objToVo(doctor);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = doctorList.stream().map(Doctor::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> doctorIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> doctorIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> doctorIdSet = doctorList.stream().map(Doctor::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<DoctorThumb> doctorThumbQueryWrapper = new QueryWrapper<>();
            doctorThumbQueryWrapper.in("doctorId", doctorIdSet);
            doctorThumbQueryWrapper.eq("userId", loginUser.getId());
            List<DoctorThumb> doctorDoctorThumbList = doctorThumbMapper.selectList(doctorThumbQueryWrapper);
            doctorDoctorThumbList.forEach(doctorDoctorThumb -> doctorIdHasThumbMap.put(doctorDoctorThumb.getDoctorId(), true));
            // 获取收藏
            QueryWrapper<DoctorFavour> doctorFavourQueryWrapper = new QueryWrapper<>();
            doctorFavourQueryWrapper.in("doctorId", doctorIdSet);
            doctorFavourQueryWrapper.eq("userId", loginUser.getId());
            List<DoctorFavour> doctorFavourList = doctorFavourMapper.selectList(doctorFavourQueryWrapper);
            doctorFavourList.forEach(doctorFavour -> doctorIdHasFavourMap.put(doctorFavour.getDoctorId(), true));
        }
        // 填充信息
        doctorVOList.forEach(doctorVO -> {
            Long userId = doctorVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            doctorVO.setUser(userService.getUserVO(user));
            doctorVO.setHasThumb(doctorIdHasThumbMap.getOrDefault(doctorVO.getId(), false));
            doctorVO.setHasFavour(doctorIdHasFavourMap.getOrDefault(doctorVO.getId(), false));
        });
        // endregion

        doctorVOPage.setRecords(doctorVOList);
        return doctorVOPage;
    }

}
