package com.lucksoft.qingdao.oracle.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * [新] DTO (数据传输对象)
 * 用于映射 Oracle 视图 V_ROTATIONAL_TASK_RECENT 的查询结果。
 * 这个类的结构与 TIMS 系统的 ScreenedRotationalTaskDTO 完全一致。
 */
public class VRotationalTaskDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("planId")
    private String planId;

    @JsonProperty("taskId")
    private String taskId;

    @JsonProperty("equipmentCode")
    private String equipmentCode;

    @JsonProperty("project")
    private String project;

    @JsonProperty("content")
    private String content;

    @JsonProperty("standard")
    private String standard;

    @JsonProperty("source")
    private Integer source;

    @JsonProperty("fullScore")
    private Integer fullScore;

    @JsonProperty("createDateTime")
    private String createDateTime;

    @JsonProperty("operator")
    private String operator;

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

    public Integer getSource() {
        return source;
    }

    public void setSource(Integer source) {
        this.source = source;
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

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getDeDupeKey() {
        return deDupeKey;
    }

    public void setDeDupeKey(String deDupeKey) {
        this.deDupeKey = deDupeKey;
    }
}