package com.lucksoft.qingdao.tmis.metrology.dto;

public class PointCheckListDTO {
    private int id;
    private String department;
    private String deviceName;
    private String deviceType; // ABC分类
    private String checkDate; // yyyy-MM-dd
    private String planStatus; // 计划状态: 已检, 未检
    private String resultStatus; // 结果状态: 正常, 异常, 未检

    public PointCheckListDTO(int id, String department, String deviceName, String deviceType, String checkDate, String planStatus, String resultStatus) {
        this.id = id;
        this.department = department;
        this.deviceName = deviceName;
        this.deviceType = deviceType;
        this.checkDate = checkDate;
        this.planStatus = planStatus;
        this.resultStatus = resultStatus;
    }

    // Getters
    public int getId() { return id; }
    public String getDepartment() { return department; }
    public String getDeviceName() { return deviceName; }
    public String getDeviceType() { return deviceType; }
    public String getCheckDate() { return checkDate; }
    public String getPlanStatus() { return planStatus; }
    public String getResultStatus() { return resultStatus; }
}

