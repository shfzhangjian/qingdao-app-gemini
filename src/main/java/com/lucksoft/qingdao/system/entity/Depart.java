package com.lucksoft.qingdao.system.entity;

import java.util.Date;

/**
 * 部门信息实体类 (对应 TDEPART 表)
 *
 * @author Gemini
 */
public class Depart {

    /**
     * 部门编号
     */
    private String sdepId;

    /**
     * 部门名称
     */
    private String sdepName;

    /**
     * 上级部门ID
     */
    private String sparentDepId;

    /**
     * 部门简称
     */
    private String sdepSm;

    /**
     * 部门全称
     */
    private String sdepFullname;

    /**
     * 部门分类
     */
    private Integer iclass;

    /**
     * 创建人ID
     */
    private Integer sregid;

    /**
     * 创建人
     */
    private String sregnm;

    /**
     * 创建日期
     */
    private Date dregt;

    /**
     * 修改人ID
     */
    private Integer smodid;

    /**
     * 修改人
     */
    private String smodnm;

    /**
     * 修改日期
     */
    private Date dmodt;

    // --- Getter and Setter ---

    public String getSdepId() {
        return sdepId;
    }

    public void setSdepId(String sdepId) {
        this.sdepId = sdepId;
    }

    public String getSdepName() {
        return sdepName;
    }

    public void setSdepName(String sdepName) {
        this.sdepName = sdepName;
    }

    public String getSparentDepId() {
        return sparentDepId;
    }

    public void setSparentDepId(String sparentDepId) {
        this.sparentDepId = sparentDepId;
    }

    public String getSdepSm() {
        return sdepSm;
    }

    public void setSdepSm(String sdepSm) {
        this.sdepSm = sdepSm;
    }

    public String getSdepFullname() {
        return sdepFullname;
    }

    public void setSdepFullname(String sdepFullname) {
        this.sdepFullname = sdepFullname;
    }

    public Integer getIclass() {
        return iclass;
    }

    public void setIclass(Integer iclass) {
        this.iclass = iclass;
    }

    public Integer getSregid() {
        return sregid;
    }

    public void setSregid(Integer sregid) {
        this.sregid = sregid;
    }

    public String getSregnm() {
        return sregnm;
    }

    public void setSregnm(String sregnm) {
        this.sregnm = sregnm;
    }

    public Date getDregt() {
        return dregt;
    }

    public void setDregt(Date dregt) {
        this.dregt = dregt;
    }

    public Integer getSmodid() {
        return smodid;
    }

    public void setSmodid(Integer smodid) {
        this.smodid = smodid;
    }

    public String getSmodnm() {
        return smodnm;
    }

    public void setSmodnm(String smodnm) {
        this.smodnm = smodnm;
    }

    public Date getDmodt() {
        return dmodt;
    }

    public void setDmodt(Date dmodt) {
        this.dmodt = dmodt;
    }
}
