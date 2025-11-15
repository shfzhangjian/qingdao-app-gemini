package com.lucksoft.qingdao.oracle.service;


import com.lucksoft.qingdao.tmis.service.TmisTaskScoreService;
import com.lucksoft.qingdao.tspm.dto.*;
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
 * [已修改] 统一异步任务服务
 * 1. 负责接收 Controller 和 Consumer 的请求，并立即返回 Task ID。
 * 2. 在后台线程池 (@Async) 中执行实际的数据处理 (查询, 防重, 推送, 调用SP)。
 * 3. 维护一个内存中的 Map 来跟踪任务状态。
 * 4. 使用 ReentrantLock 来防止高并发的重复处理。
 */
@Service
public class AsyncTaskService {

    private static final Logger log = LoggerFactory.getLogger(AsyncTaskService.class);

    // 任务状态机
    public enum TaskStatus {
        PENDING,                // 任务已提交，等待线程池调度
        RUNNING,                // 任务已在运行（正在查询或推送）
        SKIPPED,                // 任务被跳过（因为已有同类任务在运行）
        SUCCESS,                // 任务成功完成
        FAILED                  // 任务执行失败
    }

    private final Map<String, Object> taskStatusMap = new ConcurrentHashMap<>();

    // --- 并发锁 ---
    private final Lock maintenanceTaskLock = new ReentrantLock();
    private final Lock rotationalTaskLock = new ReentrantLock();
    private final Lock pmissionTaskLock = new ReentrantLock();
    private final Lock faultReportLock = new ReentrantLock();
    // [新增] 为 Kafka 消费者添加新锁
    private final Lock taskCompletionLock = new ReentrantLock();
    private final Lock taskScoreLock = new ReentrantLock();
    private final Lock recommendTaskLock = new ReentrantLock();
    private final Lock rotationalCompletionLock = new ReentrantLock();
    private final Lock rotationalScoreLock = new ReentrantLock();
    private final Lock haltCompletionLock = new ReentrantLock();
    // [新增] 故障分析报告的并发锁
    private final Lock faultAnalysisLock = new ReentrantLock();
    // --- Oracle 推送服务 (Java -> Kafka) ---
    @Autowired
    private OracleDataService oracleDataService;

    // --- TMIS 接收服务 (Kafka -> SP) ---
    @Autowired
    private TmisFaultReportService tmisFaultReportService;
    @Autowired
    private TmisTaskCompletionService tmisTaskCompletionService;
    @Autowired
    private TmisTaskScoreService tmisTaskScoreService;
    @Autowired
    private TmisRecommendTaskService tmisRecommendTaskService;
    @Autowired
    private TmisRotationalCompletionService tmisRotationalCompletionService;
    @Autowired
    private TmisRotationalScoreService tmisRotationalScoreService;
    @Autowired
    private TmisHaltCompletionService tmisHaltCompletionService;
    @Autowired
    private TmisFaultAnalysisReportService tmisFaultAnalysisReportService;

    // ===================================================================
    // [对外] 1. 提交任务 (Submitter Methods)
    // ===================================================================


