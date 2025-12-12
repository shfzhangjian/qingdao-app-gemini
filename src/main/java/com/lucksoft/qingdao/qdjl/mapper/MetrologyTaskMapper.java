package com.lucksoft.qingdao.qdjl.mapper;

import com.lucksoft.qingdao.tmis.metrology.dto.MetrologyTaskDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.TaskQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.UpdateTaskRequestDTO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;
import java.util.Map;

@Mapper
public interface MetrologyTaskMapper {


    @Results(id = "metrologyTaskResultMap", value = {
            @Result(property = "indocno", column = "INDOCNO"),
            @Result(property = "dupcheck", column = "DUPCHECK"),
            @Result(property = "idjstate", column = "IDJSTATE"),
            @Result(property = "sjno", column = "SJNO"),
            @Result(property = "sjname", column = "SJNAME"),
            @Result(property = "sggxh", column = "SGGXH"),
            @Result(property = "sfactoryno", column = "SFACTORYNO"),
            @Result(property = "splace", column = "SPLACE"),
            @Result(property = "slevel", column = "SLEVEL"),
            @Result(property = "istate", column = "ISTATE"),
            @Result(property = "sabc", column = "SABC"),
            @Result(property = "scheckresult", column = "SCHECKRESULT"),
            @Result(property = "scheckremark", column = "SCHECKREMARK"),
            @Result(property = "susedept", column = "SUSEDEPT"),
            @Result(property = "slc", column = "SLC"),
            @Result(property = "erpId", column = "SERPNO"),
            @Result(property = "dinit", column = "DINIT"),
            @Result(property = "sproduct", column = "SPRODUCT"),
            @Result(property = "suser", column = "SUSER"),
            @Result(property = "seq", column = "SEQ"),
            @Result(property = "scheckuser", column = "SCHECKUSER"),
            // [新增] 异常信息映射
            @Result(property = "exceptionDesc", column = "EXCEPTION_DESC"),
            @Result(property = "reportTime", column = "REPORT_TIME"),
            @Result(property = "reporterName", column = "REPORTER_NAME"),
            // [新增] 关联异常ID字段映射
            @Result(property = "exceptionId", column = "EXCEPTION_ID")
    })
    @Select("<script>" +
            "SELECT " +
            "   A.INDOCNO, A.DUPCHECK, A.IDJSTATE, A.SJNO, A.SJNAME, A.SGGXH, " +
            "   A.SFACTORYNO, A.SPLACE, A.SLEVEL, A.ISTATE, A.SABC, " +
            "   A.SCHECKRESULT, A.SCHECKREMARK, A.SUSEDEPT, A.SLC, " +
            "   A.SERPNO, A.DINIT, A.SPRODUCT, A.SUSER, A.SEQ, A.SCHECKUSER, " +
            "   A.EXCEPTION_ID, " + // 显式选择 A 表的字段，避免歧义
            "   B.EXCEPTION_DESC, B.REPORT_TIME, B.REPORTER_NAME " +
            "FROM V_JL_EQUIP_DXJ A " +
            "LEFT JOIN T_METROLOGY_EXCEPTION B ON A.EXCEPTION_ID = B.ID " +
            "<where>" +
            "   A.ISTATE &lt;&gt; '备用' " +
            "   <if test='deviceName != null and deviceName != \"\"'>" +
            "       AND A.SJNAME LIKE '%' || #{deviceName} || '%'" +
            "   </if>" +
            "   <if test='enterpriseId != null and enterpriseId != \"\"'>" +
            "       AND A.SJNO LIKE '%' || #{enterpriseId} || '%'" +
            "   </if>" +
            "   <if test='abcCategory != null and abcCategory != \"\" and abcCategory != \"all\"'>" +
            "       AND A.SABC = #{abcCategory}" +
            "   </if>" +
            "   <if test='taskStatus != null and taskStatus != \"\" and taskStatus != \"all\"'>" +
            "       AND A.IDJSTATE = #{taskStatus}" +
            "   </if>" +
            "   <if test='exceptionStatus != null and exceptionStatus == \"abnormal\"'>" +
            "       AND A.SCHECKRESULT = '异常'" +
            "   </if>" +
            "   <if test='dateRange != null and dateRange != \"\"'>" +
            "       <bind name='startDate' value=\"dateRange.split(' 至 ')[0]\" />" +
            "       <bind name='endDate' value=\"dateRange.split(' 至 ')[1]\" />" +
            "       AND A.DUPCHECK &gt;= TO_DATE(#{startDate}, 'YYYY-MM-DD') AND A.DUPCHECK &lt;= TO_DATE(#{endDate}, 'YYYY-MM-DD')" +
            "   </if>" +
            "   <if test='loginId != null and loginId != \"\"'>" +
            "       AND EXISTS (" +
            "           SELECT 1 FROM USERS B " +
            "           WHERE B.LOGINID = #{loginId} " +
            "           AND INSTR(" +
            "               decode('','true', ','||B.IDEPTSEE||',', '', ','||B.DEP_SCOPE_ID||',')," +
            "               ','||A.IUSEDEPT||','" +
            "           ) > 0" +
            "       )" +
            "   </if>" +

            "</where>" +
            "ORDER BY A.DINIT DESC, A.IDJSTATE, A.SJNO" +
            "</script>")
    List<MetrologyTaskDTO> findTasksByCriteria(TaskQuery query);

