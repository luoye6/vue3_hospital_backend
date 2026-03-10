package com.xiaobaitiao.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaobaitiao.springbootinit.model.entity.Prescription;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * 处方表视图
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class PrescriptionVO implements Serializable {

    /**
     * id
     */
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



    private static final long serialVersionUID = 1L;

    /**
     * 封装类转对象
     *
     * @param prescriptionVO
     * @return
     */
    public static Prescription voToObj(PrescriptionVO prescriptionVO) {
        if (prescriptionVO == null) {
            return null;
        }
        Prescription prescription = new Prescription();
        BeanUtils.copyProperties(prescriptionVO, prescription);
        return prescription;
    }

    /**
     * 对象转封装类
     *
     * @param prescription
     * @return
     */
    public static PrescriptionVO objToVo(Prescription prescription) {
        if (prescription == null) {
            return null;
        }
        PrescriptionVO prescriptionVO = new PrescriptionVO();
        BeanUtils.copyProperties(prescription, prescriptionVO);
        return prescriptionVO;
    }
}
