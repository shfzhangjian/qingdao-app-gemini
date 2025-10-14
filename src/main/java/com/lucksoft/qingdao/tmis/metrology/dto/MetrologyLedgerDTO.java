package com.lucksoft.qingdao.tmis.metrology.dto;

/**
 * 计量台账数据 DTO
 */
public class MetrologyLedgerDTO {
    private boolean expired;
    private boolean isLinked;
    private String sysId;
    private int seq;
    private String enterpriseId;
    private String erpId;
    private String deviceName;
    private String model;
    private String factoryId;
    private String range;
    private String location;
    private String accuracy;
    private String parentDevice;
    private String abc;
    private String nextDate;
    private String status;
    private String department;

    // 全参构造函数，用于模拟数据生成
    public MetrologyLedgerDTO(boolean expired, boolean isLinked, String sysId, int seq, String enterpriseId, String erpId, String deviceName, String model, String factoryId, String range, String location, String accuracy, String parentDevice, String abc, String nextDate, String status, String department) {
        this.expired = expired;
        this.isLinked = isLinked;
        this.sysId = sysId;
        this.seq = seq;
        this.enterpriseId = enterpriseId;
        this.erpId = erpId;
        this.deviceName = deviceName;
        this.model = model;
        this.factoryId = factoryId;
        this.range = range;
        this.location = location;
        this.accuracy = accuracy;
        this.parentDevice = parentDevice;
        this.abc = abc;
        this.nextDate = nextDate;
        this.status = status;
        this.department = department;
    }

    // Getters
    public boolean isExpired() { return expired; }
    public boolean isLinked() { return isLinked; }
    public String getSysId() { return sysId; }
    public int getSeq() { return seq; }
    public String getEnterpriseId() { return enterpriseId; }
    public String getErpId() { return erpId; }
    public String getDeviceName() { return deviceName; }
    public String getModel() { return model; }
    public String getFactoryId() { return factoryId; }
    public String getRange() { return range; }
    public String getLocation() { return location; }
    public String getAccuracy() { return accuracy; }
    public String getParentDevice() { return parentDevice; }
    public String getAbc() { return abc; }
    public String getNextDate() { return nextDate; }
    public String getStatus() { return status; }
    public String getDepartment() { return department; }
}
