package com.lucksoft.qingdao.selfinspection.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * 自检自控台账实体 (对应 ZJZK_TOOL 表)
 */
public class ZjzkTool implements Serializable {
    private Long indocno;       // 内码
    private Long sregid;        // 创建人ID
    private String sregnm;      // 创建人
    private Date dregt;         // 创建日期
    private Long smodid;        // 修改人ID
    private String smodnm;      // 修改人
    private Date dmodt;         // 修改日期

    private String sname;       // 名称
    private String szznolb;     // 装置编号类别
    private String sdept;       // 车间
    private String idept;       // 车间ID
    private String sjx;         // 所属机型
    private String ijx;         // 所属机型ID
    private String sfname;      // 所属设备
    private String sfcode;      // 所属设备编码
    private String sbname;      // 所属设备主数据名称
    private String scj;         // 厂家
    private String sxh;         // 规格型号
    private String sazwz;       // 安装位置
    private String ssm;         // 使用寿命
    private String syl;         // 测量原理
    private String iszc;        // 固定资产
    private String spmcode;     // PM设备编码
    private String sddno;       // 订单号
    private String szcno;       // 资产编码

    private String f1;
    private String f2;
    private String f3;
    private String f4;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dtime;         // 初次使用时间

    private Long istepid;       // 步骤ID
    private String sstepnm;     // 步骤名称
    private String sstepstate;  // 步骤状态 (审批状态)

    // [修复] 添加缺失的字段
    private Integer istepcharttype; // 步骤类型

    private String sstepoperid; // 办理人ID
    private String sstepopernm; // 办理人
    private String snote;       // 备注

    // [新增] 车速相关字段 (非数据库表 ZJZK_TOOL 原生字段，通过关联查询获得)
    private Double lastAvgSpeed;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastSpeedTime;

    public Double getLastAvgSpeed() { return lastAvgSpeed; }
    public void setLastAvgSpeed(Double lastAvgSpeed) { this.lastAvgSpeed = lastAvgSpeed; }

    public Date getLastSpeedTime() { return lastSpeedTime; }
    public void setLastSpeedTime(Date lastSpeedTime) { this.lastSpeedTime = lastSpeedTime; }

    // Getters and Setters
    public Long getIndocno() { return indocno; }
    public void setIndocno(Long indocno) { this.indocno = indocno; }
    public Long getSregid() { return sregid; }
    public void setSregid(Long sregid) { this.sregid = sregid; }
    public String getSregnm() { return sregnm; }
    public void setSregnm(String sregnm) { this.sregnm = sregnm; }
    public Date getDregt() { return dregt; }
    public void setDregt(Date dregt) { this.dregt = dregt; }
    public Long getSmodid() { return smodid; }
    public void setSmodid(Long smodid) { this.smodid = smodid; }
    public String getSmodnm() { return smodnm; }
    public void setSmodnm(String smodnm) { this.smodnm = smodnm; }
    public Date getDmodt() { return dmodt; }
    public void setDmodt(Date dmodt) { this.dmodt = dmodt; }
    public String getSname() { return sname; }
    public void setSname(String sname) { this.sname = sname; }
    public String getSzznolb() { return szznolb; }
    public void setSzznolb(String szznolb) { this.szznolb = szznolb; }
    public String getSdept() { return sdept; }
    public void setSdept(String sdept) { this.sdept = sdept; }
    public String getIdept() { return idept; }
    public void setIdept(String idept) { this.idept = idept; }
    public String getSjx() { return sjx; }
    public void setSjx(String sjx) { this.sjx = sjx; }
    public String getIjx() { return ijx; }
    public void setIjx(String ijx) { this.ijx = ijx; }
    public String getSfname() { return sfname; }
    public void setSfname(String sfname) { this.sfname = sfname; }
    public String getSfcode() { return sfcode; }
    public void setSfcode(String sfcode) { this.sfcode = sfcode; }
    public String getSbname() { return sbname; }
    public void setSbname(String sbname) { this.sbname = sbname; }
    public String getScj() { return scj; }
    public void setScj(String scj) { this.scj = scj; }
    public String getSxh() { return sxh; }
    public void setSxh(String sxh) { this.sxh = sxh; }
    public String getSazwz() { return sazwz; }
    public void setSazwz(String sazwz) { this.sazwz = sazwz; }
    public String getSsm() { return ssm; }
    public void setSsm(String ssm) { this.ssm = ssm; }
    public String getSyl() { return syl; }
    public void setSyl(String syl) { this.syl = syl; }
    public String getIszc() { return iszc; }
    public void setIszc(String iszc) { this.iszc = iszc; }
    public String getSpmcode() { return spmcode; }
    public void setSpmcode(String spmcode) { this.spmcode = spmcode; }
    public String getSddno() { return sddno; }
    public void setSddno(String sddno) { this.sddno = sddno; }
    public String getSzcno() { return szcno; }
    public void setSzcno(String szcno) { this.szcno = szcno; }

    public String getF1() { return f1; }
    public void setF1(String f1) { this.f1 = f1; }
    public String getF2() { return f2; }
    public void setF2(String f2) { this.f2 = f2; }
    public String getF3() { return f3; }
    public void setF3(String f3) { this.f3 = f3; }
    public String getF4() { return f4; }
    public void setF4(String f4) { this.f4 = f4; }

    public Date getDtime() { return dtime; }
    public void setDtime(Date dtime) { this.dtime = dtime; }

    public Long getIstepid() { return istepid; }
    public void setIstepid(Long istepid) { this.istepid = istepid; }
    public String getSstepnm() { return sstepnm; }
    public void setSstepnm(String sstepnm) { this.sstepnm = sstepnm; }
    public String getSstepstate() { return sstepstate; }
    public void setSstepstate(String sstepstate) { this.sstepstate = sstepstate; }

    // [修复] 对应的 Setter/Getter
    public Integer getIstepcharttype() { return istepcharttype; }
    public void setIstepcharttype(Integer istepcharttype) { this.istepcharttype = istepcharttype; }

    public String getSstepoperid() { return sstepoperid; }
    public void setSstepoperid(String sstepoperid) { this.sstepoperid = sstepoperid; }
    public String getSstepopernm() { return sstepopernm; }
    public void setSstepopernm(String sstepopernm) { this.sstepopernm = sstepopernm; }
    public String getSnote() { return snote; }
    public void setSnote(String snote) { this.snote = snote; }
}