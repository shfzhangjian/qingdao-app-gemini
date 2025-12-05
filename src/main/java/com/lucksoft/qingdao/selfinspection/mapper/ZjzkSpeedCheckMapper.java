package com.lucksoft.qingdao.selfinspection.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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
}