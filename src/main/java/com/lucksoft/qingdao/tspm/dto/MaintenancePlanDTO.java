package com.lucksoft.qingdao.tspm.dto;

import java.io.Serializable;

/**
 * 数据传输对象 (DTO) - 对应PDF文档第1章节: "获取保养、点检、润滑计划"
 * Topic: tims.sync.maintenance.plan
 */
public class MaintenancePlanDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 计划唯一标识
     */
    private String planId;

    /**
     * 设备编码(唯一标识)
     */
    private String equipmentCode;

    /**
     * 单据类型(例如: 1-例保, 2-日保, 3-月保, 4-轮保)
     */
    private Integer type;

    /**
     * 计划创建时间 (格式: yyyy-MM-dd HH:mm:ss)
     */
    private String createDateTime;

    /**
     * 计划开始日期时间 (格式: yyyy-MM-dd HH:mm:ss)
     */
    private String planStartTime;

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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getCreateDateTime() {
        return createDateTime;
    }

    public void setCreateDateTime(String createDateTime) {
        this.createDateTime = createDateTime;
    }

    public String getPlanStartTime() {
        return planStartTime;
    }

    public void setPlanStartTime(String planStartTime) {
        this.planStartTime = planStartTime;
    }
}
