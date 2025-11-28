package com.lucksoft.qingdao.oracle.tmis.dto;

import java.io.Serializable;
import java.util.Map;

/**
 * 通用数据查询请求对象
 */
public class TmisDataQueryDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Kafka 主题名称 (用于区分业务接口)
     * 例如: tims.sync.maintenance.task
     */
    private String topic;

    /**
     * 最后更新时间 (格式: yyyy-MM-dd HH:mm:ss)
     * 用于增量查询，通常查询大于此时间的数据
     */
    private String updateTime;

    /**
     * 额外的业务参数 (JSON对象)
     * 例如: {"equipmentCode": "EQ123", "status": "1"}
     */
    private Map<String, Object> body;

    /**
     * 当前页码 (默认1)
     */
    private int pageNum = 1;

    /**
     * 每页数量 (默认20)
     */
    private int pageSize = 20;

    // Getters and Setters
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public Map<String, Object> getBody() { return body; }
    public void setBody(Map<String, Object> body) { this.body = body; }
    public int getPageNum() { return pageNum; }
    public void setPageNum(int pageNum) { this.pageNum = pageNum; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
}