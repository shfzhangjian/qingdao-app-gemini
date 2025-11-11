package com.lucksoft.qingdao.oracle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/**
 * DTO (Data Transfer Object) for PM_MONTH_ITEM (维修计划明细项目表)
 */
public class PmMonthItemDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("id")
    private Long indocno; // 主键

    @JsonProperty("parentId")
    private Long ilinkno; // 计划头主键

    @JsonProperty("equipmentId")
    private Long idocid; // 设备内码

    @JsonProperty("equipmentCode")
    private String sfcode; // 设备编码

    @JsonProperty("equipmentName")
    private String sfname; // 设备名称

    @JsonProperty("item")
    private String sitem; // 检修项目

    @JsonProperty("content")
    private String stodo; // 检修内容

    @JsonProperty("plannedHours")
    private Double iplanhour; // 计划工时

    @JsonProperty("plannedCost")
    private Double iplanmoney; // 预算费用[计划维修费用]

    @JsonProperty("repairDepartment")
    private String sdept; // 承修单位[维修单位]

    @JsonProperty("repairContact")
    private String sduty; // 维修负责人

    @JsonProperty("planStartDate")
    private Date dplanbegin; // 计划开始时间[维修时间安排]

    @JsonProperty("planEndDate")
    private Date dplanend; // 计划完成时间[维修时间安排]

    @JsonProperty("status")
    private Integer istate; // 状态[0-正常(执行中待确认) 1-撤销(提报人) 2-完工 3-确认完成 4-确认未完成]

    @JsonProperty("kind")
    private Integer ikind; // 属性类别[0-车间 1-外协]

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

    public String getSitem() {
        return sitem;
    }

    public void setSitem(String sitem) {
        this.sitem = sitem;
    }

    public String getStodo() {
        return stodo;
    }

    public void setStodo(String stodo) {
        this.stodo = stodo;
    }

    public Double getIplanhour() {
        return iplanhour;
    }

    public void setIplanhour(Double iplanhour) {
        this.iplanhour = iplanhour;
    }

    public Double getIplanmoney() {
        return iplanmoney;
    }

    public void setIplanmoney(Double iplanmoney) {
        this.iplanmoney = iplanmoney;
    }

    public String getSdept() {
        return sdept;
    }

    public void setSdept(String sdept) {
        this.sdept = sdept;
    }

    public String getSduty() {
        return sduty;
    }

    public void setSduty(String sduty) {
        this.sduty = sduty;
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

    public Integer getIkind() {
        return ikind;
    }

    public void setIkind(Integer ikind) {
        this.ikind = ikind;
    }

    public Date getDregt() {
        return dregt;
    }

    public void setDregt(Date dregt) {
        this.dregt = dregt;
    }
}