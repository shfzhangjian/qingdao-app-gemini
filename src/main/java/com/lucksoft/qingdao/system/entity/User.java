package com.lucksoft.qingdao.system.entity;

import java.util.Date;

/**
 * 用户信息实体类 (对应 USERS 表)
 *
 * @author Gemini
 */
public class User {

    /**
     * 用户内部编号
     */
    private Long id;

    /**
     * 登陆账号*
     */
    private String loginid;

    /**
     * 密码
     */
    private String passwd;

    /**
     * 用户名*
     */
    private String name;

    /**
     * 电子邮箱
     */
    private String email;

    /**
     * 状态(1 有效,0 无效)
     */
    private String status;

    /**
     * 简介
     */
    private String descn;

    /**
     * 管理专业(SCOPECODE)*
     */
    private String vscopecode;

    /**
     * 所属部门(TDEPART)*
     */
    private String vplantno;

    /**
     * 所属系统类型(V$DEPARTTYPE)*
     */
    private Integer itype;

    /**
     * 拼音码
     */
    private String vpyidex;

    /**
     * 用户类型(1 普通用户 2 系统级)
     */
    private String vutype;

    /**
     * 缺省登陆小组
     */
    private Integer nqsxz;

    /**
     * 所属岗位(这里存储ID,部门表ID）
     */
    private String ngw;

    /**
     * 工号
     */
    private String gh;

    /**
     * 岗位名称
     */
    private String ngwname;

    /**
     * 职称
     */
    private String zc;

    /**
     * 职责
     */
    private String zz;

    /**
     * 文化程度
     */
    private String whcd;

    /**
     * 出生年月
     */
    private Date ccny;

    /**
     * 手机电话
     */
    private String telephone;

    /**
     * 创建人ID
     */
    private Integer sregid;

    /**
     * 创建人
     */
    private String sregnm;

    /**
     * 创建日期
     */
    private Date dregt;

    /**
     * 修改人ID
     */
    private Integer smodid;

    /**
     * 修改人
     */
    private String smodnm;

    /**
     * 修改日期
     */
    private Date dmodt;

    /**
     * 虚拟删除
     */
    private Integer idel;

    // ... 其他字段的 Getter 和 Setter ...


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLoginid() {
        return loginid;
    }

    public void setLoginid(String loginid) {
        this.loginid = loginid;
    }

    public String getPasswd() {
        return passwd;
    }

    public void setPasswd(String passwd) {
        this.passwd = passwd;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescn() {
        return descn;
    }

    public void setDescn(String descn) {
        this.descn = descn;
    }

    public String getVscopecode() {
        return vscopecode;
    }

    public void setVscopecode(String vscopecode) {
        this.vscopecode = vscopecode;
    }

    public String getVplantno() {
        return vplantno;
    }

    public void setVplantno(String vplantno) {
        this.vplantno = vplantno;
    }

    public Integer getItype() {
        return itype;
    }

    public void setItype(Integer itype) {
        this.itype = itype;
    }

    public String getVpyidex() {
        return vpyidex;
    }

    public void setVpyidex(String vpyidex) {
        this.vpyidex = vpyidex;
    }

    public String getVutype() {
        return vutype;
    }

    public void setVutype(String vutype) {
        this.vutype = vutype;
    }

    public Integer getNqsxz() {
        return nqsxz;
    }

    public void setNqsxz(Integer nqsxz) {
        this.nqsxz = nqsxz;
    }

    public String getNgw() {
        return ngw;
    }

    public void setNgw(String ngw) {
        this.ngw = ngw;
    }

    public String getGh() {
        return gh;
    }

    public void setGh(String gh) {
        this.gh = gh;
    }

    public String getNgwname() {
        return ngwname;
    }

    public void setNgwname(String ngwname) {
        this.ngwname = ngwname;
    }

    public String getZc() {
        return zc;
    }

    public void setZc(String zc) {
        this.zc = zc;
    }

    public String getZz() {
        return zz;
    }

    public void setZz(String zz) {
        this.zz = zz;
    }

    public String getWhcd() {
        return whcd;
    }

    public void setWhcd(String whcd) {
        this.whcd = whcd;
    }

    public Date getCcny() {
        return ccny;
    }

    public void setCcny(Date ccny) {
        this.ccny = ccny;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public Integer getSregid() {
        return sregid;
    }

    public void setSregid(Integer sregid) {
        this.sregid = sregid;
    }

    public String getSregnm() {
        return sregnm;
    }

    public void setSregnm(String sregnm) {
        this.sregnm = sregnm;
    }

    public Date getDregt() {
        return dregt;
    }

    public void setDregt(Date dregt) {
        this.dregt = dregt;
    }

    public Integer getSmodid() {
        return smodid;
    }

    public void setSmodid(Integer smodid) {
        this.smodid = smodid;
    }

    public String getSmodnm() {
        return smodnm;
    }

    public void setSmodnm(String smodnm) {
        this.smodnm = smodnm;
    }

    public Date getDmodt() {
        return dmodt;
    }

    public void setDmodt(Date dmodt) {
        this.dmodt = dmodt;
    }

    public Integer getIdel() {
        return idel;
    }

    public void setIdel(Integer idel) {
        this.idel = idel;
    }
}
