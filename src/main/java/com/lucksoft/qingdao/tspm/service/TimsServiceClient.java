package com.lucksoft.qingdao.tspm.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucksoft.common.utils.GjjDebugLogger;
import com.lucksoft.qingdao.tspm.dto.tims.CreateSelfCheckTaskReq;
import com.lucksoft.qingdao.tspm.dto.tims.CreateSelfCheckTaskResp;
import com.lucksoft.qingdao.tspm.dto.tims.GetAvgSpeedReq;
import com.lucksoft.qingdao.tspm.util.TimsApiAuthUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TIMS 主动业务服务客户端
 * 负责处理 TsPM 主动向 TIMS 发起的业务请求（非补漏类）
 * 实现接口：
 * 7. 获取设备指定时间段内平均车速
 * 8. 创建自检自控待办任务
 */
@Service
public class TimsServiceClient {

    private static final Logger log = LoggerFactory.getLogger(TimsServiceClient.class);

    @Autowired
    private TimsApiAuthUtils authUtils;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tims.api.url.speed:http://tims.qd.com/ctmc-api/mro-edge-integration/speed/avg}")
    private String speedApiUrl;

    @Value("${tims.api.url.self-check:http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/self-check/task}")
    private String selfCheckApiUrl;

    /**
     * 接口 7: 获取设备指定时间段内平均车速
     * Method: GET
     */
    public Double getAverageSpeed(GetAvgSpeedReq req) {
        String topic = "TIMS_GET_AVG_SPEED";
        GjjDebugLogger.setLogNameContext("获取平均车速_" + req.getEquipmentCode());

        try {
            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("equipmentCode", req.getEquipmentCode());
            if (req.getStartTime() != null) queryParams.put("startTime", req.getStartTime());
            if (req.getEndTime() != null) queryParams.put("endTime", req.getEndTime());

            URI signedUri = authUtils.signAndBuildUrl(speedApiUrl, queryParams, topic);

            log.info("[接口7] 获取平均车速请求: {}", signedUri);
            GjjDebugLogger.log(topic, "请求", "URL: " + signedUri);

            HttpHeaders headers = new HttpHeaders();
            headers.add(authUtils.getAuthHeaderKey(), authUtils.getAuthHeaderValue());
            HttpEntity<?> entity = new HttpEntity<>(headers);

            long start = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.exchange(signedUri, HttpMethod.GET, entity, String.class);
            long duration = System.currentTimeMillis() - start;

            GjjDebugLogger.log(topic, "响应", String.format("Status: %s, Time: %dms, Body: %s", response.getStatusCode(), duration, response.getBody()));

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                try {
                    // 响应体直接是 double 值，例如 "320.0"
                    return Double.valueOf(response.getBody());
                } catch (NumberFormatException e) {
                    // 尝试解析 JSON 包装格式 {"code":200, "data": 320.0}
                    try {
                        JsonNode root = objectMapper.readTree(response.getBody());
                        if (root.has("data")) {
                            return root.get("data").asDouble();
                        }
                    } catch (Exception ex) { /* ignore */ }

                    log.error("解析车速失败: {}", response.getBody());
                    throw new RuntimeException("TIMS返回的车速格式无效");
                }
            } else {
                throw new RuntimeException("获取车速失败，状态码: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("[接口7] 获取平均车速异常", e);
            GjjDebugLogger.logError(topic, "异常", e);
            throw new RuntimeException("获取平均车速服务异常: " + e.getMessage());
        } finally {
            GjjDebugLogger.clearLogNameContext();
        }
    }

    /**
     * 接口 8: 创建自检自控待办任务
     * Method: POST
     * Request Body: JSON Array (支持批量)
     */
    public List<CreateSelfCheckTaskResp> createSelfCheckTask(List<CreateSelfCheckTaskReq> tasks) {
        String topic = "TIMS_CREATE_SELF_CHECK_TASK";
        GjjDebugLogger.setLogNameContext("创建自检任务_" + System.currentTimeMillis());

        try {
            // 1. 构建签名 URL (POST请求参数在Body，但签名参数在URL)
            URI signedUri = authUtils.signAndBuildUrl(selfCheckApiUrl, null, topic);

            String jsonBody = objectMapper.writeValueAsString(tasks);
            log.info("[接口8] 创建自检任务请求: URL={}, BodySize={}", signedUri, tasks.size());
            GjjDebugLogger.log(topic, "请求", String.format("URL: %s\nBody: %s", signedUri, jsonBody));

            // 2. 准备 Header
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(authUtils.getAuthHeaderKey(), authUtils.getAuthHeaderValue());

            // 3. 发起请求
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);
            long start = System.currentTimeMillis();
            ResponseEntity<String> response = restTemplate.postForEntity(signedUri, entity, String.class);
            long duration = System.currentTimeMillis() - start;

            GjjDebugLogger.log(topic, "响应", String.format("Status: %s, Time: %dms, Body: %s", response.getStatusCode(), duration, response.getBody()));

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // [关键修复] 解析响应
                // 实际响应是: {"code":200,"data":null,"msg":"SUCCEED"}
                // 而非文档中的数组。我们需要兼容这两种情况。
                try {
                    JsonNode root = objectMapper.readTree(response.getBody());
                    if (root.isArray()) {
                        // 情况1: 返回数组 (文档描述)
                        return objectMapper.readValue(response.getBody(), new TypeReference<List<CreateSelfCheckTaskResp>>() {});
                    } else if (root.isObject()) {
                        // 情况2: 返回包装对象 (实际日志)
                        if (root.has("code") && root.get("code").asInt() == 200) {
                            JsonNode dataNode = root.get("data");
                            if (dataNode != null && dataNode.isArray()) {
                                return objectMapper.convertValue(dataNode, new TypeReference<List<CreateSelfCheckTaskResp>>() {});
                            }
                            // 成功但没有数据返回
                            return Collections.emptyList();
                        } else {
                            throw new RuntimeException("TIMS返回错误: " + (root.has("msg") ? root.get("msg").asText() : "未知错误"));
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("解析响应失败: " + e.getMessage());
                }
                return Collections.emptyList();
            } else {
                throw new RuntimeException("创建自检任务失败，状态码: " + response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("[接口8] 创建自检任务异常", e);
            GjjDebugLogger.logError(topic, "异常", e);
            throw new RuntimeException("创建自检任务服务异常: " + e.getMessage());
        } finally {
            GjjDebugLogger.clearLogNameContext();
        }
    }
}