package com.lucksoft.qingdao.selfinspection.mapper;

import com.lucksoft.qingdao.selfinspection.entity.ZjzkTask;
import com.lucksoft.qingdao.selfinspection.entity.ZjzkTaskDetail;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;
import java.util.Map;

@Mapper
public interface ZjzkTaskDetailMapper {

    // 关联查询台账表获取SNAME作为ToolName
    @Results(id = "zjzkDetailMap", value = {
            @Result(property = "indocno", column = "INDOCNO", id = true),
            @Result(property = "taskId", column = "TASK_ID"),
            @Result(property = "toolId", column = "TOOL_ID"),
            @Result(property = "itemName", column = "ITEM_NAME"),
            @Result(property = "checkResult", column = "CHECK_RESULT"),
            @Result(property = "checkRemark", column = "CHECK_REMARK"),
            @Result(property = "isConfirmed", column = "IS_CONFIRMED"),
            @Result(property = "operatorName", column = "OPERATOR_NAME"),
            @Result(property = "opTime", column = "OP_TIME"),
            @Result(property = "confirmName", column = "CONFIRM_NAME"),
            @Result(property = "confirmOpTime", column = "CONFIRM_OP_TIME"),
            // 关联字段
            @Result(property = "toolName", column = "TOOL_NAME"),
            @Result(property = "toolModel", column = "TOOL_MODEL")
    })
    @Select("SELECT d.*, t.SNAME as TOOL_NAME, t.SXH as TOOL_MODEL " +
            "FROM ZJZK_TASK_DETAIL d " +
            "LEFT JOIN ZJZK_TOOL t ON d.TOOL_ID = t.INDOCNO " +
            "WHERE d.TASK_ID = #{taskId} ORDER BY d.INDOCNO ASC")
    List<ZjzkTaskDetail> findByTaskId(Long taskId);

    // [修复] 为 batchInsert 中的字段添加 jdbcType=VARCHAR，防止插入 null 时报错
    @Insert("<script>" +
            "INSERT INTO ZJZK_TASK_DETAIL (INDOCNO, TASK_ID, TOOL_ID, ITEM_NAME, CHECK_RESULT, IS_CONFIRMED) " +
            "SELECT SEQ_ZJZK_TASK_DETAIL.NEXTVAL, A.* FROM (" +
            "<foreach collection='list' item='item' separator='UNION ALL'>" +
            " SELECT #{item.taskId}, #{item.toolId}, #{item.itemName, jdbcType=VARCHAR}, #{item.checkResult, jdbcType=VARCHAR}, 0 FROM DUAL" +
            "</foreach>" +
            ") A" +
            "</script>")
    int batchInsert(@Param("list") List<ZjzkTaskDetail> details);

    // [重要修复] 改为动态 SQL 更新 (<set> + <if>)。
    // 这样执行点检时(checkResult, opTime)不会覆盖清空确认信息(confirmName等)，反之亦然。
    @Update("<script>" +
            "UPDATE ZJZK_TASK_DETAIL " +
            "<set>" +
            "   <if test='checkResult != null'>CHECK_RESULT = #{checkResult, jdbcType=VARCHAR},</if>" +
            "   <if test='checkRemark != null'>CHECK_REMARK = #{checkRemark, jdbcType=VARCHAR},</if>" +
            "   <if test='operatorName != null'>OPERATOR_NAME = #{operatorName, jdbcType=VARCHAR},</if>" +
            "   <if test='opTime != null'>OP_TIME = #{opTime, jdbcType=TIMESTAMP},</if>" +
            "   <if test='isConfirmed != null'>IS_CONFIRMED = #{isConfirmed, jdbcType=NUMERIC},</if>" +
            "   <if test='confirmName != null'>CONFIRM_NAME = #{confirmName, jdbcType=VARCHAR},</if>" +
            "   <if test='confirmOpTime != null'>CONFIRM_OP_TIME = #{confirmOpTime, jdbcType=TIMESTAMP},</if>" +
            "</set>" +
            "WHERE INDOCNO = #{indocno}" +
            "</script>")
    int update(ZjzkTaskDetail detail);


    /**
     * [新增] 统计查询专用接口 (适配 ZJZK 表结构)
     * 1. 关联 ZJZK_TASK (t) 和 ZJZK_TASK_DETAIL (d)
     * 2. 字段映射：
     * - device -> t.SFNAME (所属设备)
     * - remarks -> d.CHECK_REMARK
     * - actualCheckTime -> d.OP_TIME
     */
    @Select("<script>" +
            "SELECT " +
            "   t.TASK_TIME as \"checkTime\", " +
            "   t.SFNAME as \"device\", " +
            "   d.ITEM_NAME as \"itemName\", " +
            "   d.CHECK_RESULT as \"result\", " +
            "   d.CHECK_REMARK as \"remarks\", " +
            "   t.PROD_STATUS as \"prodStatus\", " +
            "   t.SHIFT as \"shift\", " +
            "   t.SHIFT_TYPE as \"shiftType\", " +
            "   t.TASK_TYPE as \"taskType\", " +
            "   d.OP_TIME as \"actualCheckTime\", " +
            "   t.CHECKER as \"checker\", " +
            "   t.CONFIRM_STATUS as \"confirmStatus\", " +
            "   d.CONFIRM_OP_TIME as \"confirmTime\", " +
            "   t.CONFIRMER as \"confirmer\" " +
            "FROM ZJZK_TASK_DETAIL d " +
            "JOIN ZJZK_TASK t ON d.TASK_ID = t.INDOCNO " +
            "<where>" +
            "   <if test='params.device != null and params.device != \"\"'>AND t.SFNAME LIKE '%' || #{params.device} || '%'</if>" +
            "   <if test='params.checkStatus != null and params.checkStatus != \"\"'>AND d.CHECK_RESULT = #{params.checkStatus}</if>" +
            "   <if test='params.prodStatus != null and params.prodStatus != \"\"'>AND t.PROD_STATUS = #{params.prodStatus}</if>" +
            "   <if test='params.shiftType != null and params.shiftType != \"\"'>AND t.SHIFT_TYPE = #{params.shiftType}</if>" +
            "   <if test='params.shift != null and params.shift != \"\"'>AND t.SHIFT = #{params.shift}</if>" +
            "   <if test='params.startDate != null and params.startDate != \"\"'>" +
            "       AND t.TASK_TIME &gt;= TO_DATE(#{params.startDate}, 'yyyy-MM-dd')" +
            "   </if>" +
            "   <if test='params.endDate != null and params.endDate != \"\"'>" +
            "       AND t.TASK_TIME &lt;= TO_DATE(#{params.endDate} || ' 23:59:59', 'yyyy-MM-dd HH24:mi:ss')" +
            "   </if>" +
            "</where>" +
            "ORDER BY t.TASK_TIME DESC, t.INDOCNO DESC, d.INDOCNO ASC" +
            "</script>")
    List<Map<String, Object>> findStatsList(@Param("params") Map<String, Object> params);
}