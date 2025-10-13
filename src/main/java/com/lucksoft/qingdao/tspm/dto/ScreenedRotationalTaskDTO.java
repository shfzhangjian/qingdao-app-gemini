package com.lucksoft.qingdao.tspm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for 7. 获取TsPM筛选后轮保任务
 * Topic: tims.sync.rotational.task
 */
public class ScreenedRotationalTaskDTO {
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
     * 设备编码(唯一标识)
     */
    @JsonProperty("equipmentCode")
    private String equipmentCode;

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
     * 计划来源 (0--智能推荐 1-- 标准策略)
     */
    @JsonProperty("source")
    private Integer source;

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
     * 责任岗位
     */
    @JsonProperty("operator")
    private String operator;

    // Getters and Setters
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }
    public Integer getSource() { return source; }
    public void setSource(Integer source) { this.source = source; }
    public Integer getFullScore() { return fullScore; }
    public void setFullScore(Integer fullScore) { this.fullScore = fullScore; }
    public String getCreateDateTime() { return createDateTime; }
    public void setCreateDateTime(String createDateTime) { this.createDateTime = createDateTime; }
    public String getOperator() { return operator; }
    public void setOperator(String operator) { this.operator = operator; }
}

