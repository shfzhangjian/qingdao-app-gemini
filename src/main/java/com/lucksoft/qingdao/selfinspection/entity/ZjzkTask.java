package com.lucksoft.qingdao.selfinspection.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

public class ZjzkTask implements Serializable {
    private Long indocno;
    private String taskNo;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date taskTime;

    private String taskType;
    private String prodStatus;
    private String shiftType;
    private String shift;

    private String sjx;         // 所属机型
    private String sfname;      // 所属设备
    private String sbname;      // 主数据名称
    private String spmcode;     // PM编码

    private String checkStatus;
    private String confirmStatus;
    private String isOverdue;

    private String checker;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date checkTime;

    private String confirmer;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmTime;

    private Date createTime;

    private String batchNo;      // [新增] 批次号


    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    // Getters/Setters
    public Long getIndocno() { return indocno; }
    public void setIndocno(Long indocno) { this.indocno = indocno; }
    public String getTaskNo() { return taskNo; }
    public void setTaskNo(String taskNo) { this.taskNo = taskNo; }
    public Date getTaskTime() { return taskTime; }
    public void setTaskTime(Date taskTime) { this.taskTime = taskTime; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
    public String getProdStatus() { return prodStatus; }
    public void setProdStatus(String prodStatus) { this.prodStatus = prodStatus; }
    public String getShiftType() { return shiftType; }
    public void setShiftType(String shiftType) { this.shiftType = shiftType; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    public String getSjx() { return sjx; }
    public void setSjx(String sjx) { this.sjx = sjx; }
    public String getSfname() { return sfname; }
    public void setSfname(String sfname) { this.sfname = sfname; }
    public String getSbname() { return sbname; }
    public void setSbname(String sbname) { this.sbname = sbname; }
    public String getSpmcode() { return spmcode; }
    public void setSpmcode(String spmcode) { this.spmcode = spmcode; }
    public String getCheckStatus() { return checkStatus; }
    public void setCheckStatus(String checkStatus) { this.checkStatus = checkStatus; }
    public String getConfirmStatus() { return confirmStatus; }
    public void setConfirmStatus(String confirmStatus) { this.confirmStatus = confirmStatus; }
    public String getIsOverdue() { return isOverdue; }
    public void setIsOverdue(String isOverdue) { this.isOverdue = isOverdue; }
    public String getChecker() { return checker; }
    public void setChecker(String checker) { this.checker = checker; }
    public Date getCheckTime() { return checkTime; }
    public void setCheckTime(Date checkTime) { this.checkTime = checkTime; }
    public String getConfirmer() { return confirmer; }
    public void setConfirmer(String confirmer) { this.confirmer = confirmer; }
    public Date getConfirmTime() { return confirmTime; }
    public void setConfirmTime(Date confirmTime) { this.confirmTime = confirmTime; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
}