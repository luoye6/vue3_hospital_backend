package com.xiaobaitiao.springbootinit.model.vo;

import cn.hutool.json.JSONUtil;
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
     * @param prescriptionVO
     * @return
     */
    public static Prescription voToObj(PrescriptionVO prescriptionVO) {
        if (prescriptionVO == null) {
            return null;
        }
        Prescription prescription = new Prescription();
        BeanUtils.copyProperties(prescriptionVO, prescription);
        List<String> tagList = prescriptionVO.getTagList();
        prescription.setTags(JSONUtil.toJsonStr(tagList));
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
        prescriptionVO.setTagList(JSONUtil.toList(prescription.getTags(), String.class));
        return prescriptionVO;
    }
}
