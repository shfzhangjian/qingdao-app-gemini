package com.lucksoft.qingdao.system.mapper;

import com.lucksoft.qingdao.system.entity.Menu;
import com.lucksoft.qingdao.system.entity.Role;
import com.lucksoft.qingdao.system.entity.UserRole;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 用户角色关联数据访问层接口 (使用注解)
 *
 * @author Gemini
 */
@Mapper
public interface UserRoleMapper {


    /**
     * 根据用户ID查询其所拥有的所有角色信息
     * @param userId 用户ID
     * @return 角色列表
     */
    @Select("SELECT r.* FROM ROLES r " +
            "JOIN USER_ROLE ur ON r.ID = ur.ROLE_ID " +
            "WHERE ur.USER_ID = #{userId}")
    @Results(id = "roleResultMap", value = {
            @Result(property = "id", column = "ID", id = true),
            @Result(property = "name", column = "NAME"),
            @Result(property = "descn", column = "DESCN"),
            @Result(property = "itype", column = "ITYPE"),
            @Result(property = "isystem", column = "ISYSTEM"),
            @Result(property = "isort", column = "ISORT")
    })
    List<Role> findRolesByUserId(@Param("userId") Long userId);

    /**
     * 为用户指派一个角色
     * @param userRole 用户角色关联实体
     * @return 影响行数
     */
    @Insert("INSERT INTO USER_ROLE (USER_ID, ROLE_ID, IFLAG, DEND) " +
            "VALUES (#{userId}, #{roleId}, #{iflag}, #{dend})")
    int insert(UserRole userRole);

    /**
     * 根据用户ID删除其所有的角色关联
     * @param userId 用户ID
     * @return 影响行数
     */
    @Delete("DELETE FROM USER_ROLE WHERE USER_ID = #{userId}")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 移除用户的单个角色
     * @param userId 用户ID
     * @param roleId 角色ID
     * @return 影响行数
     */
    @Delete("DELETE FROM USER_ROLE WHERE USER_ID = #{userId} AND ROLE_ID = #{roleId}")
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

}
