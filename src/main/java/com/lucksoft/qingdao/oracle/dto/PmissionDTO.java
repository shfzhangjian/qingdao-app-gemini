package com.lucksoft.qingdao.oracle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/**
 * DTO (Data Transfer Object) for PMISSION (专业/精密点检)
 * This DTO is based on the INSERT statement in the PD_ZY_JM procedure.
 */
public class PmissionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- 核心字段 ---
    @JsonProperty("taskId")
    private Long idocid; // 任务id (来自 SEQ_PMISSION.NEXTVAL)

    @JsonProperty("taskClass")
    private String djClass; // 任务类型 (RS.SBOOKTYPE: '0'-专业点检, '2'-精密点检)

    @JsonProperty("taskType")
    private String mitype; // 任务类型名称

    @JsonProperty("title")
    private String mititle; // 标准名称 (RS.STITLE)

    @JsonProperty("planDate")
    private Date midate; // 任务计划执行时间 (RS.DNEXTTASK)

    @JsonProperty("createDate")
    private Date makedate; // 任务单生成时间 (SYSDATE)

    @JsonProperty("creator")
    private String makepeople; // 下发人 ('系统自动生成')

    @JsonProperty("equipmentId")
    private Long ieqno; // 设备内码 (RS.IDOCID)

    @JsonProperty("equipmentCode")
    private String makeflcode; // 设备号 (RS.SFCODE)

    @JsonProperty("equipmentName")
    private String makeflname; // 设备名 (RS.SFNAME)

    @JsonProperty("equipmentPmCode")
    private String spmcode; // (RS.PMCODE)

    @JsonProperty("bookId")
    private Long ibookno; // (RS.ILINKNO)

    @JsonProperty("bookDetailId")
    private Long makeplanno; // (RS.ILINKNO)

    @JsonProperty("item")
    private String makeponit; // (RS.SPARTEQ)

    @JsonProperty("position")
    private String makeckposition; // (RS.FUNUNIT)

    @JsonProperty("project")
    private String makeproject; // (RS.ITEMS)

    @JsonProperty("dutyId")
    private Long iduty; // (RS.IDUTY)

    @JsonProperty("dutyName")
    private String sduty; // (RS.SDUTY)

    @JsonProperty("runnerIds")
    private String srunnerid; // (V_SRUNNERID)

    @JsonProperty("runnerNames")
    private String srunnernm; // (V_SRUNNERNM)

    @JsonProperty("departmentId")
    private Long iplandept; // (RS.IDEPT)

    @JsonProperty("departmentName")
    private String splandept; // (RS.SDEPT)

    @JsonProperty("teamId")
    private String iplanbzno; // (V_BZ)

    // Getters and Setters

    public Long getIdocid() {
        return idocid;
    }

    public void setIdocid(Long idocid) {
        this.idocid = idocid;
    }

    public String getDjClass() {
        return djClass;
    }

    public void setDjClass(String djClass) {
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

    public String getSpmcode() {
        return spmcode;
    }

    public void setSpmcode(String spmcode) {
        this.spmcode = spmcode;
    }

    public Long getIbookno() {
        return ibookno;
    }

    public void setIbookno(Long ibookno) {
        this.ibookno = ibookno;
    }

    public Long getMakeplanno() {
        return makeplanno;
    }

    public void setMakeplanno(Long makeplanno) {
        this.makeplanno = makeplanno;
    }

    public String getMakeponit() {
        return makeponit;
    }

    public void setMakeponit(String makeponit) {
        this.makeponit = makeponit;
    }

    public String getMakeckposition() {
        return makeckposition;
    }

    public void setMakeckposition(String makeckposition) {
        this.makeckposition = makeckposition;
    }

    public String getMakeproject() {
        return makeproject;
    }

    public void setMakeproject(String makeproject) {
        this.makeproject = makeproject;
    }

    public Long getIduty() {
        return iduty;
    }

    public void setIduty(Long iduty) {
        this.iduty = iduty;
    }

    public String getSduty() {
        return sduty;
    }

    public void setSduty(String sduty) {
        this.sduty = sduty;
    }

    public String getSrunnerid() {
        return srunnerid;
    }

    public void setSrunnerid(String srunnerid) {
        this.srunnerid = srunnerid;
    }

    public String getSrunnernm() {
        return srunnernm;
    }

    public void setSrunnernm(String srunnernm) {
        this.srunnernm = srunnernm;
    }

    public Long getIplandept() {
        return iplandept;
    }

    public void setIplandept(Long iplandept) {
        this.iplandept = iplandept;
    }

    public String getSplandept() {
        return splandept;
    }

    public void setSplandept(String splandept) {
        this.splandept = splandept;
    }

    public String getIplanbzno() {
        return iplanbzno;
    }

    public void setIplanbzno(String iplanbzno) {
        this.iplanbzno = iplanbzno;
    }
}