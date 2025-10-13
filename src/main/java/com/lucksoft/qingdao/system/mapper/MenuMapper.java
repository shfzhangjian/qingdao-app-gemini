package com.lucksoft.qingdao.system.mapper;

import com.lucksoft.qingdao.system.entity.Menu;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 菜单数据访问层接口 (使用注解)
 *
 * @author Gemini
 */
@Mapper
public interface MenuMapper {


    /**
     * 【新增】根据多个角色ID查询所有唯一的菜单
     */
    @ResultMap("menuResultMap")
    @Select("<script>" +
            "SELECT DISTINCT m.* FROM MENUS m " +
            "JOIN MENU_ROLE mr ON m.ID = mr.MENU_ID " +
            "WHERE mr.ROLE_ID IN " +
            "<foreach item='item' collection='roleIds' open='(' separator=',' close=')'>" +
            "   #{item}" +
            "</foreach>" +
            " AND m.ISTATE = 0 ORDER BY m.PARENT_ID ASC, m.SEQ ASC" +
            "</script>")
    List<Menu> findMenusByRoleIds(@Param("roleIds") List<Long> roleIds);

    @Results(id = "menuResultMap", value = {
            @Result(property = "id", column = "ID", id = true),
            @Result(property = "parentId", column = "PARENT_ID"),
            @Result(property = "seq", column = "SEQ"),
            @Result(property = "title", column = "TITLE"),
            @Result(property = "tip", column = "TIP"),
            @Result(property = "descn", column = "DESCN"),
            @Result(property = "image", column = "IMAGE"),
            @Result(property = "forward", column = "FORWARD"),
            @Result(property = "istate", column = "ISTATE"),
            @Result(property = "sregid", column = "SREGID"),
            @Result(property = "sregnm", column = "SREGNM"),
            @Result(property = "dregt", column = "DREGT")
    })
    @Select("<script>" +
            "SELECT * FROM MENUS" +
            "<where>" +
            "   <if test=\"params.title != null and params.title != ''\">" +
            "       AND TITLE LIKE '%' || #{params.title} || '%'" +
            "   </if>" +
            "   <if test=\"params.istate != null\">" +
            "       AND ISTATE = #{params.istate}" +
            "   </if>" +
            "</where>" +
            "ORDER BY PARENT_ID ASC, SEQ ASC" +
            "</script>")
    List<Menu> findAll(@Param("params") Map<String, Object> params);

    @ResultMap("menuResultMap")
    @Select("SELECT * FROM MENUS WHERE ID = #{id}")
    Menu findById(@Param("id") Long id);

    @Insert("INSERT INTO MENUS (ID, PARENT_ID, SEQ, TITLE, FORWARD, ISTATE, DREGT) " +
            "VALUES (#{id}, #{parentId}, #{seq}, #{title}, #{forward}, #{istate}, SYSDATE)")
    int insert(Menu menu);

    @Update("<script>" +
            "UPDATE MENUS" +
            "<set>" +
            "   <if test='parentId != null'>PARENT_ID = #{parentId},</if>" +
            "   <if test='seq != null'>SEQ = #{seq},</if>" +
            "   <if test='title != null'>TITLE = #{title},</if>" +
            "   <if test='forward != null'>FORWARD = #{forward},</if>" +
            "   <if test='istate != null'>ISTATE = #{istate},</if>" +
            "   DMODT = SYSDATE" +
            "</set>" +
            "WHERE ID = #{id}" +
            "</script>")
    int update(Menu menu);

    @Delete("DELETE FROM MENUS WHERE ID = #{id}")
    int deleteById(@Param("id") Long id);
}
