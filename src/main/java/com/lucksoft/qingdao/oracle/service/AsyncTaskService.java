package com.lucksoft.qingdao.oracle.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * [新] 异步任务服务
 * 1. 负责接收 Controller 的请求，并立即返回 Task ID。
 * 2. 在后台线程池 (@Async) 中执行实际的数据处理 (查询, 防重, 推送)。
 * 3. 维护一个内存中的 Map 来跟踪任务状态。
 * 4. 使用 ReentrantLock 来防止高并发的重复处理。
 */
@Service
public class AsyncTaskService {

    private static final Logger log = LoggerFactory.getLogger(AsyncTaskService.class);

    // [核心] 任务状态机
    public enum TaskStatus {
        PENDING,                // 任务已提交，等待线程池调度
        RUNNING,                // 任务已在运行（正在查询或推送）
        SKIPPED,                // 任务被跳过（因为已有同类任务在运行）
        SUCCESS,                // 任务成功完成
        FAILED                  // 任务执行失败
    }

    // [核心] 任务状态缓存 (在生产环境中，应替换为Redis以支持分布式)
    private final Map<String, Object> taskStatusMap = new ConcurrentHashMap<>();

    // [核心] 并发锁 (一个锁用于所有高频的、基于视图的查询)
    private final Lock maintenanceTaskLock = new ReentrantLock();
    private final Lock rotationalTaskLock = new ReentrantLock();
    private final Lock pmissionTaskLock = new ReentrantLock(); // 专业点检锁

    @Autowired
    private OracleDataService oracleDataService;

    /**
     * [对外] 1. 提交一个新任务
     * @param stype 触发类型
     * @return 唯一的 Task ID
     */
    public String submitTask(String stype) {
        String taskId = UUID.randomUUID().toString();
        // 立即将任务状态设为 PENDING
        updateTaskStatus(taskId, TaskStatus.PENDING, "Task submitted for stype: " + stype, null);
        return taskId;
    }

    /**
     * [对外] 2. 查询任务状态
     * @param taskId 任务ID
     * @return 包含状态和消息的Map
     */
    public Map<String, Object> getTaskStatus(String taskId) {
        Object status = taskStatusMap.get(taskId);
        if (status == null) {
            // [JDK 1.8] 替换 Map.of()
            Map<String, Object> notFound = new HashMap<>();
            notFound.put("taskId", taskId);
            notFound.put("status", "NOT_FOUND");
            return notFound;
        }
        return (Map<String, Object>) status;
    }

    /**
     * [对外] 3. 更新任务状态 (由 Controller 调用)
     * (这是一个公共方法，允许其他服务更新状态)
     */
    public void updateTaskStatus(String taskId, TaskStatus status, String message, Map<String, Object> resultData) {
        // [JDK 1.8] 替换 Map.of()
        Map<String, Object> statusDetails = new HashMap<>();
        statusDetails.put("taskId", taskId);
        statusDetails.put("status", status.name());
        statusDetails.put("message", message);
        statusDetails.put("timestamp", System.currentTimeMillis());
        if (resultData != null) {
            statusDetails.put("result", resultData);
        }
        taskStatusMap.put(taskId, statusDetails);
        log.info("[Task {}] 状态更新: {} - {}", taskId, status.name(), message);
    }

    // --- 异步处理方法 ---

    /**
     * [异步] 接口 1: 处理保养任务 (高并发, 带锁)
     */
    @Async
    public void processMaintenanceTasks(String taskId) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Acquiring lock...", null);

