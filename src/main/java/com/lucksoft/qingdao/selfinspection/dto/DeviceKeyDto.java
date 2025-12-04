package com.lucksoft.qingdao.selfinspection.dto;

import java.io.Serializable;

/**
 * 生成任务时选中的设备联合主键
 */
public class DeviceKeyDto implements Serializable {
    private String spmcode;
    private String sname;

    public String getSpmcode() { return spmcode; }
    public void setSpmcode(String spmcode) { this.spmcode = spmcode; }

    public String getSname() { return sname; }
    public void setSname(String sname) { this.sname = sname; }
}