package com.lucksoft.qingdao.oracle.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * DTO 用于映射 PMISSIONBOARDBAOYANG 表 (由 SP_PLANBOARD 或 JOB_GENERATE_ALL_BAOYANG_TASKS 生成的任务)
 * 字段基于 INSERT 语句
 */
public class PmissionBoardBaoYangDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- 核心字段 ---
    private Long idocid;       // 任务id (来自 SEQ_PMISSION.NEXTVAL)
    private Integer djClass;   // 任务类型 (8888)
    private String mitype;     // 任务类型名称 ('例保任务')
    private String mititle;    // 标准名称 (来自 A.STITLE)
    private Date midate;       // 任务计划执行时间 (SYSDATE)
    private Date makedate;     // 任务单生成时间 (SYSDATE)
    private String makepeople; // 下发人 (来自 V_IN_MAKEPEOPLE)
    private Long ieqno;        // 设备内码 (来自 C.IDOCID)
    private String makeflcode; // 设备号 (来自 C.SFCODE)
    private String makeflname; // 设备名 (来自 C.SFNAME)
    private String iduty;      // 岗位内码 (来自 V_IDUTY)
    private String sduty;      // 岗位名称 (来自 V_SDUTY)
    private Date dbegin;       // 班次开始时间
    private Date dend;         // 班次结束时间
    private Integer iidc;      // 表主键 (IIDCID)

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

    public String getMakeflcode() {
        return makeflcode;
    }

    public void setMakeflcode(String makeflcode) {
        this.makeflcode = makeflcode;
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

    public Date getDbegin() {
        return dbegin;
    }

    public void setDbegin(Date dbegin) {
        this.dbegin = dbegin;
    }

    public Date getDend() {
        return dend;
    }

    public void setDend(Date dend) {
        this.dend = dend;
    }

    public Integer getIidc() {
        return iidc;
    }

    public void setIidc(Integer iidc) {
        this.iidc = iidc;
    }
}