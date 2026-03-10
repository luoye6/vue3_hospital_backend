package com.xiaobaitiao.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.constant.CommonConstant;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.mapper.PrescriptionMapper;
import com.xiaobaitiao.springbootinit.model.dto.prescription.PrescriptionQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.Prescription;
import com.xiaobaitiao.springbootinit.model.entity.PrescriptionFavour;
import com.xiaobaitiao.springbootinit.model.entity.PrescriptionThumb;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.PrescriptionVO;
import com.xiaobaitiao.springbootinit.model.vo.UserVO;
import com.xiaobaitiao.springbootinit.service.PrescriptionService;
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
 * 处方表服务实现
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Service
@Slf4j
public class PrescriptionServiceImpl extends ServiceImpl<PrescriptionMapper, Prescription> implements PrescriptionService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param prescription
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validPrescription(Prescription prescription, boolean add) {
        ThrowUtils.throwIf(prescription == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = prescription.getTitle();
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
     * @param prescriptionQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Prescription> getQueryWrapper(PrescriptionQueryRequest prescriptionQueryRequest) {
        QueryWrapper<Prescription> queryWrapper = new QueryWrapper<>();
        if (prescriptionQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = prescriptionQueryRequest.getId();
        Long notId = prescriptionQueryRequest.getNotId();
        String title = prescriptionQueryRequest.getTitle();
        String content = prescriptionQueryRequest.getContent();
        String searchText = prescriptionQueryRequest.getSearchText();
        String sortField = prescriptionQueryRequest.getSortField();
        String sortOrder = prescriptionQueryRequest.getSortOrder();
        List<String> tagList = prescriptionQueryRequest.getTags();
        Long userId = prescriptionQueryRequest.getUserId();
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
     * 获取处方表封装
     *
     * @param prescription
     * @param request
     * @return
     */
    @Override
    public PrescriptionVO getPrescriptionVO(Prescription prescription, HttpServletRequest request) {
        // 对象转封装类
        PrescriptionVO prescriptionVO = PrescriptionVO.objToVo(prescription);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = prescription.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        prescriptionVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long prescriptionId = prescription.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<PrescriptionThumb> prescriptionThumbQueryWrapper = new QueryWrapper<>();
            prescriptionThumbQueryWrapper.in("prescriptionId", prescriptionId);
            prescriptionThumbQueryWrapper.eq("userId", loginUser.getId());
            PrescriptionThumb prescriptionThumb = prescriptionThumbMapper.selectOne(prescriptionThumbQueryWrapper);
            prescriptionVO.setHasThumb(prescriptionThumb != null);
            // 获取收藏
            QueryWrapper<PrescriptionFavour> prescriptionFavourQueryWrapper = new QueryWrapper<>();
            prescriptionFavourQueryWrapper.in("prescriptionId", prescriptionId);
            prescriptionFavourQueryWrapper.eq("userId", loginUser.getId());
            PrescriptionFavour prescriptionFavour = prescriptionFavourMapper.selectOne(prescriptionFavourQueryWrapper);
            prescriptionVO.setHasFavour(prescriptionFavour != null);
        }
        // endregion

        return prescriptionVO;
    }

    /**
     * 分页获取处方表封装
     *
     * @param prescriptionPage
     * @param request
     * @return
     */
    @Override
    public Page<PrescriptionVO> getPrescriptionVOPage(Page<Prescription> prescriptionPage, HttpServletRequest request) {
        List<Prescription> prescriptionList = prescriptionPage.getRecords();
        Page<PrescriptionVO> prescriptionVOPage = new Page<>(prescriptionPage.getCurrent(), prescriptionPage.getSize(), prescriptionPage.getTotal());
        if (CollUtil.isEmpty(prescriptionList)) {
            return prescriptionVOPage;
        }
        // 对象列表 => 封装对象列表
        List<PrescriptionVO> prescriptionVOList = prescriptionList.stream().map(prescription -> {
            return PrescriptionVO.objToVo(prescription);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = prescriptionList.stream().map(Prescription::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> prescriptionIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> prescriptionIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> prescriptionIdSet = prescriptionList.stream().map(Prescription::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<PrescriptionThumb> prescriptionThumbQueryWrapper = new QueryWrapper<>();
            prescriptionThumbQueryWrapper.in("prescriptionId", prescriptionIdSet);
            prescriptionThumbQueryWrapper.eq("userId", loginUser.getId());
            List<PrescriptionThumb> prescriptionPrescriptionThumbList = prescriptionThumbMapper.selectList(prescriptionThumbQueryWrapper);
            prescriptionPrescriptionThumbList.forEach(prescriptionPrescriptionThumb -> prescriptionIdHasThumbMap.put(prescriptionPrescriptionThumb.getPrescriptionId(), true));
            // 获取收藏
            QueryWrapper<PrescriptionFavour> prescriptionFavourQueryWrapper = new QueryWrapper<>();
            prescriptionFavourQueryWrapper.in("prescriptionId", prescriptionIdSet);
            prescriptionFavourQueryWrapper.eq("userId", loginUser.getId());
            List<PrescriptionFavour> prescriptionFavourList = prescriptionFavourMapper.selectList(prescriptionFavourQueryWrapper);
            prescriptionFavourList.forEach(prescriptionFavour -> prescriptionIdHasFavourMap.put(prescriptionFavour.getPrescriptionId(), true));
        }
        // 填充信息
        prescriptionVOList.forEach(prescriptionVO -> {
            Long userId = prescriptionVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            prescriptionVO.setUser(userService.getUserVO(user));
            prescriptionVO.setHasThumb(prescriptionIdHasThumbMap.getOrDefault(prescriptionVO.getId(), false));
            prescriptionVO.setHasFavour(prescriptionIdHasFavourMap.getOrDefault(prescriptionVO.getId(), false));
        });
        // endregion

        prescriptionVOPage.setRecords(prescriptionVOList);
        return prescriptionVOPage;
    }

}
