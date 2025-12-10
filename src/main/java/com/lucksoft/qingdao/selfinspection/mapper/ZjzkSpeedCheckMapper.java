package com.lucksoft.qingdao.selfinspection.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

@Mapper
public interface ZjzkSpeedCheckMapper {

    @Insert("<script>" +
            "INSERT INTO ZJZK_SPEED_CHECK (INDOCNO, BATCH_NO, SPMCODE, AVG_SPEED, START_TIME, END_TIME) " +
            "SELECT SEQ_ZJZK_SPEED_CHECK.NEXTVAL, A.* FROM (" +
            "<foreach collection='list' item='item' separator='UNION ALL'>" +
            " SELECT #{item.batchNo}, #{item.spmcode}, #{item.avgSpeed}, #{item.startTime}, #{item.endTime} FROM DUAL" +
            "</foreach>" +
            ") A" +
            "</script>")
    int batchInsert(@Param("list") List<Map<String, Object>> list);

    @Select("<script>" +
            "SELECT * FROM ZJZK_SPEED_CHECK " +
            "<where>" +
            "   <if test='params.batchNo != null and params.batchNo != \"\"'>" +
            "       AND BATCH_NO = #{params.batchNo}" +
            "   </if>" +
            "   <if test='params.spmcode != null and params.spmcode != \"\"'>" +
            "       AND SPMCODE LIKE '%' || #{params.spmcode} || '%'" +
            "   </if>" +
            "   <!-- 过滤车速范围 (例如: 查找异常车速) -->" +
            "   <if test='params.minSpeed != null'>" +
            "       AND AVG_SPEED &gt;= #{params.minSpeed}" +
            "   </if>" +
            "   <if test='params.maxSpeed != null'>" +
            "       AND AVG_SPEED &lt;= #{params.maxSpeed}" +
            "   </if>" +
            "</where>" +
            "ORDER BY INDOCNO DESC" +
            "</script>")
    @Results({
            @Result(property = "id", column = "INDOCNO", id = true),
            @Result(property = "batchNo", column = "BATCH_NO"),
            @Result(property = "spmcode", column = "SPMCODE"),
            @Result(property = "avgSpeed", column = "AVG_SPEED"),
            @Result(property = "startTime", column = "START_TIME"),
            @Result(property = "endTime", column = "END_TIME")
    })
    List<Map<String, Object>> findRecentRecords(@Param("params") Map<String, Object> params);


    /**
     * [新增] 查询最近的车速检查记录
     */
    @Select("SELECT * FROM ZJZK_SPEED_CHECK ORDER BY INDOCNO DESC FETCH FIRST 100 ROWS ONLY")
    List<Map<String, Object>> findRecentRecords();
}