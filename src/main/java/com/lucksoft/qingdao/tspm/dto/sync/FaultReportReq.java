package com.lucksoft.qingdao.tspm.dto.sync;

import java.io.Serializable;

/**
 * 接口 4/10 请求参数: 故障维修报告创建
 * Topic: tims.create.fault.report
 */
public class FaultReportReq implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 故障报告ID (可选，精确查询)
     */
    private Integer id;

    /**
     * 设备编码 (可选，精确查询)
     */
    private String equipmentCode;

    /**
     * 页码 (选填)
     */
    private Integer pageNum;

    /**
     * 每页数量 (选填)
     */
    private Integer pageSize;

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEquipmentCode() {
        return equipmentCode;
    }

    public void setEquipmentCode(String equipmentCode) {
        this.equipmentCode = equipmentCode;
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