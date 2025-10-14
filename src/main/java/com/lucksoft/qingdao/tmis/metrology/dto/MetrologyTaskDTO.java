package com.lucksoft.qingdao.tmis.metrology.dto;

/**
 * 计量任务数据 DTO
 */
public class MetrologyTaskDTO {
    private int id;
    private String date;
    private String pointCheckStatus;
    private String enterpriseId;
    private String erpId;
    private String deviceName;
    private String model;
    private String factoryId;
    private String range;
    private String location;
    private String accuracy;
    private String status;
    private String abc;

    // Getters
    public int getId() { return id; }
    public String getDate() { return date; }
    public String getPointCheckStatus() { return pointCheckStatus; }
    public String getEnterpriseId() { return enterpriseId; }
    public String getErpId() { return erpId; }
    public String getDeviceName() { return deviceName; }
    public String getModel() { return model; }
    public String getFactoryId() { return factoryId; }
    public String getRange() { return range; }
    public String getLocation() { return location; }
    public String getAccuracy() { return accuracy; }
    public String getStatus() { return status; }
    public String getAbc() { return abc; }
}