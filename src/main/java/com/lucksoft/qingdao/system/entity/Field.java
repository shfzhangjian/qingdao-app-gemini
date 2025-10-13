package com.lucksoft.qingdao.system.entity;

/**
 * 数据字典主表实体类 (对应 FIELDS 表)
 *
 * @author Gemini
 */
public class Field {

    /**
     * 唯一id
     */
    private Long id;

    /**
     * 从属模块(类型)
     */
    private String stype;

    /**
     * 字段名称
     */
    private String sfieldname;

    /**
     * 说明
     */
    private String smemo;

    /**
     * 操作级别(1-用户维护的,2-系统级,3-管理级)
     */
    private Integer slevel;

    /**
     * 父节点
     */
    private Long vfid;

    /**
     * 从属模块ID
     */
    private Integer itype;

    // --- Getter and Setter ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStype() {
        return stype;
    }

    public void setStype(String stype) {
        this.stype = stype;
    }

    public String getSfieldname() {
        return sfieldname;
    }

    public void setSfieldname(String sfieldname) {
        this.sfieldname = sfieldname;
    }

    public String getSmemo() {
        return smemo;
    }

    public void setSmemo(String smemo) {
        this.smemo = smemo;
    }

    public Integer getSlevel() {
        return slevel;
    }

    public void setSlevel(Integer slevel) {
        this.slevel = slevel;
    }

    public Long getVfid() {
        return vfid;
    }

    public void setVfid(Long vfid) {
        this.vfid = vfid;
    }

    public Integer getItype() {
        return itype;
    }

    public void setItype(Integer itype) {
        this.itype = itype;
    }
}
