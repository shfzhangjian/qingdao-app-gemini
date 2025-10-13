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
 * 实体: Maintainbookdt
 * 对应表: MAINTAINBOOKDT
 * @author migration-tool
 */
@Entity
@Table(name = "MAINTAINBOOKDT")
public class Maintainbookdt implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "INDOCNO")
    private BigDecimal indocno;
    @Column(name = "DMODT")
    private Date dmodt;
    @Column(name = "DREGT")
    private Date dregt;
    @Column(name = "F1")
    private String f1;
    @Column(name = "F2")
    private String f2;
    /**
     * 分值
     */
    @Column(name = "IBASEVALUE")
    private BigDecimal ibasevalue;
    @Column(name = "ICHECKDUTYS")
    private String icheckdutys;
    /**
     * 抽查类型
     */
    @Column(name = "ICHECKFLAG")
    private String icheckflag;
    /**
     * 序
     */
    @Column(name = "IDTNO")
    private BigDecimal idtno;
    @Column(name = "IDUTYS")
    private String idutys;
    @Column(name = "ILINKNO")
    private BigDecimal ilinkno;
    @Column(name = "IPARENT")
    private BigDecimal iparent;
    /**
     * 状态
     */
    @Column(name = "ISTATE")
    private BigDecimal istate;
    /**
     * 用时(小时)
     */
    @Column(name = "IWORKHOUR")
    private String iworkhour;
    /**
     * 检查岗位
     */
    @Column(name = "SCHECKDUTYS")
    private String scheckdutys;
    /**
     * 保养内容
     */
    @Column(name = "SDETAIL")
    private String sdetail;
    /**
     * 责任岗位
     */
    @Column(name = "SDUTYS")
    private String sdutys;
    @Column(name = "SMODID")
    private BigDecimal smodid;
    @Column(name = "SMODNM")
    private String smodnm;
    /**
     * 备注
     */
    @Column(name = "SNOTES")
    private String snotes;
    @Column(name = "SREGID")
    private BigDecimal sregid;
    @Column(name = "SREGNM")
    private String sregnm;
    /**
     * 保养标准
     */
    @Column(name = "SSTANDARD")
    private String sstandard;
    /**
     * 保养项目
     */
    @Column(name = "STITLE")
    private String stitle;

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
    public String getF1() {
        return f1;
    }

    public void setF1(String f1) {
        this.f1 = f1;
    }
    public String getF2() {
        return f2;
    }

    public void setF2(String f2) {
        this.f2 = f2;
    }
    public BigDecimal getIbasevalue() {
        return ibasevalue;
    }

    public void setIbasevalue(BigDecimal ibasevalue) {
        this.ibasevalue = ibasevalue;
    }
    public String getIcheckdutys() {
        return icheckdutys;
    }

    public void setIcheckdutys(String icheckdutys) {
        this.icheckdutys = icheckdutys;
    }
    public String getIcheckflag() {
        return icheckflag;
    }

    public void setIcheckflag(String icheckflag) {
        this.icheckflag = icheckflag;
    }
    public BigDecimal getIdtno() {
        return idtno;
    }

    public void setIdtno(BigDecimal idtno) {
        this.idtno = idtno;
    }
    public String getIdutys() {
        return idutys;
    }

    public void setIdutys(String idutys) {
        this.idutys = idutys;
    }
    public BigDecimal getIlinkno() {
        return ilinkno;
    }

    public void setIlinkno(BigDecimal ilinkno) {
        this.ilinkno = ilinkno;
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
    public String getIworkhour() {
        return iworkhour;
    }

    public void setIworkhour(String iworkhour) {
        this.iworkhour = iworkhour;
    }
    public String getScheckdutys() {
        return scheckdutys;
    }

    public void setScheckdutys(String scheckdutys) {
        this.scheckdutys = scheckdutys;
    }
    public String getSdetail() {
        return sdetail;
    }

    public void setSdetail(String sdetail) {
        this.sdetail = sdetail;
    }
    public String getSdutys() {
        return sdutys;
    }

    public void setSdutys(String sdutys) {
        this.sdutys = sdutys;
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
    public String getSstandard() {
        return sstandard;
    }

    public void setSstandard(String sstandard) {
        this.sstandard = sstandard;
    }
    public String getStitle() {
        return stitle;
    }

    public void setStitle(String stitle) {
        this.stitle = stitle;
    }
    //</editor-fold>
}