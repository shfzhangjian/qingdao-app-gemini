package com.lucksoft.qingdao.tspm.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucksoft.qingdao.tspm.service.TspmLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * 统一的TsPM Kafka生产者服务 (已根据Excel重构)
 * 负责向所有`tims.*`开头的topic发送消息
 */
@Service
public class TspmProducerService {

    private static final Logger logger = LoggerFactory.getLogger(TspmProducerService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private TspmLogService logService;

    /**
     * 通用的消息发送方法
     * @param topic   目标Topic
     * @param payload 要发送的数据对象
     */
    public void sendMessage(String topic, Object payload) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            kafkaTemplate.send(topic, jsonPayload).addCallback(
                    success -> {
                        String logContent = String.format("成功发送到 Topic: %s, Partition: %d, Offset: %d",
                                success.getRecordMetadata().topic(),
                                success.getRecordMetadata().partition(),
                                success.getRecordMetadata().offset());
                        logger.info(logContent);
                        logService.addLog("PUSH", topic, jsonPayload); // 记录成功日志
                    },
                    failure -> {
                        String errorLog = String.format("发送到 Topic %s 失败: %s", topic, failure.getMessage());
                        logger.error(errorLog, failure);
                        logService.addLog("PUSH_ERROR", topic, errorLog); // 记录失败日志
                    }
            );
        } catch (Exception e) {
            String errorLog = String.format("序列化并发送到 Topic %s 时出错: %s", topic, e.getMessage());
            logger.error(errorLog, e);
            logService.addLog("PUSH_ERROR", topic, errorLog); // 记录序列化失败日志
        }
    }
}

