package com.lucksoft.qingdao.tmis.metrology.dto;

public class MetrologyTaskDTO {
    private int id;
    private String date;
    private String enterpriseId;
    private String erpId;
    private String deviceName;
    private String model;
    private String factoryId;
    private String range;
    private String location;
    private String accuracy;
    private String status;
    private String pointCheckStatus;
    private boolean isAbnormal;
    private String abc;

    public MetrologyTaskDTO() {}

    public MetrologyTaskDTO(int id, String date, String enterpriseId, String erpId, String deviceName, String model, String factoryId, String range, String location, String accuracy, String status, String pointCheckStatus, boolean isAbnormal, String abc) {
        this.id = id;
        this.date = date;
        this.enterpriseId = enterpriseId;
        this.erpId = erpId;
        this.deviceName = deviceName;
        this.model = model;
        this.factoryId = factoryId;
        this.range = range;
        this.location = location;
        this.accuracy = accuracy;
        this.status = status;
        this.pointCheckStatus = pointCheckStatus;
        this.isAbnormal = isAbnormal;
        this.abc = abc;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(String enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getErpId() { return erpId; }
    public void setErpId(String erpId) { this.erpId = erpId; }
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getFactoryId() { return factoryId; }
    public void setFactoryId(String factoryId) { this.factoryId = factoryId; }
    public String getRange() { return range; }
    public void setRange(String range) { this.range = range; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getAccuracy() { return accuracy; }
    public void setAccuracy(String accuracy) { this.accuracy = accuracy; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPointCheckStatus() { return pointCheckStatus; }
    public void setPointCheckStatus(String pointCheckStatus) { this.pointCheckStatus = pointCheckStatus; }
    public boolean isAbnormal() { return isAbnormal; }
    public void setAbnormal(boolean abnormal) { isAbnormal = abnormal; }
    public String getAbc() { return abc; }
    public void setAbc(String abc) { this.abc = abc; }
}

