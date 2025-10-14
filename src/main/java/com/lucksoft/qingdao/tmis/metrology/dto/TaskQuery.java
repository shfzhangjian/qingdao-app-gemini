package com.lucksoft.qingdao.tmis.metrology.dto;


import com.lucksoft.qingdao.tmis.dto.PageQuery;

/**
 * 计量任务查询参数 DTO
 */
public class TaskQuery extends PageQuery {
    private String deviceName;

    // Getters and Setters
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    @Override
    public String toString() {
        return "TaskQuery{" +
                "deviceName='" + deviceName + '\'' +
                ", pageNum=" + getPageNum() +
                ", pageSize=" + getPageSize() +
                '}';
    }
}
