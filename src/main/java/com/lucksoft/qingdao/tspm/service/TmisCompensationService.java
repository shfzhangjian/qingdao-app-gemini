package com.lucksoft.qingdao.tspm.service;

import cn.hutool.core.date.DateUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucksoft.common.utils.GjjDebugLogger;
import com.lucksoft.qingdao.oracle.service.AsyncTaskService;
import com.lucksoft.qingdao.system.entity.TmisData;
import com.lucksoft.qingdao.system.mapper.TmisDataMapper;
import com.lucksoft.qingdao.tspm.dto.*;
import com.lucksoft.qingdao.tspm.util.TimsApiAuthUtils; // [新增] 引入工具类
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TMIS 数据补漏服务 (升级版 V3 - 支持 API_AUTH)
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

    @Autowired
    private TimsApiAuthUtils authUtils; // [新增] 注入认证工具

    private final RestTemplate restTemplate = new RestTemplate();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 异步执行单个主题的补偿任务
     */
    @Async
    public void compensateTopic(TmisData config) {
        String topic = config.getTopic();
        String description = config.getDescription() != null ? config.getDescription() : "未知接口";
        String logFileName = description + "_" + topic;

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

            boolean isGetRequest = "GET".equalsIgnoreCase((String) fixedParams.get("method"));

            JsonNode responseData;
            long startTime = System.currentTimeMillis();

            // [新增] 准备公共 Header (apiAuth: API)
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.add(authUtils.getAuthHeaderKey(), authUtils.getAuthHeaderValue());

            if (isGetRequest) {
                // --- GET 请求逻辑 ---

                // 1. 准备参数 Map
                Map<String, Object> queryParams = new HashMap<>();
                queryParams.put("lastSyncDateTime", lastTime);
                if (fixedParams.containsKey("type")) {
                    queryParams.put("type", fixedParams.get("type"));
                }

                // 2. [关键修改] 使用工具类生成带签名的 URI
                URI signedUri = authUtils.signAndBuildUrl(baseUrl, queryParams, topic);

                log.debug("[补漏任务] GET 请求(Signed): {}", signedUri);
                GjjDebugLogger.log(topic, "发起 GET 请求", "URL: " + signedUri);

                // 3. 发送请求 (带 Header)
                HttpEntity<?> entity = new HttpEntity<>(headers);
                ResponseEntity<String> response = restTemplate.exchange(signedUri, HttpMethod.GET, entity, String.class);

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

                // 1. 准备 Body 参数 (业务参数)
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

                // 2. [关键修改] POST 请求的签名参数通常放在 URL 上
                // 我们传入一个空的 extraParams map，因为参数都在 Body 里，
                // 但根据文档 "url参数要包含如下公用参数"，我们需要把 _timestamp, _sign 等挂在 URL 上。
                URI signedUri = authUtils.signAndBuildUrl(baseUrl, null, topic);

                GjjDebugLogger.log(topic, "发起 POST 请求",
                        String.format("URL: %s\nPayload: %s", signedUri, objectMapper.writeValueAsString(payload)));

                // 3. 发送请求 (带 Header)
                HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
                ResponseEntity<String> response = restTemplate.exchange(signedUri, HttpMethod.POST, request, String.class);

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
            GjjDebugLogger.clearLogNameContext();
        }
    }

    private void updateWatermark(String topic) {
        String now = sdf.format(new Date());
        tmisDataMapper.updateLastTime(topic, now);
    }

    /*
       	TOPIC	LAST_UPDATE_TIME	API_URL	FIXED_PARAMS	DESCRIPTION	ENABLED	CRON_EXPRESSION	ROWID
1	tims.feedback.completed.maintenance.task_0	2025-12-04 02:00:01	http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/completed/task	{"method":"GET", "type":0}	接口2: 任务完成情况（例保）	1	0 0 14,20,22 * * ?	AABdIFAAMAAG6tuAAA
10	tims.feedback.completed.maintenance.task_1	2025-12-04 02:00:01	http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/completed/task	{"method":"GET", "type":1}	接口2: 任务完成情况（日保）	1	0 0 14,20,22 * * ?
9	tims.feedback.completed.maintenance.task_2	2025-12-04 02:00:01	http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/completed/task	{"method":"GET", "type":2}	接口2: 任务完成情况（月保）	1	0 0 14,20,22 * * ?
8	tims.feedback.completed.maintenance.task_4	2025-12-04 02:00:01	http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/completed/task	{"method":"GET", "type":4}	接口2: 任务完成情况（点检）	1	0 0 14,20,22 * * ?
7	tims.feedback.completed.maintenance.task_5	2025-12-04 02:00:01	http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/completed/task	{"method":"GET", "type":5}	接口2: 任务完成情况（润滑）	1	0 0 14,20,22 * * ?
13	tims.feedback.maintenance.task.score_1	2025-12-04 11:17:20	http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/task/score	{"method":"GET", "type":1}	接口3: 任务得分（日保）	1	0 0 2 * * ?
12	tims.feedback.maintenance.task.score_2	2025-12-04 11:17:20	http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/task/score	{"method":"GET", "type":2}	接口3: 任务得分（月保）	1	0 0 2 * * ?
14	tims.feedback.maintenance.task.score_5	2025-12-04 11:17:20	http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/task/score	{"method":"GET", "type":4}	接口3: 任务得分（点检）	1	0 0 2 * * ?
11	tims.feedback.maintenance.task.score_4	2025-12-04 11:17:20	http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/task/score	{"method":"GET", "type":5}	接口3: 任务得分（润滑）	1	0 0 2 * * ?
2	tims.feedback.maintenance.task.score_0	2025-12-04 11:26:49	http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/task/score	{"method":"GET", "type":0}	接口3: 任务得分（例保）	1	0 0 2 * * ?	AABdIFAAMAAG6tuAAB
3	tims.recommend.rotational.task	2025-12-04 11:18:41	http://tims.qd.com/ctmc-api/mro-edge-integration/rotational/sync/recommend/task	{"method":"GET"}	接口6: 推荐任务	1	0 0 2 * * ?	AABdIFAAMAAG6tuAAD
4	tims.feedback.completed.rotational.task	2025-12-04 02:00:01	http://tims.qd.com/ctmc-api/mro-edge-integration/rotational/sync/completed/task	{"method":"GET"}	接口8: 轮保完成	1	0 0 2 * * ?	AABdIFAAMAAG6tuAAE
5	tims.feedback.rotational.task.score	2025-12-04 11:18:21	http://tims.qd.com/ctmc-api/mro-edge-integration/rotational/sync/task/score	{"method":"GET"}	接口9: 轮保得分	1	0 0 2 * * ?	AABdIFAAMAAG6tuAAF
6	tims.feedback.completed.production.halt.maintenance.task	2025-12-04 02:00:01	http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/completed/production-halt/task	{"method":"GET"}	接口14: 停产检修完成	1	0 0 2 * * ?	AABdIFAAMAAG6tuAAH

     */

    private void processData(String topic, JsonNode listNode) throws Exception {
        // 逻辑保持不变，直接复用
        switch (topic) {
            case "tims.feedback.completed.maintenance.task":
                List<TaskCompletionFeedbackDTO> list2 = objectMapper.convertValue(listNode, new TypeReference<List<TaskCompletionFeedbackDTO>>() {});
                list2.forEach(asyncTaskService::submitTaskCompletion);
                break;

            case "tims.feedback.completed.maintenance.task_0":
                List<TaskCompletionFeedbackDTO> list2_0 = objectMapper.convertValue(listNode, new TypeReference<List<TaskCompletionFeedbackDTO>>() {});
                list2_0.forEach(asyncTaskService::submitTaskCompletion);
                break;
            case "tims.feedback.completed.maintenance.task_1":
                List<TaskCompletionFeedbackDTO> list2_1 = objectMapper.convertValue(listNode, new TypeReference<List<TaskCompletionFeedbackDTO>>() {});
                list2_1.forEach(asyncTaskService::submitTaskCompletion);
                break;
            case "tims.feedback.completed.maintenance.task_2":
                List<TaskCompletionFeedbackDTO> list2_2 = objectMapper.convertValue(listNode, new TypeReference<List<TaskCompletionFeedbackDTO>>() {});
                list2_2.forEach(asyncTaskService::submitTaskCompletion);
                break;

            case "tims.feedback.completed.maintenance.task_4":
                List<TaskCompletionFeedbackDTO> list2_4 = objectMapper.convertValue(listNode, new TypeReference<List<TaskCompletionFeedbackDTO>>() {});
                list2_4.forEach(asyncTaskService::submitTaskCompletion);
                break;

            case "tims.feedback.completed.maintenance.task_5":
                List<TaskCompletionFeedbackDTO> list2_5 = objectMapper.convertValue(listNode, new TypeReference<List<TaskCompletionFeedbackDTO>>() {});
                list2_5.forEach(asyncTaskService::submitTaskCompletion);
                break;

            case "tims.feedback.maintenance.task.score":
                List<TaskScoreFeedbackDTO> list3 = objectMapper.convertValue(listNode, new TypeReference<List<TaskScoreFeedbackDTO>>() {});
                list3.forEach(asyncTaskService::submitTaskScore);
                break;

            case "tims.feedback.maintenance.task.score_0":
                List<TaskScoreFeedbackDTO> list3_0 = objectMapper.convertValue(listNode, new TypeReference<List<TaskScoreFeedbackDTO>>() {});
                list3_0.forEach(asyncTaskService::submitTaskScore);
                break;

            case "tims.feedback.maintenance.task.score_1":
                List<TaskScoreFeedbackDTO> list3_1 = objectMapper.convertValue(listNode, new TypeReference<List<TaskScoreFeedbackDTO>>() {});
                list3_1.forEach(asyncTaskService::submitTaskScore);
                break;
            case "tims.feedback.maintenance.task.score_2":
                List<TaskScoreFeedbackDTO> list3_2 = objectMapper.convertValue(listNode, new TypeReference<List<TaskScoreFeedbackDTO>>() {});
                list3_2.forEach(asyncTaskService::submitTaskScore);
                break;
            case "tims.feedback.maintenance.task.score_4":
                List<TaskScoreFeedbackDTO> list3_4 = objectMapper.convertValue(listNode, new TypeReference<List<TaskScoreFeedbackDTO>>() {});
                list3_4.forEach(asyncTaskService::submitTaskScore);
                break;

            case "tims.feedback.maintenance.task.score_5":
                List<TaskScoreFeedbackDTO> list3_5 = objectMapper.convertValue(listNode, new TypeReference<List<TaskScoreFeedbackDTO>>() {});
                list3_5.forEach(asyncTaskService::submitTaskScore);
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