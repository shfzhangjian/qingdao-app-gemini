package com.lucksoft.qingdao.oracle.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO 用于映射 PMISSIONBOARD 表 (由 SP_QD_PLANBOARD_LB 生成的任务)
 * 字段基于 SP_QD_PLANBOARD_LB 中的 INSERT 语句
 */
public class PmissionBoardDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- 核心字段 ---
    private Long idocid;       // 任务id (来自 SEQ_PMISSION.NEXTVAL)
    private Integer djClass;   // 任务类型 (如 31, 32)
    private String mitype;     // 任务类型名称 (如 轮保, 月保)
    private String mititle;    // 标准名称 (来自 RESBZ.STITLE)
    private Date midate;       // 任务计划执行时间 (来自 RESPLANDT.DBEGIN)
    private Date makedate;     // 任务单生成时间 (sysdate)
    private String makepeople; // 下发人 (来自 ROW_USERS.name)
    private Long ieqno;        // 设备内码 (来自 RESPLANDT.IDOCID)
    private String makeflname; // 设备名 (来自 RESPLANDT.SFNAME)
    private String iduty;      // 岗位内码 (来自 RESBZDT.IDUTYS)
    private String sduty;      // 岗位名称 (来自 RESBZDT.SDUTYS)

    // Getters and Setters
    public Long getIdocid() {
        return idocid;
    }

    public void setIdocid(Long idocid) {
        this.idocid = idocid;
    }

    public Integer getDjClass() {
        return djClass;
    }

    public void setDjClass(Integer djClass) {
        this.djClass = djClass;
    }

    public String getMitype() {
        return mitype;
    }

    public void setMitype(String mitype) {
        this.mitype = mitype;
    }

    public String getMititle() {
        return mititle;
    }

    public void setMititle(String mititle) {
        this.mititle = mititle;
    }

    public Date getMidate() {
        return midate;
    }

    public void setMidate(Date midate) {
        this.midate = midate;
    }

    public Date getMakedate() {
        return makedate;
    }

    public void setMakedate(Date makedate) {
        this.makedate = makedate;
    }

    public String getMakepeople() {
        return makepeople;
    }

    public void setMakepeople(String makepeople) {
        this.makepeople = makepeople;
    }

    public Long getIeqno() {
        return ieqno;
    }

    public void setIeqno(Long ieqno) {
        this.ieqno = ieqno;
    }

    public String getMakeflname() {
        return makeflname;
    }

    public void setMakeflname(String makeflname) {
        this.makeflname = makeflname;
    }

    public String getIduty() {
        return iduty;
    }

    public void setIduty(String iduty) {
        this.iduty = iduty;
    }

    public String getSduty() {
        return sduty;
    }

    public void setSduty(String sduty) {
        this.sduty = sduty;
    }
}