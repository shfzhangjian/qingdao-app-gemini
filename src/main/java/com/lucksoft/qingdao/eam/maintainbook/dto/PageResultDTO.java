package com.lucksoft.qingdao.eam.maintainbook.dto;

import java.io.Serializable;
import java.util.List;

/**
 * 通用分页查询结果封装
 *
 * @param <T> 分页数据类型
 */
public class PageResultDTO<T> implements Serializable {
    private static final long serialVersionUID = 1L;

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
     * 分页数据
     */
    private List<T> list;

    // 省略构造函数、Getter和Setter...
    // 您可以在生成的代码中按需添加，或者使用Lombok @Data 注解

    public PageResultDTO() {
    }

    public PageResultDTO(List<T> list) {
        // 这是一个简化的构造函数，实际项目中通常使用PageInfo (from PageHelper)来填充所有字段
        this.list = list;
        // 示例: this.total = new PageInfo<>(list).getTotal();
    }

    // Getters and Setters...
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

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
