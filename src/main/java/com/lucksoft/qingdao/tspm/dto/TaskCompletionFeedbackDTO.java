package com.lucksoft.qingdao.tspm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for 2. 反馈保养、点检、润滑任务完成情况
 * Topic: tims.feedback.completed.maintenance.task
 */
public class TaskCompletionFeedbackDTO {
    /**
     * 任务唯一标识
     */
    @JsonProperty("taskId")
    private String taskId;

    /**
     * 完成人
     */
    @JsonProperty("completeUser")
    private String completeUser;

    /**
     * 完成时间
     */
    @JsonProperty("completeDateTime")
    private String completeDateTime;

    /**
     * 点检反馈实际值
     */
    @JsonProperty("inspectionActualValue")
    private Float inspectionActualValue;

    @JsonProperty("type")
    private Integer type;
    public Integer getType() {        return type;    }
    public void setType(Integer type) {        this.type = type;    }

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getCompleteUser() { return completeUser; }
    public void setCompleteUser(String completeUser) { this.completeUser = completeUser; }
    public String getCompleteDateTime() { return completeDateTime; }
    public void setCompleteDateTime(String completeDateTime) { this.completeDateTime = completeDateTime; }
    public Float getInspectionActualValue() { return inspectionActualValue; }
    public void setInspectionActualValue(Float inspectionActualValue) { this.inspectionActualValue = inspectionActualValue; }
}

