package com.xiaobaitiao.springbootinit.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.constant.CommonConstant;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.mapper.DepartmentMapper;
import com.xiaobaitiao.springbootinit.model.dto.department.DepartmentQueryRequest;
import com.xiaobaitiao.springbootinit.model.entity.Department;
import com.xiaobaitiao.springbootinit.model.entity.DepartmentFavour;
import com.xiaobaitiao.springbootinit.model.entity.DepartmentThumb;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.DepartmentVO;
import com.xiaobaitiao.springbootinit.model.vo.UserVO;
import com.xiaobaitiao.springbootinit.service.DepartmentService;
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
 * 科室表服务实现
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Service
@Slf4j
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Resource
    private UserService userService;

    /**
     * 校验数据
     *
     * @param department
     * @param add      对创建的数据进行校验
     */
    @Override
    public void validDepartment(Department department, boolean add) {
        ThrowUtils.throwIf(department == null, ErrorCode.PARAMS_ERROR);
        // todo 从对象中取值
        String title = department.getTitle();
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
     * @param departmentQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Department> getQueryWrapper(DepartmentQueryRequest departmentQueryRequest) {
        QueryWrapper<Department> queryWrapper = new QueryWrapper<>();
        if (departmentQueryRequest == null) {
            return queryWrapper;
        }
        // todo 从对象中取值
        Long id = departmentQueryRequest.getId();
        Long notId = departmentQueryRequest.getNotId();
        String title = departmentQueryRequest.getTitle();
        String content = departmentQueryRequest.getContent();
        String searchText = departmentQueryRequest.getSearchText();
        String sortField = departmentQueryRequest.getSortField();
        String sortOrder = departmentQueryRequest.getSortOrder();
        List<String> tagList = departmentQueryRequest.getTags();
        Long userId = departmentQueryRequest.getUserId();
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
     * 获取科室表封装
     *
     * @param department
     * @param request
     * @return
     */
    @Override
    public DepartmentVO getDepartmentVO(Department department, HttpServletRequest request) {
        // 对象转封装类
        DepartmentVO departmentVO = DepartmentVO.objToVo(department);

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Long userId = department.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        departmentVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        long departmentId = department.getId();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<DepartmentThumb> departmentThumbQueryWrapper = new QueryWrapper<>();
            departmentThumbQueryWrapper.in("departmentId", departmentId);
            departmentThumbQueryWrapper.eq("userId", loginUser.getId());
            DepartmentThumb departmentThumb = departmentThumbMapper.selectOne(departmentThumbQueryWrapper);
            departmentVO.setHasThumb(departmentThumb != null);
            // 获取收藏
            QueryWrapper<DepartmentFavour> departmentFavourQueryWrapper = new QueryWrapper<>();
            departmentFavourQueryWrapper.in("departmentId", departmentId);
            departmentFavourQueryWrapper.eq("userId", loginUser.getId());
            DepartmentFavour departmentFavour = departmentFavourMapper.selectOne(departmentFavourQueryWrapper);
            departmentVO.setHasFavour(departmentFavour != null);
        }
        // endregion

        return departmentVO;
    }

    /**
     * 分页获取科室表封装
     *
     * @param departmentPage
     * @param request
     * @return
     */
    @Override
    public Page<DepartmentVO> getDepartmentVOPage(Page<Department> departmentPage, HttpServletRequest request) {
        List<Department> departmentList = departmentPage.getRecords();
        Page<DepartmentVO> departmentVOPage = new Page<>(departmentPage.getCurrent(), departmentPage.getSize(), departmentPage.getTotal());
        if (CollUtil.isEmpty(departmentList)) {
            return departmentVOPage;
        }
        // 对象列表 => 封装对象列表
        List<DepartmentVO> departmentVOList = departmentList.stream().map(department -> {
            return DepartmentVO.objToVo(department);
        }).collect(Collectors.toList());

        // todo 可以根据需要为封装对象补充值，不需要的内容可以删除
        // region 可选
        // 1. 关联查询用户信息
        Set<Long> userIdSet = departmentList.stream().map(Department::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> departmentIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> departmentIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> departmentIdSet = departmentList.stream().map(Department::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<DepartmentThumb> departmentThumbQueryWrapper = new QueryWrapper<>();
            departmentThumbQueryWrapper.in("departmentId", departmentIdSet);
            departmentThumbQueryWrapper.eq("userId", loginUser.getId());
            List<DepartmentThumb> departmentDepartmentThumbList = departmentThumbMapper.selectList(departmentThumbQueryWrapper);
            departmentDepartmentThumbList.forEach(departmentDepartmentThumb -> departmentIdHasThumbMap.put(departmentDepartmentThumb.getDepartmentId(), true));
            // 获取收藏
            QueryWrapper<DepartmentFavour> departmentFavourQueryWrapper = new QueryWrapper<>();
            departmentFavourQueryWrapper.in("departmentId", departmentIdSet);
            departmentFavourQueryWrapper.eq("userId", loginUser.getId());
            List<DepartmentFavour> departmentFavourList = departmentFavourMapper.selectList(departmentFavourQueryWrapper);
            departmentFavourList.forEach(departmentFavour -> departmentIdHasFavourMap.put(departmentFavour.getDepartmentId(), true));
        }
        // 填充信息
        departmentVOList.forEach(departmentVO -> {
            Long userId = departmentVO.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            departmentVO.setUser(userService.getUserVO(user));
            departmentVO.setHasThumb(departmentIdHasThumbMap.getOrDefault(departmentVO.getId(), false));
            departmentVO.setHasFavour(departmentIdHasFavourMap.getOrDefault(departmentVO.getId(), false));
        });
        // endregion

        departmentVOPage.setRecords(departmentVOList);
        return departmentVOPage;
    }

}