        if (!maintenanceTaskLock.tryLock()) {
            updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Another maintenance task is already processing)", null);
            return; // **跳过**
        }

        try {
            updateTaskStatus(taskId, TaskStatus.RUNNING, "Lock acquired. Executing query V_MAINTENANCE_TASKS_RECENT...", null);
            Map<String, Object> result = oracleDataService.findAndPushNewMaintenanceTasks(taskId);
            updateTaskStatus(taskId, TaskStatus.SUCCESS, "Processing complete.", result);
        } catch (Exception e) {
            log.error("[Task {}] 异步处理保养任务失败: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage(), null);
        } finally {
            maintenanceTaskLock.unlock();
            log.info("[Task {}] Maintenance lock released.", taskId);
        }
    }

    /**
     * [异步] 接口 5: 处理轮保计划 (低并发, 无锁, Redis防重)
     */
    @Async
    public void processEqPlanLbArchive(String taskId, Long indocno) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Executing query for EQ_PLANLB_ARCHIVED (indocno=" + indocno + ")...", null);
        try {
            Map<String, Object> result = oracleDataService.getAndFilterEqPlanLbData(indocno);
            updateTaskStatus(taskId, TaskStatus.SUCCESS, "Processing complete.", result);
        } catch (Exception e) {
            log.error("[Task {}] 异步处理轮保计划失败: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage(), null);
        }
    }

    /**
     * [异步] 接口 7: 处理轮保任务 (高并发, 带锁)
     */
    @Async
    public void processRotationalTasks(String taskId) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Acquiring lock...", null);

        if (!rotationalTaskLock.tryLock()) {
            updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Another rotational task is already processing)", null);
            return; // **跳过**
        }

        try {
            updateTaskStatus(taskId, TaskStatus.RUNNING, "Lock acquired. Executing query V_ROTATIONAL_TASK_RECENT...", null);
            Map<String, Object> result = oracleDataService.findAndPushNewRotationalTasks(taskId);
            updateTaskStatus(taskId, TaskStatus.SUCCESS, "Processing complete.", result);
        } catch (Exception e) {
            log.error("[Task {}] 异步处理轮保任务失败: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage(), null);
        } finally {
            rotationalTaskLock.unlock();
            log.info("[Task {}] Rotational task lock released.", taskId);
        }
    }

    /**
     * [异步] 接口 12: 处理故障编码 (低并发, 无锁, Redis防重)
     */
    @Async
    public void processFaultReportCode(String taskId, Integer timsId) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Executing query for TIMS_PUSH_FAULT_REPORT_CODE (timsId=" + timsId + ")...", null);
        try {
            Map<String, Object> result = oracleDataService.getAndFilterFaultReportCode(timsId);
            updateTaskStatus(taskId, TaskStatus.SUCCESS, "Processing complete.", result);
        } catch (Exception e) {
            log.error("[Task {}] 异步处理故障编码失败: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage(), null);
        }
    }

    /**
     * [异步] 接口 13: 处理停产检修 (低并发, 无锁, Redis防重)
     */
    @Async
    public void processPmMonthArchive(String taskId, Long indocno) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Executing query for PM_MONTH_ARCHIVED (indocno=" + indocno + ")...", null);
        try {
            Map<String, Object> result = oracleDataService.getAndFilterPmMonthData(indocno);
            updateTaskStatus(taskId, TaskStatus.SUCCESS, "Processing complete.", result);
        } catch (Exception e) {
            log.error("[Task {}] 异步处理维修计划失败: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage(), null);
        }
    }

    /**
     * [异步] 旧接口: 处理专业点检 (高并发, 带锁)
     */
    @Async
    public void processPmissionTasks(String taskId) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Acquiring lock...", null);

        if (!pmissionTaskLock.tryLock()) {
            updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Another pmission task is already processing)", null);
            return; // **跳过**
        }

        try {
            updateTaskStatus(taskId, TaskStatus.RUNNING, "Lock acquired. Executing query PMISSION (PD_ZY_JM)...", null);
            Map<String, Object> result = oracleDataService.findAndPushNewPmissionTasks(taskId);
            updateTaskStatus(taskId, TaskStatus.SUCCESS, "Processing complete.", result);
        } catch (Exception e) {
            log.error("[Task {}] 异步处理专业点检任务失败: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage(), null);
        } finally {
            pmissionTaskLock.unlock();
            log.info("[Task {}] Pmission task lock released.", taskId);
        }
    }

}