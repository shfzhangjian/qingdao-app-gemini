package com.lucksoft.qingdao.system.mapper;

import com.lucksoft.qingdao.system.entity.Field;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 数据字典主表数据访问层接口 (使用注解)
 *
 * @author Gemini
 */
@Mapper
public interface FieldMapper {

    @Results(id = "fieldResultMap", value = {
            @Result(property = "id", column = "ID", id = true),
            @Result(property = "stype", column = "STYPE"),
            @Result(property = "sfieldname", column = "SFIELDNAME"),
            @Result(property = "smemo", column = "SMEMO"),
            @Result(property = "slevel", column = "SLEVEL"),
            @Result(property = "vfid", column = "VFID"),
            @Result(property = "itype", column = "ITYPE")
    })
    @Select("<script>" +
            "SELECT * FROM FIELDS" +
            "<where>" +
            "   <if test=\"params.sfieldname != null and params.sfieldname != ''\">" +
            "       AND SFIELDNAME LIKE '%' || #{params.sfieldname} || '%'" +
            "   </if>" +
            "</where>" +
            "ORDER BY ID DESC" +
            "</script>")
    List<Field> findAll(@Param("params") Map<String, Object> params);

    @Select("<script>" +
            "SELECT count(*) FROM FIELDS" +
            "<where>" +
            "   <if test=\"params.sfieldname != null and params.sfieldname != ''\">" +
            "       AND SFIELDNAME LIKE '%' || #{params.sfieldname} || '%'" +
            "   </if>" +
            "</where>" +
            "</script>")
    long countAll(@Param("params") Map<String, Object> params);

    @ResultMap("fieldResultMap")
    @Select("SELECT * FROM FIELDS WHERE ID = #{id}")
    Field findById(@Param("id") Long id);

    @Insert("INSERT INTO FIELDS (ID, STYPE, SFIELDNAME, SMEMO, SLEVEL, VFID, ITYPE) " +
            "VALUES (#{id}, #{stype}, #{sfieldname}, #{smemo}, #{slevel}, #{vfid}, #{itype})")
    int insert(Field field);

    @Update("<script>" +
            "UPDATE FIELDS" +
            "<set>" +
            "   <if test='stype != null'>STYPE = #{stype},</if>" +
            "   <if test='sfieldname != null'>SFIELDNAME = #{sfieldname},</if>" +
            "   <if test='smemo != null'>SMEMO = #{smemo},</if>" +
            "   <if test='slevel != null'>SLEVEL = #{slevel},</if>" +
            "   <if test='vfid != null'>VFID = #{vfid},</if>" +
            "   <if test='itype != null'>ITYPE = #{itype},</if>" +
            "</set>" +
            "WHERE ID = #{id}" +
            "</script>")
    int update(Field field);

    @Delete("DELETE FROM FIELDS WHERE ID = #{id}")
    int deleteById(@Param("id") Long id);
}
