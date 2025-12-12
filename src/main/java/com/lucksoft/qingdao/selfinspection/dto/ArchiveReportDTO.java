package com.lucksoft.qingdao.selfinspection.dto;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 归档报表相关的 DTO
 */
public class ArchiveReportDTO {

    /**
     * 归档/报表请求参数
     */
    public static class Request implements Serializable {
        private String spmcode;      // 设备PM编码 (唯一标识)
        private String deviceName;   // 设备名称 (用于显示)
        private String taskType;     // 任务类型 (如: 三班电气)
        private String dateRange;    // 日期范围 (yyyy-MM-dd 至 yyyy-MM-dd)
        private String month;        // 指定月份 (yyyy-MM, 优先于 dateRange 用于表头显示)

        // Getters & Setters
        public String getSpmcode() { return spmcode; }
        public void setSpmcode(String spmcode) { this.spmcode = spmcode; }
        public String getDeviceName() { return deviceName; }
        public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
        public String getTaskType() { return taskType; }
        public void setTaskType(String taskType) { this.taskType = taskType; }
        public String getDateRange() { return dateRange; }
        public void setDateRange(String dateRange) { this.dateRange = dateRange; }
        public String getMonth() { return month; }
        public void setMonth(String month) { this.month = month; }
    }

    /**
     * 报表响应数据 (用于前端渲染交叉表)
     */
    public static class Response implements Serializable {
        private String title;           // 标题 (e.g. ZB48A ... (三班电气))
        private String yearMonth;       // 年月 (e.g. 2025年 5月)
        private String machineName;     // 机台名称
        private String machineCode;     // 机台编号
        private List<Integer> days;     // 天数列表 (1..31)
        private List<RowData> rows;     // 数据行
        private Map<Integer, String> checkerSigns; // 检查人签字 (Day -> Name)
        private Map<Integer, String> operatorSigns; // 操作工签字 (Day -> Name)

        // Getters & Setters
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getYearMonth() { return yearMonth; }
        public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
        public String getMachineName() { return machineName; }
        public void setMachineName(String machineName) { this.machineName = machineName; }
        public String getMachineCode() { return machineCode; }
        public void setMachineCode(String machineCode) { this.machineCode = machineCode; }
        public List<Integer> getDays() { return days; }
        public void setDays(List<Integer> days) { this.days = days; }
        public List<RowData> getRows() { return rows; }
        public void setRows(List<RowData> rows) { this.rows = rows; }
        public Map<Integer, String> getCheckerSigns() { return checkerSigns; }
        public void setCheckerSigns(Map<Integer, String> checkerSigns) { this.checkerSigns = checkerSigns; }
        public Map<Integer, String> getOperatorSigns() { return operatorSigns; }
        public void setOperatorSigns(Map<Integer, String> operatorSigns) { this.operatorSigns = operatorSigns; }
    }

    /**
     * 报表行数据
     */
    public static class RowData implements Serializable {
        private int seq;             // 序号
        private String itemName;     // 检测装置/项目
        private Map<Integer, String> dailyResults; // 每日结果 (Day -> "√"/"×"/null)

        public RowData(int seq, String itemName) {
            this.seq = seq;
            this.itemName = itemName;
        }

        // Getters & Setters
        public int getSeq() { return seq; }
        public void setSeq(int seq) { this.seq = seq; }
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        public Map<Integer, String> getDailyResults() { return dailyResults; }
        public void setDailyResults(Map<Integer, String> dailyResults) { this.dailyResults = dailyResults; }
    }
}