package com.xiaobaitiao.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.xiaobaitiao.springbootinit.model.entity.UserAiMessage;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户对话表视图
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class UserAiMessageVO implements Serializable {

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
     * @param userAiMessageVO
     * @return
     */
    public static UserAiMessage voToObj(UserAiMessageVO userAiMessageVO) {
        if (userAiMessageVO == null) {
            return null;
        }
        UserAiMessage userAiMessage = new UserAiMessage();
        BeanUtils.copyProperties(userAiMessageVO, userAiMessage);
        List<String> tagList = userAiMessageVO.getTagList();
        userAiMessage.setTags(JSONUtil.toJsonStr(tagList));
        return userAiMessage;
    }

    /**
     * 对象转封装类
     *
     * @param userAiMessage
     * @return
     */
    public static UserAiMessageVO objToVo(UserAiMessage userAiMessage) {
        if (userAiMessage == null) {
            return null;
        }
        UserAiMessageVO userAiMessageVO = new UserAiMessageVO();
        BeanUtils.copyProperties(userAiMessage, userAiMessageVO);
        userAiMessageVO.setTagList(JSONUtil.toList(userAiMessage.getTags(), String.class));
        return userAiMessageVO;
    }
}
