package com.lucksoft.qingdao.tmis.metrology.dto;

import com.lucksoft.qingdao.tmis.dto.PageQuery;
import com.lucksoft.qingdao.tmis.metrology.ExportColumn;

import java.util.List;


public class PointCheckQuery extends PageQuery {
    private String category; // ABC
    private String dateRange;
    private String department;
    private String planStatus; // 计划状态
    private String resultStatus; // 结果状态
    private String viewMode;
    private List<ExportColumn> columns;

    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getDateRange() { return dateRange; }
    public void setDateRange(String dateRange) { this.dateRange = dateRange; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getPlanStatus() { return planStatus; }
    public void setPlanStatus(String planStatus) { this.planStatus = planStatus; }
    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }
    public String getViewMode() { return viewMode; }
    public void setViewMode(String viewMode) { this.viewMode = viewMode; }
    public List<ExportColumn> getColumns() { return columns; }
    public void setColumns(List<ExportColumn> columns) { this.columns = columns; }
}

