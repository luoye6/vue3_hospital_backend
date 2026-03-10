package com.xiaobaitiao.springbootinit.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName prescription
 */
@TableName(value ="prescription")
@Data
public class Prescription implements Serializable {
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 患者id
     */
    private Long patientId;

    /**
     * 预约表id
     */
    private Long appointmentId;

    /**
     * 诊断结果
     */
    private String diagnosticResult;

    /**
     * 处方内容
     */
    private String prescriptionContent;

    /**
     * 注意事项
     */
    private String precautions;

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