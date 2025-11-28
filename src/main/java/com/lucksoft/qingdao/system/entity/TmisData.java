package com.lucksoft.qingdao.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat; // 1. 引入 Jackson 注解包
import java.io.Serializable;
import java.util.Date;

/**
 * TMIS接口配置实体类
 * 对应数据库表: TMIS_DATA
 */
@TableName("TMIS_DATA")
public class TmisData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 接口标识/主题 (主键)
     * 数据库列: TOPIC
     */
    @TableId("TOPIC")
    private String topic;

    /**
     * 接口描述
     * 数据库列: DESCRIPTION
     */
    @TableField("DESCRIPTION")
    private String description;

    /**
     * 接口API地址
     * 数据库列: API_URL
     */
    @TableField("API_URL")
    private String apiUrl;

    /**
     * 固定参数 (JSON格式字符串)
     * 数据库列: FIXED_PARAMS
     */
    @TableField("FIXED_PARAMS")
    private String fixedParams;

    /**
     * 最后更新时间
     * 数据库列: LAST_UPDATE_TIME
     * 2. 添加 @JsonFormat 注解，指定格式为 yyyy-MM-dd HH:mm，并固定时区
     */
    @TableField("LAST_UPDATE_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdateTime;

    /**
     * 是否启用 (1:启用, 0:禁用)
     * 数据库列: ENABLED
     */
    @TableField("ENABLED")
    private Integer enabled;

    // --- Getters and Setters ---

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getFixedParams() {
        return fixedParams;
    }

    public void setFixedParams(String fixedParams) {
        this.fixedParams = fixedParams;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}