package com.xiaobaitiao.springbootinit.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaobaitiao.springbootinit.model.entity.Department;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;

/**
 * 科室表视图
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class DepartmentVO implements Serializable {

    /**
     * id
     */
    private Long id;

    /**
     * 科室名称
     */
    private String departmentName;

    /**
     * 科室简介
     */
    private String departmentDescription;

    /**
     * 0 未启用 1已启用
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
     * @param departmentVO
     * @return
     */
    public static Department voToObj(DepartmentVO departmentVO) {
        if (departmentVO == null) {
            return null;
        }
        Department department = new Department();
        BeanUtils.copyProperties(departmentVO, department);
        return department;
    }

    /**
     * 对象转封装类
     *
     * @param department
     * @return
     */
    public static DepartmentVO objToVo(Department department) {
        if (department == null) {
            return null;
        }
        DepartmentVO departmentVO = new DepartmentVO();
        BeanUtils.copyProperties(department, departmentVO);
        return departmentVO;
    }
}
