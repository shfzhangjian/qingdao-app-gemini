package com.lucksoft.qingdao.tspm.service;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucksoft.common.utils.GjjDebugLogger; // [新增] 引入日志工具
import com.lucksoft.qingdao.oracle.service.AsyncTaskService;
import com.lucksoft.qingdao.system.entity.TmisData;
import com.lucksoft.qingdao.system.mapper.TmisDataMapper;
import com.lucksoft.qingdao.tspm.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TMIS 数据补漏服务 (升级版 V2)
 * 集成详细的文件日志记录功能。
 */
@Service
public class TmisCompensationService {

    private static final Logger log = LoggerFactory.getLogger(TmisCompensationService.class);

    @Autowired
    private AsyncTaskService asyncTaskService;

    @Autowired
    private TmisDataMapper tmisDataMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 异步执行单个主题的补偿任务
     */
    @Async
    public void compensateTopic(TmisData config) {
        String topic = config.getTopic();
        String description = config.getDescription() != null ? config.getDescription() : "未知接口";
        // [日志] 构造日志文件名: 描述_Topic.log (例如: 轮保完成_tims.feedback.completed.rotational.task)
        String logFileName = description + "_" + topic;

        // 设置 Logger 上下文 (GjjDebugLogger 使用 ThreadLocal)
        GjjDebugLogger.setLogNameContext(logFileName);

        String baseUrl = config.getApiUrl();
        String lastTime = DateUtil.formatDateTime(config.getLastUpdateTime());

        log.info("[补漏任务] 处理主题: {}, 水位线: {}", topic, lastTime);
        GjjDebugLogger.log(topic, "任务开始", String.format("URL: %s\nLastUpdateTime: %s", baseUrl, lastTime));

        if (baseUrl == null || baseUrl.isEmpty()) {
            GjjDebugLogger.log(topic, "错误", "API URL 为空，任务中止。");
            return;
        }

        try {
            // 解析配置参数
            Map<String, Object> fixedParams = new HashMap<>();
            if (config.getFixedParams() != null) {
                fixedParams = objectMapper.readValue(config.getFixedParams(), new TypeReference<Map<String, Object>>() {});
            }

            // 判断请求方式
            boolean isGetRequest = "GET".equalsIgnoreCase((String) fixedParams.get("method"));

            JsonNode responseData;
            String fullUrl = baseUrl;
            Object requestBody = null;

            long startTime = System.currentTimeMillis();

            if (isGetRequest) {
                // --- GET 请求逻辑 ---
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .queryParam("lastSyncDateTime", lastTime);

                if (fixedParams.containsKey("type")) {
                    builder.queryParam("type", fixedParams.get("type"));
                }

                fullUrl = builder.toUriString();
                log.debug("[补漏任务] GET 请求: {}", fullUrl);
                GjjDebugLogger.log(topic, "发起 GET 请求", "URL: " + fullUrl);

                ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);

                GjjDebugLogger.log(topic, "收到响应",
                        String.format("Status: %s\nTime: %d ms\nBody: %s",
                                response.getStatusCode(), (System.currentTimeMillis() - startTime), response.getBody()));

                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.error("[补漏任务] 接口调用失败: {}", response.getStatusCode());
                    return;
                }
                responseData = objectMapper.readTree(response.getBody());

            } else {
                // --- POST 请求逻辑 ---
                Map<String, Object> payload = new HashMap<>();
                payload.put("topic", topic);
                payload.put("updateTime", lastTime);
                payload.put("pageNum", 1);
                payload.put("pageSize", 1000);

                if (config.getFixedParams() != null && !config.getFixedParams().isEmpty()) {
                    try {
                        Map<String, Object> body = objectMapper.readValue(config.getFixedParams(), new TypeReference<Map<String, Object>>() {});
                        payload.put("body", body);
                    } catch (Exception e) {
                        payload.put("body", new HashMap<>());
                    }
                }
                requestBody = payload;

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

                GjjDebugLogger.log(topic, "发起 POST 请求",
                        String.format("URL: %s\nPayload: %s", baseUrl, objectMapper.writeValueAsString(payload)));

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);

