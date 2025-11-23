package com.lucksoft.qingdao.selfinspection.mapper;

import com.lucksoft.qingdao.selfinspection.entity.SiTask;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 点检任务 Mapper
 * 对应表: T_SI_TASK
 */
@Mapper
public interface SiTaskMapper {

    @Select("<script>" +
            "SELECT * FROM T_SI_TASK " +
            "<where>" +
            "   <if test='params.device != null and params.device != \"\"'>AND DEVICE LIKE '%' || #{params.device} || '%'</if>" +
            "   <if test='params.prodStatus != null and params.prodStatus != \"\"'>AND PROD_STATUS = #{params.prodStatus}</if>" +
            "   <if test='params.shiftType != null and params.shiftType != \"\"'>AND SHIFT_TYPE = #{params.shiftType}</if>" +
            "   <if test='params.shift != null and params.shift != \"\"'>AND SHIFT = #{params.shift}</if>" +
            "   <if test='params.checkStatus != null and params.checkStatus != \"\"'>AND CHECK_STATUS = #{params.checkStatus}</if>" +
            "   <if test='params.confirmStatus != null and params.confirmStatus != \"\"'>AND CONFIRM_STATUS = #{params.confirmStatus}</if>" +
            "   <if test='params.checkTime != null and params.checkTime != \"\"'>AND TO_CHAR(TASK_TIME, 'yyyy-MM-dd') = #{params.checkTime}</if>" +
            "   <if test='params.checker != null and params.checker != \"\"'>AND CHECKER LIKE '%' || #{params.checker} || '%'</if>" +
            "   <if test='params.confirmer != null and params.confirmer != \"\"'>AND CONFIRMER LIKE '%' || #{params.confirmer} || '%'</if>" +
            "</where>" +
            "ORDER BY TASK_TIME DESC, ID DESC" +
            "</script>")
    @Results(id = "siTaskMap", value = {
            @Result(property = "id", column = "ID", id = true),
            @Result(property = "ledgerId", column = "LEDGER_ID"),
            @Result(property = "model", column = "MODEL"),
            @Result(property = "device", column = "DEVICE"),
            @Result(property = "prodStatus", column = "PROD_STATUS"),
            @Result(property = "shiftType", column = "SHIFT_TYPE"),
            @Result(property = "shift", column = "SHIFT"),
            @Result(property = "checkStatus", column = "CHECK_STATUS"),
            @Result(property = "confirmStatus", column = "CONFIRM_STATUS"),
            @Result(property = "taskTime", column = "TASK_TIME"),
            @Result(property = "taskType", column = "TASK_TYPE"),
            @Result(property = "isOverdue", column = "IS_OVERDUE"),
            @Result(property = "checker", column = "CHECKER"),
            @Result(property = "checkTime", column = "CHECK_TIME"),
            @Result(property = "confirmer", column = "CONFIRMER"),
            @Result(property = "confirmTime", column = "CONFIRM_TIME")
    })
    List<SiTask> findList(@Param("params") Map<String, Object> params);

    @Select("SELECT * FROM T_SI_TASK WHERE ID = #{id}")
    @ResultMap("siTaskMap")
    SiTask findById(Long id);

    @Insert("INSERT INTO T_SI_TASK (ID, LEDGER_ID, MODEL, DEVICE, PROD_STATUS, SHIFT_TYPE, SHIFT, CHECK_STATUS, CONFIRM_STATUS, TASK_TIME, TASK_TYPE, IS_OVERDUE) " +
            "VALUES (SEQ_SI_TASK.NEXTVAL, #{ledgerId}, #{model}, #{device}, #{prodStatus}, #{shiftType}, #{shift}, #{checkStatus}, #{confirmStatus}, #{taskTime}, #{taskType}, #{isOverdue})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "ID")
    int insert(SiTask task);

    /**
     * [修复] 为所有可能为 NULL 的字段显式指定 jdbcType。
     * Oracle 驱动在参数为 null 时必须知道其类型。
     * - String -> VARCHAR
     * - Date   -> TIMESTAMP
     */
    @Update("UPDATE T_SI_TASK SET " +
            "CHECK_STATUS=#{checkStatus, jdbcType=VARCHAR}, " +
            "CONFIRM_STATUS=#{confirmStatus, jdbcType=VARCHAR}, " +
            "CHECKER=#{checker, jdbcType=VARCHAR}, " +
            "CHECK_TIME=#{checkTime, jdbcType=TIMESTAMP}, " +
            "CONFIRMER=#{confirmer, jdbcType=VARCHAR}, " +
            "CONFIRM_TIME=#{confirmTime, jdbcType=TIMESTAMP} " +
            "WHERE ID=#{id}")
    int updateStatus(SiTask task);
}