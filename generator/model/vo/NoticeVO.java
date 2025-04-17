package com.xiaobaitiao.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.xiaobaitiao.springbootinit.model.entity.Notice;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 公告视图
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class NoticeVO implements Serializable {

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
     * @param noticeVO
     * @return
     */
    public static Notice voToObj(NoticeVO noticeVO) {
        if (noticeVO == null) {
            return null;
        }
        Notice notice = new Notice();
        BeanUtils.copyProperties(noticeVO, notice);
        List<String> tagList = noticeVO.getTagList();
        notice.setTags(JSONUtil.toJsonStr(tagList));
        return notice;
    }

    /**
     * 对象转封装类
     *
     * @param notice
     * @return
     */
    public static NoticeVO objToVo(Notice notice) {
        if (notice == null) {
            return null;
        }
        NoticeVO noticeVO = new NoticeVO();
        BeanUtils.copyProperties(notice, noticeVO);
        noticeVO.setTagList(JSONUtil.toList(notice.getTags(), String.class));
        return noticeVO;
    }
}
