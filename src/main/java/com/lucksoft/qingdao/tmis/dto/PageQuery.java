package com.lucksoft.qingdao.tmis.dto;

import java.io.Serializable;

/**
 * 通用分页查询参数基类 DTO.
 * 所有需要分页的查询请求DTO都可以继承此类。
 */
public class PageQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    private int pageNum = 1;
    private int pageSize = 10;

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
}
