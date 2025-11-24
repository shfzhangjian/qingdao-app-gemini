package com.lucksoft.qingdao.selfinspection.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 点检标准明细
 * 对应表: T_SI_STANDARD
 */
public class SiStandard implements Serializable {
    private Long id;
    private Long ledgerId;        // 关联台账ID
    private String devicePart;    // 检测装置 (SJCZZ)
    private String itemName;      // 检测项目 (SJCXM)
    private String standardDesc;  // 检测标准 (SJCBZ)
    private String executorRole;  // 执行人 (SEXUSER)
    private Integer checkCycle;   // 检查周期(天)

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date nextExecDate;    // 下次执行时间

    // --- Getters and Setters ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getLedgerId() { return ledgerId; }
    public void setLedgerId(Long ledgerId) { this.ledgerId = ledgerId; }
    public String getDevicePart() { return devicePart; }
    public void setDevicePart(String devicePart) { this.devicePart = devicePart; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getStandardDesc() { return standardDesc; }
    public void setStandardDesc(String standardDesc) { this.standardDesc = standardDesc; }
    public String getExecutorRole() { return executorRole; }
    public void setExecutorRole(String executorRole) { this.executorRole = executorRole; }
    public Integer getCheckCycle() { return checkCycle; }
    public void setCheckCycle(Integer checkCycle) { this.checkCycle = checkCycle; }
    public Date getNextExecDate() { return nextExecDate; }
    public void setNextExecDate(Date nextExecDate) { this.nextExecDate = nextExecDate; }
}