package com.xiaobaitiao.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户刷题记录表视图
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class UserQuestionRecordVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 标签列表
     */
    private List<String> tagList;

    /**
     * 创建用户信息
     */
    private UserVO user;

    /**
     * 封装类转对象
     *
     * @param userQuestionRecordVO
     * @return
     */
    public static UserQuestionRecord voToObj(UserQuestionRecordVO userQuestionRecordVO) {
        if (userQuestionRecordVO == null) {
            return null;
        }
        UserQuestionRecord userQuestionRecord = new UserQuestionRecord();
        BeanUtils.copyProperties(userQuestionRecordVO, userQuestionRecord);
        List<String> tagList = userQuestionRecordVO.getTagList();
        userQuestionRecord.setTags(JSONUtil.toJsonStr(tagList));
        return userQuestionRecord;
    }

    /**
     * 对象转封装类
     *
     * @param userQuestionRecord
     * @return
     */
    public static UserQuestionRecordVO objToVo(UserQuestionRecord userQuestionRecord) {
        if (userQuestionRecord == null) {
            return null;
        }
        UserQuestionRecordVO userQuestionRecordVO = new UserQuestionRecordVO();
        BeanUtils.copyProperties(userQuestionRecord, userQuestionRecordVO);
        userQuestionRecordVO.setTagList(JSONUtil.toList(userQuestionRecord.getTags(), String.class));
        return userQuestionRecordVO;
    }
}
