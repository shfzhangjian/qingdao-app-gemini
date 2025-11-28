package com.lucksoft.qingdao.tspm.dto.sync;

import java.io.Serializable;

/**
 * 接口7请求参数: 获取包机信息
 * Topic: tims.sync.user.equipment
 */
public class UserEquipmentReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 上一次同步的日期时间 (必填)
     * 格式: yyyy-MM-dd HH:mm:ss
     */
    private String lastSyncDateTime;

    /**
     * 页码 (选填)
     */
    private Integer pageNum;

    /**
     * 每页数量 (选填)
     */
    private Integer pageSize;

    // Getters and Setters
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