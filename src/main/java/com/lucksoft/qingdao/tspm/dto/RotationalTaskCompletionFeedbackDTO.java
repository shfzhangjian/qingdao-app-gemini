package com.lucksoft.qingdao.tspm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for 8. 反馈轮保任务完成情况
 * Topic: tims.feedback.completed.rotational.task
 */
public class RotationalTaskCompletionFeedbackDTO {
    /**
     * 轮保任务唯一标识
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

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getCompleteUser() { return completeUser; }
    public void setCompleteUser(String completeUser) { this.completeUser = completeUser; }
    public String getCompleteDateTime() { return completeDateTime; }
    public void setCompleteDateTime(String completeDateTime) { this.completeDateTime = completeDateTime; }
}

