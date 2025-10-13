package com.lucksoft.qingdao.system.dto;


import com.lucksoft.qingdao.system.entity.Menu;
import com.lucksoft.qingdao.system.entity.Role;
import com.lucksoft.qingdao.system.entity.User;

import java.io.Serializable;
import java.util.List;

// --- 存储在 Redis 中的详细用户信息 DTO ---
public class UserInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private User user;
    private List<Role> roles;
    private List<Menu> menuTree; // 树状结构的菜单

    // getters and setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public List<Role> getRoles() { return roles; }
    public void setRoles(List<Role> roles) { this.roles = roles; }
    public List<Menu> getMenuTree() { return menuTree; }
    public void setMenuTree(List<Menu> menuTree) { this.menuTree = menuTree; }
}