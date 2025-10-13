package com.lucksoft.qingdao.eam.maintainbook.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
// 其他需要的imports...

/**
 * 实体: Maintainbook
 * 对应表: MAINTAINBOOK
 *
 * @author migration-tool
 */
@Entity
@Table(name = "MAINTAINBOOK")
public class Maintainbook implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "INDOCNO")
    private BigDecimal indocno;
    /**
     * 修改时间
     */
    @Column(name = "DMODT")
    private Date dmodt;
    /**
     * 创建时间
     */
    @Column(name = "DREGT")
    private Date dregt;
    /**
     * 标准分类ID
     */
    @Column(name = "ICLASSID")
    private BigDecimal iclassid;
    /**
     * 部门ID
     */
    @Column(name = "IDEPT")
    private BigDecimal idept;
    /**
     * 机型ID
     */
    @Column(name = "IMACHINETYPE")
    private BigDecimal imachinetype;
    /**
     * 主标准ID
     */
    @Column(name = "IPARENT")
    private BigDecimal iparent;
    /**
     * 状态
     */
    @Column(name = "ISTATE")
    private BigDecimal istate;
    /**
     * 编号
     */
    @Column(name = "SBOOKNO")
    private String sbookno;
    /**
     * 分类
     */
    @Column(name = "SCLASSNM")
    private String sclassnm;
    /**
     * 所属部门
     */
    @Column(name = "SDEPT")
    private String sdept;
    /**
     * 机型/工段
     */
    @Column(name = "SMACHINTETYPE")
    private String smachintetype;
    /**
     * 修改人ID
     */
    @Column(name = "SMODID")
    private BigDecimal smodid;
    /**
     * 修改人
     */
    @Column(name = "SMODNM")
    private String smodnm;
    /**
     * 备注说明
     */
    @Column(name = "SNOTES")
    private String snotes;
    /**
     * 关联标准
     */
    @Column(name = "SPARENT")
    private String sparent;
    /**
     * 创建人ID
     */
    @Column(name = "SREGID")
    private BigDecimal sregid;
    /**
     * 创建人
     */
    @Column(name = "SREGNM")
    private String sregnm;
    /**
     * 标准标题
     */
    @Column(name = "STITLE")
    private String stitle;
    /**
     * 版本
     */
    @Column(name = "SVERSION")
    private String sversion;
    @Column(name = "VERSION")
    private String version;

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public BigDecimal getIndocno() {
        return indocno;
    }

    public void setIndocno(BigDecimal indocno) {
        this.indocno = indocno;
    }

    public Date getDmodt() {
        return dmodt;
    }

    public void setDmodt(Date dmodt) {
        this.dmodt = dmodt;
    }

    public Date getDregt() {
        return dregt;
    }

    public void setDregt(Date dregt) {
        this.dregt = dregt;
    }

    public BigDecimal getIclassid() {
        return iclassid;
    }

    public void setIclassid(BigDecimal iclassid) {
        this.iclassid = iclassid;
    }

    public BigDecimal getIdept() {
        return idept;
    }

    public void setIdept(BigDecimal idept) {
        this.idept = idept;
    }

    public BigDecimal getImachinetype() {
        return imachinetype;
    }

    public void setImachinetype(BigDecimal imachinetype) {
        this.imachinetype = imachinetype;
    }

    public BigDecimal getIparent() {
        return iparent;
    }

    public void setIparent(BigDecimal iparent) {
        this.iparent = iparent;
    }

    public BigDecimal getIstate() {
        return istate;
    }

    public void setIstate(BigDecimal istate) {
        this.istate = istate;
    }

    public String getSbookno() {
        return sbookno;
    }

    public void setSbookno(String sbookno) {
        this.sbookno = sbookno;
    }

    public String getSclassnm() {
        return sclassnm;
    }

    public void setSclassnm(String sclassnm) {
        this.sclassnm = sclassnm;
    }

    public String getSdept() {
        return sdept;
    }

    public void setSdept(String sdept) {
        this.sdept = sdept;
    }

    public String getSmachintetype() {
        return smachintetype;
    }

    public void setSmachintetype(String smachintetype) {
        this.smachintetype = smachintetype;
    }

    public BigDecimal getSmodid() {
        return smodid;
    }

    public void setSmodid(BigDecimal smodid) {
        this.smodid = smodid;
    }

    public String getSmodnm() {
        return smodnm;
    }

    public void setSmodnm(String smodnm) {
        this.smodnm = smodnm;
    }

    public String getSnotes() {
        return snotes;
    }

    public void setSnotes(String snotes) {
        this.snotes = snotes;
    }

    public String getSparent() {
        return sparent;
    }

    public void setSparent(String sparent) {
        this.sparent = sparent;
    }

    public BigDecimal getSregid() {
        return sregid;
    }

    public void setSregid(BigDecimal sregid) {
        this.sregid = sregid;
    }

    public String getSregnm() {
        return sregnm;
    }

    public void setSregnm(String sregnm) {
        this.sregnm = sregnm;
    }

    public String getStitle() {
        return stitle;
    }

    public void setStitle(String stitle) {
        this.stitle = stitle;
    }

    public String getSversion() {
        return sversion;
    }

    public void setSversion(String sversion) {
        this.sversion = sversion;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
    //</editor-fold>
}