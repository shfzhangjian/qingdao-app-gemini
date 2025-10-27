package com.lucksoft.qingdao.qdjl.mapper;

import com.lucksoft.qingdao.tmis.metrology.dto.MetrologyTaskDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.TaskQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.UpdateTaskRequestDTO;
import org.apache.ibatis.annotations.*;

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
}

