package com.lucksoft.qingdao.system.entity;

import java.util.Date;

/**
 * 附件信息实体类 (对应 ATTACHMENT 表)
 *
 * @author Gemini
 */
public class Attachment {

    /**
     * 主键
     */
    private Long attId;

    /**
     * 附件文件名
     */
    private String stitle;

    /**
     * 附件存放地址(文件在服务器上的名称)
     */
    private String spath;

    /**
     * 文档号（对应文档主键）
     */
    private Long docno;

    /**
     * 文档类型 *（对应文档类型名称）
     */
    private String doctype;

    /**
     * 状态 (0:正常状态 1:已删除状态不可查询)
     */
    private Integer attState;

    /**
     * 附件格式（存储 RAR 等数据-要求全部为小写)
     */
    private String attType;

    /**
     * 文档类型号(参考DOCTYPE)
     */
    private Long doctypeid;

    /**
     * 文件大小
     */
    private Long attSize;

    /**
     * 版本号
     */
    private String sversion;

    /**
     * 备注
     */
    private String snotes;

    /**
     * 上传时间
     */
    private Date created;

    /**
     * 上传人ID号
     */
    private Integer sregid;

    /**
     * 上传人名
     */
    private String sregnm;

    // --- Getter and Setter ---

    public Long getAttId() {
        return attId;
    }

    public void setAttId(Long attId) {
        this.attId = attId;
    }

    public String getStitle() {
        return stitle;
    }

    public void setStitle(String stitle) {
        this.stitle = stitle;
    }

    public String getSpath() {
        return spath;
    }

    public void setSpath(String spath) {
        this.spath = spath;
    }

    public Long getDocno() {
        return docno;
    }

    public void setDocno(Long docno) {
        this.docno = docno;
    }

    public String getDoctype() {
        return doctype;
    }

    public void setDoctype(String doctype) {
        this.doctype = doctype;
    }

    public Integer getAttState() {
        return attState;
    }

    public void setAttState(Integer attState) {
        this.attState = attState;
    }

    public String getAttType() {
        return attType;
    }

    public void setAttType(String attType) {
        this.attType = attType;
    }

    public Long getDoctypeid() {
        return doctypeid;
    }

    public void setDoctypeid(Long doctypeid) {
        this.doctypeid = doctypeid;
    }

    public Long getAttSize() {
        return attSize;
    }

    public void setAttSize(Long attSize) {
        this.attSize = attSize;
    }

    public String getSversion() {
        return sversion;
    }

    public void setSversion(String sversion) {
        this.sversion = sversion;
    }

    public String getSnotes() {
        return snotes;
    }

    public void setSnotes(String snotes) {
        this.snotes = snotes;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
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
}
