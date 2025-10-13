package com.lucksoft.qingdao.system.mapper;

import com.lucksoft.qingdao.system.entity.FieldValue;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 数据字典明细数据访问层接口 (使用注解)
 *
 * @author Gemini
 */
@Mapper
public interface FieldValueMapper {

    @Results(id = "fieldValueResultMap", value = {
            @Result(property = "id", column = "ID", id = true),
            @Result(property = "fsid", column = "FSID"),
            @Result(property = "sitemno", column = "SITEMNO"),
            @Result(property = "svalue", column = "SVALUE"),
            @Result(property = "smemo", column = "SMEMO"),
            @Result(property = "ienabled", column = "IENABLED")
    })
    @Select("SELECT * FROM FIELDVALUE WHERE FSID = #{fieldId} ORDER BY SITEMNO ASC")
    List<FieldValue> findByFieldId(@Param("fieldId") Long fieldId);

    @ResultMap("fieldValueResultMap")
    @Select("SELECT * FROM FIELDVALUE WHERE ID = #{id}")
    FieldValue findById(@Param("id") Integer id);

    @Insert("INSERT INTO FIELDVALUE (ID, FSID, SITEMNO, SVALUE, SMEMO, IENABLED) " +
            "VALUES (#{id}, #{fsid}, #{sitemno}, #{svalue}, #{smemo}, #{ienabled})")
    int insert(FieldValue fieldValue);

    @Update("<script>" +
            "UPDATE FIELDVALUE" +
            "<set>" +
            "   <if test='sitemno != null'>SITEMNO = #{sitemno},</if>" +
            "   <if test='svalue != null'>SVALUE = #{svalue},</if>" +
            "   <if test='smemo != null'>SMEMO = #{smemo},</if>" +
            "   <if test='ienabled != null'>IENABLED = #{ienabled},</if>" +
            "</set>" +
            "WHERE ID = #{id}" +
            "</script>")
    int update(FieldValue fieldValue);

    @Delete("DELETE FROM FIELDVALUE WHERE ID = #{id}")
    int deleteById(@Param("id") Integer id);
}
