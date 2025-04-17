package com.xiaobaitiao.springbootinit.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaobaitiao.springbootinit.model.dto.userQuestionRecord.UserQuestionRecordQueryRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户刷题记录表服务
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
public interface UserQuestionRecordService extends IService<UserQuestionRecord> {

    /**
     * 校验数据
     *
     * @param userQuestionRecord
     * @param add 对创建的数据进行校验
     */
    void validUserQuestionRecord(UserQuestionRecord userQuestionRecord, boolean add);

    /**
     * 获取查询条件
     *
     * @param userQuestionRecordQueryRequest
     * @return
     */
    QueryWrapper<UserQuestionRecord> getQueryWrapper(UserQuestionRecordQueryRequest userQuestionRecordQueryRequest);
    
    /**
     * 获取用户刷题记录表封装
     *
     * @param userQuestionRecord
     * @param request
     * @return
     */
    UserQuestionRecordVO getUserQuestionRecordVO(UserQuestionRecord userQuestionRecord, HttpServletRequest request);

    /**
     * 分页获取用户刷题记录表封装
     *
     * @param userQuestionRecordPage
     * @param request
     * @return
     */
    Page<UserQuestionRecordVO> getUserQuestionRecordVOPage(Page<UserQuestionRecord> userQuestionRecordPage, HttpServletRequest request);
}
