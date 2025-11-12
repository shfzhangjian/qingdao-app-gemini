package com.lucksoft.qingdao.oracle.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * [新功能]
 * 异步任务服务
 * 负责接收Controller层提交的任务，立即返回TaskId，并在后台线程中安全地（带锁）执行实际的业务逻辑。
 * 同时，它还负责跟踪每个任务的执行状态。
 */
@Service
public class AsyncTaskService {

    private static final Logger log = LoggerFactory.getLogger(AsyncTaskService.class);

    // 用于管理和查询任务状态的内存Map
    private final Map<String, String> taskStatusMap = new ConcurrentHashMap<>();

    // 并发锁，用于确保同一时间只有一个线程在执行保养任务的数据推送
    // [修改] 使用 tryLock() 来防止阻塞，并立即返回 "SKIPPED" 状态
    private final ReentrantLock maintenanceTaskLock = new ReentrantLock();

    @Autowired
    private OracleDataService oracleDataService; // 注入实际的业务服务

    /**
     * 异步处理保养任务（日保、月保、例保）
     *
     * @param taskId Controller层生成的唯一任务ID
     */
    @Async // 声明这是一个异步方法，它将在Spring的后台线程池中执行
    public void processMaintenanceTasks(String taskId) {
        log.info("[Task {}] 异步任务已启动...", taskId);
        taskStatusMap.put(taskId, "RUNNING");

        // 核心逻辑：尝试获取锁，如果获取不到（意味着已有任务在处理），则立即跳过
        if (!maintenanceTaskLock.tryLock()) {
            log.warn("[Task {}] 处理被跳过：另一个保养任务推送已在进行中。", taskId);
            taskStatusMap.put(taskId, "SKIPPED (Already processing)");
            return; // 立即退出线程
        }

        log.info("[Task {}] 成功获取到锁，开始执行核心业务...", taskId);
        try {
            // 调用OracleDataService中*同步*的业务方法
            // [修改] 调用新的 findAndPushNewMaintenanceTasks 方法
            Map<String, Object> result = oracleDataService.findAndPushNewMaintenanceTasks(taskId);

            // 根据业务逻辑的返回结果更新任务状态
            int pushedCount = (int) result.getOrDefault("pushedCount", 0);
            if (pushedCount > 0) {
                log.info("[Task {}] 异步任务成功完成，推送了 {} 条新数据。", taskId, pushedCount);
                taskStatusMap.put(taskId, "SUCCESS (Pushed " + pushedCount + " tasks)");
            } else {
                log.info("[Task {}] 异步任务成功完成，但未发现新数据。", taskId);
                taskStatusMap.put(taskId, "SUCCESS (No new data)");
            }

        } catch (Exception e) {
            log.error("[Task {}] 异步任务执行失败: {}", taskId, e.getMessage(), e);
            taskStatusMap.put(taskId, "FAILED: " + e.getMessage());
        } finally {
            maintenanceTaskLock.unlock(); // 确保在任何情况下都释放锁
            log.info("[Task {}] 释放锁。", taskId);
        }
    }

    /**
     * 异步处理其他类型的任务（如果需要的话，例如专业点检）
     *
     * @param taskId Controller层生成的唯一任务ID
     */
    @Async
    public void processProfessionalCheckTasks(String taskId) {
        log.info("[Task {}] 异步任务（专业点检）已启动...", taskId);
        taskStatusMap.put(taskId, "RUNNING");

        // 注意：这里我们假设专业点检任务*不需要*与其他任务互斥
        // 如果它也需要，您可以使用另一个单独的 ReentrantLock
        try {
            Map<String, Object> result = oracleDataService.findAndPushNewPmissionTasks(taskId); // 调用点检的业务逻辑

            int pushedCount = (int) result.getOrDefault("pushedCount", 0);
            if (pushedCount > 0) {
                taskStatusMap.put(taskId, "SUCCESS (Pushed " + pushedCount + " tasks)");
            } else {
                taskStatusMap.put(taskId, "SUCCESS (No new data)");
            }
            log.info("[Task {}] 异步任务（专业点检）成功完成。", taskId);

        } catch (Exception e) {
            log.error("[Task {}] 异步任务（专业点检）执行失败: {}", taskId, e.getMessage(), e);
            taskStatusMap.put(taskId, "FAILED: " + e.getMessage());
        }
    }


    /**
     * 生成一个新的、唯一的任务ID
     *
     * @return 任务ID字符串
     */
    public String submitTask() {
        String taskId = UUID.randomUUID().toString();
        taskStatusMap.put(taskId, "PENDING");
        return taskId;
    }

    /**
     * 根据任务ID查询任务的当前状态
     *
     * @param taskId 任务ID
     * @return 状态描述 (PENDING, RUNNING, SUCCESS, FAILED, SKIPPED, NOT_FOUND)
     */
    public String getTaskStatus(String taskId) {
        return taskStatusMap.getOrDefault(taskId, "NOT_FOUND");
    }

    /**
     * [NEW]
     * 允许外部（如Controller）同步更新一个任务的状态。
     * @param taskId 任务ID
     * @param status 新的状态
     */
    public void updateTaskStatus(String taskId, String status) {
        if (taskId == null || status == null) {
            log.warn("尝试使用 null taskId 或 status 更新任务状态。");
            return;
        }
        taskStatusMap.put(taskId, status);
    }

    /**
     * [新增] 获取所有任务的状态（用于调试）
     * @return 包含所有任务状态的Map
     */
    public Map<String, String> getAllTaskStatuses() {
        return new ConcurrentHashMap<>(taskStatusMap);
    }
}