package com.lucksoft.qingdao.tspm.service;

import com.lucksoft.qingdao.tspm.dto.LogMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * 负责记录和推送实时日志的服务
 */
@Service
public class TspmLogService {

    // 线程安全的List，用于存储内存日志
    private final List<LogMessage> logs = new CopyOnWriteArrayList<>();

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * 基础方法：添加一条新日志，并将其通过WebSocket推送到前端
     * @param type    日志类型 (e.g., "PUSH", "RECEIVE")
     * @param topic   相关的Kafka Topic
     * @param content 日志内容 (通常是JSON字符串)
     */
    public void addLog(String type, String topic, String content) {
        LogMessage logMessage = new LogMessage(type, topic, content);
        logs.add(0, logMessage); // 添加到列表开头，实现倒序

        // 限制内存中最多只保存200条日志，防止内存溢出
        if (logs.size() > 200) {
            logs.remove(logs.size() - 1);
        }

        // 通过WebSocket将新日志推送到所有订阅了/topic/logs的客户端
        messagingTemplate.convertAndSend("/topic/logs", logMessage);
    }

    /**
     * 记录一条成功的推送日志
     * @param topic   Kafka Topic
     * @param content 推送的JSON内容
     */
    public void logPush(String topic, String content) {
        addLog("PUSH", topic, content);
    }

    /**
     * 记录一条失败的推送日志
     * @param topic   Kafka Topic
     * @param errorContent 错误信息
     */
    public void logPushError(String topic, String errorContent) {
        addLog("PUSH_ERROR", topic, errorContent);
    }

    /**
     * 记录一条成功的接收日志
     * @param topic   Kafka Topic
     * @param content 接收到的JSON内容
     */
    public void logReceive(String topic, String content) {
        addLog("RECEIVE", topic, content);
    }

    /**
     * 记录一条失败的接收日志
     * @param topic   Kafka Topic
     * @param errorContent 错误信息
     */
    public void logReceiveError(String topic, String errorContent) {
        addLog("RECEIVE_ERROR", topic, errorContent);
    }


    /**
     * 获取当前所有内存中的日志记录
     * @return 日志列表
     */
    public List<LogMessage> getLogs() {
        return logs;
    }
}

