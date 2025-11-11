package com.lucksoft.qingdao.oracle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * DTO (Data Transfer Object) for PM_MONTH (维修计划主表)
 * This DTO includes a list of its child items (PmMonthItemDTO).
 */
public class PmMonthDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    // --- 核心字段 ---
    @JsonProperty("id")
    private Long indocno; // 主键

    @JsonProperty("planId")
    private String splanno; // 计划编号

    @JsonProperty("title")
    private String stitle; // 计划名称

    @JsonProperty("applicant")
    private String sapplyer; // 计划提报人

    @JsonProperty("department")
    private String sdept; // 计划提报部门名称

    @JsonProperty("planType")
    private Integer iplantype; // 计划类型-[0-计划维修 1-轮保维修计划 2-停产检修计划]

    @JsonProperty("year")
    private Integer iyear; // 计划年份

    @JsonProperty("month")
    private Integer imonth; // 计划月份

    @JsonProperty("applyDate")
    private Date dapplydate; // 提报日期

    @JsonProperty("planStartDate")
    private Date dplanbegin; // 计划开始时间

    @JsonProperty("planEndDate")
    private Date dplanend; // 计划结束时间

    @JsonProperty("status")
    private Integer istate; // 状态[0-待提交、1-待审核、2-审核通过、3-审核退回、4-已汇总]

    @JsonProperty("workStatus")
    private Integer iworkstate; // 执行状态[0-未执行 1-执行中 2-完工并关闭]

    @JsonProperty("archiveStatus")
    private String sstepstate; // 步骤状态 (用于触发 '已归档')

    @JsonProperty("createDate")
    private Date dregt; // 创建日期

    // --- 关联的子表 ---
    @JsonProperty("items")
    private List<PmMonthItemDTO> items;

    // Getters and Setters

    public Long getIndocno() {
        return indocno;
    }

    public void setIndocno(Long indocno) {
        this.indocno = indocno;
    }

    public String getSplanno() {
        return splanno;
    }

    public void setSplanno(String splanno) {
        this.splanno = splanno;
    }

    public String getStitle() {
        return stitle;
    }

    public void setStitle(String stitle) {
        this.stitle = stitle;
    }

    public String getSapplyer() {
        return sapplyer;
    }

    public void setSapplyer(String sapplyer) {
        this.sapplyer = sapplyer;
    }

    public String getSdept() {
        return sdept;
    }

    public void setSdept(String sdept) {
        this.sdept = sdept;
    }

    public Integer getIplantype() {
        return iplantype;
    }

    public void setIplantype(Integer iplantype) {
        this.iplantype = iplantype;
    }

    public Integer getIyear() {
        return iyear;
    }

    public void setIyear(Integer iyear) {
        this.iyear = iyear;
    }

    public Integer getImonth() {
        return imonth;
    }

    public void setImonth(Integer imonth) {
        this.imonth = imonth;
    }

    public Date getDapplydate() {
        return dapplydate;
    }

    public void setDapplydate(Date dapplydate) {
        this.dapplydate = dapplydate;
    }

    public Date getDplanbegin() {
        return dplanbegin;
    }

    public void setDplanbegin(Date dplanbegin) {
        this.dplanbegin = dplanbegin;
    }

    public Date getDplanend() {
        return dplanend;
    }

    public void setDplanend(Date dplanend) {
        this.dplanend = dplanend;
    }

    public Integer getIstate() {
        return istate;
    }

    public void setIstate(Integer istate) {
        this.istate = istate;
    }

    public Integer getIworkstate() {
        return iworkstate;
    }

    public void setIworkstate(Integer iworkstate) {
        this.iworkstate = iworkstate;
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

    public List<PmMonthItemDTO> getItems() {
        return items;
    }

    public void setItems(List<PmMonthItemDTO> items) {
        this.items = items;
    }
}