    /**
     * [对外] 1i. 提交 Kafka 接口 11 (故障分析报告)
     */
    public String submitFaultAnalysisReport(FaultAnalysisReportDTO dto) {
        String taskId = UUID.randomUUID().toString();
        updateTaskStatus(taskId, TaskStatus.PENDING, "Task submitted for FaultAnalysisReport: " + dto.getId(), null);
        processFaultAnalysisReport(taskId, dto);
        return taskId;
    }
    /**
     * [对外] 1a. 提交一个由 Oracle 触发的任务
     */
    public String submitTask(String stype) {
        String taskId = UUID.randomUUID().toString();
        updateTaskStatus(taskId, TaskStatus.PENDING, "Task submitted for stype: " + stype, null);

        // --- 路由: 根据 stype 触发不同的异步任务 ---
        try {
            if ("SP_GENDAYTASK".equals(stype) || "PMBOARD.SP_QD_PLANBOARD_LB".equals(stype) || "JOB_GEN_BAOYANG_TASKS".equals(stype)) {
                processMaintenanceTasks(taskId); // 接口 1
            } else if (stype.startsWith("EQ_PLANLB_ARCHIVED:")) {
                processEqPlanLbArchive(taskId, Long.parseLong(stype.split(":")[1])); // 接口 5
            } else if ("TIMS_PUSH_ROTATIONAL_TASK".equals(stype)) {
                processRotationalTasks(taskId); // 接口 7
            } else if (stype.startsWith("TIMS_PUSH_FAULT_REPORT_CODE:")) {
                processFaultReportCode(taskId, Integer.parseInt(stype.split(":")[1])); // 接口 12
            } else if (stype.startsWith("PM_MONTH_ARCHIVED:")) {
                processPmMonthArchive(taskId, Long.parseLong(stype.split(":")[1])); // 接口 13
            } else if ("PD_ZY_JM".equals(stype)) {
                processPmissionTasks(taskId); // 旧接口
            } else {
                log.warn("[Task {}] 收到了一个未处理的 stype: {}", taskId, stype);
                updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Unknown stype)", null);
            }
        } catch (Exception e) {
            log.error("[Task {}] 触发异步任务时发生同步错误: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, "Failed to submit task: " + e.getMessage(), null);
        }
        return taskId;
    }

    /**
     * [对外] 1b. 提交 Kafka 接口 2 (任务完成)
     */
    public String submitTaskCompletion(TaskCompletionFeedbackDTO dto) {
        String taskId = UUID.randomUUID().toString();
        updateTaskStatus(taskId, TaskStatus.PENDING, "Task submitted for TaskCompletion: " + dto.getTaskId(), null);
        processTaskCompletion(taskId, dto);
        return taskId;
    }

    /**
     * [对外] 1c. 提交 Kafka 接口 3 (任务得分)
     */
    public String submitTaskScore(TaskScoreFeedbackDTO dto) {
        String taskId = UUID.randomUUID().toString();
        updateTaskStatus(taskId, TaskStatus.PENDING, "Task submitted for TaskScore: " + dto.getTaskId(), null);
        processTaskScore(taskId, dto);
        return taskId;
    }

    /**
     * [对外] 1d. 提交 Kafka 接口 4/10 (故障报告)
     */
    public String submitFaultReportTask(FaultReportDTO report) {
        String taskId = UUID.randomUUID().toString();
        updateTaskStatus(taskId, TaskStatus.PENDING, "Task submitted for Fault Report ID: " + report.getId(), null);
        processIncomingFaultReport(taskId, report);
        return taskId;
    }

    /**
     * [对外] 1e. 提交 Kafka 接口 6 (推荐任务)
     */
    public String submitRecommendTask(RecommendedRotationalTaskDTO dto) {
        String taskId = UUID.randomUUID().toString();
        updateTaskStatus(taskId, TaskStatus.PENDING, "Task submitted for RecommendTask: " + dto.getPlanId(), null);
        processRecommendTask(taskId, dto);
        return taskId;
    }

    /**
     * [对外] 1f. 提交 Kafka 接口 8 (轮保完成)
     */
    public String submitRotationalCompletion(RotationalTaskCompletionFeedbackDTO dto) {
        String taskId = UUID.randomUUID().toString();
        updateTaskStatus(taskId, TaskStatus.PENDING, "Task submitted for RotationalCompletion: " + dto.getTaskId(), null);
        processRotationalCompletion(taskId, dto);
        return taskId;
    }

    /**
     * [对外] 1g. 提交 Kafka 接口 9 (轮保得分)
     */
    public String submitRotationalScore(RotationalTaskScoreFeedbackDTO dto) {
        String taskId = UUID.randomUUID().toString();
        updateTaskStatus(taskId, TaskStatus.PENDING, "Task submitted for RotationalScore: " + dto.getTaskId(), null);
        processRotationalScore(taskId, dto);
        return taskId;
    }

