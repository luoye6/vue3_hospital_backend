package com.xiaobaitiao.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.xiaobaitiao.springbootinit.model.entity.DoctorSchedule;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 医生排班表视图
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class DoctorScheduleVO implements Serializable {

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
     * @param doctorScheduleVO
     * @return
     */
    public static DoctorSchedule voToObj(DoctorScheduleVO doctorScheduleVO) {
        if (doctorScheduleVO == null) {
            return null;
        }
        DoctorSchedule doctorSchedule = new DoctorSchedule();
        BeanUtils.copyProperties(doctorScheduleVO, doctorSchedule);
        List<String> tagList = doctorScheduleVO.getTagList();
        doctorSchedule.setTags(JSONUtil.toJsonStr(tagList));
        return doctorSchedule;
    }

    /**
     * 对象转封装类
     *
     * @param doctorSchedule
     * @return
     */
    public static DoctorScheduleVO objToVo(DoctorSchedule doctorSchedule) {
        if (doctorSchedule == null) {
            return null;
        }
        DoctorScheduleVO doctorScheduleVO = new DoctorScheduleVO();
        BeanUtils.copyProperties(doctorSchedule, doctorScheduleVO);
        doctorScheduleVO.setTagList(JSONUtil.toList(doctorSchedule.getTags(), String.class));
        return doctorScheduleVO;
    }
}
