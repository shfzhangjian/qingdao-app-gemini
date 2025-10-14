package com.lucksoft.qingdao.tmis.metrology.dto;

import com.lucksoft.qingdao.tmis.dto.PageQuery;
import com.lucksoft.qingdao.tmis.metrology.ExportColumn;
import java.util.List;

/**
 * 计量台账查询参数 DTO
 */
public class LedgerQuery extends PageQuery {
    // --- QueryForm fields ---
    private String deviceName;
    private String enterpriseId;
    private String factoryId;
    private String department;
    private String locationUser;
    private String parentDevice;

    // --- DataTable filter fields ---
    private String deviceStatus;
    private String abcCategory;

    // --- Export fields ---
    private List<ExportColumn> columns;

    // Getters and Setters
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    public String getEnterpriseId() { return enterpriseId; }
    public void setEnterpriseId(String enterpriseId) { this.enterpriseId = enterpriseId; }
    public String getFactoryId() { return factoryId; }
    public void setFactoryId(String factoryId) { this.factoryId = factoryId; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getLocationUser() { return locationUser; }
    public void setLocationUser(String locationUser) { this.locationUser = locationUser; }
    public String getParentDevice() { return parentDevice; }
    public void setParentDevice(String parentDevice) { this.parentDevice = parentDevice; }
    public String getDeviceStatus() { return deviceStatus; }
    public void setDeviceStatus(String deviceStatus) { this.deviceStatus = deviceStatus; }
    public String getAbcCategory() { return abcCategory; }
    public void setAbcCategory(String abcCategory) { this.abcCategory = abcCategory; }
    public List<ExportColumn> getColumns() { return columns; }
    public void setColumns(List<ExportColumn> columns) { this.columns = columns; }


    @Override
    public String toString() {
        return "LedgerQuery{" +
                "deviceName='" + deviceName + '\'' +
                ", enterpriseId='" + enterpriseId + '\'' +
                ", factoryId='" + factoryId + '\'' +
                ", department='" + department + '\'' +
                ", locationUser='" + locationUser + '\'' +
                ", parentDevice='" + parentDevice + '\'' +
                ", deviceStatus='" + deviceStatus + '\'' +
                ", abcCategory='" + abcCategory + '\'' +
                ", columns=" + (columns != null ? columns.size() : 0) + " items" +
                ", pageNum=" + getPageNum() +
                ", pageSize=" + getPageSize() +
                '}';
    }
}

