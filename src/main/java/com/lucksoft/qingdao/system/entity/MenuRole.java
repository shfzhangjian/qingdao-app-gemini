package com.lucksoft.qingdao.system.entity;

/**
 * 角色菜单关联实体类 (对应 MENU_ROLE 表)
 *
 * @author Gemini
 */
public class MenuRole {

    /**
     * 角色ID
     */
    private Long roleId;

    /**
     * 菜单ID
     */
    private Long menuId;

    // --- Getter and Setter ---

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Long getMenuId() {
        return menuId;
    }

    public void setMenuId(Long menuId) {
        this.menuId = menuId;
    }
}
