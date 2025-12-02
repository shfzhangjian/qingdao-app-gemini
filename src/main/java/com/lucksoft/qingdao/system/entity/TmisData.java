package com.lucksoft.qingdao.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
     */
    @TableId("TOPIC")
    private String topic;

    /**
     * 接口描述
     */
    @TableField("DESCRIPTION")
    private String description;

    /**
     * 接口API地址
     */
    @TableField("API_URL")
    private String apiUrl;

    /**
     * 固定参数 (JSON格式字符串)
     */
    @TableField("FIXED_PARAMS")
    private String fixedParams;

    /**
     * [新增] 定时任务 Cron 表达式
     * 例如: 0 0 14,20,22 * * ?
     */
    @TableField("CRON_EXPRESSION")
    private String cronExpression;

    /**
     * 最后更新时间
     */
    @TableField("LAST_UPDATE_TIME")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date lastUpdateTime;

    /**
     * 是否启用 (1:启用, 0:禁用)
     */
    @TableField("ENABLED")
    private Integer enabled;

    // --- Getters and Setters ---

    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getApiUrl() { return apiUrl; }
    public void setApiUrl(String apiUrl) { this.apiUrl = apiUrl; }

    public String getFixedParams() { return fixedParams; }
    public void setFixedParams(String fixedParams) { this.fixedParams = fixedParams; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public Date getLastUpdateTime() { return lastUpdateTime; }
    public void setLastUpdateTime(Date lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }

    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
}