package com.lucksoft.qingdao.tspm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for 15. 获取包机信息
 * Topic: tims.sync.user.equipment
 */
public class UserEquipmentDTO {
    /**
     * 用户编码(唯一标识)
     */
    @JsonProperty("userCode")
    private String userCode;

    /**
     * 设备编码
     */
    @JsonProperty("equipmentCode")
    private String equipmentCode;

    /**
     * 班次开始时间
     */
    @JsonProperty("shiftStartTime")
    private String shiftStartTime;

    /**
     * 班次ID
     */
    @JsonProperty("shiftId")
    private Integer shiftId;

    /**
     * 班次名
     */
    @JsonProperty("shiftName")
    private String shiftName;

    /**
     * 类型 (0--第二包机, 1--第一包机)
     */
    @JsonProperty("type")
    private Integer type;

    // Getters and Setters
    public String getUserCode() { return userCode; }
    public void setUserCode(String userCode) { this.userCode = userCode; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getShiftStartTime() { return shiftStartTime; }
    public void setShiftStartTime(String shiftStartTime) { this.shiftStartTime = shiftStartTime; }
    public Integer getShiftId() { return shiftId; }
    public void setShiftId(Integer shiftId) { this.shiftId = shiftId; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
}

