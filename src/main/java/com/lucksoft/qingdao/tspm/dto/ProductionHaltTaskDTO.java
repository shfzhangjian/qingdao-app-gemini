package com.lucksoft.qingdao.tspm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for 13. 获取停产检修计划任务
 * Topic: tims.sync.production.halt.maintenance.task
 */
public class ProductionHaltTaskDTO {
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
     * 检修内容
     */
    @JsonProperty("content")
    private String content;

    /**
     * 负责人
     */
    @JsonProperty("head")
    private String head;

    /**
     * 班组
     */
    @JsonProperty("teamName")
    private String teamName;

    /**
     * 执行人
     */
    @JsonProperty("executor")
    private String executor;

    /**
     * 计划开始时间
     */
    @JsonProperty("planStartTime")
    private String planStartTime;

    /**
     * 计划结束时间
     */
    @JsonProperty("planEndTime")
    private String planEndTime;

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getHead() { return head; }
    public void setHead(String head) { this.head = head; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getExecutor() { return executor; }
    public void setExecutor(String executor) { this.executor = executor; }
    public String getPlanStartTime() { return planStartTime; }
    public void setPlanStartTime(String planStartTime) { this.planStartTime = planStartTime; }
    public String getPlanEndTime() { return planEndTime; }
    public void setPlanEndTime(String planEndTime) { this.planEndTime = planEndTime; }
}

