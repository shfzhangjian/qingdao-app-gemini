package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.tspm.dto.UserEquipmentDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 专门用于查询 V_USER_EQUIPMENT_TODAY 视图
 */
@Mapper
public interface VUserEquipmentMapper {

    /**
     * 根据时间条件查询包机信息
     * 假设 shiftStartTime 是字符串格式的日期，或者数据库支持直接比较
     */
    @Results(id = "userEquipmentResultMap", value = {
            @Result(property = "userCode", column = "userCode"),
            @Result(property = "equipmentCode", column = "equipmentCode"),
            @Result(property = "shiftStartTime", column = "shiftStartTime"),
            @Result(property = "shiftId", column = "shiftId"),
            @Result(property = "shiftName", column = "shiftName"),
            @Result(property = "type", column = "type")
    })
    @Select("<script>" +
            "SELECT * FROM V_USER_EQUIPMENT_TODAY " +
            "<where>" +
            "   <if test='lastSyncDateTime != null and lastSyncDateTime != \"\"'>" +
            "       AND \"shiftStartTime\" &gt;= #{lastSyncDateTime}" +
            "   </if>" +
            "</where>" +
            "ORDER BY \"shiftStartTime\" DESC" +
            "</script>")
    List<UserEquipmentDTO> findUserEquipmentsByCondition(@Param("lastSyncDateTime") String lastSyncDateTime);
}