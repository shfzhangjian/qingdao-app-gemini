package com.lucksoft.qingdao.selfinspection.dto;

import java.util.Date;
import java.util.List;

/**
 * 生成任务的请求参数 (更新版)
 */
public class GenerateTaskReq {
    private Date taskTime;
    private String taskType;
    private String prodStatus;
    private String shiftType;
    private String shift;

    // [修改] 不再使用 ID 列表，而是使用联合主键列表
    private List<DeviceKeyDto> selectedDevices;

    // Getters/Setters
    public Date getTaskTime() { return taskTime; }
    public void setTaskTime(Date taskTime) { this.taskTime = taskTime; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getProdStatus() { return prodStatus; }
    public void setProdStatus(String prodStatus) { this.prodStatus = prodStatus; }
    public String getShiftType() { return shiftType; }
    public void setShiftType(String shiftType) { this.shiftType = shiftType; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }

    public List<DeviceKeyDto> getSelectedDevices() { return selectedDevices; }
    public void setSelectedDevices(List<DeviceKeyDto> selectedDevices) { this.selectedDevices = selectedDevices; }
}