                GjjDebugLogger.log(topic, "收到响应",
                        String.format("Status: %s\nTime: %d ms\nBody: %s",
                                response.getStatusCode(), (System.currentTimeMillis() - startTime), response.getBody()));

                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.error("[补漏任务] 接口调用失败: {}", response.getStatusCode());
                    return;
                }
                JsonNode root = objectMapper.readTree(response.getBody());
                responseData = root.get("list");
            }

            // 统一数据处理
            if (responseData == null || !responseData.isArray() || responseData.size() == 0) {
                log.info("[补漏任务] 主题 {} 无新数据。", topic);
                GjjDebugLogger.log(topic, "处理结果", "无新数据，更新水位线至当前时间。");
                updateWatermark(topic);
                return;
            }

            int count = responseData.size();
            log.info("[补漏任务] 主题 {} 获取到 {} 条数据，开始回填...", topic, count);
            GjjDebugLogger.log(topic, "数据处理", "获取到 " + count + " 条数据，准备写入 Oracle...");

            processData(topic, responseData);
            updateWatermark(topic);

            GjjDebugLogger.log(topic, "完成", "任务执行成功，水位线已更新。");

        } catch (Exception e) {
            log.error("[补漏任务] 主题 {} 处理异常: {}", topic, e.getMessage(), e);
            GjjDebugLogger.logError(topic, "发生异常", e);
        } finally {
            // 清理 ThreadLocal
            GjjDebugLogger.clearLogNameContext();
        }
    }

    private void updateWatermark(String topic) {
        String now = sdf.format(new Date());
        tmisDataMapper.updateLastTime(topic, now);
    }

    private void processData(String topic, JsonNode listNode) throws Exception {
        // 逻辑保持不变，直接复用
        switch (topic) {
            case "tims.feedback.completed.maintenance.task":
                List<TaskCompletionFeedbackDTO> list2 = objectMapper.convertValue(listNode, new TypeReference<List<TaskCompletionFeedbackDTO>>() {});
                list2.forEach(asyncTaskService::submitTaskCompletion);
                break;
            case "tims.feedback.maintenance.task.score":
                List<TaskScoreFeedbackDTO> list3 = objectMapper.convertValue(listNode, new TypeReference<List<TaskScoreFeedbackDTO>>() {});
                list3.forEach(asyncTaskService::submitTaskScore);
                break;
            case "tims.recommend.rotational.task":
                List<RecommendedRotationalTaskDTO> list6 = objectMapper.convertValue(listNode, new TypeReference<List<RecommendedRotationalTaskDTO>>() {});
                list6.forEach(asyncTaskService::submitRecommendTask);
                break;
            case "tims.feedback.completed.rotational.task":
                List<RotationalTaskCompletionFeedbackDTO> list8 = objectMapper.convertValue(listNode, new TypeReference<List<RotationalTaskCompletionFeedbackDTO>>() {});
                list8.forEach(asyncTaskService::submitRotationalCompletion);
                break;
            case "tims.feedback.rotational.task.score":
                List<RotationalTaskScoreFeedbackDTO> list9 = objectMapper.convertValue(listNode, new TypeReference<List<RotationalTaskScoreFeedbackDTO>>() {});
                list9.forEach(asyncTaskService::submitRotationalScore);
                break;
            case "tims.feedback.completed.production.halt.maintenance.task":
                List<ProductionHaltCompletionFeedbackDTO> list14 = objectMapper.convertValue(listNode, new TypeReference<List<ProductionHaltCompletionFeedbackDTO>>() {});
                list14.forEach(asyncTaskService::submitHaltCompletion);
                break;
            default:
                log.warn("[补漏任务] 未处理的 Topic: {}", topic);
                GjjDebugLogger.log(topic, "警告", "未找到对应 Topic 的 DTO 映射逻辑，跳过处理。");
        }
    }
}