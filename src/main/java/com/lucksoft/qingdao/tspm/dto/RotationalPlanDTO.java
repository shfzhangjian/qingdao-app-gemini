package com.lucksoft.qingdao.tspm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for 5. 获取轮保计划排期
 * Topic: tims.sync.rotational.plan
 */
public class RotationalPlanDTO {
    /**
     * 计划唯一标识
     */
    @JsonProperty("planId")
    private String planId;

    /**
     * 设备编码(唯一标识)
     */
    @JsonProperty("equipmentCode")
    private String equipmentCode;

    /**
     * 轮保计划日期
     */
    @JsonProperty("planDate")
    private String planDate;

    /**
     * 轮保计划创建日期
     */
    @JsonProperty("createDate")
    private String createDate;

    // Getters and Setters
    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getPlanDate() { return planDate; }
    public void setPlanDate(String planDate) { this.planDate = planDate; }
    public String getCreateDate() { return createDate; }
    public void setCreateDate(String createDate) { this.createDate = createDate; }
}

