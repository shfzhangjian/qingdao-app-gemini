package com.lucksoft.qingdao.tmis.metrology.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;

/**
 * 计量点检任务数据 DTO (已更新以匹配 V_JL_EQUIP_DXJ 视图)
 */
public class MetrologyTaskDTO {

    @JsonProperty("id")
    private Long indocno; // 主键

    @JsonProperty("date")
    private Date dupcheck; // 确认日期

    @JsonProperty("pointCheckStatus")
    private String idjstate; // 点检状态

    @JsonProperty("enterpriseId")
    private String sjno; // 企业编号

    @JsonProperty("deviceName")
    private String sjname; // 设备名称

    @JsonProperty("model")
    private String sggxh; // 规格型号

    @JsonProperty("factoryId")
    private String sfactoryno; // 出厂编号

    @JsonProperty("location")
    private String splace; // 安装位置/使用人

    @JsonProperty("accuracy")
    private String slevel; // 准确度等级

    @JsonProperty("status")
    private String istate; // 设备状态

    @JsonProperty("abc")
    private String sabc; // ABC分类

    // 根据 SCHECKRESULT 动态计算
    @JsonProperty("isAbnormal")
    private boolean isAbnormal;

    // --- 隐藏字段 ---
    private String scheckresult; // 检查结果 (用于计算 isAbnormal)
    private String scheckremark; // 检查备注 (异常描述)
    private String susedept; // 使用部门
    private String erpId;
    private String range; // 量程范围 (V_JL_EQUIP_DXJ 中为 slc)
    private String slc;
    private String dinit;//生成任务时间
    private String sproduct;//制造单位
    private String suser;//责任人
    private String scheckuser;//检定员
    private String seq;//所属设备

    public String getSproduct() {
        return sproduct;
    }

    public void setSproduct(String sproduct) {
        this.sproduct = sproduct;
    }

    public String getSuser() {
        return suser;
    }

    public void setSuser(String suser) {
        this.suser = suser;
    }

    public String getScheckuser() {
        return scheckuser;
    }

    public void setScheckuser(String scheckuser) {
        this.scheckuser = scheckuser;
    }

    public String getSeq() {
        return seq;
    }

    public void setSeq(String seq) {
        this.seq = seq;
    }



    // Getters and Setters
    public Long getIndocno() {
        return indocno;
    }

    public void setIndocno(Long indocno) {
        this.indocno = indocno;
    }

    public Date getDupcheck() {
        return dupcheck;
    }

    public void setDupcheck(Date dupcheck) {
        this.dupcheck = dupcheck;
    }

    public String getIdjstate() {
        return idjstate;
    }

    public void setIdjstate(String idjstate) {
        this.idjstate = idjstate;
    }

    public String getSjno() {
        return sjno;
    }

    public void setSjno(String sjno) {
        this.sjno = sjno;
    }

    public String getSjname() {
        return sjname;
    }

    public void setSjname(String sjname) {
        this.sjname = sjname;
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

    public String getIstate() {
        return istate;
    }

    public void setIstate(String istate) {
        this.istate = istate;
    }

    public String getSabc() {
        return sabc;
    }

    public void setSabc(String sabc) {
        this.sabc = sabc;
    }

    public boolean isAbnormal() {
        return isAbnormal;
    }

    public void setAbnormal(boolean abnormal) {
        isAbnormal = abnormal;
    }

    public String getScheckresult() {
        return scheckresult;
    }

    public void setScheckresult(String scheckresult) {
        this.scheckresult = scheckresult;
        // 动态计算 isAbnormal
        this.isAbnormal = "异常".equals(scheckresult);
    }

    public String getScheckremark() {
        return scheckremark;
    }

    public void setScheckremark(String scheckremark) {
        this.scheckremark = scheckremark;
    }

    public String getSusedept() {
        return susedept;
    }

    public void setSusedept(String susedept) {
        this.susedept = susedept;
    }

    public String getErpId() { return erpId; }
    public void setErpId(String erpId) { this.erpId = erpId; }
    public String getRange() { return this.slc; } // getRange() 返回 slc 的值
    public void setRange(String range) { this.range = range; }
    public String getSlc() { return slc; }
    public void setSlc(String slc) { this.slc = slc; }
    public String getDinit() { return dinit; }
    public void setDinit(String dinit) { this.dinit = dinit; }
}
