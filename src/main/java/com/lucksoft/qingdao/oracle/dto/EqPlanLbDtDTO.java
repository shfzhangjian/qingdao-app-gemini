package com.lucksoft.qingdao.oracle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/**
 * DTO (Data Transfer Object) for EQ_PLANLBDT (精益日保计划明细)
 * Based on the table definition provided.
 */
public class EqPlanLbDtDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long indocno; // 主键

    @JsonProperty("parentId")
    private Long ilinkno; // 计划主键

    @JsonProperty("bookId")
    private Long ibookid; // 标准主键

    @JsonProperty("equipmentId")
    private Long idocid; // 设备主键

    @JsonProperty("equipmentCode")
    private String sfcode; // 设备编码

    @JsonProperty("equipmentName")
    private String sfname; // 设备名称

    @JsonProperty("timerId")
    private Long itimer; // 时间段(0,1,2,3,4)系统写死，可配置

    @JsonProperty("teamId")
    private Long soccteam; // 班组

    @JsonProperty("type")
    private Long itype; // 1-装封箱机 2-保养机台 3-喂丝机 4-咀棒发射机

    @JsonProperty("date")
    private Date dbegin; // 日期

    @JsonProperty("opType")
    private Long iop; // 分类（0，1手动补录）

    @JsonProperty("createDate")
    private Date dregt; // 创建日期


    // Getters and Setters

    public Long getIndocno() {
        return indocno;
    }

    public void setIndocno(Long indocno) {
        this.indocno = indocno;
    }

    public Long getIlinkno() {
        return ilinkno;
    }

    public void setIlinkno(Long ilinkno) {
        this.ilinkno = ilinkno;
    }

    public Long getIbookid() {
        return ibookid;
    }

    public void setIbookid(Long ibookid) {
        this.ibookid = ibookid;
    }

    public Long getIdocid() {
        return idocid;
    }

    public void setIdocid(Long idocid) {
        this.idocid = idocid;
    }

    public String getSfcode() {
        return sfcode;
    }

    public void setSfcode(String sfcode) {
        this.sfcode = sfcode;
    }

    public String getSfname() {
        return sfname;
    }

    public void setSfname(String sfname) {
        this.sfname = sfname;
    }

    public Long getItimer() {
        return itimer;
    }

    public void setItimer(Long itimer) {
        this.itimer = itimer;
    }

    public Long getSoccteam() {
        return soccteam;
    }

    public void setSoccteam(Long soccteam) {
        this.soccteam = soccteam;
    }

    public Long getItype() {
        return itype;
    }

    public void setItype(Long itype) {
        this.itype = itype;
    }

    public Date getDbegin() {
        return dbegin;
    }

    public void setDbegin(Date dbegin) {
        this.dbegin = dbegin;
    }

    public Long getIop() {
        return iop;
    }

    public void setIop(Long iop) {
        this.iop = iop;
    }

    public Date getDregt() {
        return dregt;
    }

    public void setDregt(Date dregt) {
        this.dregt = dregt;
    }
}