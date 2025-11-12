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
 * 1. [安全] 受 ApiKeyAuthFilter 保护, 必须提供 X-API-KEY。
 * 2. [异步] 接收到请求后，立即将任务提交给 AsyncTaskService，并返回 taskId。
 * 3. [路由] 根据 stype 路由到不同的后台处理逻辑。
 * 4. [移除] 不再处理 EQ_PLANLB_ARCHIVED（已由 RotationalPlanPushJob 自动处理）。
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
    private AsyncTaskService asyncTaskService; // [新] 注入异步任务服务


    /**
     * [已重构]
     * 接收来自 Oracle 过程的 JSON 推送。
     * 立即返回 200 OK 和一个 taskId，实际工作在后台线程完成。
     *
     * @param payload 包含 "stype" 的 JSON 负载
     * @return 一个包含 taskId 的 JSON 响应
     */
    @PostMapping("/receive")
    public ResponseEntity<Map<String, Object>> receiveOraclePush(@RequestBody OracleRequestPayload payload) {

        String receivedStype = (payload != null) ? payload.getStype() : "null";
        log.info("--- [Oracle Trigger] 收到推送请求: {} ---", receivedStype);

        if (receivedStype == null || receivedStype.isEmpty()) {
            throw new IllegalArgumentException("stype 不能为空");
        }

        // --- 1. 立即生成任务ID ---
        String taskId = asyncTaskService.submitTask();
        log.info("[Task {}] 已为 stype '{}' 创建异步任务。", taskId, receivedStype);

        try {
            // --- 2. 路由: 根据 stype 触发*不同*的异步任务 ---

            // A. 统一处理所有保养任务 (日保, 轮保/月保, 例保)
            if ("SP_GENDAYTASK".equals(receivedStype) ||
                    "PMBOARD.SP_QD_PLANBOARD_LB".equals(receivedStype) ||
                    "JOB_GEN_BAOYANG_TASKS".equals(receivedStype))
            {
                log.info("[Task {}] 路由到: 保养任务处理 (processMaintenanceTasks)", taskId);
                asyncTaskService.processMaintenanceTasks(taskId);
            }
            // B. 处理专业/精密点检
            else if ("PD_ZY_JM".equals(receivedStype))
            {
                log.info("[Task {}] 路由到: 专业点检处理 (processProfessionalCheckTasks)", taskId);
                asyncTaskService.processProfessionalCheckTasks(taskId);
            }
            // C. [同步处理] 维修计划归档 (数据量小，非高并发源，无需异步)
            else if (receivedStype.startsWith("PM_MONTH_ARCHIVED:"))
            {
                log.info("[Task {}] 路由到: 维修计划 (同步处理)", taskId);
                Long indocno = Long.parseLong(receivedStype.split(":")[1]);
                Map<String, Object> result = oracleDataService.getAndFilterPmMonthData(indocno);

                // [修复] 调用 asyncTaskService 来更新状态
                asyncTaskService.updateTaskStatus(taskId, "SUCCESS (Pushed " + result.get("pushedCount") + " tasks)");

                // [修改] 将推送的数据也返回给 Oracle
                Map<String, Object> response = new HashMap<>();
                response.put("status", "Task processed synchronously");
                response.put("taskId", taskId);
                response.put("pushedData", result.get("pushedData"));
                return ResponseEntity.ok(response);
            }
            // D. [已移除] 轮保计划归档
            else if (receivedStype.startsWith("EQ_PLANLB_ARCHIVED:"))
            {
                log.warn("[Task {}] 路由到: 轮保计划 (已忽略)", taskId);
                log.warn("stype '{}' 已被忽略，此逻辑现由 'RotationalPlanPushJob' 定时任务自动处理。", receivedStype);
                asyncTaskService.updateTaskStatus(taskId, "SKIPPED (Deprecated, handled by Job)");
            }
            // E. 其他
            else
            {
                log.warn("[Task {}] 路由到: 未知 (SKIPPED)", taskId);
                asyncTaskService.updateTaskStatus(taskId, "SKIPPED (Unknown stype)");
            }

        } catch (Exception e) {
            log.error("[Task {}] 提交异步任务时发生严重错误: {}", taskId, e.getMessage(), e);
            asyncTaskService.updateTaskStatus(taskId, "FAILED (Submission Error)");

            // 即使提交失败，也要返回 200 和 TaskId，让Oracle知道我们收到了请求
            // 真正的错误会在 /task-status/{taskId} 中反映出来
        }

        // --- 3. 立即返回 200 OK 和 TaskId ---
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Task submitted");
        response.put("taskId", taskId);
        return ResponseEntity.ok(response);
    }

    /**
     * [新]
     * 检查一个异步任务的执行状态。
     *
     * @param taskId 任务ID
     * @return 任务状态
     */
    @GetMapping("/task-status/{taskId}")
    public ResponseEntity<Map<String, String>> getTaskStatus(@PathVariable String taskId) {
        String status = asyncTaskService.getTaskStatus(taskId);

        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId);
        response.put("status", status);

        if ("NOT_FOUND".equals(status)) {
            return ResponseEntity.status(404).body(response);
        }
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
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }


    /**
     * 用于映射 Oracle 发送的 JSON 负载 {"stype": "..."} 的简单 DTO。
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