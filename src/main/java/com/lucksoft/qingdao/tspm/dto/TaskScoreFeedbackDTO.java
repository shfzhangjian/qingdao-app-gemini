package com.lucksoft.qingdao.tspm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for 3. 反馈保养、点检、润滑任务完成得分
 * Topic: tims.feedback.maintenance.task.score
 */
public class TaskScoreFeedbackDTO {
    /**
     * 任务唯一标识
     */
    @JsonProperty("taskId")
    private String taskId;

    /**
     * 维修工对任务完成情况打分
     */
    @JsonProperty("score")
    private Integer score;

    @JsonProperty("type")
    private Integer type;
    public Integer getType() {        return type;    }
    public void setType(Integer type) {        this.type = type;    }

    /**
     * 整改内容
     */
    @JsonProperty("rectificationContent")
    private String rectificationContent;

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    public String getRectificationContent() { return rectificationContent; }
    public void setRectificationContent(String rectificationContent) { this.rectificationContent = rectificationContent; }
}

