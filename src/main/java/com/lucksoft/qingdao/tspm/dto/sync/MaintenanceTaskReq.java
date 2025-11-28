package com.lucksoft.qingdao.tspm.dto.sync;

import java.io.Serializable;

/**
 * 接口1请求参数: 获取保养、点检、润滑任务
 */
public class MaintenanceTaskReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 单据类型 (必填)
     * 0 -- 保养 (例保)
     * 1 -- 保养 (日保)
     * 2 -- 保养 (月保)
     * 3 -- 保养 (轮保)
     * 4 -- 点检
     * 5 -- 润滑
     */
    private Integer type;

    /**
     * 上一次同步的日期时间 (必填)
     * 格式: yyyy-MM-dd HH:mm:ss
     */
    private String lastSyncDateTime;

    /**
     * 页码 (选填，不传则不分页)
     */
    private Integer pageNum;

    /**
     * 每页数量 (选填)
     */
    private Integer pageSize;

    // Getters and Setters
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getLastSyncDateTime() {
        return lastSyncDateTime;
    }

    public void setLastSyncDateTime(String lastSyncDateTime) {
        this.lastSyncDateTime = lastSyncDateTime;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}