package com.lucksoft.qingdao.system.dto;

import com.lucksoft.qingdao.system.entity.Menu;
import com.lucksoft.qingdao.system.entity.Role;
import com.lucksoft.qingdao.system.entity.User;

import java.io.Serializable;
import java.util.List;

// --- 登录请求 DTO ---
public class LoginRequest {
    private String loginid;
    private String password;

    // getters and setters
    public String getLoginid() { return loginid; }
    public void setLoginid(String loginid) { this.loginid = loginid; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}