package com.lucksoft.qingdao.tspm.dto.tims;

import java.io.Serializable;

/**
 * 接口8: 创建自检自控待办任务 - 响应参数
 */
public class CreateSelfCheckTaskResp implements Serializable {
    private Integer id;           // TIMS数据表主键ID
    private String equipmentCode;
    private String taskId;
    private String content;
    private String createTime;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getEquipmentCode() { return equipmentCode; }
    public void setEquipmentCode(String equipmentCode) { this.equipmentCode = equipmentCode; }
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
}