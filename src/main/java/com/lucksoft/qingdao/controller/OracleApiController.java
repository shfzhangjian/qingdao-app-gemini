package com.lucksoft.qingdao.controller;

import com.lucksoft.qingdao.oracle.dto.*;
import com.lucksoft.qingdao.oracle.service.OracleDataService;
import com.lucksoft.qingdao.tspm.dto.MaintenanceTaskDTO;
import com.lucksoft.qingdao.tspm.dto.ProductionHaltTaskDTO;
import com.lucksoft.qingdao.tspm.dto.RotationalPlanDTO;
import com.lucksoft.qingdao.tspm.producer.TspmProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 专门用于接收来自 Oracle 数据库 UTL_HTTP 请求的控制器。
 *
 * @author Gemini
 */
@RestController
@RequestMapping("/api/oracle")
public class OracleApiController {

    private static final Logger log = LoggerFactory.getLogger(OracleApiController.class);

    @Autowired
    private OracleDataService oracleDataService;

    @Autowired
    private TspmProducerService producerService;

    // --- [修改] 注入 TIMS 标准 Topics ---
    @Value("${kafka.topics.sync-maintenance-task}")
    private String syncMaintenanceTaskTopic;

    @Value("${kafka.topics.sync-production-halt-maintenance-task}")
    private String syncProductionHaltTaskTopic;

    /**
     * [新增] 注入轮保计划Topic
     */
    @Value("${kafka.topics.sync-rotational-plan}")
    private String syncRotationalPlanTopic;

    // --- [保留] 未被重构的 Topics ---
    @Value("${kafka.topics.push-pmission-zy-jm}")
    private String pushPmissionZyJmTopic;


