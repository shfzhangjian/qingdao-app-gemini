package com.lucksoft.qingdao.system.entity;

/**
 * 角色信息实体类 (对应 ROLES 表)
 *
 * @author Gemini
 */
public class Role {

    /**
     * 角色编号
     */
    private Long id;

    /**
     * 角色名称
     */
    private String name;

    /**
     * 描述
     */
    private String descn;

    /**
     * 角色分类
     */
    private Integer itype;

    /**
     * 角色归属系统，引用数据字典103
     */
    private Integer isystem;

    /**
     * 排序号
     */
    private Integer isort;

    // --- Getter and Setter ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescn() {
        return descn;
    }

    public void setDescn(String descn) {
        this.descn = descn;
    }

    public Integer getItype() {
        return itype;
    }

    public void setItype(Integer itype) {
        this.itype = itype;
    }

    public Integer getIsystem() {
        return isystem;
    }

    public void setIsystem(Integer isystem) {
        this.isystem = isystem;
    }

    public Integer getIsort() {
        return isort;
    }

    public void setIsort(Integer isort) {
        this.isort = isort;
    }
}
