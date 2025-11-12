package com.lucksoft.qingdao.oracle.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * [新] DTO (数据传输对象)
 * 用于映射 Oracle 视图 V_ROTATIONAL_PLANS_RECENT 的查询结果。
 * 这个类的结构与 TIMS 系统的 RotationalPlanDTO 完全一致。
 */
public class VRotationalPlanDTO implements Serializable {
    private static final long serialVersionUID = 1L;

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

    /**
     * 这个字段用于 Redis 防重，但不需要序列化到最终的 Kafka JSON 中。
     * @JsonIgnore 确保它不会被 ObjectMapper 包含。
     */
    @JsonIgnore
    private String deDupeKey;

    // Getters and Setters

    public String getPlanId() {
        return planId;
    }

    public void setPlanId(String planId) {
        this.planId = planId;
    }

    public String getEquipmentCode() {
        return equipmentCode;
    }

    public void setEquipmentCode(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }

    public String getPlanDate() {
        return planDate;
    }

    public void setPlanDate(String planDate) {
        this.planDate = planDate;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getDeDupeKey() {
        return deDupeKey;
    }

    public void setDeDupeKey(String deDupeKey) {
        this.deDupeKey = deDupeKey;
    }
}