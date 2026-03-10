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
import com.xiaobaitiao.springbootinit.model.dto.doctor.DoctorAddRequest;
import com.xiaobaitiao.springbootinit.model.dto.doctor.DoctorEditRequest;
import com.xiaobaitiao.springbootinit.model.dto.doctor.DoctorQueryRequest;
import com.xiaobaitiao.springbootinit.model.dto.doctor.DoctorUpdateRequest;
import com.xiaobaitiao.springbootinit.model.entity.Doctor;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.DoctorVO;
import com.xiaobaitiao.springbootinit.service.DoctorService;
import com.xiaobaitiao.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 医生表接口
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@RestController
@RequestMapping("/doctor")
@Slf4j
public class DoctorController {

    @Resource
    private DoctorService doctorService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建医生表
     *
     * @param doctorAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addDoctor(@RequestBody DoctorAddRequest doctorAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(doctorAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        Doctor doctor = new Doctor();
        BeanUtils.copyProperties(doctorAddRequest, doctor);
        // 数据校验
        doctorService.validDoctor(doctor, true);
        // 写入数据库
        boolean result = doctorService.save(doctor);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newDoctorId = doctor.getId();
        return ResultUtils.success(newDoctorId);
    }

    /**
     * 删除医生表
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteDoctor(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Doctor oldDoctor = doctorService.getById(id);
        ThrowUtils.throwIf(oldDoctor == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅管理员可删除
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = doctorService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新医生表（仅管理员可用）
     *
     * @param doctorUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateDoctor(@RequestBody DoctorUpdateRequest doctorUpdateRequest) {
        if (doctorUpdateRequest == null || doctorUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Doctor doctor = new Doctor();
        BeanUtils.copyProperties(doctorUpdateRequest, doctor);
        // 数据校验
        doctorService.validDoctor(doctor, false);
        // 判断是否存在
        long id = doctorUpdateRequest.getId();
        Doctor oldDoctor = doctorService.getById(id);
        ThrowUtils.throwIf(oldDoctor == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = doctorService.updateById(doctor);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取医生表（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<DoctorVO> getDoctorVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Doctor doctor = doctorService.getById(id);
        ThrowUtils.throwIf(doctor == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(doctorService.getDoctorVO(doctor, request));
    }

    /**
     * 分页获取医生表列表（仅管理员可用）
     *
     * @param doctorQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Doctor>> listDoctorByPage(@RequestBody DoctorQueryRequest doctorQueryRequest) {
        long current = doctorQueryRequest.getCurrent();
        long size = doctorQueryRequest.getPageSize();
        // 查询数据库
        Page<Doctor> doctorPage = doctorService.page(new Page<>(current, size),
                doctorService.getQueryWrapper(doctorQueryRequest));
        return ResultUtils.success(doctorPage);
    }

    /**
     * 分页获取医生表列表（封装类）
     *
     * @param doctorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<DoctorVO>> listDoctorVOByPage(@RequestBody DoctorQueryRequest doctorQueryRequest,
                                                               HttpServletRequest request) {
        long current = doctorQueryRequest.getCurrent();
        long size = doctorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Doctor> doctorPage = doctorService.page(new Page<>(current, size),
                doctorService.getQueryWrapper(doctorQueryRequest));
        // 获取封装类
        return ResultUtils.success(doctorService.getDoctorVOPage(doctorPage, request));
    }

    /**
     * 分页获取当前登录用户创建的医生表列表
     *
     * @param doctorQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<DoctorVO>> listMyDoctorVOByPage(@RequestBody DoctorQueryRequest doctorQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(doctorQueryRequest == null, ErrorCode.PARAMS_ERROR);
        long current = doctorQueryRequest.getCurrent();
        long size = doctorQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Doctor> doctorPage = doctorService.page(new Page<>(current, size),
                doctorService.getQueryWrapper(doctorQueryRequest));
        // 获取封装类
        return ResultUtils.success(doctorService.getDoctorVOPage(doctorPage, request));
    }



    // endregion
}
