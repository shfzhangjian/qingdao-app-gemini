package com.lucksoft.qingdao.tspm.dto.tims;

import java.io.Serializable;

/**
 * 接口8: 创建自检自控待办任务 - 请求参数
 */
public class CreateSelfCheckTaskReq implements Serializable {
    private String equipmentCode; // 设备资产编码
    private String taskId;        // 自检自控任务唯一标识
    private String content;       // 内容
    private String createTime;    // 创建时间 (yyyy-MM-dd HH:mm:ss)

    public CreateSelfCheckTaskReq() {}

    public CreateSelfCheckTaskReq(String equipmentCode, String taskId, String content, String createTime) {
        this.equipmentCode = equipmentCode;
        this.taskId = taskId;
        this.content = content;
        this.createTime = createTime;
    }

    // Getters and Setters
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}