package com.lucksoft.qingdao.system.mapper;

import com.lucksoft.qingdao.system.entity.Depart;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 部门数据访问层接口 (使用注解)
 *
 * @author Gemini
 */
@Mapper
public interface DepartMapper {

    @Results(id = "departResultMap", value = {
            @Result(property = "sdepId", column = "SDEP_ID", id = true),
            @Result(property = "sdepName", column = "SDEP_NAME"),
            @Result(property = "sparentDepId", column = "SPARENT_DEP_ID"),
            @Result(property = "sdepSm", column = "SDEP_SM"),
            @Result(property = "sdepFullname", column = "SDEP_FULLNAME"),
            @Result(property = "iclass", column = "ICLASS"),
            @Result(property = "sregid", column = "SREGID"),
            @Result(property = "sregnm", column = "SREGNM"),
            @Result(property = "dregt", column = "DREGT"),
            @Result(property = "smodid", column = "SMODID"),
            @Result(property = "smodnm", column = "SMODNM"),
            @Result(property = "dmodt", column = "DMODT")
    })
    @Select("<script>" +
            "SELECT * FROM TDEPART" +
            "<where>" +
            "   <if test=\"params.sdepName != null and params.sdepName != ''\">" +
            "       AND SDEP_NAME LIKE '%' || #{params.sdepName} || '%'" +
            "   </if>" +
            "</where>" +
            "ORDER BY SDEP_ID ASC" +
            "</script>")
    List<Depart> findAll(@Param("params") Map<String, Object> params);

    @Select("<script>" +
            "SELECT count(*) FROM TDEPART" +
            "<where>" +
            "   <if test=\"params.sdepName != null and params.sdepName != ''\">" +
            "       AND SDEP_NAME LIKE '%' || #{params.sdepName} || '%'" +
            "   </if>" +
            "</where>" +
            "</script>")
    long countAll(@Param("params") Map<String, Object> params);

    @ResultMap("departResultMap")
    @Select("SELECT * FROM TDEPART WHERE SDEP_ID = #{id}")
    Depart findById(@Param("id") String id);

    @Insert("INSERT INTO TDEPART (SDEP_ID, SDEP_NAME, SPARENT_DEP_ID, SDEP_FULLNAME, DREGT) " +
            "VALUES (#{sdepId}, #{sdepName}, #{sparentDepId}, #{sdepFullname}, SYSDATE)")
    int insert(Depart depart);

    @Update("<script>" +
            "UPDATE TDEPART" +
            "<set>" +
            "   <if test='sdepName != null'>SDEP_NAME = #{sdepName},</if>" +
            "   <if test='sparentDepId != null'>SPARENT_DEP_ID = #{sparentDepId},</if>" +
            "   <if test='sdepFullname != null'>SDEP_FULLNAME = #{sdepFullname},</if>" +
            "   DMODT = SYSDATE" +
            "</set>" +
            "WHERE SDEP_ID = #{sdepId}" +
            "</script>")
    int update(Depart depart);

    @Delete("DELETE FROM TDEPART WHERE SDEP_ID = #{id}")
    int deleteById(@Param("id") String id);
}
