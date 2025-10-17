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

    // --- 新增列表查询字段 ---
    private String sjno; // 企业编号
    private String sjname; // 设备名称
    private Integer istate; // 设备状态
    private Integer iqj; // 强检标识
    private String snytype; // 能源分类
    private String year; // 年份，用于统计查询
    private Long userId; // 当前用户ID，用于权限过滤

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
    public String getSjno() { return sjno; }
    public void setSjno(String sjno) { this.sjno = sjno; }
    public String getSjname() { return sjname; }
    public void setSjname(String sjname) { this.sjname = sjname; }
    public Integer getIstate() { return istate; }
    public void setIstate(Integer istate) { this.istate = istate; }
    public Integer getIqj() { return iqj; }
    public void setIqj(Integer iqj) { this.iqj = iqj; }
    public String getSnytype() { return snytype; }
    public void setSnytype(String snytype) { this.snytype = snytype; }
    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}
