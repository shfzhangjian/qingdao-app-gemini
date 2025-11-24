package com.lucksoft.qingdao.selfinspection.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 自检自控任务明细表
 * 对应表: T_SI_TASK_DETAIL
 */
public class SiTaskDetail implements Serializable {
    private Long id;
    private Long taskId;
    private String mainDevice;    // 主设备名称 (快照)
    private String itemName;      // 检查项目名 (快照)

    private String result;        // 检查结果: 正常/异常/不用
    private String remarks;       // 检查说明

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkTime;       // 单项检查时间

    private Integer isConfirmed;  // 是否确认 (0/1)

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date confirmTime;     // 单项确认时间

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public String getMainDevice() { return mainDevice; }
    public void setMainDevice(String mainDevice) { this.mainDevice = mainDevice; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }
    public Date getCheckTime() { return checkTime; }
    public void setCheckTime(Date checkTime) { this.checkTime = checkTime; }
    public Integer getIsConfirmed() { return isConfirmed; }
    public void setIsConfirmed(Integer isConfirmed) { this.isConfirmed = isConfirmed; }
    public Date getConfirmTime() { return confirmTime; }
    public void setConfirmTime(Date confirmTime) { this.confirmTime = confirmTime; }
}