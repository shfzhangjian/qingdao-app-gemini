package com.lucksoft.qingdao.tmis.metrology.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

/**
 * 计量台账数据 DTO (已更新以匹配 V_JL_EQUIP 视图)
 */
public class MetrologyLedgerDTO {

    // --- 核心字段 ---
    @JsonProperty("id")
    private Long indocno; // 主键

    @JsonProperty("sysId")
    private String sjno; // 系统编号 (使用企业编号)

    @JsonProperty("deviceName")
    private String sjname; // 设备名称

    @JsonProperty("status")
    private String istate; // 设备状态

    @JsonProperty("isLinked")
    private boolean isLinked = false; // 台账挂接 (默认逻辑，可后续修改)

    @JsonProperty("enterpriseId")
    private String enterpriseId; // 企业编号 (冗余字段，方便前端)

    @JsonProperty("model")
    private String sggxh; // 规格型号

    @JsonProperty("factoryId")
    private String sfactoryno; // 出厂编号

    @JsonProperty("location")
    private String splace; // 安装位置/使用人

    @JsonProperty("accuracy")
    private String slevel; // 准确度等级

    @JsonProperty("nextDate")
    private Date dnextcheck; // 下次确认日期

    @JsonProperty("parentDevice")
    private String seq; // 所属设备

    @JsonProperty("department")
    private String susedept; // 使用部门

    @JsonProperty("abc")
    private String sabc; // ABC分类

    // --- 计算字段 ---
    @JsonProperty("expired")
    private boolean expired; // 是否过期

    // --- 隐藏字段 ---
    private String iqj; // 强检标识
    private String izj; // 质检仪器
    private String slc; // 量程范围
    private String sproduct; // 制造单位
    private Date dfactory; // 出厂时间
    private String suser; // 责任人
    private String sverifier; // 检定员
    private String sdefine1; // 确认方式
    private String scertificate; // 证书编号
    private String sbuytype; // 购置形式
    private Date dcheck; // 本次确认日期
    private String sconfirmbasis; // 确认依据
    private String snotes; // 备注

    // --- Getters and Setters ---

    public Long getIndocno() {
        return indocno;
    }

    public void setIndocno(Long indocno) {
        this.indocno = indocno;
    }

    public String getSjno() {
        return sjno;
    }

    public void setSjno(String sjno) {
        this.sjno = sjno;
        this.enterpriseId = sjno; // 同步企业编号
    }

    public String getSjname() {
        return sjname;
    }

    public void setSjname(String sjname) {
        this.sjname = sjname;
    }

    public String getIstate() {
        return istate;
    }

    public void setIstate(String istate) {
        this.istate = istate;
    }

    public boolean isLinked() {
        return isLinked;
    }

    public void setLinked(boolean linked) {
        isLinked = linked;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getSggxh() {
        return sggxh;
    }

    public void setSggxh(String sggxh) {
        this.sggxh = sggxh;
    }

    public String getSfactoryno() {
        return sfactoryno;
    }

    public void setSfactoryno(String sfactoryno) {
        this.sfactoryno = sfactoryno;
    }

    public String getSplace() {
        return splace;
    }

    public void setSplace(String splace) {
        this.splace = splace;
    }

    public String getSlevel() {
        return slevel;
    }

    public void setSlevel(String slevel) {
        this.slevel = slevel;
    }

    public Date getDnextcheck() {
        return dnextcheck;
    }

    public void setDnextcheck(Date dnextcheck) {
        this.dnextcheck = dnextcheck;
        if (dnextcheck != null) {
            this.expired = dnextcheck.before(new Date());
        } else {
            this.expired = false;
        }
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }

    public String getSusedept() {
        return susedept;
    }

    public void setSusedept(String susedept) {
        this.susedept = susedept;
    }

    public String getSabc() {
        return sabc;
    }

    public void setSabc(String sabc) {
        this.sabc = sabc;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    public String getIqj() {
        return iqj;
    }

    public void setIqj(String iqj) {
        this.iqj = iqj;
    }

    public String getIzj() {
        return izj;
    }

    public void setIzj(String izj) {
        this.izj = izj;
    }

    public String getSlc() {
        return slc;
    }

    public void setSlc(String slc) {
        this.slc = slc;
    }

    public String getSproduct() {
        return sproduct;
    }

    public void setSproduct(String sproduct) {
        this.sproduct = sproduct;
    }

    public Date getDfactory() {
        return dfactory;
    }

    public void setDfactory(Date dfactory) {
        this.dfactory = dfactory;
    }

    public String getSuser() {
        return suser;
    }

    public void setSuser(String suser) {
        this.suser = suser;
    }

    public String getSverifier() {
        return sverifier;
    }

    public void setSverifier(String sverifier) {
        this.sverifier = sverifier;
    }

    public String getSdefine1() {
        return sdefine1;
    }

    public void setSdefine1(String sdefine1) {
        this.sdefine1 = sdefine1;
    }

    public String getScertificate() {
        return scertificate;
    }

    public void setScertificate(String scertificate) {
        this.scertificate = scertificate;
    }

    public String getSbuytype() {
        return sbuytype;
    }

    public void setSbuytype(String sbuytype) {
        this.sbuytype = sbuytype;
    }

    public Date getDcheck() {
        return dcheck;
    }

    public void setDcheck(Date dcheck) {
        this.dcheck = dcheck;
    }

    public String getSconfirmbasis() {
        return sconfirmbasis;
    }

    public void setSconfirmbasis(String sconfirmbasis) {
        this.sconfirmbasis = sconfirmbasis;
    }

    public String getSnotes() {
        return snotes;
    }

    public void setSnotes(String snotes) {
        this.snotes = snotes;
    }
}
