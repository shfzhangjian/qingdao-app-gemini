package com.lucksoft.qingdao.selfinspection.mapper;

import com.lucksoft.qingdao.selfinspection.entity.ZjzkTaskDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;

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

    @Update("UPDATE ZJZK_TASK_DETAIL SET " +
            "CHECK_RESULT=#{checkResult, jdbcType=VARCHAR}, " +
            "CHECK_REMARK=#{checkRemark, jdbcType=VARCHAR}, " +
            "OPERATOR_NAME=#{operatorName, jdbcType=VARCHAR}, " +
            "OP_TIME=#{opTime, jdbcType=TIMESTAMP}, " +
            "IS_CONFIRMED=#{isConfirmed, jdbcType=NUMERIC}, " +
            "CONFIRM_NAME=#{confirmName, jdbcType=VARCHAR}, " +
            "CONFIRM_OP_TIME=#{confirmOpTime, jdbcType=TIMESTAMP} " +
            "WHERE INDOCNO=#{indocno}")
    int update(ZjzkTaskDetail detail);
}