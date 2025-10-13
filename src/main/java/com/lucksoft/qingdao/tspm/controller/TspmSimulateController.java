package com.lucksoft.qingdao.tspm.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucksoft.qingdao.tspm.dto.*;
import com.lucksoft.qingdao.tspm.producer.TspmProducerService;
import com.lucksoft.qingdao.tspm.service.DynamicQueryService;
import com.lucksoft.qingdao.tspm.service.ReceivedDataCacheService;
import com.lucksoft.qingdao.tspm.service.TspmLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用于模拟和测试的控制器 (已根据Excel重构并集成日志)
 */
@RestController
@RequestMapping("/api/tspm")
public class TspmSimulateController {

    @Autowired
    private TspmProducerService producerService;

    @Autowired
    private TspmLogService logService;

    @Autowired
    private ObjectMapper objectMapper;

    // 注入所有TsPM作为生产者的Topic
    @Value("${kafka.topics.sync-maintenance-task}") private String syncMaintenanceTaskTopic;
    @Value("${kafka.topics.sync-rotational-plan}") private String syncRotationalPlanTopic;
    @Value("${kafka.topics.sync-rotational-task}") private String syncRotationalTaskTopic;
    @Value("${kafka.topics.receive-fault-report-code}") private String receiveFaultReportCodeTopic;
    @Value("${kafka.topics.sync-production-halt-maintenance-task}") private String syncProductionHaltMaintenanceTaskTopic;
    @Value("${kafka.topics.sync-user-equipment}") private String syncUserEquipmentTopic;


    /**
     * 接口，用于从前端接收请求，模拟向Kafka推送消息
     * @param payload 请求体，包含 "type" 和 "data"
     * @return 响应结果
     */
    @PostMapping("/simulate/push")
    public ResponseEntity<String> pushMessage(@RequestBody JsonNode payload) {
        String type = payload.get("type").asText();
        JsonNode data = payload.get("data");

        try {
            // 根据接口文档，所有推送的数据都应该是JSON数组格式
            if (!data.isArray()) {
                logService.addLog("PUSH_ERROR", "N/A", "数据格式错误: 前端应始终推送JSON数组 (以 [ 开头)");
                return ResponseEntity.badRequest().body("数据格式错误: 必须是JSON数组。");
            }

            switch (type) {
                case "syncMaintenanceTask":
                    List<MaintenanceTaskDTO> maintenanceTasks = objectMapper.convertValue(data, new TypeReference<List<MaintenanceTaskDTO>>() {});
                    producerService.sendMessage(syncMaintenanceTaskTopic, maintenanceTasks);
                    break;
                case "syncRotationalPlan":
                    List<RotationalPlanDTO> rotationalPlans = objectMapper.convertValue(data, new TypeReference<List<RotationalPlanDTO>>() {});
                    producerService.sendMessage(syncRotationalPlanTopic, rotationalPlans);
                    break;
                case "syncRotationalTask":
                    List<ScreenedRotationalTaskDTO> screenedTasks = objectMapper.convertValue(data, new TypeReference<List<ScreenedRotationalTaskDTO>>() {});
                    producerService.sendMessage(syncRotationalTaskTopic, screenedTasks);
                    break;
                case "receiveFaultReportCode":
                    List<FaultReportCodeFeedbackDTO> codeFeedbacks = objectMapper.convertValue(data, new TypeReference<List<FaultReportCodeFeedbackDTO>>() {});
                    producerService.sendMessage(receiveFaultReportCodeTopic, codeFeedbacks);
                    break;
                case "syncProductionHaltTask":
                    List<ProductionHaltTaskDTO> haltTasks = objectMapper.convertValue(data, new TypeReference<List<ProductionHaltTaskDTO>>() {});
                    producerService.sendMessage(syncProductionHaltMaintenanceTaskTopic, haltTasks);
                    break;
                case "syncUserEquipment":
                    List<UserEquipmentDTO> userEquipments = objectMapper.convertValue(data, new TypeReference<List<UserEquipmentDTO>>() {});
                    producerService.sendMessage(syncUserEquipmentTopic, userEquipments);
                    break;
                default:
                    return ResponseEntity.badRequest().body("未知的推送类型: " + type);
            }
            return ResponseEntity.ok("消息已提交到Kafka Topic");
        } catch (Exception e) {
            logService.addLog("PUSH_ERROR", "N/A", "前端JSON解析或处理失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body("处理请求失败: " + e.getMessage());
        }
    }

    @Autowired
    private ReceivedDataCacheService cacheService; //

    /**
     * 接口，用于获取指定Topic缓存的接收数据
     * @param topic Kafka Topic
     * @return 缓存的数据列表
     */
    @GetMapping("/received-data")
    public ResponseEntity<List<Map<String, Object>>> getReceivedData(@RequestParam String topic) {
        return ResponseEntity.ok(cacheService.getData(topic));
    }

    @Autowired
    private DynamicQueryService dynamicQueryService; // 新增注入
    /**
     * 接口，用于执行SQL并生成对应的JSON
     * @param request 包含SQL和limit的请求对象
     * @return 生成的JSON字符串
     */
    @PostMapping("/generate-json")
    public ResponseEntity<?> generateJsonFromSql(@RequestBody SqlQueryRequest request) {
        try {
            // 【关键改进】: 调用支持参数化查询的新版executeQuery方法
            List<Map<String, Object>> result = dynamicQueryService.executeQuery(request.getSql(), request.getLimit(), request.getParams());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logService.addLog("PUSH_ERROR", "SQL_EXECUTION", "SQL查询或转换失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body("SQL查询执行失败: " + e.getMessage());
        }
    }

    /**
     * 新增接口，用于前端页面加载时获取历史日志
     * @return 日志列表
     */
    @GetMapping("/logs")
    public ResponseEntity<List<LogMessage>> getLogs() {
        return ResponseEntity.ok(logService.getLogs());
    }
}

