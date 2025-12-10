package com.lucksoft.qingdao.tmis.metrology.dto;

import com.lucksoft.qingdao.tmis.dto.PageQuery;
import com.lucksoft.qingdao.tmis.metrology.ExportColumn;
import java.util.List;

public class TaskQuery extends PageQuery {
    private String deviceName;
    private String enterpriseId;
    private String dateRange;
    private String taskStatus;
    private String abcCategory;
    private String exceptionStatus; // [新增] 异常状态: "all" or "abnormal"
    private List<ExportColumn> columns;
    private String loginId;

    // Getters and Setters
    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getDateRange() {
        return dateRange;
    }

    public void setDateRange(String dateRange) {
        this.dateRange = dateRange;
    }

    public String getTaskStatus() {
        return taskStatus;
    }

    public void setTaskStatus(String taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getAbcCategory() {
        return abcCategory;
    }

    public void setAbcCategory(String abcCategory) {
        this.abcCategory = abcCategory;
    }

    public String getExceptionStatus() {
        return exceptionStatus;
    }

    public void setExceptionStatus(String exceptionStatus) {
        this.exceptionStatus = exceptionStatus;
    }

    public List<ExportColumn> getColumns() {
        return columns;
    }

    public void setColumns(List<ExportColumn> columns) {
        this.columns = columns;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    @Override
    public String toString() {
        return "TaskQuery{" +
                "deviceName='" + deviceName + '\'' +
                ", enterpriseId='" + enterpriseId + '\'' +
                ", dateRange='" + dateRange + '\'' +
                ", taskStatus='" + taskStatus + '\'' +
                ", abcCategory='" + abcCategory + '\'' +
                ", exceptionStatus='" + exceptionStatus + '\'' +
                ", loginId='" + loginId + '\'' +
                ", pageNum=" + getPageNum() +
                ", pageSize=" + getPageSize() +
                '}';
    }
}