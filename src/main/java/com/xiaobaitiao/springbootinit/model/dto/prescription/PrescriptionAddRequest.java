package com.xiaobaitiao.springbootinit.model.dto.prescription;

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
 * 创建处方表请求
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class PrescriptionAddRequest implements Serializable {


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




    private static final long serialVersionUID = 1L;
}