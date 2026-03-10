package com.xiaobaitiao.springbootinit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaobaitiao.springbootinit.annotation.AuthCheck;
import com.xiaobaitiao.springbootinit.common.BaseResponse;
import com.xiaobaitiao.springbootinit.common.DeleteRequest;
import com.xiaobaitiao.springbootinit.common.ErrorCode;
import com.xiaobaitiao.springbootinit.common.ResultUtils;
import com.xiaobaitiao.springbootinit.constant.UserConstant;
import com.xiaobaitiao.springbootinit.exception.BusinessException;
import com.xiaobaitiao.springbootinit.exception.ThrowUtils;
import com.xiaobaitiao.springbootinit.model.dto.appointment.AppointmentAddRequest;
import com.xiaobaitiao.springbootinit.model.dto.appointment.AppointmentEditRequest;
import com.xiaobaitiao.springbootinit.model.dto.appointment.AppointmentQueryRequest;
import com.xiaobaitiao.springbootinit.model.dto.appointment.AppointmentUpdateRequest;
import com.xiaobaitiao.springbootinit.model.entity.Appointment;
import com.xiaobaitiao.springbootinit.model.entity.DoctorSchedule;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.AppointmentVO;
import com.xiaobaitiao.springbootinit.service.AppointmentService;
import com.xiaobaitiao.springbootinit.service.DoctorScheduleService;
import com.xiaobaitiao.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 预约挂号表接口
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@RestController
@RequestMapping("/appointment")
@Slf4j
public class AppointmentController {

    @Resource
    private AppointmentService appointmentService;

    @Resource
    private UserService userService;
    @Resource
    private DoctorScheduleService doctorScheduleService;
    @Resource
    private TransactionTemplate transactionTemplate;
    // region 增删改查

    /**
     * 创建预约挂号表
     *
     * @param appointmentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addAppointment(@RequestBody AppointmentAddRequest appointmentAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(appointmentAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(appointmentAddRequest, appointment);
        // 数据校验
        appointmentService.validAppointment(appointment, true);
        // 写入数据库
        boolean result = appointmentService.save(appointment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newAppointmentId = appointment.getId();
        return ResultUtils.success(newAppointmentId);
    }

    /**
     * 删除预约挂号表
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteAppointment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Appointment oldAppointment = appointmentService.getById(id);
        ThrowUtils.throwIf(oldAppointment == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅管理员可删除
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = appointmentService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新预约挂号表（仅管理员可用）
     *
     * @param appointmentUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateAppointment(@RequestBody AppointmentUpdateRequest appointmentUpdateRequest) {
        if (appointmentUpdateRequest == null || appointmentUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Appointment appointment = new Appointment();
        BeanUtils.copyProperties(appointmentUpdateRequest, appointment);
        // 数据校验
        appointmentService.validAppointment(appointment, false);
        // 判断是否存在
        long id = appointmentUpdateRequest.getId();
        Appointment oldAppointment = appointmentService.getById(id);
        ThrowUtils.throwIf(oldAppointment == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = appointmentService.updateById(appointment);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取预约挂号表（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<AppointmentVO> getAppointmentVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Appointment appointment = appointmentService.getById(id);
        ThrowUtils.throwIf(appointment == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(appointmentService.getAppointmentVO(appointment, request));
    }

    /**
     * 分页获取预约挂号表列表（仅管理员可用）
     *
     * @param appointmentQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Appointment>> listAppointmentByPage(@RequestBody AppointmentQueryRequest appointmentQueryRequest) {
        long current = appointmentQueryRequest.getCurrent();
        long size = appointmentQueryRequest.getPageSize();
        // 查询数据库
        Page<Appointment> appointmentPage = appointmentService.page(new Page<>(current, size),
                appointmentService.getQueryWrapper(appointmentQueryRequest));
        return ResultUtils.success(appointmentPage);
    }

    /**
     * 分页获取预约挂号表列表（封装类）
     *
     * @param appointmentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<AppointmentVO>> listAppointmentVOByPage(@RequestBody AppointmentQueryRequest appointmentQueryRequest,
                                                                     HttpServletRequest request) {
        long current = appointmentQueryRequest.getCurrent();
        long size = appointmentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Appointment> appointmentPage = appointmentService.page(new Page<>(current, size),
                appointmentService.getQueryWrapper(appointmentQueryRequest));
        // 获取封装类
        return ResultUtils.success(appointmentService.getAppointmentVOPage(appointmentPage, request));
    }

    /**
     * 分页获取当前登录用户创建的预约挂号表列表
     *
     * @param appointmentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<AppointmentVO>> listMyAppointmentVOByPage(@RequestBody AppointmentQueryRequest appointmentQueryRequest,
                                                                       HttpServletRequest request) {
        ThrowUtils.throwIf(appointmentQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = appointmentQueryRequest.getCurrent();
        long size = appointmentQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Appointment> appointmentPage = appointmentService.page(new Page<>(current, size),
                appointmentService.getQueryWrapper(appointmentQueryRequest));
        // 获取封装类
        return ResultUtils.success(appointmentService.getAppointmentVOPage(appointmentPage, request));
    }

    /**
     * 获取用户就诊日历热力图数据
     *
     * @param patientId 患者ID
     * @return 日期和就诊标记(0或1)的列表
     */
    @GetMapping("/calendarData")
    public BaseResponse<List<Map<String, Object>>> getAppointmentCalendarData(
            @RequestParam Long patientId) {
        ThrowUtils.throwIf(patientId == null || patientId <= 0, ErrorCode.PARAMS_ERROR);

        // 计算一年前的Date时间（考虑时区）
        Date oneYearAgo = Date.from(LocalDateTime.now()
                .minusYears(1)
                .atZone(ZoneId.systemDefault())
                .toInstant());

        // 查询该患者最近一年的就诊记录
        List<Appointment> appointments = appointmentService.lambdaQuery()
                .eq(Appointment::getPatientId, patientId)
                .ge(Appointment::getCreateTime, oneYearAgo)
                .list();

        // 转换为日历热力图需要的数据格式
        List<Map<String, Object>> result = appointments.stream()
                .collect(Collectors.groupingBy(
                        appointment -> {
                            Date createTime = appointment.getCreateTime();
                            if (createTime == null) return "";
                            // Date转LocalDate（使用GMT+8时区）
                            return createTime.toInstant()
                                    .atZone(ZoneId.of("GMT+8"))
                                    .toLocalDate()
                                    .toString();
                        },
                        Collectors.counting()
                ))
                .entrySet()
                .stream()
                .filter(entry -> !entry.getKey().isEmpty())
                .map(entry -> {
                    Map<String, Object> item = new HashMap<>();
                    item.put("date", entry.getKey());
                    item.put("value", entry.getValue() > 0 ? 1 : 0);
                    return item;
                })
                .collect(Collectors.toList());

        return ResultUtils.success(result);
    }

