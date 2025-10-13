package com.lucksoft.qingdao.system.entity;

/**
 * 数据字典明细实体类 (对应 FIELDVALUE 表)
 *
 * @author Gemini
 */
public class FieldValue {

    /**
     * 唯一id
     */
    private Integer id;

    /**
     * FIELDS(id)
     */
    private Long fsid;

    /**
     * 明细序号
     */
    private String sitemno;

    /**
     * 字段值
     */
    private String svalue;

    /**
     * 说明
     */
    private String smemo;

    /**
     * 是否激活（0为不激活，1为激活）
     */
    private Integer ienabled;

    // --- Getter and Setter ---

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getFsid() {
        return fsid;
    }

    public void setFsid(Long fsid) {
        this.fsid = fsid;
    }

    public String getSitemno() {
        return sitemno;
    }

    public void setSitemno(String sitemno) {
        this.sitemno = sitemno;
    }

    public String getSvalue() {
        return svalue;
    }

    public void setSvalue(String svalue) {
        this.svalue = svalue;
    }

    public String getSmemo() {
        return smemo;
    }

    public void setSmemo(String smemo) {
        this.smemo = smemo;
    }

    public Integer getIenabled() {
        return ienabled;
    }

    public void setIenabled(Integer ienabled) {
        this.ienabled = ienabled;
    }
}
