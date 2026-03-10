package com.xiaobaitiao.springbootinit.model.dto.doctor;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 编辑医生表请求
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class DoctorEditRequest implements Serializable {

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


    private static final long serialVersionUID = 1L;
}