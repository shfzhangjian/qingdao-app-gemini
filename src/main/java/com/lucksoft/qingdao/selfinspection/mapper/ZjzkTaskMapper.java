package com.lucksoft.qingdao.selfinspection.mapper;

import com.lucksoft.qingdao.selfinspection.entity.ZjzkTask;
import org.apache.ibatis.annotations.*;

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
    @Select("<script>" +
            "SELECT * FROM ZJZK_TASK " +
            "<where>" +
            "   <if test='params.sbname != null and params.sbname != \"\"'>AND SBNAME LIKE '%' || #{params.sbname} || '%'</if>" +
            "   <if test='params.sfname != null and params.sfname != \"\"'>AND SFNAME LIKE '%' || #{params.sfname} || '%'</if>" +
            "   <if test='params.prodStatus != null and params.prodStatus != \"\"'>AND PROD_STATUS = #{params.prodStatus}</if>" +
            "   <if test='params.checkStatus != null and params.checkStatus != \"\"'>AND CHECK_STATUS = #{params.checkStatus}</if>" +
            "   <if test='params.taskTime != null and params.taskTime != \"\"'>AND TO_CHAR(TASK_TIME, 'yyyy-MM-dd') = #{params.taskTime}</if>" +
            "</where>" +
            "ORDER BY TASK_TIME DESC, INDOCNO DESC" +
            "</script>")
    List<ZjzkTask> findList(@Param("params") Map<String, Object> params);

    @ResultMap("zjzkTaskMap")
    @Select("SELECT * FROM ZJZK_TASK WHERE INDOCNO = #{id}")
    ZjzkTask findById(Long id);

    // [修复] 为所有可能为空的字段添加 jdbcType=VARCHAR，解决 Oracle 插入 null 值报错的问题
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
}