    /**
     * 预约挂号接口
     *
     * @param patientId          患者ID
     * @param doctorScheduleId   医生排班ID
     * @param symptomDescription 症状描述
     * @param paymentAmount      支付金额
     * @param request            HTTP请求
     * @return 是否预约成功
     */
    @PostMapping("/appointment")
    public BaseResponse<Boolean> bookAppointment(
            @RequestParam Long patientId,
            @RequestParam Long doctorScheduleId,
            @RequestParam String symptomDescription,
            @RequestParam BigDecimal paymentAmount,
            HttpServletRequest request) {

        // 1. 参数校验
        ThrowUtils.throwIf(patientId == null || patientId <= 0, ErrorCode.PARAMS_ERROR, "患者ID错误");
        ThrowUtils.throwIf(doctorScheduleId == null || doctorScheduleId <= 0, ErrorCode.PARAMS_ERROR, "医生排班ID错误");
        ThrowUtils.throwIf(symptomDescription == null || symptomDescription.isEmpty(), ErrorCode.PARAMS_ERROR, "症状描述不能为空");
        ThrowUtils.throwIf(paymentAmount == null || paymentAmount.compareTo(BigDecimal.ZERO) <= 0, ErrorCode.PARAMS_ERROR, "支付金额错误");

        // 2. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);
        if (!loginUser.getId().equals(patientId) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能为自己预约");
        }

        // 3. 检查医生排班
        DoctorSchedule doctorSchedule = doctorScheduleService.getById(doctorScheduleId);
        if (doctorSchedule == null || doctorSchedule.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "医生排班不存在");
        }
        if (doctorSchedule.getIsEnabled() == 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该排班未启用");
        }
        if (doctorSchedule.getAlreadyAppointment() >= doctorSchedule.getMaxAppointment()) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该排班已满");
        }

        // 4. 检查用户余额
        User patient = userService.getById(patientId);
        if (patient == null || patient.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "患者不存在");
        }
        if (patient.getBalance().compareTo(paymentAmount) < 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "余额不足");
        }

        // 5. 计算预约号
        long appointmentCount = appointmentService.lambdaQuery()
                .eq(Appointment::getDoctorScheduleId, doctorScheduleId)
                .count();
        int appointmentNumber = (int) (appointmentCount + 1);

        // 6. 创建预约记录
        Appointment appointment = new Appointment();
        appointment.setPatientId(patientId);
        appointment.setDoctorScheduleId(doctorScheduleId);
        appointment.setAppointmentNumber(appointmentNumber);
        appointment.setAppointmentStatus(0); // 已预约
        appointment.setSymptomDescription(symptomDescription);
        appointment.setPayStatus(1); // 假设直接支付
        appointment.setPaymentAmount(paymentAmount);

        // 验证预约数据
        appointmentService.validAppointment(appointment, true);

        // 7. 事务处理：创建预约、更新排班、扣除余额
        // 事务处理：创建预约、更新排班、扣除余额
        boolean result = transactionTemplate.execute(status -> {
            try {
                // 尝试保存预约记录（可能触发唯一约束异常）
                boolean saveResult = appointmentService.save(appointment);
                if (!saveResult) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "创建预约失败");
                }

                // 更新排班已预约数
                boolean updateScheduleResult = doctorScheduleService.lambdaUpdate()
                        .eq(DoctorSchedule::getId, doctorScheduleId)
                        .setSql("alreadyAppointment = alreadyAppointment + 1")
                        .update();
                if (!updateScheduleResult) {
                    status.setRollbackOnly();
                    return false;
                }

                // 带条件更新用户余额
                boolean deductResult = userService.lambdaUpdate()
                        .eq(User::getId, patientId)
                        .ge(User::getBalance, paymentAmount)
                        .setSql("balance = balance - " + paymentAmount)
                        .update();
                if (!deductResult) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "余额不足");
                }

                return true;
            } catch (DuplicateKeyException e) {
                // 明确捕获唯一键冲突异常
                status.setRollbackOnly();
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "您已预约过该号源，请勿重复操作");
            } catch (DataIntegrityViolationException e) {
                // 处理其他数据完整性异常
                if (e.getCause() instanceof DuplicateKeyException) {
                    status.setRollbackOnly();
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "重复预约请求被拦截");
                }
                throw e;
            } catch (BusinessException e) {
                // 已处理过的业务异常直接回滚
                status.setRollbackOnly();
                throw e;
            } catch (Exception e) {
                // 其他未知异常处理
                status.setRollbackOnly();
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "预约服务暂时不可用");
            }
        });


        return ResultUtils.success(result);
    }

    /**
     * 取消预约接口
     *
     * @param id           预约ID
     * @param cancelReason 取消原因
     * @param request      HTTP请求
     * @return 是否取消成功
     */
    @PostMapping("/cancel")
    public BaseResponse<Boolean> cancelAppointment(
            @RequestParam Long id,
            @RequestParam String cancelReason,
            HttpServletRequest request) {

        // 1. 参数校验
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "预约ID错误");
        ThrowUtils.throwIf(cancelReason == null || cancelReason.isEmpty(),
                ErrorCode.PARAMS_ERROR, "取消原因不能为空");

        // 2. 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 3. 检查预约记录
        Appointment appointment = appointmentService.getById(id);
        if (appointment == null || appointment.getIsDelete() == 1) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "预约记录不存在");
        }

        // 4. 权限检查（只能取消自己的预约或管理员）
        if (!appointment.getPatientId().equals(loginUser.getId()) && !userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只能取消自己的预约");
        }

        // 5. 检查预约状态（只有"已预约"状态才能取消）
        if (appointment.getAppointmentStatus() != 0) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR,
                    "只有已预约状态的预约才能取消，当前状态: " + appointment.getAppointmentStatus());
        }

        // 6. 事务处理：更新预约、退还余额、更新排班
        boolean result = transactionTemplate.execute(status -> {
            // 更新预约状态和取消原因
            boolean updateAppointment = appointmentService.lambdaUpdate()
                    .eq(Appointment::getId, id)
                    .set(Appointment::getAppointmentStatus, 2) // 已取消
                    .set(Appointment::getCancelReason, cancelReason)
                    .update();
            if (!updateAppointment) {
                return false;
            }

            // 如果已支付，退还余额
            if (appointment.getPayStatus() == 1 && appointment.getPaymentAmount() != null
                    && appointment.getPaymentAmount().compareTo(BigDecimal.ZERO) > 0) {
                boolean refundResult = userService.lambdaUpdate()
                        .eq(User::getId, appointment.getPatientId())
                        .setSql("balance = balance + " + appointment.getPaymentAmount())
                        .update();
                if (!refundResult) {
                    status.setRollbackOnly();
                    return false;
                }
            }

            // 减少排班的已预约数
            boolean updateSchedule = doctorScheduleService.lambdaUpdate()
                    .eq(DoctorSchedule::getId, appointment.getDoctorScheduleId())
                    .gt(DoctorSchedule::getAlreadyAppointment, 0)
                    .setSql("alreadyAppointment = alreadyAppointment - 1")
                    .update();
            if (!updateSchedule) {
                status.setRollbackOnly();
                return false;
            }

            return true;
        });

        return ResultUtils.success(result);
    }
    // endregion
}
