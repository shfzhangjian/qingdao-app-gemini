package com.lucksoft.qingdao.tspm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for 9. 反馈轮保任务完成得分
 * Topic: tims.feedback.rotational.task.score
 */
public class RotationalTaskScoreFeedbackDTO {
    /**
     * 轮保任务唯一标识
     */
    @JsonProperty("taskId")
    private String taskId;

    @JsonProperty("type")
    private Integer type;
    public Integer getType() {        return type;    }
    public void setType(Integer type) {        this.type = type;    }
    /**
     * 维修工对任务完成情况打分
     */
    @JsonProperty("score")
    private Integer score;

    // Getters and Setters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }


}

