package com.lucksoft.qingdao.kb.dto;

import java.util.List;

public class CompleteRequest {
    private List<String> taskIds;
    private String abnormalReason;

    // Getters and Setters
    public List<String> getTaskIds() { return taskIds; }
    public void setTaskIds(List<String> taskIds) { this.taskIds = taskIds; }
    public String getAbnormalReason() { return abnormalReason; }
    public void setAbnormalReason(String abnormalReason) { this.abnormalReason = abnormalReason; }
}
