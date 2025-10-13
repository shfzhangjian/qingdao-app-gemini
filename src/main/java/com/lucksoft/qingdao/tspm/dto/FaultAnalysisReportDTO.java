package com.lucksoft.qingdao.tspm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for 11. 故障分析报告创建
 * Topic: tims.create.fault.analysis.report
 */
public class FaultAnalysisReportDTO {
    /**
     * TIMS系统报告数据记录主键(唯一标识)
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * 故障报告/名称
     */
    @JsonProperty("name")
    private String name;

    /**
     * 设备编码
     */
    @JsonProperty("equipmentCode")
    private String equipmentCode;

    /**
     * 报告属性
     */
    @JsonProperty("attribute")
    private String attribute;

    /**
     * 故障分类
     */
    @JsonProperty("category")
    private String category;

    /**
     * 故障性质
     */
    @JsonProperty("faultNature")
    private String faultNature;

    /**
     * 发生班组
     */
    @JsonProperty("teamName")
    private String teamName;

    /**
     * 汇报人
     */
    @JsonProperty("reporter")
    private String reporter;

    /**
     * 汇报时间
     */
    @JsonProperty("debriefingTime")
    private String debriefingTime;

    /**
     * 设备部位
     */
    @JsonProperty("equipmentPart")
    private String equipmentPart;

    /**
     * 故障现象
     */
    @JsonProperty("faultPhenomenon")
    private String faultPhenomenon;

    /**
     * 故障原因
     */
    @JsonProperty("faultReason")
    private String faultReason;

    /**
     * 处理措施
     */
    @JsonProperty("measures")
    private String measures;

    /**
     * 停机开始
     */
    @JsonProperty("haltStartTime")
    private String haltStartTime;

    /**
     * 停机结束
     */
    @JsonProperty("haltEndTime")
    private String haltEndTime;

    /**
     * 停机时长(小时)
     */
    @JsonProperty("haltDuration")
    private Float haltDuration;

    /**
     * 故障现场描述
     */
    @JsonProperty("faultSiteDesc")
    private String faultSiteDesc;

    /**
     * 故障原因分析
     */
    @JsonProperty("faultCausesAnalysis")
    private String faultCausesAnalysis;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getAttribute() { return attribute; }
    public void setAttribute(String attribute) { this.attribute = attribute; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getFaultNature() { return faultNature; }
    public void setFaultNature(String faultNature) { this.faultNature = faultNature; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getReporter() { return reporter; }
    public void setReporter(String reporter) { this.reporter = reporter; }
    public String getDebriefingTime() { return debriefingTime; }
    public void setDebriefingTime(String debriefingTime) { this.debriefingTime = debriefingTime; }
    public String getEquipmentPart() { return equipmentPart; }
    public void setEquipmentPart(String equipmentPart) { this.equipmentPart = equipmentPart; }
    public String getFaultPhenomenon() { return faultPhenomenon; }
    public void setFaultPhenomenon(String faultPhenomenon) { this.faultPhenomenon = faultPhenomenon; }
    public String getFaultReason() { return faultReason; }
    public void setFaultReason(String faultReason) { this.faultReason = faultReason; }
    public String getMeasures() { return measures; }
    public void setMeasures(String measures) { this.measures = measures; }
    public String getHaltStartTime() { return haltStartTime; }
    public void setHaltStartTime(String haltStartTime) { this.haltStartTime = haltStartTime; }
    public String getHaltEndTime() { return haltEndTime; }
    public void setHaltEndTime(String haltEndTime) { this.haltEndTime = haltEndTime; }
    public Float getHaltDuration() { return haltDuration; }
    public void setHaltDuration(Float haltDuration) { this.haltDuration = haltDuration; }
    public String getFaultSiteDesc() { return faultSiteDesc; }
    public void setFaultSiteDesc(String faultSiteDesc) { this.faultSiteDesc = faultSiteDesc; }
    public String getFaultCausesAnalysis() { return faultCausesAnalysis; }
    public void setFaultCausesAnalysis(String faultCausesAnalysis) { this.faultCausesAnalysis = faultCausesAnalysis; }
}

