package com.lucksoft.qingdao.tspm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for 6. TIMS智能推荐预测性维修任务
 * Topic: tims.recommend.rotational.task
 */
public class RecommendedRotationalTaskDTO {
    /**
     * 所属计划的唯一标识
     */
    @JsonProperty("planId")
    private String planId;

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
     * 任务创建日期时间
     */
    @JsonProperty("createDateTime")
    private String createDateTime;

    // Getters and Setters
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getProject() { return project; }
    public void setProject(String project) { this.project = project; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStandard() { return standard; }
    public void setStandard(String standard) { this.standard = standard; }
    public String getCreateDateTime() { return createDateTime; }
    public void setCreateDateTime(String createDateTime) { this.createDateTime = createDateTime; }
}