    /**
     * 接收来自 Oracle 过程的 JSON 推送。
     *
     * @param payload 包含 "stype" 的 JSON 负载
     * @return 一个简单的 JSON 响应，表示成功
     */
    @PostMapping("/receive")
    public ResponseEntity<Map<String, Object>> receiveOraclePush(@RequestBody OracleRequestPayload payload) {

        LocalDateTime receptionTime = LocalDateTime.now();
        String receivedStype = (payload != null) ? payload.getStype() : "null";

        log.info("--- Oracle 推送接收成功 ---");
        log.info("接收时间: {}", receptionTime);
        log.info("接收参数 (stype): {}", receivedStype);

        try {
            if (receivedStype == null || receivedStype.isEmpty()) {
                throw new IllegalArgumentException("stype 不能为空");
            }

            // --- 路由: 根据 stype 调用不同的服务 ---

            // 1. [修改] 精益日保 (SP_GENDAYTASK) - 转换为标准DTO
            if ("SP_GENDAYTASK".equals(receivedStype)) {
                log.info("识别到 stype [{}], 开始处理(精益日保)数据推送...", receivedStype);
                // [修改] service返回转换后的TIMS DTO列表
                List<MaintenanceTaskDTO> newTasks = oracleDataService.getAndFilterNewDayTasks();
                if (!newTasks.isEmpty()) {
                    // [修改] 推送到标准的保养Topic
                    producerService.sendMessage(syncMaintenanceTaskTopic, newTasks);
                    log.info("成功推送 {} 条 [MaintenanceTaskDTO] 任务到 Kafka Topic: {}", newTasks.size(), syncMaintenanceTaskTopic);
                }

                // 2. [修改] 轮保/月保 (SP_QD_PLANBOARD_LB) - 转换为标准DTO
            } else if ("PMBOARD.SP_QD_PLANBOARD_LB".equals(receivedStype)) {
                log.info("识别到 stype [{}], 开始处理(轮保/月保)数据推送...", receivedStype);
                // [修改] service返回转换后的TIMS DTO列表
                List<MaintenanceTaskDTO> newTasks = oracleDataService.getAndFilterNewPmissionBoardTasks();
                if (!newTasks.isEmpty()) {
                    // [修改] 推送到标准的保养Topic
                    producerService.sendMessage(syncMaintenanceTaskTopic, newTasks);
                    log.info("成功推送 {} 条 [MaintenanceTaskDTO] 任务到 Kafka Topic: {}", newTasks.size(), syncMaintenanceTaskTopic);
                }

                // 3. [修改] 例保 (JOB_GEN_BAOYANG_TASKS) - 转换为标准DTO
            } else if ("JOB_GEN_BAOYANG_TASKS".equals(receivedStype)) {
                log.info("识别到 stype [{}], 开始处理(例保)数据推送...", receivedStype);
                // [修改] service返回转换后的TIMS DTO列表
                List<MaintenanceTaskDTO> newTasks = oracleDataService.getAndFilterNewPmissionBoardBaoYangTasks();
                if (!newTasks.isEmpty()) {
                    // [修改] 推送到标准的保养Topic
                    producerService.sendMessage(syncMaintenanceTaskTopic, newTasks);
                    log.info("成功推送 {} 条 [MaintenanceTaskDTO] 任务到 Kafka Topic: {}", newTasks.size(), syncMaintenanceTaskTopic);
                }

                // 4. [修改] 维修计划归档 (PM_MONTH_ARCHIVED:ID) - 转换为标准DTO
            } else if (receivedStype.startsWith("PM_MONTH_ARCHIVED:")) {
                log.info("识别到 stype [{}], 开始处理(维修计划归档)数据推送...", receivedStype);
                Long indocno = Long.parseLong(receivedStype.split(":")[1]);
                // [修改] service返回转换后的TIMS DTO列表
                List<ProductionHaltTaskDTO> newTasks = oracleDataService.getAndFilterPmMonthData(indocno);
                if (newTasks != null && !newTasks.isEmpty()) {
                    // [修改] 推送到标准的停产检修Topic
                    producerService.sendMessage(syncProductionHaltTaskTopic, newTasks);
                    log.info("成功推送 {} 条 [ProductionHaltTaskDTO] 任务 (来自 INDOCNO: {}) 到 Kafka Topic: {}", newTasks.size(), indocno, syncProductionHaltTaskTopic);
                }

                // 5. [修改] 轮保计划归档 (EQ_PLANLB_ARCHIVED:ID) - 转换为标准DTO
            } else if (receivedStype.startsWith("EQ_PLANLB_ARCHIVED:")) {
                log.info("识别到 stype [{}], 开始处理(轮保计划归档)数据推送...", receivedStype);
                Long indocno = Long.parseLong(receivedStype.split(":")[1]);
                // [修改] service返回转换后的TIMS DTO列表
                List<RotationalPlanDTO> newTasks = oracleDataService.getAndFilterEqPlanLbData(indocno);
                if (newTasks != null && !newTasks.isEmpty()) {
                    // [修改] 推送到标准的轮保计划Topic
                    producerService.sendMessage(syncRotationalPlanTopic, newTasks);
                    log.info("成功推送 {} 条 [RotationalPlanDTO] 计划 (来自 INDOCNO: {}) 到 Kafka Topic: {}", newTasks.size(), indocno, syncRotationalPlanTopic);
                }

                // 6. [保留] 专业/精密点检 (PD_ZY_JM) - 按要求不转换
            } else if ("PD_ZY_JM".equals(receivedStype)) {
                log.info("识别到 stype [{}], 开始处理(专业/精密点检)数据推送...", receivedStype);
                List<PmissionDTO> newTasks = oracleDataService.getAndFilterNewPmissionTasks();
                if (!newTasks.isEmpty()) {
                    // [保留] 仍推送到旧的 'oracle.push.*' topic
                    producerService.sendMessage(pushPmissionZyJmTopic, newTasks);
                    log.info("成功推送 {} 条新任务到 Kafka Topic: {}", newTasks.size(), pushPmissionZyJmTopic);
                }

                // ... (可以继续添加其他 stype 的 else if) ...

                // 默认: 收到 stype 但没有匹配的处理器
            } else {
                log.warn("收到了一个未处理的 stype: {}", receivedStype);
            }

        } catch (Exception e) {
            log.error("处理 Oracle 推送 (stype={}) 时发生严重错误: {}", receivedStype, e.getMessage(), e);

            // 返回 500 错误给 Oracle
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("receivedStype", receivedStype);
            errorResponse.put("errorMessage", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }

        // 2. 构建一个成功的响应返回给 Oracle
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("receivedAt", receptionTime.toString());
        response.put("receivedStype", receivedStype);

        log.info("--- Oracle 推送处理完毕 ---");
        return ResponseEntity.ok(response);
    }

    /**
     * [新增] 调试接口：清空所有 Oracle 推送任务的 Redis 缓存
     * @return 清理结果
     */
    @PostMapping("/clear-push-cache")
    public ResponseEntity<Map<String, Object>> clearOraclePushCache() {
        log.warn("--- [调试] 收到清空 Oracle 推送缓存的请求 ---");
        try {
            Set<String> deletedKeys = oracleDataService.clearAllPushTaskCache();
            log.warn("--- [调试] 成功删除 {} 个 Redis 键 ---", deletedKeys.size());

            Map<String, Object> response = new HashMap<>();
            response.put("message", "成功清空 Oracle 推送缓存");
            response.put("deletedKeysCount", deletedKeys.size());
            response.put("deletedKeys", deletedKeys);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("清空 Redis 缓存失败", e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }


    /**
     * 用于映射 Oracle 发送的 JSON 负载 {"stype": "..."} 的简单 DTO。
     * 作为一个静态内部类，它不需要单独的文件。
     */
    static class OracleRequestPayload implements Serializable {
        private static final long serialVersionUID = 1L;

        private String stype;

        public String getStype() {
            return stype;
        }

        public void setStype(String stype) {
            this.stype = stype;
        }
    }
}