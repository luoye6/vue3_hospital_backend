package com.xiaobaitiao.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.xiaobaitiao.springbootinit.model.entity.Doctor;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 医生表视图
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class DoctorVO implements Serializable {

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
     * @param doctorVO
     * @return
     */
    public static Doctor voToObj(DoctorVO doctorVO) {
        if (doctorVO == null) {
            return null;
        }
        Doctor doctor = new Doctor();
        BeanUtils.copyProperties(doctorVO, doctor);
        List<String> tagList = doctorVO.getTagList();
        doctor.setTags(JSONUtil.toJsonStr(tagList));
        return doctor;
    }

    /**
     * 对象转封装类
     *
     * @param doctor
     * @return
     */
    public static DoctorVO objToVo(Doctor doctor) {
        if (doctor == null) {
            return null;
        }
        DoctorVO doctorVO = new DoctorVO();
        BeanUtils.copyProperties(doctor, doctorVO);
        doctorVO.setTagList(JSONUtil.toList(doctor.getTags(), String.class));
        return doctorVO;
    }
}
