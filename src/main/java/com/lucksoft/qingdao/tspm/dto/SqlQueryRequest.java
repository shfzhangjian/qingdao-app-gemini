package com.lucksoft.qingdao.tspm.dto;

import java.util.List;

/**
 * 封装从前端发送的SQL查询请求的数据传输对象.
 */
public class SqlQueryRequest {

    /**
     * 要执行的SQL查询语句 (可能包含 ? 占位符).
     */
    private String sql;

    /**
     * 希望从查询结果中获取的最大行数.
     */
    private int limit;

    /**
     * 与SQL中 ? 占位符按顺序对应的参数列表.
     */
    private List<Object> params;

    // --- Getters and Setters ---

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<Object> getParams() {
        return params;
    }

    public void setParams(List<Object> params) {
        this.params = params;
    }
}

