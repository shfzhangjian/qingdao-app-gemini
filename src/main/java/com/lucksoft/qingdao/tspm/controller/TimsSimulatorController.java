package com.lucksoft.qingdao.tspm.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucksoft.qingdao.tspm.producer.TspmProducerService;
import com.lucksoft.qingdao.tspm.service.TspmLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * [新功能]
 * 模拟 TIMS 系统向 TsPM 推送消息的控制器。
 * 这允许开发人员在没有真实 TIMS 环境的情况下测试 TsPM 的 Kafka 消费者逻辑。
 */
@RestController
@RequestMapping("/api/tims")
public class TimsSimulatorController {

    private static final Logger log = LoggerFactory.getLogger(TimsSimulatorController.class);

    @Autowired
    private TspmProducerService producerService;

    @Autowired
    private TspmLogService logService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 接收来自模拟器前端的请求，并将消息推送到指定的 Kafka 主题。
     * @param payload 包含 "topic" 和 "payload" (JSON 字符串) 的请求体
     * @return 推送结果
     */
    @PostMapping("/push")
    public ResponseEntity<Map<String, String>> simulateTimsPush(@RequestBody Map<String, String> payload) {
        String topic = payload.get("topic");
        String jsonPayloadString = payload.get("payload");

        if (topic == null || topic.isEmpty() || jsonPayloadString == null || jsonPayloadString.isEmpty()) {
            return ResponseEntity.badRequest().body(createResponse("error", "主题(topic)和内容(payload)不能为空"));
        }

        try {
            // 1. 验证 JSON 格式是否正确
            // 我们期望 TIMS 发送的是一个 JSON 数组，所以我们尝试将其解析为 JsonNode
            JsonNode jsonNode = objectMapper.readTree(jsonPayloadString);

            // 2. [关键] 调用通用的生产者服务，将消息发送到 *目标主题*
            // TspmProducerService 内部使用 KafkaTemplate，可以向任何主题发送消息。
            // 我们在这里模拟 TIMS，所以我们发送到 TsPM 正在 *监听* 的主题。
            // 我们使用 sendSync 来确保消息被发送，如果 Kafka 代理有问题会立即抛出异常。
            producerService.sendSync(topic, jsonNode);

            String logMessage = String.format("已模拟 TIMS 向 Kafka 主题 '%s' 推送消息", topic);
            log.info(logMessage);

            // 3. (可选) 将此 *模拟动作* 也记录到 WebSocket 日志中
            logService.addLog("SIMULATE_PUSH", topic, jsonPayloadString);

            return ResponseEntity.ok(createResponse("success", logMessage));

        } catch (Exception e) {
            log.error("模拟 TIMS 推送时失败: {}", e.getMessage(), e);
            logService.addLog("SIMULATE_ERROR", topic, "模拟推送失败: " + e.getMessage());
            return ResponseEntity.status(500).body(createResponse("error", "推送失败: " + e.getMessage()));
        }
    }

    private Map<String, String> createResponse(String status, String message) {
        Map<String, String> response = new HashMap<>();
        response.put(status, message);
        return response;
    }
}