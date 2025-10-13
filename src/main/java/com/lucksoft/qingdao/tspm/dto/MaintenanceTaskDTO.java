package com.lucksoft.qingdao.tspm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for 1. 获取保养、点检、润滑任务
 * Topic: tims.sync.maintenance.task
 */
public class MaintenanceTaskDTO {
    /**
     * 所属计划的唯一标识
     */
    @JsonProperty("planId")
    private String planId;

    /**
     * 任务唯一标识
     */
    @JsonProperty("taskId")
    private String taskId;

    /**
     * 设备编码
     */
    @JsonProperty("equipmentCode")
    private String equipmentCode;

    /**
     * 单据类型 (0-保养(例保), 1-保养(日保), 2-保养(月保), 3-保养(轮保), 4-点检, 5-润滑)
     */
    @JsonProperty("type")
    private Integer type;

    /**
     * 保养项目
     */
    @JsonProperty("project")
    private String project;

    /**
     * 保养内容
     */
    @JsonProperty("content")
    private String content;

    /**
     * 保养标准
     */
    @JsonProperty("standard")
    private String standard;

    /**
     * 工具
     */
    @JsonProperty("tool")
    private String tool;

    /**
     * 该项任务满分多少
     */
    @JsonProperty("fullScore")
    private Integer fullScore;

    /**
     * 任务创建日期时间
     */
    @JsonProperty("createDateTime")
    private String createDateTime;

    /**
     * 计划开始日期时间
     */
    @JsonProperty("planStartTime")
    private String planStartTime;

    /**
     * 责任岗位
     */
    @JsonProperty("operator")
    private String operator;

    /**
     * 油品(润滑任务特有)
     */
    @JsonProperty("oilId")
    private String oilId;

    /**
     * 油的定量(润滑任务特有)
     */
    @JsonProperty("oilQuantity")
    private String oilQuantity;

    // Getters and Setters
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }
    public String getTool() { return tool; }
    public void setTool(String tool) { this.tool = tool; }
    public Integer getFullScore() { return fullScore; }
    public void setFullScore(Integer fullScore) { this.fullScore = fullScore; }
    public String getCreateDateTime() { return createDateTime; }
    public void setCreateDateTime(String createDateTime) { this.createDateTime = createDateTime; }
    public String getPlanStartTime() { return planStartTime; }
    public void setPlanStartTime(String planStartTime) { this.planStartTime = planStartTime; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
    public String getOilId() { return oilId; }
    public void setOilId(String oilId) { this.oilId = oilId; }
    public String getOilQuantity() { return oilQuantity; }
    public void setOilQuantity(String oilQuantity) { this.oilQuantity = oilQuantity; }
}

