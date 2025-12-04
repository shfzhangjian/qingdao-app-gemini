package com.lucksoft.qingdao.selfinspection.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

public class ZjzkTaskDetail implements Serializable {
    private Long indocno;
    private Long taskId;
    private Long toolId;    // 关联 ZJZK_TOOL

    private String itemName;    // 安装位置 (SAZWZ)

    private String checkResult;
    private String checkRemark;
    private Integer isConfirmed;

    private String operatorName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date opTime;

    private String confirmName;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date confirmOpTime;

    // 辅助字段（不需要存库，仅查询用）
    private String toolName;   // 台账名称 SNAME
    private String toolModel;  // 规格型号 SXH

    // Getters/Setters
    public Long getIndocno() { return indocno; }
    public void setIndocno(Long indocno) { this.indocno = indocno; }
    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }
    public Long getToolId() { return toolId; }
    public void setToolId(Long toolId) { this.toolId = toolId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getCheckResult() { return checkResult; }
    public void setCheckResult(String checkResult) { this.checkResult = checkResult; }
    public String getCheckRemark() { return checkRemark; }
    public void setCheckRemark(String checkRemark) { this.checkRemark = checkRemark; }
    public Integer getIsConfirmed() { return isConfirmed; }
    public void setIsConfirmed(Integer isConfirmed) { this.isConfirmed = isConfirmed; }
    public String getOperatorName() { return operatorName; }
    public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
    public Date getOpTime() { return opTime; }
    public void setOpTime(Date opTime) { this.opTime = opTime; }
    public String getConfirmName() { return confirmName; }
    public void setConfirmName(String confirmName) { this.confirmName = confirmName; }
    public Date getConfirmOpTime() { return confirmOpTime; }
    public void setConfirmOpTime(Date confirmOpTime) { this.confirmOpTime = confirmOpTime; }

    public String getToolName() { return toolName; }
    public void setToolName(String toolName) { this.toolName = toolName; }
    public String getToolModel() { return toolModel; }
    public void setToolModel(String toolModel) { this.toolModel = toolModel; }
}