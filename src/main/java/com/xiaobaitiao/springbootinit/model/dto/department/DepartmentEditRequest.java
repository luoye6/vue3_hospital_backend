package com.xiaobaitiao.springbootinit.model.dto.department;

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
 * 编辑科室表请求
 *
 * @author 程序员小白条
 * @from <a href="https://luoye6.github.io/"> 个人博客
 */
@Data
public class DepartmentEditRequest implements Serializable {

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



    private static final long serialVersionUID = 1L;
}