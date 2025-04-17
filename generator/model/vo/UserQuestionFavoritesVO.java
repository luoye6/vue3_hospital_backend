package com.xiaobaitiao.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 用户题目收藏关联视图
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class UserQuestionFavoritesVO implements Serializable {

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
     * @param userQuestionFavoritesVO
     * @return
     */
    public static UserQuestionFavorites voToObj(UserQuestionFavoritesVO userQuestionFavoritesVO) {
        if (userQuestionFavoritesVO == null) {
            return null;
        }
        UserQuestionFavorites userQuestionFavorites = new UserQuestionFavorites();
        BeanUtils.copyProperties(userQuestionFavoritesVO, userQuestionFavorites);
        List<String> tagList = userQuestionFavoritesVO.getTagList();
        userQuestionFavorites.setTags(JSONUtil.toJsonStr(tagList));
        return userQuestionFavorites;
    }

    /**
     * 对象转封装类
     *
     * @param userQuestionFavorites
     * @return
     */
    public static UserQuestionFavoritesVO objToVo(UserQuestionFavorites userQuestionFavorites) {
        if (userQuestionFavorites == null) {
            return null;
        }
        UserQuestionFavoritesVO userQuestionFavoritesVO = new UserQuestionFavoritesVO();
        BeanUtils.copyProperties(userQuestionFavorites, userQuestionFavoritesVO);
        userQuestionFavoritesVO.setTagList(JSONUtil.toList(userQuestionFavorites.getTags(), String.class));
        return userQuestionFavoritesVO;
    }
}
