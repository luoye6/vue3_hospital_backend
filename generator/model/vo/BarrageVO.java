package com.xiaobaitiao.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.xiaobaitiao.springbootinit.model.entity.Barrage;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 弹幕视图
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class BarrageVO implements Serializable {

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
     * @param barrageVO
     * @return
     */
    public static Barrage voToObj(BarrageVO barrageVO) {
        if (barrageVO == null) {
            return null;
        }
        Barrage barrage = new Barrage();
        BeanUtils.copyProperties(barrageVO, barrage);
        List<String> tagList = barrageVO.getTagList();
        barrage.setTags(JSONUtil.toJsonStr(tagList));
        return barrage;
    }

    /**
     * 对象转封装类
     *
     * @param barrage
     * @return
     */
    public static BarrageVO objToVo(Barrage barrage) {
        if (barrage == null) {
            return null;
        }
        BarrageVO barrageVO = new BarrageVO();
        BeanUtils.copyProperties(barrage, barrageVO);
        barrageVO.setTagList(JSONUtil.toList(barrage.getTags(), String.class));
        return barrageVO;
    }
}
