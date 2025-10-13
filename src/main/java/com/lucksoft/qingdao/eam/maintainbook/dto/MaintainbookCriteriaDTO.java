package com.lucksoft.qingdao.eam.maintainbook.dto;

import java.io.Serializable;

/**
 * Criteria DTO for {@link com.lucksoft.qdtspm.maintain.db.maintainbook.entity.Maintainbook}.
 */
public class MaintainbookCriteriaDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private String icheckdutys;
    /**
     *
     */
    private Long iclassid;
    /**
     *
     */
    private Long idutys;
    /**
     *
     */
    private Long imachinetype;
    /**
     * 状态
     */
    private Long istate;
    /**
     * 检查岗位
     */
    private String scheckdutys;
    /**
     * 标准分类
     */
    private String sclassnm;
    /**
     * 责任岗位
     */
    private String sdutys;
    /**
     * 保养项目
     */
    private String sitemnm;
    /**
     * 机型/工段
     */
    private String smachintetype;
    /**
     * 检查标题
     */
    private String stitle;
    /**
     * 保养项目
     */
    private String stitle1;

    //<editor-fold defaultstate="collapsed" desc="Getters and Setters">
    public String getIcheckdutys() {
        return icheckdutys;
    }

    public void setIcheckdutys(String icheckdutys) {
        this.icheckdutys = icheckdutys;
    }

    public Long getIclassid() {
        return iclassid;
    }

    public void setIclassid(Long iclassid) {
        this.iclassid = iclassid;
    }

    public Long getIdutys() {
        return idutys;
    }

    public void setIdutys(Long idutys) {
        this.idutys = idutys;
    }

    public Long getImachinetype() {
        return imachinetype;
    }

    public void setImachinetype(Long imachinetype) {
        this.imachinetype = imachinetype;
    }

    public Long getIstate() {
        return istate;
    }

    public void setIstate(Long istate) {
        this.istate = istate;
    }

    public String getScheckdutys() {
        return scheckdutys;
    }

    public void setScheckdutys(String scheckdutys) {
        this.scheckdutys = scheckdutys;
    }

    public String getSclassnm() {
        return sclassnm;
    }

    public void setSclassnm(String sclassnm) {
        this.sclassnm = sclassnm;
    }

    public String getSdutys() {
        return sdutys;
    }

    public void setSdutys(String sdutys) {
        this.sdutys = sdutys;
    }

    public String getSitemnm() {
        return sitemnm;
    }

    public void setSitemnm(String sitemnm) {
        this.sitemnm = sitemnm;
    }

    public String getSmachintetype() {
        return smachintetype;
    }

    public void setSmachintetype(String smachintetype) {
        this.smachintetype = smachintetype;
    }

    public String getStitle() {
        return stitle;
    }

    public void setStitle(String stitle) {
        this.stitle = stitle;
    }

    public String getStitle1() {
        return stitle1;
    }

    public void setStitle1(String stitle1) {
        this.stitle1 = stitle1;
    }
    //</editor-fold>
}
