package com.lucksoft.qingdao.oracle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * DTO (Data Transfer Object) for EQ_PLANLB (设备-轮保计划)
 * This DTO includes a list of its child items (EqPlanLbDtDTO).
 */
public class EqPlanLbDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- 核心字段 ---
    @JsonProperty("id")
    private Long indocno; // 主键

    @JsonProperty("planId")
    private String sno; // 计划编号

    @JsonProperty("title")
    private String stitle; // 标题

    @JsonProperty("planDate")
    private Date dday; // 计划开始日期

    @JsonProperty("planEndDate")
    private Date dend; // 计划结束日期

    @JsonProperty("applicant")
    private String smaker; // 制表人

    @JsonProperty("department")
    private String sdept; // 部门

    @JsonProperty("status")
    private Integer istate; // 工作流状态

    @JsonProperty("archiveDate")
    private Date dsave; // 归档日期

    @JsonProperty("archiveUser")
    private String ssave; // 归档人

    @JsonProperty("archiveStatus")
    private String sstepstate; // 步骤状态 (用于触发 '已归档')

    @JsonProperty("createDate")
    private Date dregt; // 创建日期

    // --- 关联的子表 ---
    @JsonProperty("items")
    private List<EqPlanLbDtDTO> items;

    // Getters and Setters
    public Long getIndocno() {
        return indocno;
    }

    public void setIndocno(Long indocno) {
        this.indocno = indocno;
    }

    public String getSno() {
        return sno;
    }

    public void setSno(String sno) {
        this.sno = sno;
    }

    public String getStitle() {
        return stitle;
    }

    public void setStitle(String stitle) {
        this.stitle = stitle;
    }

    public Date getDday() {
        return dday;
    }

    public void setDday(Date dday) {
        this.dday = dday;
    }

    public Date getDend() {
        return dend;
    }

    public void setDend(Date dend) {
        this.dend = dend;
    }

    public String getSmaker() {
        return smaker;
    }

    public void setSmaker(String smaker) {
        this.smaker = smaker;
    }

    public String getSdept() {
        return sdept;
    }

    public void setSdept(String sdept) {
        this.sdept = sdept;
    }

    public Integer getIstate() {
        return istate;
    }

    public void setIstate(Integer istate) {
        this.istate = istate;
    }

    public Date getDsave() {
        return dsave;
    }

    public void setDsave(Date dsave) {
        this.dsave = dsave;
    }

    public String getSsave() {
        return ssave;
    }

    public void setSsave(String ssave) {
        this.ssave = ssave;
    }

    public String getSstepstate() {
        return sstepstate;
    }

    public void setSstepstate(String sstepstate) {
        this.sstepstate = sstepstate;
    }

    public Date getDregt() {
        return dregt;
    }

    public void setDregt(Date dregt) {
        this.dregt = dregt;
    }

    public List<EqPlanLbDtDTO> getItems() {
        return items;
    }

    public void setItems(List<EqPlanLbDtDTO> items) {
        this.items = items;
    }
}