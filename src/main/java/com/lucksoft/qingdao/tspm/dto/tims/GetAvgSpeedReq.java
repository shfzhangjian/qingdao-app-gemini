package com.lucksoft.qingdao.tspm.dto.tims;

import java.io.Serializable;

/**
 * 接口7: 获取设备指定时间段内平均车速 - 请求参数
 */
public class GetAvgSpeedReq implements Serializable {
    private String equipmentCode; // 必填
    private String startTime;     // 可选 (yyyy-MM-dd HH:mm:ss)
    private String endTime;       // 可选 (yyyy-MM-dd HH:mm:ss)


    public GetAvgSpeedReq() {
    }

    public GetAvgSpeedReq(String equipmentCode) {
        this.equipmentCode = equipmentCode;
    }

    public GetAvgSpeedReq(String equipmentCode, String startTime, String endTime) {
        this.equipmentCode = equipmentCode;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}