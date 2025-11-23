package com.lucksoft.qingdao.selfinspection.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 自检自控任务主表
 * 对应表: T_SI_TASK
 */
public class SiTask implements Serializable {
    private Long id;
    private Long ledgerId;
    private String model;
    private String device;
    private String prodStatus;    // 生产状态
    private String shiftType;     // 班别
    private String shift;         // 班次
    private String checkStatus;   // 点检状态
    private String confirmStatus; // 确认状态

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date taskTime;        // 任务日期

    private String taskType;      // 任务类型
    private String isOverdue;

    private String checker;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date checkTime;

    private String confirmer;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmTime;

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLedgerId() { return ledgerId; }
    public void setLedgerId(Long ledgerId) { this.ledgerId = ledgerId; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
    public String getProdStatus() { return prodStatus; }
    public void setProdStatus(String prodStatus) { this.prodStatus = prodStatus; }
    public String getShiftType() { return shiftType; }
    public void setShiftType(String shiftType) { this.shiftType = shiftType; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    public String getCheckStatus() { return checkStatus; }
    public void setCheckStatus(String checkStatus) { this.checkStatus = checkStatus; }
    public String getConfirmStatus() { return confirmStatus; }
    public void setConfirmStatus(String confirmStatus) { this.confirmStatus = confirmStatus; }
    public Date getTaskTime() { return taskTime; }
    public void setTaskTime(Date taskTime) { this.taskTime = taskTime; }
    public String getTaskType() { return taskType; }
    public void setTaskType(String taskType) { this.taskType = taskType; }
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
}