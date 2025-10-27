package com.lucksoft.qingdao.kb.dto;

import java.util.Date;

public class Task {
    private String id;
    private String project;
    private String content;
    private String standard;
    private int score;
    private String executor;
    private String responsible;
    private String status; // pending, completed
    private boolean isAbnormal;
    private String abnormalReason;
    private Date completeDate;
    private Integer checkedScore;
    private String checker;

    // A temporary field for client-side score adjustments before submitting
    private int currentScore;

    public Task(String id, String project, String content, String standard, int score, String executor) {
        this.id = id;
        this.project = project;
        this.content = content;
        this.standard = standard;
        this.score = score;
        this.executor = executor;
        // Default values
        this.currentScore = score;
        this.responsible = "操作工";
        this.status = "pending";
        this.isAbnormal = false;
    }


    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }
    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }
    public String getExecutor() { return executor; }
    public void setExecutor(String executor) { this.executor = executor; }
    public String getResponsible() { return responsible; }
    public void setResponsible(String responsible) { this.responsible = responsible; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public boolean getIsAbnormal() { return isAbnormal; }
    public void setIsAbnormal(boolean isAbnormal) { this.isAbnormal = isAbnormal; }
    public String getAbnormalReason() { return abnormalReason; }
    public void setAbnormalReason(String abnormalReason) { this.abnormalReason = abnormalReason; }
    public Date getCompleteDate() { return completeDate; }
    public void setCompleteDate(Date completeDate) { this.completeDate = completeDate; }
    public Integer getCheckedScore() { return checkedScore; }
    public void setCheckedScore(Integer checkedScore) { this.checkedScore = checkedScore; }
    public String getChecker() { return checker; }
    public void setChecker(String checker) { this.checker = checker; }
    public int getCurrentScore() { return currentScore; }
    public void setCurrentScore(int currentScore) { this.currentScore = currentScore; }
}
