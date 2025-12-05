package com.lucksoft.qingdao.selfinspection.mapper;

import com.lucksoft.qingdao.selfinspection.entity.ZjzkTask;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;
import java.util.Map;

@Mapper
public interface ZjzkTaskMapper {

    @Results(id = "zjzkTaskMap", value = {
            @Result(property = "indocno", column = "INDOCNO", id = true),
            @Result(property = "taskNo", column = "TASK_NO"),
            @Result(property = "taskTime", column = "TASK_TIME"),
            @Result(property = "taskType", column = "TASK_TYPE"),
            @Result(property = "prodStatus", column = "PROD_STATUS"),
            @Result(property = "shiftType", column = "SHIFT_TYPE"),
            @Result(property = "shift", column = "SHIFT"),
            @Result(property = "sjx", column = "SJX"),
            @Result(property = "sfname", column = "SFNAME"),
            @Result(property = "sbname", column = "SBNAME"),
            @Result(property = "spmcode", column = "SPMCODE"),
            @Result(property = "checkStatus", column = "CHECK_STATUS"),
            @Result(property = "confirmStatus", column = "CONFIRM_STATUS"),
            @Result(property = "isOverdue", column = "IS_OVERDUE"),
            @Result(property = "checker", column = "CHECKER"),
            @Result(property = "checkTime", column = "CHECK_TIME"),
            @Result(property = "confirmer", column = "CONFIRMER"),
            @Result(property = "confirmTime", column = "CONFIRM_TIME"),
            @Result(property = "createTime", column = "CREATE_TIME")
    })
    /**
     * [修复] 补全了查询条件：
     * - 修复了 prodStatus, checkStatus 等基本过滤。
     * - 新增了 shiftType, shift, confirmStatus, checker, confirmer 的过滤支持。
     * - 调整了 taskTime 的查询逻辑，使用 TO_CHAR 匹配日期。
     */
    @Select("<script>" +
            "SELECT * FROM ZJZK_TASK " +
            "<where>" +
            "   <if test='params.sbname != null and params.sbname != \"\"'>AND SBNAME LIKE '%' || #{params.sbname} || '%'</if>" +
            "   <if test='params.sfname != null and params.sfname != \"\"'>AND SFNAME LIKE '%' || #{params.sfname} || '%'</if>" +

            "   <if test='params.prodStatus != null and params.prodStatus != \"\"'>AND PROD_STATUS = #{params.prodStatus}</if>" +
            "   <if test='params.shiftType != null and params.shiftType != \"\"'>AND SHIFT_TYPE = #{params.shiftType}</if>" +
            "   <if test='params.shift != null and params.shift != \"\"'>AND SHIFT = #{params.shift}</if>" +

            "   <if test='params.checkStatus != null and params.checkStatus != \"\"'>AND CHECK_STATUS = #{params.checkStatus}</if>" +
            "   <if test='params.confirmStatus != null and params.confirmStatus != \"\"'>AND CONFIRM_STATUS = #{params.confirmStatus}</if>" +

            "   <if test='params.checker != null and params.checker != \"\"'>AND CHECKER LIKE '%' || #{params.checker} || '%'</if>" +
            "   <if test='params.confirmer != null and params.confirmer != \"\"'>AND CONFIRMER LIKE '%' || #{params.confirmer} || '%'</if>" +

            "   <if test='params.taskTime != null and params.taskTime != \"\"'>AND TO_CHAR(TASK_TIME, 'yyyy-MM-dd') = #{params.taskTime}</if>" +
            "</where>" +
            "ORDER BY TASK_TIME DESC, INDOCNO DESC" +
            "</script>")
    List<ZjzkTask> findList(@Param("params") Map<String, Object> params);

    @ResultMap("zjzkTaskMap")
    @Select("SELECT * FROM ZJZK_TASK WHERE INDOCNO = #{id}")
    ZjzkTask findById(Long id);

    @Insert("INSERT INTO ZJZK_TASK (INDOCNO, TASK_NO, TASK_TIME, TASK_TYPE, PROD_STATUS, SHIFT_TYPE, SHIFT, SJX, SFNAME, SBNAME, SPMCODE, CHECK_STATUS, CONFIRM_STATUS, IS_OVERDUE, CREATE_TIME) " +
            "VALUES (SEQ_ZJZK_TASK.NEXTVAL, #{taskNo, jdbcType=VARCHAR}, #{taskTime, jdbcType=TIMESTAMP}, #{taskType, jdbcType=VARCHAR}, " +
            "#{prodStatus, jdbcType=VARCHAR}, #{shiftType, jdbcType=VARCHAR}, #{shift, jdbcType=VARCHAR}, " +
            "#{sjx, jdbcType=VARCHAR}, #{sfname, jdbcType=VARCHAR}, #{sbname, jdbcType=VARCHAR}, #{spmcode, jdbcType=VARCHAR}, " +
            "#{checkStatus, jdbcType=VARCHAR}, #{confirmStatus, jdbcType=VARCHAR}, #{isOverdue, jdbcType=VARCHAR}, SYSDATE)")
    @Options(useGeneratedKeys = true, keyProperty = "indocno", keyColumn = "INDOCNO")
    int insert(ZjzkTask task);

    @Update("UPDATE ZJZK_TASK SET CHECK_STATUS=#{checkStatus, jdbcType=VARCHAR}, CONFIRM_STATUS=#{confirmStatus, jdbcType=VARCHAR}, " +
            "CHECKER=#{checker, jdbcType=VARCHAR}, CHECK_TIME=#{checkTime, jdbcType=TIMESTAMP}, " +
            "CONFIRMER=#{confirmer, jdbcType=VARCHAR}, CONFIRM_TIME=#{confirmTime, jdbcType=TIMESTAMP} WHERE INDOCNO=#{indocno}")
    int updateStatus(ZjzkTask task);



    // [新增] 调用自动生成存储过程
    @Select(value = "{call PKG_ZJZK_TASK.P_AUTO_GEN_SHIFT_TASKS(" +
            "#{shiftName, mode=IN, jdbcType=VARCHAR}, " +
            "#{batchNo, mode=IN, jdbcType=VARCHAR}, " +
            "#{userName, mode=IN, jdbcType=VARCHAR}, " +
            "#{outCount, mode=OUT, jdbcType=INTEGER}, " +
            "#{outMsg, mode=OUT, jdbcType=VARCHAR})}")
    @Options(statementType = StatementType.CALLABLE)
    void callAutoGenTask(Map<String, Object> params);

    // [新增] 人工生成任务 (传入JSON)
    @Select(value = "{call PKG_ZJZK_TASK.P_MANUAL_GEN_TASK(" +
            "#{jsonParams, mode=IN, jdbcType=CLOB}, " +
            "#{outCount, mode=OUT, jdbcType=INTEGER}, " +
            "#{outMsg, mode=OUT, jdbcType=VARCHAR})}")
    @Options(statementType = StatementType.CALLABLE)
    void callManualGenTask(Map<String, Object> params);


    // [新增] 按批次号查询任务 (用于推送TIMS)
    @ResultMap("zjzkTaskMap")
    @Select("SELECT * FROM ZJZK_TASK WHERE BATCH_NO = #{batchNo}")
    List<ZjzkTask> findByBatchNo(String batchNo);
}