package com.lucksoft.qingdao.tmis.metrology.dto;

import java.io.Serializable;
import java.util.Date;

/**
 * 点检列表原始数据DTO，用于接收从数据库查询出的列表行数据。
 * 字段名直接对应数据库视图JL_EQUIP_DXJ中的列名。
 */
public class PointCheckListItemDTO  implements Serializable {
    private static final long serialVersionUID = 1L;
    // 主键
    private Long indocno;
    // 企业内码
    private Long sjid;
    // 企业编码
    private String sjno;
    // 设备名称
    private String sjname;
    // 点检日期
    private Date dinit;
    // 使用部门ID
    private Long iusedept;
    // 使用部门
    private String susedept;
    // ABC分类 (1, 2, 3)
    private Integer sabc;
    // 点检状态 (1:已检, 2:待检)
    private Integer idjstate;
    // 设备状态 (1:在用, 2:封存...)
    private Integer istate;
    // 强检标识 (1:是, 2:否)
    private Integer iqj;
    // 能源分类
    private String snytype;

    // Getters and Setters
    public Long getIndocno() { return indocno; }
    public void setIndocno(Long indocno) { this.indocno = indocno; }
    public Long getSjid() { return sjid; }
    public void setSjid(Long sjid) { this.sjid = sjid; }
    public String getSjno() { return sjno; }
    public void setSjno(String sjno) { this.sjno = sjno; }
    public String getSjname() { return sjname; }
    public void setSjname(String sjname) { this.sjname = sjname; }
    public Date getDinit() { return dinit; }
    public void setDinit(Date dinit) { this.dinit = dinit; }
    public Long getIusedept() { return iusedept; }
    public void setIusedept(Long iusedept) { this.iusedept = iusedept; }
    public String getSusedept() { return susedept; }
    public void setSusedept(String susedept) { this.susedept = susedept; }
    public Integer getSabc() { return sabc; }
    public void setSabc(Integer sabc) { this.sabc = sabc; }
    public Integer getIdjstate() { return idjstate; }
    public void setIdjstate(Integer idjstate) { this.idjstate = idjstate; }
    public Integer getIstate() { return istate; }
    public void setIstate(Integer istate) { this.istate = istate; }
    public Integer getIqj() { return iqj; }
    public void setIqj(Integer iqj) { this.iqj = iqj; }
    public String getSnytype() { return snytype; }
    public void setSnytype(String snytype) { this.snytype = snytype; }
}
