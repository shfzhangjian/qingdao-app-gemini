package com.lucksoft.qingdao.tmis.metrology.dto;

import java.util.List;

/**
 * 用于接收前端更新计量任务请求的 DTO.
 */
public class UpdateTaskRequestDTO {

    /**
     * [核心修复] 将 List<Integer> 修改为 List<String>
     * 以正确接收前端传来的由 data-row-id 构成的字符串数组。
     */
    private List<String> ids;
    private String pointCheckStatus;
    private String checkResult;
    private String abnormalDesc;
    private String checkRemark;

    public String getCheckRemark() {
        return checkRemark;
    }

    public void setCheckRemark(String checkRemark) {
        this.checkRemark = checkRemark;
    }

    // Getters and Setters
    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    public String getPointCheckStatus() {
        return pointCheckStatus;
    }

    public void setPointCheckStatus(String pointCheckStatus) {
        this.pointCheckStatus = pointCheckStatus;
    }

    public String getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }

    public String getAbnormalDesc() {
        return abnormalDesc;
    }

    public void setAbnormalDesc(String abnormalDesc) {
        this.abnormalDesc = abnormalDesc;
    }
}