    /**
     * [对外] 1h. 提交 Kafka 接口 14 (停产检修完成)
     */
    public String submitHaltCompletion(ProductionHaltCompletionFeedbackDTO dto) {
        String taskId = UUID.randomUUID().toString();
        updateTaskStatus(taskId, TaskStatus.PENDING, "Task submitted for HaltCompletion: " + dto.getTaskId(), null);
        processHaltCompletion(taskId, dto);
        return taskId;
    }


    // ===================================================================
    // [内部] 2. 任务状态管理 (Status Methods)
    // ===================================================================

    public Map<String, Object> getTaskStatus(String taskId) {
        Object status = taskStatusMap.get(taskId);
        if (status == null) {
            Map<String, Object> notFound = new HashMap<>();
            notFound.put("taskId", taskId);
            notFound.put("status", "NOT_FOUND");
            return notFound;
        }
        return (Map<String, Object>) status;
    }



    /**
     * [新] 异步处理 Kafka 接口 11 (故障分析报告)
     */
    @Async
    public void processFaultAnalysisReport(String taskId, FaultAnalysisReportDTO dto) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Acquiring lock for FaultAnalysisReport: " + dto.getId(), null);
        if (!faultAnalysisLock.tryLock()) {
            updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Another FaultAnalysisReport task is processing)", null);
            return;
        }
        try {
            tmisFaultAnalysisReportService.processFaultAnalysisReport(dto);
            updateTaskStatus(taskId, TaskStatus.SUCCESS, "Processing complete.", null);
        } catch (Exception e) {
            log.error("[Task {}] 异步处理故障分析报告失败: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage(), null);
        } finally {
            faultAnalysisLock.unlock();
        }
    }

    public void updateTaskStatus(String taskId, TaskStatus status, String message, Map<String, Object> resultData) {
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

    // ===================================================================
    // [内部] 3. 异步处理 (Processor @Async Methods)
    // ===================================================================

    // --- A. Oracle 触发 (Java -> Kafka) ---

    @Async
    public void processMaintenanceTasks(String taskId) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Acquiring lock...", null);
        if (!maintenanceTaskLock.tryLock()) {
            updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Another maintenance task is already processing)", null);
            return;
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
        }
    }

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

    @Async
    public void processRotationalTasks(String taskId) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Acquiring lock...", null);
        if (!rotationalTaskLock.tryLock()) {
            updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Another rotational task is already processing)", null);
            return;
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
        }
    }

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

    @Async
    public void processPmissionTasks(String taskId) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Acquiring lock...", null);
        if (!pmissionTaskLock.tryLock()) {
            updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Another pmission task is already processing)", null);
            return;
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
        }
    }

    // --- B. Kafka 触发 (Kafka -> Java -> SP) ---

    @Async
    public void processTaskCompletion(String taskId, TaskCompletionFeedbackDTO dto) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Acquiring lock for TaskCompletion: " + dto.getTaskId(), null);
        if (!taskCompletionLock.tryLock()) {
            updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Another TaskCompletion task is processing)", null);
            return;
        }
        try {
            tmisTaskCompletionService.processTaskCompletion(dto);
            updateTaskStatus(taskId, TaskStatus.SUCCESS, "Processing complete.", null);
        } catch (Exception e) {
            log.error("[Task {}] 异步处理任务完成失败: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage(), null);
        } finally {
            taskCompletionLock.unlock();
        }
    }

    @Async
    public void processTaskScore(String taskId, TaskScoreFeedbackDTO dto) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Acquiring lock for TaskScore: " + dto.getTaskId(), null);
        if (!taskScoreLock.tryLock()) {
            updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Another TaskScore task is processing)", null);
            return;
        }
        try {
            tmisTaskScoreService.processTaskScore(dto);
            updateTaskStatus(taskId, TaskStatus.SUCCESS, "Processing complete.", null);
        } catch (Exception e) {
            log.error("[Task {}] 异步处理任务得分失败: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage(), null);
        } finally {
            taskScoreLock.unlock();
        }
    }

    @Async
    public void processIncomingFaultReport(String taskId, FaultReportDTO report) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Acquiring fault report lock...", null);
        if (!faultReportLock.tryLock()) {
            updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Another fault report task is already processing)", null);
            return;
        }
        try {
            updateTaskStatus(taskId, TaskStatus.RUNNING, "Lock acquired. Executing createReportAndTriggerPush for ID: " + report.getId(), null);
            tmisFaultReportService.createReportAndTriggerPush(report);
            Map<String, Object> result = new HashMap<>();
            result.put("processedReportId", report.getId());
            updateTaskStatus(taskId, TaskStatus.SUCCESS, "Processing complete.", result);
        } catch (Exception e) {
            log.error("[Task {}] 异步处理故障报告失败: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage(), null);
        } finally {
            faultReportLock.unlock();
        }
    }

    @Async
    public void processRecommendTask(String taskId, RecommendedRotationalTaskDTO dto) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Acquiring lock for RecommendTask: " + dto.getPlanId(), null);
        if (!recommendTaskLock.tryLock()) {
            updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Another RecommendTask task is processing)", null);
            return;
        }
        try {
            tmisRecommendTaskService.processRecommendTask(dto);
            updateTaskStatus(taskId, TaskStatus.SUCCESS, "Processing complete.", null);
        } catch (Exception e) {
            log.error("[Task {}] 异步处理推荐任务失败: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage(), null);
        } finally {
            recommendTaskLock.unlock();
        }
    }

    @Async
    public void processRotationalCompletion(String taskId, RotationalTaskCompletionFeedbackDTO dto) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Acquiring lock for RotationalCompletion: " + dto.getTaskId(), null);
        if (!rotationalCompletionLock.tryLock()) {
            updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Another RotationalCompletion task is processing)", null);
            return;
        }
        try {
            tmisRotationalCompletionService.processRotationalCompletion(dto);
            updateTaskStatus(taskId, TaskStatus.SUCCESS, "Processing complete.", null);
        } catch (Exception e) {
            log.error("[Task {}] 异步处理轮保完成失败: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage(), null);
        } finally {
            rotationalCompletionLock.unlock();
        }
    }

    @Async
    public void processRotationalScore(String taskId, RotationalTaskScoreFeedbackDTO dto) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Acquiring lock for RotationalScore: " + dto.getTaskId(), null);
        if (!rotationalScoreLock.tryLock()) {
            updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Another RotationalScore task is processing)", null);
            return;
        }
        try {
            tmisRotationalScoreService.processRotationalScore(dto);
            updateTaskStatus(taskId, TaskStatus.SUCCESS, "Processing complete.", null);
        } catch (Exception e) {
            log.error("[Task {}] 异步处理轮保得分失败: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage(), null);
        } finally {
            rotationalScoreLock.unlock();
        }
    }

    @Async
    public void processHaltCompletion(String taskId, ProductionHaltCompletionFeedbackDTO dto) {
        updateTaskStatus(taskId, TaskStatus.RUNNING, "Acquiring lock for HaltCompletion: " + dto.getTaskId(), null);
        if (!haltCompletionLock.tryLock()) {
            updateTaskStatus(taskId, TaskStatus.SKIPPED, "Skipped (Another HaltCompletion task is processing)", null);
            return;
        }
        try {
            tmisHaltCompletionService.processHaltCompletion(dto);
            updateTaskStatus(taskId, TaskStatus.SUCCESS, "Processing complete.", null);
        } catch (Exception e) {
            log.error("[Task {}] 异步处理停产检修完成失败: {}", taskId, e.getMessage(), e);
            updateTaskStatus(taskId, TaskStatus.FAILED, e.getMessage(), null);
        } finally {
            haltCompletionLock.unlock();
        }
    }
}