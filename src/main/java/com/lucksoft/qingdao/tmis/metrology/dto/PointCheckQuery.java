package com.lucksoft.qingdao.tmis.metrology.dto;

import com.lucksoft.qingdao.tmis.dto.PageQuery;

/**
 * 点检统计查询参数 DTO
 */
public class PointCheckQuery extends PageQuery {
    private String category;

    // Getters and Setters
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    @Override
    public String toString() {
        return "PointCheckQuery{" +
                "category='" + category + '\'' +
                ", pageNum=" + getPageNum() +
                ", pageSize=" + getPageSize() +
                '}';
    }
}
