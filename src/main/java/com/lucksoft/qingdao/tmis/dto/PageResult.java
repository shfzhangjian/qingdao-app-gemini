package com.lucksoft.qingdao.tmis.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 通用分页查询结果封装
 *
 * @param <T> 分页数据类型
 */
public class PageResult<T> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 当前页的数据列表
     */
    private List<T> list;

    /**
     * 当前页码
     */
    private int pageNum;

    /**
     * 每页数量
     */
    private int pageSize;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private int pages;

    /**
     * 无参构造函数 (必须保留，以兼容某些序列化框架)
     */
    public PageResult() {
    }

    /**
     * 【新增】全参构造函数，用于方便地创建分页结果对象
     */
    public PageResult(List<T> list, int pageNum, int pageSize, long total, int pages) {
        this.list = list;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.pages = pages;
    }

    // Getters and Setters...
    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}

