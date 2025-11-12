package com.lucksoft.qingdao.oracle.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * [新] DTO (数据传输对象)
 * 用于映射 Oracle 视图 V_TMIS_REPORT_CODE 的查询结果。
 * 这个类的结构与 TIMS 系统的 FaultReportCodeFeedbackDTO 完全一致。
 */
public class VFaultReportCodeDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 报告类型 (0--故障报告, 1--故障分析报告)
     */
    @JsonProperty("type")
    private Integer type;

    /**
     * TIMS系统报告数据记录主键(唯一标识)
     */
    @JsonProperty("id")
    private Integer id;

    /**
     * 报告编号
     */
    @JsonProperty("code")
    private String code;

    // Getters and Setters

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}