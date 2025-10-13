package com.lucksoft.qingdao.system.entity;

import java.util.Date;

/**
 * 用户角色关联实体类 (对应 USER_ROLE 表)
 *
 * @author Gemini
 */
public class UserRole {

    /**
     * 用户编号
     */
    private Long userId;

    /**
     * 角色编号
     */
    private Long roleId;

    /**
     * 0未授权 1授权
     */
    private Integer iflag;

    /**
     * 截止日期
     */
    private Date dend;

    // --- Getter and Setter ---

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    public Integer getIflag() {
        return iflag;
    }

    public void setIflag(Integer iflag) {
        this.iflag = iflag;
    }

    public Date getDend() {
        return dend;
    }

    public void setDend(Date dend) {
        this.dend = dend;
    }
}
