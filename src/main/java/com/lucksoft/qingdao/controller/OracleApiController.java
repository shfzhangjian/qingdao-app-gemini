package com.lucksoft.qingdao.controller;

import com.lucksoft.qingdao.oracle.service.AsyncTaskService;
import com.lucksoft.qingdao.oracle.service.OracleDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * [已重构]
 * 专门用于接收来自 Oracle 数据库 UTL_HTTP 请求的控制器。
 * 1. [安全] 依赖 ApiKeyAuthFilter (X-API-KEY) + SecurityConfig (ROLE_API) 进行验证。
 * 2. [异步] 接收请求后立即提交到 AsyncTaskService，并返回 HTTP 200 OK 和 TaskId。
 * 3. [查询] 提供 /task-status/{taskId} 端点用于轮询任务状态。
 * 4. [注释] 增加了您要求的5个接口的路由注释。
 */
@RestController
@RequestMapping("/api/oracle")
public class OracleApiController {

    private static final Logger log = LoggerFactory.getLogger(OracleApiController.class);

    @Autowired
    private OracleDataService oracleDataService;

    @Autowired
    private AsyncTaskService asyncTaskService; // [新] 注入异步服务

    /**
     * [核心入口] 接收来自 Oracle 过程的 JSON 推送。
     *
     * @param payload 包含 "stype" 的 JSON 负载
     * @return HTTP 200 OK + TaskId，或 HTTP 400/500 错误
     */
    @PostMapping("/receive")
    public ResponseEntity<Map<String, Object>> receiveOraclePush(@RequestBody OracleRequestPayload payload) {

        String receivedStype = (payload != null) ? payload.getStype() : null;
        if (receivedStype == null || receivedStype.isEmpty()) {
            log.warn("--- Oracle 推送被拒绝：stype 不能为空 ---");
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "stype 不能为空");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        String taskId = asyncTaskService.submitTask(receivedStype); // 1. 立即生成 TaskId
        log.info("--- [Task {}] Oracle 推送接收成功 (stype: {}) ---", taskId, receivedStype);

        try {
            // --- 路由: 根据 stype 触发不同的异步任务 ---

            // 接口 1: 推送保养任务 (高并发, 视图查询)
            // 路由: SP_GENDAYTASK, PMBOARD.SP_QD_PLANBOARD_LB, JOB_GEN_BAOYANG_TASKS
            if ("SP_GENDAYTASK".equals(receivedStype) ||
                    "PMBOARD.SP_QD_PLANBOARD_LB".equals(receivedStype) ||
                    "JOB_GEN_BAOYANG_TASKS".equals(receivedStype)) {

                asyncTaskService.processMaintenanceTasks(taskId);

                // 接口 5: 推送轮保计划 (低并发, 表查询)
                // 路由: EQ_PLANLB_ARCHIVED:{id}
            } else if (receivedStype.startsWith("EQ_PLANLB_ARCHIVED:")) {

                Long indocno = Long.parseLong(receivedStype.split(":")[1]);
                asyncTaskService.processEqPlanLbArchive(taskId, indocno);

                // [新] 接口 7: 推送轮保任务 (高并发, 视图查询)
                // 路由: TIMS_PUSH_ROTATIONAL_TASK
            } else if ("TIMS_PUSH_ROTATIONAL_TASK".equals(receivedStype)) {

                asyncTaskService.processRotationalTasks(taskId);

                // [新] 接口 12: 推送故障报告编码 (低并发, 视图查询)
                // 路由: TIMS_PUSH_FAULT_REPORT_CODE:{id}
            } else if (receivedStype.startsWith("TIMS_PUSH_FAULT_REPORT_CODE:")) {

                Integer timsId = Integer.parseInt(receivedStype.split(":")[1]);
                asyncTaskService.processFaultReportCode(taskId, timsId);

                // 接口 13: 推送停产检修任务 (低并发, 表查询)
                // 路由: PM_MONTH_ARCHIVED:{id}
            } else if (receivedStype.startsWith("PM_MONTH_ARCHIVED:")) {

                Long indocno = Long.parseLong(receivedStype.split(":")[1]);
                asyncTaskService.processPmMonthArchive(taskId, indocno);

                // (旧) 接口: 专业/精密点检 (高并发, 表查询)
                // 路由: PD_ZY_JM
            } else if ("PD_ZY_JM".equals(receivedStype)) {

                asyncTaskService.processPmissionTasks(taskId);

            } else {
                log.warn("[Task {}] 收到了一个未处理的 stype: {}", taskId, receivedStype);
                // 即使未处理，也更新状态为SKIPPED
                asyncTaskService.updateTaskStatus(taskId, AsyncTaskService.TaskStatus.SKIPPED, "Skipped (Unknown stype)", null);
            }

        } catch (Exception e) {
            log.error("[Task {}] 触发异步任务时发生严重错误: {}", taskId, e.getMessage(), e);
            // 同步阶段就失败了（例如解析ID失败），更新状态
            asyncTaskService.updateTaskStatus(taskId, AsyncTaskService.TaskStatus.FAILED, "Failed to submit task: " + e.getMessage(), null);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("taskId", taskId);
            errorResponse.put("message", "提交任务失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }

        // 2. 立即返回 200 OK 和 TaskId
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Task submitted");
        response.put("taskId", taskId);
        return ResponseEntity.ok(response);
    }

    /**
     * [新增] 异步任务状态查询接口
     * @param taskId 任务ID
     * @return 任务状态
     */
    @GetMapping("/task-status/{taskId}")
    public ResponseEntity<Map<String, Object>> getTaskStatus(@PathVariable String taskId) {
        Map<String, Object> status = asyncTaskService.getTaskStatus(taskId);
        if ("NOT_FOUND".equals(status.get("status"))) {
            return ResponseEntity.status(404).body(status);
        }
        return ResponseEntity.ok(status);
    }


    /**
     * 调试接口：清空所有 Oracle 推送任务的 Redis 缓存
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
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
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