package com.lucksoft.qingdao.oracle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import java.util.Date;

/**
 * DTO, 用于封装从 PMISSIONBOARDDAY 表查询的数据.
 * 字段名已根据TSPM DTO的风格进行了轻微调整。
 */
public class PmissionBoardDayDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("taskId")
    private Long idocid; // 任务id

    @JsonProperty("taskType")
    private String miType; // 任务类型名称

    @JsonProperty("taskTitle")
    private String miTitle; // 标准名称

    @JsonProperty("planDate")
    private Date miDate; // 任务计划执行时间

    @JsonProperty("creatorId")
    private Long makePeople; // 下发人ID

    @JsonProperty("equipmentName")
    private String makeFlName; // 设备名

    @JsonProperty("dutyName")
    private String sDuty; // 岗位名称

    @JsonProperty("planStartTime")
    private Date dBegin; // 计划开始时间

    @JsonProperty("planEndTime")
    private Date dEnd; // 计划结束时间

    @JsonProperty("createTime")
    private Date dRegt; // 任务创建时间

    // Getters and Setters

    public Long getIdocid() {
        return idocid;
    }

    public void setIdocid(Long idocid) {
        this.idocid = idocid;
    }

    public String getMiType() {
        return miType;
    }

    public void setMiType(String miType) {
        this.miType = miType;
    }

    public String getMiTitle() {
        return miTitle;
    }

    public void setMiTitle(String miTitle) {
        this.miTitle = miTitle;
    }

    public Date getMiDate() {
        return miDate;
    }

    public void setMiDate(Date miDate) {
        this.miDate = miDate;
    }

    public Long getMakePeople() {
        return makePeople;
    }

    public void setMakePeople(Long makePeople) {
        this.makePeople = makePeople;
    }

    public String getMakeFlName() {
        return makeFlName;
    }

    public void setMakeFlName(String makeFlName) {
        this.makeFlName = makeFlName;
    }

    public String getsDuty() {
        return sDuty;
    }

    public void setsDuty(String sDuty) {
        this.sDuty = sDuty;
    }

    public Date getdBegin() {
        return dBegin;
    }

    public void setdBegin(Date dBegin) {
        this.dBegin = dBegin;
    }

    public Date getdEnd() {
        return dEnd;
    }

    public void setdEnd(Date dEnd) {
        this.dEnd = dEnd;
    }

    public Date getdRegt() {
        return dRegt;
    }

    public void setdRegt(Date dRegt) {
        this.dRegt = dRegt;
    }
}