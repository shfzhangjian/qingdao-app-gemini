package com.lucksoft.qingdao.qdjl.mapper;

import com.lucksoft.qingdao.tmis.metrology.dto.MetrologyTaskDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.TaskQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.UpdateTaskRequestDTO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;

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
            @Result(property = "scheckuser", column = "SCHECKUSER")
    })
    @Select("<script>" +
            "SELECT * FROM V_JL_EQUIP_DXJ " +
            "<where>" +
            "   ISTATE &lt;&gt; '备用' " +
            "   <if test='deviceName != null and deviceName != \"\"'>" +
            "       AND SJNAME LIKE '%' || #{deviceName} || '%'" +
            "   </if>" +
            "   <if test='enterpriseId != null and enterpriseId != \"\"'>" +
            "       AND SJNO LIKE '%' || #{enterpriseId} || '%'" +
            "   </if>" +
            "   <if test='abcCategory != null and abcCategory != \"\" and abcCategory != \"all\"'>" +
            "       AND SABC = #{abcCategory}" +
            "   </if>" +
            "   <if test='taskStatus != null and taskStatus != \"\" and taskStatus != \"all\"'>" +
            "       AND IDJSTATE = #{taskStatus}" +
            "   </if>" +
            "   <if test='dateRange != null and dateRange != \"\"'>" +
            "       <bind name='startDate' value=\"dateRange.split(' 至 ')[0]\" />" +
            "       <bind name='endDate' value=\"dateRange.split(' 至 ')[1]\" />" +
            "       AND DUPCHECK &gt;= TO_DATE(#{startDate}, 'YYYY-MM-DD') AND DUPCHECK &lt;= TO_DATE(#{endDate}, 'YYYY-MM-DD')" +
            "   </if>" +

//            // 假设 SUSER 存储的是责任人的登录账号 (loginid)
//            "   <if test='loginId != null and loginId != \"\"'>" +
//            "       AND SUSER = #{loginId}" +
//            "   </if>" +

            "</where>" +
            "ORDER BY DINIT DESC, IDJSTATE, SJNO" +
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

    /**
     * 查询指定列的去重值，用于前端下拉补全。
     * 注意：columnName 由 Service 层校验，防止 SQL 注入。
     */
    @Select("SELECT DISTINCT ${columnName} FROM V_JL_EQUIP_DXJ WHERE ${columnName} IS NOT NULL ORDER BY NLSSORT(${columnName}, 'NLS_SORT=SCHINESE_PINYIN_M')")
    List<String> findDistinctValues(@Param("columnName") String columnName);

    /**
     * [新增] 调用 tmis.update_jl_task 存储过程。
     * 假设存储过程签名为:
     * PROCEDURE update_jl_task(
     * p_ids_str IN VARCHAR2,      -- 逗号分隔的ID字符串
     * p_result  IN VARCHAR2,      -- 检查结果 (正常/异常)
     * p_remark  IN VARCHAR2,      -- 备注/异常描述
     * p_loginid IN VARCHAR2,      -- 操作员账号
     * p_username IN VARCHAR2      -- 操作员姓名
     * )
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
}