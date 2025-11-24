package com.lucksoft.qingdao.selfinspection.mapper;

import com.lucksoft.qingdao.selfinspection.entity.SiTaskDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 点检任务明细 Mapper
 * 对应表: T_SI_TASK_DETAIL
 */
@Mapper
public interface SiTaskDetailMapper {

    @Select("SELECT * FROM T_SI_TASK_DETAIL WHERE TASK_ID = #{taskId} ORDER BY ID ASC")
    @Results({
            @Result(property = "id", column = "ID", id = true),
            @Result(property = "taskId", column = "TASK_ID"),
            @Result(property = "mainDevice", column = "MAIN_DEVICE"),
            @Result(property = "itemName", column = "ITEM_NAME"),
            @Result(property = "result", column = "CHECK_RESULT"),
            @Result(property = "remarks", column = "CHECK_REMARKS"),
            @Result(property = "checkTime", column = "CHECK_TIME"),
            @Result(property = "isConfirmed", column = "IS_CONFIRMED"),
            @Result(property = "confirmTime", column = "CONFIRM_TIME")
    })
    List<SiTaskDetail> findByTaskId(Long taskId);

    @Insert("<script>" +
            "INSERT INTO T_SI_TASK_DETAIL (ID, TASK_ID, MAIN_DEVICE, ITEM_NAME, CHECK_RESULT, CHECK_REMARKS, CHECK_TIME, IS_CONFIRMED, CONFIRM_TIME) " +
            "SELECT SEQ_SI_TASK_DETAIL.NEXTVAL, A.* FROM (" +
            "<foreach collection='list' item='item' separator='UNION ALL'>" +
            " SELECT #{item.taskId}, #{item.mainDevice}, #{item.itemName}, #{item.result}, #{item.remarks}, #{item.checkTime}, #{item.isConfirmed}, #{item.confirmTime} FROM DUAL" +
            "</foreach>" +
            ") A" +
            "</script>")
    int batchInsert(@Param("list") List<SiTaskDetail> details);

    @Update("UPDATE T_SI_TASK_DETAIL SET " +
            "CHECK_RESULT=#{result, jdbcType=VARCHAR}, " +
            "CHECK_REMARKS=#{remarks, jdbcType=VARCHAR}, " +
            "CHECK_TIME=#{checkTime, jdbcType=TIMESTAMP}, " +
            "IS_CONFIRMED=#{isConfirmed, jdbcType=NUMERIC}, " +
            "CONFIRM_TIME=#{confirmTime, jdbcType=TIMESTAMP} " +
            "WHERE ID=#{id}")
    int update(SiTaskDetail detail);

    /**
     * [新增] 统计查询专用接口
     * 关联主表和明细表，返回扁平化数据 Map
     */
    @Select("<script>" +
            "SELECT " +
            "   t.TASK_TIME as \"checkTime\", " +
            "   t.DEVICE as \"device\", " +
            "   d.ITEM_NAME as \"itemName\", " +
            "   d.CHECK_RESULT as \"result\", " +
            "   d.CHECK_REMARKS as \"remarks\", " +
            "   t.PROD_STATUS as \"prodStatus\", " +
            "   t.SHIFT as \"shift\", " +
            "   t.SHIFT_TYPE as \"shiftType\", " +
            "   t.TASK_TYPE as \"taskType\", " +
            "   d.CHECK_TIME as \"actualCheckTime\", " +
            "   t.CHECKER as \"checker\", " +
            "   t.CONFIRM_STATUS as \"confirmStatus\", " +
            "   d.CONFIRM_TIME as \"confirmTime\", " +
            "   t.CONFIRMER as \"confirmer\" " +
            "FROM T_SI_TASK_DETAIL d " +
            "JOIN T_SI_TASK t ON d.TASK_ID = t.ID " +
            "<where>" +
            "   <if test='params.device != null and params.device != \"\"'>AND t.DEVICE LIKE '%' || #{params.device} || '%'</if>" +
            // 注意：前端传来的 checkStatus (正常/异常) 对应的是明细表的 CHECK_RESULT
            "   <if test='params.checkStatus != null and params.checkStatus != \"\"'>AND d.CHECK_RESULT = #{params.checkStatus}</if>" +
            "   <if test='params.prodStatus != null and params.prodStatus != \"\"'>AND t.PROD_STATUS = #{params.prodStatus}</if>" +
            "   <if test='params.shiftType != null and params.shiftType != \"\"'>AND t.SHIFT_TYPE = #{params.shiftType}</if>" +
            "   <if test='params.shift != null and params.shift != \"\"'>AND t.SHIFT = #{params.shift}</if>" +
            // 时间范围过滤
            "   <if test='params.startDate != null and params.startDate != \"\"'>" +
            "       AND t.TASK_TIME &gt;= TO_DATE(#{params.startDate}, 'yyyy-MM-dd')" +
            "   </if>" +
            "   <if test='params.endDate != null and params.endDate != \"\"'>" +
            "       AND t.TASK_TIME &lt;= TO_DATE(#{params.endDate} || ' 23:59:59', 'yyyy-MM-dd HH24:mi:ss')" +
            "   </if>" +
            "</where>" +
            "ORDER BY t.TASK_TIME DESC, t.ID DESC, d.ID ASC" +
            "</script>")
    List<Map<String, Object>> findStatsList(@Param("params") Map<String, Object> params);
}