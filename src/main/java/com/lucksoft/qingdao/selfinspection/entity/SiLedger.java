package com.lucksoft.qingdao.selfinspection.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 自检自控台账实体
 * 对应表: T_SI_LEDGER
 */
public class SiLedger implements Serializable {
    private Long id;
    private String workshop;      // 车间
    private String name;          // 名称
    private String model;         // 所属机型
    private String device;        // 所属设备
    private String mainDevice;    // 所属设备主数据名称
    private String factory;       // 厂家
    private String spec;          // 规格型号
    private String location;      // 安装位置
    private String principle;     // 测量原理
    private String pmCode;        // PM设备编码
    private String orderNo;       // 订单号
    private String assetCode;     // 资产编码

    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date firstUseDate;    // 初次使用时间

    private String auditStatus;   // 审批状态
    private Integer hasStandard;  // 是否上传标准 (0/1)

    private Date createTime;
    private Date updateTime;

    // --- Getters and Setters (省略 Lombok 以保证纯 JDK 兼容性展示) ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getWorkshop() { return workshop; }
    public void setWorkshop(String workshop) { this.workshop = workshop; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public String getDevice() { return device; }
    public void setDevice(String device) { this.device = device; }
    public String getMainDevice() { return mainDevice; }
    public void setMainDevice(String mainDevice) { this.mainDevice = mainDevice; }
    public String getFactory() { return factory; }
    public void setFactory(String factory) { this.factory = factory; }
    public String getSpec() { return spec; }
    public void setSpec(String spec) { this.spec = spec; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getPrinciple() { return principle; }
    public void setPrinciple(String principle) { this.principle = principle; }
    public String getPmCode() { return pmCode; }
    public void setPmCode(String pmCode) { this.pmCode = pmCode; }
    public String getOrderNo() { return orderNo; }
    public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    public String getAssetCode() { return assetCode; }
    public void setAssetCode(String assetCode) { this.assetCode = assetCode; }
    public Date getFirstUseDate() { return firstUseDate; }
    public void setFirstUseDate(Date firstUseDate) { this.firstUseDate = firstUseDate; }
    public String getAuditStatus() { return auditStatus; }
    public void setAuditStatus(String auditStatus) { this.auditStatus = auditStatus; }
    public Integer getHasStandard() { return hasStandard; }
    public void setHasStandard(Integer hasStandard) { this.hasStandard = hasStandard; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}