package com.xiaobaitiao.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName doctor
 */
@TableName(value ="doctor")
@Data
public class Doctor implements Serializable {
    /**
     * 
     */
    @TableId(type = IdType.ASSIGN_ID)
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

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}