    @Update("<script>" +
            "UPDATE JL_EQUIP_DXJ " +
            "<set>" +
            "   IDJSTATE = 1, " + // 1: 已检
            "   <if test='req.checkResult != null'>SCHECKRESULT = #{req.checkResult},</if>" +
            "   <if test='req.checkRemark != null'>SCHECKREMARK = #{req.checkRemark},</if>" +
            "   DCHECK = SYSDATE " + // 更新点检日期为当前时间
            "</set>" +
            "WHERE INDOCNO IN " +
            "<foreach item='id' collection='req.ids' open='(' separator=',' close=')'>" +
            "   #{id, jdbcType=NUMERIC}" +
            "</foreach>" +
            "</script>")
    int updateTask(@Param("req") UpdateTaskRequestDTO req);

    @Select("SELECT DISTINCT ${columnName} FROM V_JL_EQUIP_DXJ WHERE ${columnName} IS NOT NULL ORDER BY NLSSORT(${columnName}, 'NLS_SORT=SCHINESE_PINYIN_M')")
    List<String> findDistinctValues(@Param("columnName") String columnName);

    /**
     * 调用旧的通用更新过程 (用于正常提交)
     */
    @Update("{CALL tmis.update_jl_task(" +
            "#{idsStr, jdbcType=VARCHAR, mode=IN}, " +
            "#{result, jdbcType=VARCHAR, mode=IN}, " +
            "#{remark, jdbcType=VARCHAR, mode=IN}, " +
            "#{loginId, jdbcType=VARCHAR, mode=IN}, " +
            "#{userName, jdbcType=VARCHAR, mode=IN}" +
            ")}")
    @Options(statementType = StatementType.CALLABLE)
    void callUpdateJlTaskProcedure(
            @Param("idsStr") String idsStr,
            @Param("result") String result,
            @Param("remark") String remark,
            @Param("loginId") String loginId,
            @Param("userName") String userName
    );

    /**
     * [新增] 调用批量异常提报存储过程
     */
    @Select(value = "{call PKG_METROLOGY.P_REPORT_BATCH_EXCEPTION(" +
            "#{idsStr, mode=IN, jdbcType=VARCHAR}, " +
            "#{reason, mode=IN, jdbcType=VARCHAR}, " +
            "#{loginId, mode=IN, jdbcType=VARCHAR}, " +
            "#{userName, mode=IN, jdbcType=VARCHAR}, " +
            "#{outCount, mode=OUT, jdbcType=INTEGER}, " +
            "#{outMsg, mode=OUT, jdbcType=VARCHAR})}")
    @Options(statementType = StatementType.CALLABLE)
    void callBatchReportException(Map<String, Object> params);
}