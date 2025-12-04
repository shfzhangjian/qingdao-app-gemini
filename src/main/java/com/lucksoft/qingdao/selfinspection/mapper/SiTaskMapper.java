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

    /**
     * [修复]
     * 1. 移除了 @Param("params")，直接传递 Map。MyBatis 会将 Map 的 key 直接暴露为变量。
     * 2. 在 XML script 中，直接使用 key 名称 (如 device, prodStatus) 而不是 params.device。
     * 3. 这样可以确保 OGNL 表达式正确解析参数。
     */
    @Select("<script>" +
            "SELECT * FROM T_SI_TASK " +
            "<where>" +
            "   <if test='device != null and device != \"\"'>AND DEVICE LIKE '%' || #{device} || '%'</if>" +
            "   <if test='prodStatus != null and prodStatus != \"\"'>AND PROD_STATUS = #{prodStatus}</if>" +
            "   <if test='shiftType != null and shiftType != \"\"'>AND SHIFT_TYPE = #{shiftType}</if>" +
            "   <if test='shift != null and shift != \"\"'>AND SHIFT = #{shift}</if>" +
            "   <if test='checkStatus != null and checkStatus != \"\"'>AND CHECK_STATUS = #{checkStatus}</if>" +
            "   <if test='confirmStatus != null and confirmStatus != \"\"'>AND CONFIRM_STATUS = #{confirmStatus}</if>" +
            "   <if test='checkTime != null and checkTime != \"\"'>AND TO_CHAR(TASK_TIME, 'yyyy-MM-dd') = #{checkTime}</if>" +
            "   <if test='checker != null and checker != \"\"'>AND CHECKER LIKE '%' || #{checker} || '%'</if>" +
            "   <if test='confirmer != null and confirmer != \"\"'>AND CONFIRMER LIKE '%' || #{confirmer} || '%'</if>" +
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
            @Result(property = "checkerId", column = "CHECKER_ID"),
            @Result(property = "checkerNo", column = "CHECKER_NO"),
            @Result(property = "checkTime", column = "CHECK_TIME"),
            @Result(property = "confirmer", column = "CONFIRMER"),
            @Result(property = "confirmerId", column = "CONFIRMER_ID"),
            @Result(property = "confirmerNo", column = "CONFIRMER_NO"),
            @Result(property = "confirmTime", column = "CONFIRM_TIME")
    })
    List<SiTask> findList(Map<String, Object> params);

    @Select("SELECT * FROM T_SI_TASK WHERE ID = #{id}")
    @ResultMap("siTaskMap")
    SiTask findById(Long id);

    @Insert("INSERT INTO T_SI_TASK (ID, LEDGER_ID, MODEL, DEVICE, PROD_STATUS, SHIFT_TYPE, SHIFT, CHECK_STATUS, CONFIRM_STATUS, TASK_TIME, TASK_TYPE, IS_OVERDUE) " +
            "VALUES (SEQ_SI_TASK.NEXTVAL, #{ledgerId}, #{model}, #{device}, #{prodStatus}, #{shiftType}, #{shift}, #{checkStatus}, #{confirmStatus}, #{taskTime}, #{taskType}, #{isOverdue})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "ID")
    int insert(SiTask task);

    @Update("UPDATE T_SI_TASK SET " +
            "CHECK_STATUS=#{checkStatus, jdbcType=VARCHAR}, " +
            "CONFIRM_STATUS=#{confirmStatus, jdbcType=VARCHAR}, " +
            "CHECKER=#{checker, jdbcType=VARCHAR}, " +
            "CHECKER_ID=#{checkerId, jdbcType=NUMERIC}, " +
            "CHECKER_NO=#{checkerNo, jdbcType=VARCHAR}, " +
            "CHECK_TIME=#{checkTime, jdbcType=TIMESTAMP}, " +
            "CONFIRMER=#{confirmer, jdbcType=VARCHAR}, " +
            "CONFIRMER_ID=#{confirmerId, jdbcType=NUMERIC}, " +
            "CONFIRMER_NO=#{confirmerNo, jdbcType=VARCHAR}, " +
            "CONFIRM_TIME=#{confirmTime, jdbcType=TIMESTAMP} " +
            "WHERE ID=#{id}")
    int updateStatus(SiTask task);
}