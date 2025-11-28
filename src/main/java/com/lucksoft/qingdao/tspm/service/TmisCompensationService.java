package com.lucksoft.qingdao.tspm.service;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
 * TMIS 数据补漏服务 (升级版)
 * 支持文档 D1 定义的 GET 接口协议
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
        String baseUrl = config.getApiUrl();
        String lastTime = DateUtil.formatDateTime(config.getLastUpdateTime());

        if (baseUrl == null || baseUrl.isEmpty()) return;

        log.info("[补漏任务] 处理主题: {}, 水位线: {}", topic, lastTime);

        try {
            // 解析配置参数
            Map<String, Object> fixedParams = new HashMap<>();
            if (config.getFixedParams() != null) {
                fixedParams = objectMapper.readValue(config.getFixedParams(), new TypeReference<Map<String, Object>>() {});
            }

            // 判断请求方式 (默认为 POST，如果配置中有 "method":"GET" 则用 GET)
            boolean isGetRequest = "GET".equalsIgnoreCase((String) fixedParams.get("method"));

            JsonNode responseData;

            if (isGetRequest) {
                // --- GET 请求逻辑 (适配文档接口) ---
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(baseUrl)
                        .queryParam("lastSyncDateTime", lastTime);

                // 追加其他参数 (如 type)
                if (fixedParams.containsKey("type")) {
                    builder.queryParam("type", fixedParams.get("type"));
                }

                String fullUrl = builder.toUriString();
                log.debug("[补漏任务] GET 请求: {}", fullUrl);

                ResponseEntity<String> response = restTemplate.getForEntity(fullUrl, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.error("[补漏任务] 接口调用失败: {}", response.getStatusCode());
                    return;
                }

                // 文档接口直接返回数组 [...]
                responseData = objectMapper.readTree(response.getBody());

            } else {
                // --- 原有 POST 逻辑 (保留兼容性) ---
                // 1. 构造请求参数 Payload
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

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

                ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
                if (!response.getStatusCode().is2xxSuccessful()) {
                    log.error("[补漏任务] 接口调用失败: {}", response.getStatusCode());
                    return;
                }
                JsonNode root = objectMapper.readTree(response.getBody());
                responseData = root.get("list");
            }

            // 统一数据处理
            // 检查是否为数组
            if (responseData == null || !responseData.isArray() || responseData.size() == 0) {
                log.info("[补漏任务] 主题 {} 无新数据。", topic);
                // 更新时间戳到当前，避免一直停滞
                updateWatermark(topic);
                return;
            }

            int count = responseData.size();
            log.info("[补漏任务] 主题 {} 获取到 {} 条数据，开始回填...", topic, count);

            processData(topic, responseData);
            updateWatermark(topic);

        } catch (Exception e) {
            log.error("[补漏任务] 主题 {} 处理异常: {}", topic, e.getMessage(), e);
        }
    }

    private void updateWatermark(String topic) {
        String now = sdf.format(new Date());
        tmisDataMapper.updateLastTime(topic, now);
    }

    private void processData(String topic, JsonNode listNode) throws Exception {
        // 这里的逻辑保持不变，因为文档返回的 JSON 结构与我们之前的 DTO 定义是兼容的
        switch (topic) {
            // 1. 任务完成
            case "tims.feedback.completed.maintenance.task":
                List<TaskCompletionFeedbackDTO> list2 = objectMapper.convertValue(listNode, new TypeReference<List<TaskCompletionFeedbackDTO>>() {});
                list2.forEach(asyncTaskService::submitTaskCompletion);
                break;

            // 2. 任务得分
            case "tims.feedback.maintenance.task.score":
                List<TaskScoreFeedbackDTO> list3 = objectMapper.convertValue(listNode, new TypeReference<List<TaskScoreFeedbackDTO>>() {});
                list3.forEach(asyncTaskService::submitTaskScore);
                break;

            // 3. 推荐任务 (接口6)
            case "tims.recommend.rotational.task":
                List<RecommendedRotationalTaskDTO> list6 = objectMapper.convertValue(listNode, new TypeReference<List<RecommendedRotationalTaskDTO>>() {});
                list6.forEach(asyncTaskService::submitRecommendTask);
                break;

            // 4. 轮保完成 (接口8)
            case "tims.feedback.completed.rotational.task":
                List<RotationalTaskCompletionFeedbackDTO> list8 = objectMapper.convertValue(listNode, new TypeReference<List<RotationalTaskCompletionFeedbackDTO>>() {});
                list8.forEach(asyncTaskService::submitRotationalCompletion);
                break;

            // 5. 轮保得分 (接口9)
            case "tims.feedback.rotational.task.score":
                List<RotationalTaskScoreFeedbackDTO> list9 = objectMapper.convertValue(listNode, new TypeReference<List<RotationalTaskScoreFeedbackDTO>>() {});
                list9.forEach(asyncTaskService::submitRotationalScore);
                break;

            // 6. 停产检修完成 (接口14)
            case "tims.feedback.completed.production.halt.maintenance.task":
                List<ProductionHaltCompletionFeedbackDTO> list14 = objectMapper.convertValue(listNode, new TypeReference<List<ProductionHaltCompletionFeedbackDTO>>() {});
                list14.forEach(asyncTaskService::submitHaltCompletion);
                break;

            default:
                log.warn("[补漏任务] 未处理的 Topic: {}", topic);
        }
    }
}