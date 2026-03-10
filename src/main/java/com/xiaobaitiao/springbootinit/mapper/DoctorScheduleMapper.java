package com.xiaobaitiao.springbootinit.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaobaitiao.springbootinit.model.entity.DoctorSchedule;
import com.xiaobaitiao.springbootinit.model.vo.DoctorScheduleRankVO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* @author zhao9
* @description 针对表【doctor_schedule】的数据库操作Mapper
* @createDate 2025-04-19 15:58:20
* @Entity generator.domain.DoctorSchedule
*/
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {
    /**
     * 获取医生出诊次数排行榜TOP10
     * @return 医生出诊次数排行TOP10列表
     */
    @Select("SELECT ds.doctorId, d.doctorName as doctorName, COUNT(*) as scheduleCount " +
            "FROM doctor_schedule ds " +
            "LEFT JOIN doctor d ON ds.doctorId = d.id " +
            "WHERE ds.isDelete = 0 AND ds.isEnabled = 1 " +
            "GROUP BY ds.doctorId, d.doctorName " +
            "ORDER BY scheduleCount DESC " +
            "LIMIT 10")
    List<DoctorScheduleRankVO> getDoctorScheduleRankTop10();
}




