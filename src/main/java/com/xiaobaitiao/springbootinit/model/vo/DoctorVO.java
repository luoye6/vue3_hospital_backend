package com.xiaobaitiao.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
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
     *
     */
    private Long id;

    /**
     * 科室id
     */
    private Long departmentId;

    /**
     * 医生姓名
     */
    private String doctorName;

    /**
     * 性别
     */
    private String doctorSex;

    /**
     * 医生头像
     */
    private String doctorAvatar;

    /**
     * 医生职称
     */
    private String doctorTitle;

    /**
     * 医生简介
     */
    private String doctorDescription;

    /**
     * 是否启用 0 未启用 1已启用
     */
    private Integer isEnabled;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date updateTime;

    private static final long serialVersionUID = 1L;
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
        return doctorVO;
    }
}
