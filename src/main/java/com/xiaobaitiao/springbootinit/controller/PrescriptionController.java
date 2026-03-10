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
import com.xiaobaitiao.springbootinit.model.dto.prescription.PrescriptionAddRequest;
import com.xiaobaitiao.springbootinit.model.dto.prescription.PrescriptionEditRequest;
import com.xiaobaitiao.springbootinit.model.dto.prescription.PrescriptionQueryRequest;
import com.xiaobaitiao.springbootinit.model.dto.prescription.PrescriptionUpdateRequest;
import com.xiaobaitiao.springbootinit.model.entity.Prescription;
import com.xiaobaitiao.springbootinit.model.entity.User;
import com.xiaobaitiao.springbootinit.model.vo.PrescriptionVO;
import com.xiaobaitiao.springbootinit.service.PrescriptionService;
import com.xiaobaitiao.springbootinit.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * 处方表接口
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@RestController
@RequestMapping("/prescription")
@Slf4j
public class PrescriptionController {

    @Resource
    private PrescriptionService prescriptionService;

    @Resource
    private UserService userService;

    // region 增删改查

    /**
     * 创建处方表
     *
     * @param prescriptionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addPrescription(@RequestBody PrescriptionAddRequest prescriptionAddRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(prescriptionAddRequest == null, ErrorCode.PARAMS_ERROR);
        // todo 在此处将实体类和 DTO 进行转换
        Prescription prescription = new Prescription();
        BeanUtils.copyProperties(prescriptionAddRequest, prescription);
        // 数据校验
        prescriptionService.validPrescription(prescription, true);

        // 写入数据库
        boolean result = prescriptionService.save(prescription);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        // 返回新写入的数据 id
        long newPrescriptionId = prescription.getId();
        return ResultUtils.success(newPrescriptionId);
    }

    /**
     * 删除处方表
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePrescription(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Prescription oldPrescription = prescriptionService.getById(id);
        ThrowUtils.throwIf(oldPrescription == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅管理员可删除
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        // 操作数据库
        boolean result = prescriptionService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 更新处方表（仅管理员可用）
     *
     * @param prescriptionUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updatePrescription(@RequestBody PrescriptionUpdateRequest prescriptionUpdateRequest) {
        if (prescriptionUpdateRequest == null || prescriptionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // todo 在此处将实体类和 DTO 进行转换
        Prescription prescription = new Prescription();
        BeanUtils.copyProperties(prescriptionUpdateRequest, prescription);
        // 数据校验
        prescriptionService.validPrescription(prescription, false);
        // 判断是否存在
        long id = prescriptionUpdateRequest.getId();
        Prescription oldPrescription = prescriptionService.getById(id);
        ThrowUtils.throwIf(oldPrescription == null, ErrorCode.NOT_FOUND_ERROR);
        // 操作数据库
        boolean result = prescriptionService.updateById(prescription);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取处方表（封装类）
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PrescriptionVO> getPrescriptionVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Prescription prescription = prescriptionService.getById(id);
        ThrowUtils.throwIf(prescription == null, ErrorCode.NOT_FOUND_ERROR);
        // 获取封装类
        return ResultUtils.success(prescriptionService.getPrescriptionVO(prescription, request));
    }

    /**
     * 分页获取处方表列表（仅管理员可用）
     *
     * @param prescriptionQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Prescription>> listPrescriptionByPage(@RequestBody PrescriptionQueryRequest prescriptionQueryRequest) {
        long current = prescriptionQueryRequest.getCurrent();
        long size = prescriptionQueryRequest.getPageSize();
        // 查询数据库
        Page<Prescription> prescriptionPage = prescriptionService.page(new Page<>(current, size),
                prescriptionService.getQueryWrapper(prescriptionQueryRequest));
        return ResultUtils.success(prescriptionPage);
    }

    /**
     * 分页获取处方表列表（封装类）
     *
     * @param prescriptionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PrescriptionVO>> listPrescriptionVOByPage(@RequestBody PrescriptionQueryRequest prescriptionQueryRequest,
                                                               HttpServletRequest request) {
        long current = prescriptionQueryRequest.getCurrent();
        long size = prescriptionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Prescription> prescriptionPage = prescriptionService.page(new Page<>(current, size),
                prescriptionService.getQueryWrapper(prescriptionQueryRequest));
        // 获取封装类
        return ResultUtils.success(prescriptionService.getPrescriptionVOPage(prescriptionPage, request));
    }

    /**
     * 分页获取当前登录用户创建的处方表列表
     *
     * @param prescriptionQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<PrescriptionVO>> listMyPrescriptionVOByPage(@RequestBody PrescriptionQueryRequest prescriptionQueryRequest,
                                                                 HttpServletRequest request) {
        ThrowUtils.throwIf(prescriptionQueryRequest == null, ErrorCode.PARAMS_ERROR);
        // 补充查询条件，只查询当前登录用户的数据
        long current = prescriptionQueryRequest.getCurrent();
        long size = prescriptionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        // 查询数据库
        Page<Prescription> prescriptionPage = prescriptionService.page(new Page<>(current, size),
                prescriptionService.getQueryWrapper(prescriptionQueryRequest));
        // 获取封装类
        return ResultUtils.success(prescriptionService.getPrescriptionVOPage(prescriptionPage, request));
    }



    // endregion
}
