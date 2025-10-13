package com.lucksoft.qingdao.system.mapper;

import com.lucksoft.qingdao.system.entity.Menu;
import com.lucksoft.qingdao.system.entity.MenuRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 角色菜单关联数据访问层接口 (使用注解)
 *
 * @author Gemini
 */
@Mapper
public interface MenuRoleMapper {

    /**
     * 根据角色ID查询其所能访问的所有菜单信息
     * @param roleId 角色ID
     * @return 菜单列表
     */
    @Select("SELECT m.* FROM MENUS m " +
            "JOIN MENU_ROLE mr ON m.ID = mr.MENU_ID " +
            "WHERE mr.ROLE_ID = #{roleId}")
    @Results(id = "menuResultMap", value = {
            @Result(property = "id", column = "ID", id = true),
            @Result(property = "parentId", column = "PARENT_ID"),
            @Result(property = "seq", column = "SEQ"),
            @Result(property = "title", column = "TITLE"),
            @Result(property = "forward", column = "FORWARD"),
            @Result(property = "istate", column = "ISTATE")
    })
    List<Menu> findMenusByRoleId(@Param("roleId") Long roleId);

    /**
     * 为角色指派一个菜单
     * @param menuRole 角色菜单关联实体
     * @return 影响行数
     */
    @Insert("INSERT INTO MENU_ROLE (ROLE_ID, MENU_ID) VALUES (#{roleId}, #{menuId})")
    int insert(MenuRole menuRole);

    /**
     * 根据角色ID删除其所有的菜单关联
     * @param roleId 角色ID
     * @return 影响行数
     */
    @Delete("DELETE FROM MENU_ROLE WHERE ROLE_ID = #{roleId}")
    int deleteByRoleId(@Param("roleId") Long roleId);

}
