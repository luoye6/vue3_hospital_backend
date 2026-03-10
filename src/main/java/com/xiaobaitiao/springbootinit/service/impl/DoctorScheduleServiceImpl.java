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
import com.xiaobaitiao.springbootinit.model.vo.DoctorScheduleRankVO;
import com.xiaobaitiao.springbootinit.model.vo.DoctorScheduleVO;
import com.xiaobaitiao.springbootinit.service.DoctorScheduleService;
import com.xiaobaitiao.springbootinit.service.UserService;
import com.xiaobaitiao.springbootinit.utils.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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
    private DoctorScheduleMapper doctorScheduleMapper;
    /**
     * 校验数据
     *
     * @param doctorSchedule
     * @param add            对创建的数据进行校验
     */
    @Override
    public void validDoctorSchedule(DoctorSchedule doctorSchedule, boolean add) {
        ThrowUtils.throwIf(doctorSchedule == null, ErrorCode.PARAMS_ERROR);
        Long doctorId = doctorSchedule.getDoctorId();
        String timeSlot = doctorSchedule.getTimeSlot();
        Integer maxAppointment = doctorSchedule.getMaxAppointment();

        // 创建数据时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(doctorId == null || doctorId <= 0, ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(maxAppointment == null || maxAppointment <= 0, ErrorCode.PARAMS_ERROR);
            ThrowUtils.throwIf(StringUtils.isBlank(timeSlot), ErrorCode.PARAMS_ERROR);
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
        // 从对象中取值
        Long id = doctorScheduleQueryRequest.getId();
        Long doctorId = doctorScheduleQueryRequest.getDoctorId();
        Date scheduleDate = doctorScheduleQueryRequest.getScheduleDate();
        String timeSlot = doctorScheduleQueryRequest.getTimeSlot();
        Integer isEnabled = doctorScheduleQueryRequest.getIsEnabled();
        String sortField = doctorScheduleQueryRequest.getSortField();
        String sortOrder = doctorScheduleQueryRequest.getSortOrder();

        // 日期转换：将 Date 类型的 scheduleDate 转换为 'yyyy-MM-dd' 格式的字符串
        if (scheduleDate != null) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String formattedDate = dateFormat.format(scheduleDate);
            queryWrapper.like("scheduleDate", formattedDate); // 模糊查询当日
        }

        // 模糊查询
        queryWrapper.like(StringUtils.isNotBlank(timeSlot), "timeSlot", timeSlot);

        // 精确查询
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(doctorId), "doctorId", doctorId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(isEnabled), "isEnabled", isEnabled);

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
        return DoctorScheduleVO.objToVo(doctorSchedule);
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
        List<DoctorScheduleVO> doctorScheduleVOList = doctorScheduleList.stream().map(DoctorScheduleVO::objToVo).collect(Collectors.toList());

        doctorScheduleVOPage.setRecords(doctorScheduleVOList);
        return doctorScheduleVOPage;
    }

    @Override
    public List<DoctorScheduleRankVO> getDoctorScheduleRankTop10() {
        return doctorScheduleMapper.getDoctorScheduleRankTop10();
    }

}
