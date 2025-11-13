package com.lucksoft.qingdao.tspm.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucksoft.qingdao.tspm.service.TspmLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * [已重构] 统一的TsPM Kafka生产者服务
 * 1. 移除了异步回调逻辑 (addCallback)。
 * 2. 新增了 sendSync 方法，用于同步阻塞发送，确保在写入Redis前Kafka已确认。
 */
@Service
public class TspmProducerService {

    private static final Logger logger = LoggerFactory.getLogger(TspmProducerService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * [新] 定义一个合理的同步发送超时时间 (例如 15 秒)
     * 如果 Kafka Broker 在 15 秒内未确认，则抛出 TimeoutException
     */
    private static final long SYNC_SEND_TIMEOUT = 15; // 单位: 秒

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private TspmLogService logService;

    /**
     * [已废弃的异步发送]
     * 原始的异步发送方法，存在数据丢失风险 (如果Kafka超时)。
     * @param topic   目标Topic
     * @param payload 要发送的数据对象
     */
    @Deprecated
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

    /**
     * [新] 同步发送消息
     * 此方法会阻塞当前线程，直到 Kafka 确认收到消息，或者发生超时。
     *
     * @param topic   目标Topic
     * @param payload 要发送的数据对象
     * @throws ExecutionException   如果 Kafka 发送时发生内部异常
     * @throws InterruptedException 如果当前线程被中断
     * @throws TimeoutException     如果 Kafka 在指定时间内未确认 (例如: 15秒)
     * @throws com.fasterxml.jackson.core.JsonProcessingException 如果JSON序列化失败
     */
    public void sendSync(String topic, Object payload)
            throws com.fasterxml.jackson.core.JsonProcessingException, ExecutionException, InterruptedException, TimeoutException {

        String jsonPayload = objectMapper.writeValueAsString(payload);

        logger.info("准备同步发送到 Topic: {}", topic);
        logService.addLog("PUSH_SYNC", topic, jsonPayload); // 记录同步推送日志

        try {
            // 1. 发送消息并获取 Future, 等待最多 15 秒
            SendResult<String, String> sendResult = kafkaTemplate.send(topic, jsonPayload)
                    .get(SYNC_SEND_TIMEOUT, TimeUnit.SECONDS);

            // 2. 如果 get() 成功返回 (未抛出异常), 说明已收到 Broker 确认
            logger.info("同步发送成功. Topic: {}, Partition: {}, Offset: {}",
                    sendResult.getRecordMetadata().topic(),
                    sendResult.getRecordMetadata().partition(),
                    sendResult.getRecordMetadata().offset());


        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            // 3. 捕获所有发送失败/超时的异常 (例如您日志中的 TimeoutException)
            String errorLog = String.format("同步发送到 Topic %s 失败 (超时或执行错误): %s", topic, e.getMessage());
            logger.error(errorLog, e);
            logService.addLog("PUSH_ERROR", topic, errorLog); // 记录失败日志
            throw e; // [关键] 必须向上抛出, 以便 OracleDataService 捕获并停止写入 Redis
        }
    }
}