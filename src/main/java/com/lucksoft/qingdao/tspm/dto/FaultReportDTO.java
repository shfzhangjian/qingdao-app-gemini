package com.lucksoft.qingdao.tspm.dto;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for 4. 点检异常填报 and 10. 故障维修报告创建
 * Topic: tims.create.fault.report
 */
public class FaultReportDTO {
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
     * 专业
     */
    @JsonProperty("major")
    private String major;

    /**
     * 故障来源
     */
    @JsonProperty("faultSource")
    private String faultSource;

    /**
     * 设备状态
     */
    @JsonProperty("equipmentStatus")
    private String equipmentStatus;

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
     * 是否需要换件
     */
    @JsonProperty("replacementRequired")
    private Boolean replacementRequired;

    /**
     * 解决措施
     */
    @JsonProperty("solution")
    private String solution;

    /**
     * 具体描述及要求维修内容
     */
    @JsonProperty("specificDesc")
    private String specificDesc;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getMajor() { return major; }
    public void setMajor(String major) { this.major = major; }
    public String getFaultSource() { return faultSource; }
    public void setFaultSource(String faultSource) { this.faultSource = faultSource; }
    public String getEquipmentStatus() { return equipmentStatus; }
    public void setEquipmentStatus(String equipmentStatus) { this.equipmentStatus = equipmentStatus; }
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
    public Boolean getReplacementRequired() { return replacementRequired; }
    public void setReplacementRequired(Boolean replacementRequired) { this.replacementRequired = replacementRequired; }
    public String getSolution() { return solution; }
    public void setSolution(String solution) { this.solution = solution; }
    public String getSpecificDesc() { return specificDesc; }
    public void setSpecificDesc(String specificDesc) { this.specificDesc = specificDesc; }
}

