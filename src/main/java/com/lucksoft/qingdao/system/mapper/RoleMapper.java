package com.lucksoft.qingdao.system.mapper;

import com.lucksoft.qingdao.system.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 角色数据访问层接口 (使用注解)
 *
 * @author Gemini
 */
@Mapper
public interface RoleMapper {

    /**
     * 定义数据库列名到Java实体属性的映射关系
     */
    @Results(id = "roleResultMap", value = {
            @Result(property = "id", column = "ID", id = true),
            @Result(property = "name", column = "NAME"),
            @Result(property = "descn", column = "DESCN"),
            @Result(property = "itype", column = "ITYPE"),
            @Result(property = "isystem", column = "ISYSTEM"),
            @Result(property = "isort", column = "ISORT")
    })
    @Select("<script>" +
            "SELECT * FROM ROLES" +
            "<where>" +
            "   <if test=\"params.name != null and params.name != ''\">" +
            "       AND NAME LIKE '%' || #{params.name} || '%'" +
            "   </if>" +
            "</where>" +
            "ORDER BY ISORT ASC, ID DESC" +
            "</script>")
    List<Role> findAll(@Param("params") Map<String, Object> params);

    @Select("<script>" +
            "SELECT count(*) FROM ROLES" +
            "<where>" +
            "   <if test=\"params.name != null and params.name != ''\">" +
            "       AND NAME LIKE '%' || #{params.name} || '%'" +
            "   </if>" +
            "</where>" +
            "</script>")
    long countAll(@Param("params") Map<String, Object> params);

    @ResultMap("roleResultMap")
    @Select("SELECT * FROM ROLES WHERE ID = #{id}")
    Role findById(@Param("id") Long id);

    @Insert("INSERT INTO ROLES (ID, NAME, DESCN, ITYPE, ISYSTEM, ISORT) " +
            "VALUES (#{id}, #{name}, #{descn}, #{itype}, #{isystem}, #{isort})")
    int insert(Role role);

    @Update("<script>" +
            "UPDATE ROLES" +
            "<set>" +
            "   <if test='name != null'>NAME = #{name},</if>" +
            "   <if test='descn != null'>DESCN = #{descn},</if>" +
            "   <if test='itype != null'>ITYPE = #{itype},</if>" +
            "   <if test='isystem != null'>ISYSTEM = #{isystem},</if>" +
            "   <if test='isort != null'>ISORT = #{isort},</if>" +
            "</set>" +
            "WHERE ID = #{id}" +
            "</script>")
    int update(Role role);

    @Delete("DELETE FROM ROLES WHERE ID = #{id}")
    int deleteById(@Param("id") Long id);
}
