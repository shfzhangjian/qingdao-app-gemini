package com.lucksoft.qingdao.tspm.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 用于WebSocket实时推送的日志消息实体
 */
public class LogMessage implements Serializable {
    private static final long serialVersionUID = 1L;
    private String timestamp;
    private String type; // PUSH or RECEIVE
    private String topic;
    private String content;

    // 使用
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    public LogMessage(String type, String topic, String content) {
        this.timestamp = sdf.format(new Date());
        this.type = type;
        this.topic = topic;
        this.content = content;
    }

    // Getters and Setters
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTopic() { return topic; }
    public void setTopic(String topic) { this.topic = topic; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
