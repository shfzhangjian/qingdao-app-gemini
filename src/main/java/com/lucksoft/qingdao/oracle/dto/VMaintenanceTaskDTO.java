package com.lucksoft.qingdao.oracle.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * [新] DTO (数据传输对象)
 * 用于映射 Oracle 视图 V_MAINTENANCE_TASKS_RECENT 的查询结果。
 * 这个类的结构与 TIMS 系统的 MaintenanceTaskDTO 完全一致，
 * 使得 Java 代码无需再次转换，可以直接将查询结果推送到 Kafka。
 */
public class VMaintenanceTaskDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("planId")
    private String planId;

    @JsonProperty("taskId")
    private String taskId;

    @JsonProperty("equipmentCode")
    private String equipmentCode;

    @JsonProperty("type")
    private Integer type;

    @JsonProperty("project")
    private String project;

    @JsonProperty("content")
    private String content;

    @JsonProperty("standard")
    private String standard;

    @JsonProperty("tool")
    private String tool;

    @JsonProperty("fullScore")
    private Integer fullScore;

    @JsonProperty("createDateTime")
    private String createDateTime;

    @JsonProperty("planStartTime")
    private String planStartTime;

    @JsonProperty("operator")
    private String operator;

    @JsonProperty("oilId")
    private String oilId;

    @JsonProperty("oilQuantity")
    private String oilQuantity;

    /**
     * 这个字段用于 Redis 防重，但不需要序列化到最终的 Kafka JSON 中。
     * @JsonIgnore 确保它不会被 ObjectMapper 包含。
     */
    @JsonIgnore
    private String deDupeKey;

    // Getters and Setters

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getEquipmentCode() {
        return equipmentCode;
    }

    public void setEquipmentCode(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getProject() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getTool() {
        return tool;
    }

    public void setTool(String tool) {
        this.tool = tool;
    }

    public Integer getFullScore() {
        return fullScore;
    }

    public void setFullScore(Integer fullScore) {
        this.fullScore = fullScore;
    }

    public String getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(String createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getPlanStartTime() {
        return planStartTime;
    }

    public void setPlanStartTime(String planStartTime) {
        this.planStartTime = planStartTime;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getOilId() {
        return oilId;
    }

    public void setOilId(String oilId) {
        this.oilId = oilId;
    }

    public String getOilQuantity() {
        return oilQuantity;
    }

    public void setOilQuantity(String oilQuantity) {
        this.oilQuantity = oilQuantity;
    }

    public String getDeDupeKey() {
        return deDupeKey;
    }

    public void setDeDupeKey(String deDupeKey) {
        this.deDupeKey = deDupeKey;
    }
}