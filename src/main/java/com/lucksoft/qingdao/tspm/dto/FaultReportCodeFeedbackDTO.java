package com.lucksoft.qingdao.tspm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO for 12. 接收故障报告创建成功后的报告编码
 * Topic: tims.receive.fault.report.code
 */
public class FaultReportCodeFeedbackDTO {
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
    public Integer getType() { return type; }
    public void setType(Integer type) { this.type = type; }
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}

