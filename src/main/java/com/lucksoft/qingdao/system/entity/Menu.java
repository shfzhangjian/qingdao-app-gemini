package com.lucksoft.qingdao.system.entity;

import java.util.Date;
import java.util.List;

/**
 * 菜单信息实体类 (对应 MENUS 表)
 *
 * @author Gemini
 */
public class Menu {

    /**
     * 菜单ID
     */
    private Long id;

    /**
     * 上级菜单ID
     */
    private Long parentId;

    /**
     * 顺序编码
     * 【已修改】将类型从 Integer 修改为 Long 来防止数字溢出
     */
    private Long seq;

    /**
     * 菜单名
     */
    private String title;

    /**
     * 提示信息
     */
    private String tip;

    /**
     * 菜单描述
     */
    private String descn;

    /**
     * 图片
     */
    private String image;

    /**
     * 超链地址
     */
    private String forward;

    /**
     * 0启用 1禁用
     */
    private Integer istate;

    /**
     * 创建人ID
     */
    private String sregid;

    /**
     * 创建人
     */
    private String sregnm;

    /**
     * 创建日期
     */
    private Date dregt;

    /**
     * 子菜单列表 (用于构建树形结构)
     */
    private List<Menu> children;


    // --- Getter and Setter ---

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Long getSeq() {
        return seq;
    }

    public void setSeq(Long seq) {
        this.seq = seq;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTip() {
        return tip;
    }

    public void setTip(String tip) {
        this.tip = tip;
    }

    public String getDescn() {
        return descn;
    }

    public void setDescn(String descn) {
        this.descn = descn;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getForward() {
        return forward;
    }

    public void setForward(String forward) {
        this.forward = forward;
    }

    public Integer getIstate() {
        return istate;
    }

    public void setIstate(Integer istate) {
        this.istate = istate;
    }

    public String getSregid() {
        return sregid;
    }

    public void setSregid(String sregid) {
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

    public List<Menu> getChildren() {
        return children;
    }

    public void setChildren(List<Menu> children) {
        this.children = children;
    }
}
