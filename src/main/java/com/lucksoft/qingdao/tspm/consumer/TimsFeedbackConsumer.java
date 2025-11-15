package com.lucksoft.qingdao.tspm.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucksoft.qingdao.controller.OracleApiController;
import com.lucksoft.qingdao.oracle.service.AsyncTaskService;
import com.lucksoft.qingdao.tspm.dto.*;
import com.lucksoft.qingdao.tspm.service.ReceivedDataCacheService;
import com.lucksoft.qingdao.tspm.service.TspmLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 监听并处理所有来自TIMS系统的消息
 * [已重构] 所有消费者现在都调用 AsyncTaskService 来异步处理数据
 */
@Service
public class TimsFeedbackConsumer {
    private static final Logger log = LoggerFactory.getLogger(OracleApiController.class);

    // --- UI实时日志服务 ---
    @Autowired
    private TspmLogService logService;

    @Autowired
    private ObjectMapper objectMapper;

    // --- 专用文件日志记录器 ---
    private static final Logger maintenanceCompletionLogger = LoggerFactory.getLogger("kafka.consumer.maintenance.completion");
    private static final Logger maintenanceScoreLogger = LoggerFactory.getLogger("kafka.consumer.maintenance.score");
    private static final Logger faultReportLogger = LoggerFactory.getLogger("kafka.consumer.fault.report");
    private static final Logger recommendTaskLogger = LoggerFactory.getLogger("kafka.consumer.recommend.task");
    private static final Logger rotationalCompletionLogger = LoggerFactory.getLogger("kafka.consumer.rotational.completion");
    private static final Logger rotationalScoreLogger = LoggerFactory.getLogger("kafka.consumer.rotational.score");
    private static final Logger faultAnalysisLogger = LoggerFactory.getLogger("kafka.consumer.fault.analysis");
    private static final Logger haltCompletionLogger = LoggerFactory.getLogger("kafka.consumer.halt.completion");

    @Autowired
    private ReceivedDataCacheService cacheService;

    // 注入统一异步任务服务
    @Autowired
    private AsyncTaskService asyncTaskService;

    /**
     * 接口 2: 消费“反馈保养、点检、润滑任务完成情况”
     */
    @KafkaListener(topics = "${kafka.topics.feedback-completed-maintenance-task}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTaskCompletionFeedback(String message) {
        String topic = "tims.feedback.completed.maintenance.task";
        try {
            List<TaskCompletionFeedbackDTO> feedbacks = objectMapper.readValue(message, new TypeReference<List<TaskCompletionFeedbackDTO>>(){});
            for (TaskCompletionFeedbackDTO feedback : feedbacks) {
                String logContent = objectMapper.writeValueAsString(feedback);
                maintenanceCompletionLogger.info(logContent);
                logService.logReceive(topic, logContent);

                Map<String, Object> dataMap = objectMapper.convertValue(feedback, new TypeReference<Map<String, Object>>() {});
                cacheService.addData(topic, dataMap);

                // [新] 提交给异步服务处理
                log.info("为接收到的任务完成 (ID: {}) 派发异步任务...", feedback.getTaskId());
                asyncTaskService.submitTaskCompletion(feedback);
            }
        } catch (Exception e) {
            String errorMessage = "消息处理失败: " + e.getMessage() + ", 原始消息: " + message;
            maintenanceCompletionLogger.error(errorMessage);
            logService.logReceiveError(topic, errorMessage);
        }
    }

    /**
     * 接口 3: 消费“反馈保养、点检、润滑任务完成得分”
     */
    @KafkaListener(topics = "${kafka.topics.feedback-maintenance-task-score}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTaskScoreFeedback(String message) {
        String topic = "tims.feedback.maintenance.task.score";
        try {
            List<TaskScoreFeedbackDTO> feedbacks = objectMapper.readValue(message, new TypeReference<List<TaskScoreFeedbackDTO>>(){});
            for (TaskScoreFeedbackDTO feedback : feedbacks) {
                String logContent = objectMapper.writeValueAsString(feedback);
                maintenanceScoreLogger.info(logContent);
                logService.logReceive(topic, logContent);

                Map<String, Object> dataMap = objectMapper.convertValue(feedback, new TypeReference<Map<String, Object>>() {});
                cacheService.addData(topic, dataMap);

                // [新] 提交给异步服务处理
                log.info("为接收到的任务得分 (ID: {}) 派发异步任务...", feedback.getTaskId());
                asyncTaskService.submitTaskScore(feedback);
            }
        } catch (Exception e) {
            String errorMessage = "消息处理失败: " + e.getMessage() + ", 原始消息: " + message;
            maintenanceScoreLogger.error(errorMessage);
            logService.logReceiveError(topic, errorMessage);
        }
    }

    /**
     * 接口 4 & 10: 消费“点检异常填报”或“故障维修报告创建”
     */
    @KafkaListener(topics = "${kafka.topics.create-fault-report}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeFaultReport(String message) {
        String topic = "tims.create.fault.report";
        try {
            List<FaultReportDTO> reports = objectMapper.readValue(message, new TypeReference<List<FaultReportDTO>>(){});

            for (FaultReportDTO report : reports) {
                String logContent = objectMapper.writeValueAsString(report);
                faultReportLogger.info(logContent);
                logService.logReceive(topic, logContent);

                Map<String, Object> dataMap = objectMapper.convertValue(report, new TypeReference<Map<String, Object>>() {});
                cacheService.addData(topic, dataMap);

                // [已修改] 调用 AsyncTaskService 来异步管理此任务
                log.info("为接收到的故障报告 (ID: {}) 派发异步任务...", report.getId());
                asyncTaskService.submitFaultReportTask(report);
            }
        } catch (Exception e) {
            String errorMessage = "消息处理失败: " + e.getMessage() + ", 原始消息: " + message;
            faultReportLogger.error(errorMessage);
            logService.logReceiveError(topic, errorMessage);
        }
    }

    /**
     * 接口 6: 消费“TIMS智能推荐预测性维修任务”
     */
    @KafkaListener(topics = "${kafka.topics.recommend-rotational-task}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeRecommendedTask(String message) {
        String topic = "tims.recommend.rotational.task";
        try {
            List<RecommendedRotationalTaskDTO> tasks = objectMapper.readValue(message, new TypeReference<List<RecommendedRotationalTaskDTO>>(){});
            for (RecommendedRotationalTaskDTO task : tasks) {
                String logContent = objectMapper.writeValueAsString(task);
                recommendTaskLogger.info(logContent);
                logService.logReceive(topic, logContent);

                Map<String, Object> dataMap = objectMapper.convertValue(task, new TypeReference<Map<String, Object>>() {});
                cacheService.addData(topic, dataMap);

                // [新] 提交给异步服务处理
                log.info("为接收到的推荐任务 (PlanID: {}) 派发异步任务...", task.getPlanId());
                asyncTaskService.submitRecommendTask(task);
            }
        } catch (Exception e) {
            String errorMessage = "消息处理失败: " + e.getMessage() + ", 原始消息: " + message;
            recommendTaskLogger.error(errorMessage);
            logService.logReceiveError(topic, errorMessage);
        }
    }

    /**
     * 接口 8: 消费“反馈轮保任务完成情况”
     */
    @KafkaListener(topics = "${kafka.topics.feedback-completed-rotational-task}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeRotationalCompletion(String message) {
        String topic = "tims.feedback.completed.rotational.task";
        try {
            List<RotationalTaskCompletionFeedbackDTO> feedbacks = objectMapper.readValue(message, new TypeReference<List<RotationalTaskCompletionFeedbackDTO>>(){});
            for (RotationalTaskCompletionFeedbackDTO feedback : feedbacks) {
                String logContent = objectMapper.writeValueAsString(feedback);
                rotationalCompletionLogger.info(logContent);
                logService.logReceive(topic, logContent);

                Map<String, Object> dataMap = objectMapper.convertValue(feedback, new TypeReference<Map<String, Object>>() {});
                cacheService.addData(topic, dataMap);

                // [新] 提交给异步服务处理
                log.info("为接收到的轮保完成 (ID: {}) 派发异步任务...", feedback.getTaskId());
                asyncTaskService.submitRotationalCompletion(feedback);
            }
        } catch (Exception e) {
            String errorMessage = "消息处理失败: " + e.getMessage() + ", 原始消息: " + message;
            rotationalCompletionLogger.error(errorMessage);
            logService.logReceiveError(topic, errorMessage);
        }
    }

    /**
     * 接口 9: 消费“反馈轮保任务完成得分”
     */
    @KafkaListener(topics = "${kafka.topics.feedback-rotational-task-score}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeRotationalScore(String message) {
        String topic = "tims.feedback.rotational.task.score";
        try {
            List<RotationalTaskScoreFeedbackDTO> feedbacks = objectMapper.readValue(message, new TypeReference<List<RotationalTaskScoreFeedbackDTO>>(){});
            for (RotationalTaskScoreFeedbackDTO feedback : feedbacks) {
                String logContent = objectMapper.writeValueAsString(feedback);
                rotationalScoreLogger.info(logContent);
                logService.logReceive(topic, logContent);

                Map<String, Object> dataMap = objectMapper.convertValue(feedback, new TypeReference<Map<String, Object>>() {});
                cacheService.addData(topic, dataMap);

                // [新] 提交给异步服务处理
                log.info("为接收到的轮保得分 (ID: {}) 派发异步任务...", feedback.getTaskId());
                asyncTaskService.submitRotationalScore(feedback);
            }
        } catch (Exception e) {
            String errorMessage = "消息处理失败: " + e.getMessage() + ", 原始消息: " + message;
            rotationalScoreLogger.error(errorMessage);
            logService.logReceiveError(topic, errorMessage);
        }
    }

    /**
     * 接口 11: 消费“故障分析报告创建”
     * [注意] 此接口缺少 DTO 规范，暂不调用存储过程。
     */
    @KafkaListener(topics = "${kafka.topics.create-fault-analysis-report}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeFaultAnalysisReport(String message) {
        String topic = "tims.create.fault.analysis.report";
        try {
            List<FaultAnalysisReportDTO> reports = objectMapper.readValue(message, new TypeReference<List<FaultAnalysisReportDTO>>(){});
            for (FaultAnalysisReportDTO report : reports) {
                String logContent = objectMapper.writeValueAsString(report);
                faultAnalysisLogger.info(logContent);
                logService.logReceive(topic, logContent);

                Map<String, Object> dataMap = objectMapper.convertValue(report, new TypeReference<Map<String, Object>>() {});
                cacheService.addData(topic, dataMap);


                log.warn("接收到故障分析报告 (ID: {})，但未实现存储过程调用。", report.getId());
                asyncTaskService.submitFaultAnalysisReport(report);
            }
        } catch (Exception e) {
            String errorMessage = "消息处理失败: " + e.getMessage() + ", 原始消息: " + message;
            faultAnalysisLogger.error(errorMessage);
            logService.logReceiveError(topic, errorMessage);
        }
    }

    /**
     * 接口 14: 消费“反馈停产检修计划任务完成情况”
     */
    @KafkaListener(topics = "${kafka.topics.feedback-completed-production-halt-maintenance-task}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeProductionHaltCompletion(String message) {
        String topic = "tims.feedback.completed.production.halt.maintenance.task";
        try {
            List<ProductionHaltCompletionFeedbackDTO> feedbacks = objectMapper.readValue(message, new TypeReference<List<ProductionHaltCompletionFeedbackDTO>>(){});
            for (ProductionHaltCompletionFeedbackDTO feedback : feedbacks) {
                String logContent = objectMapper.writeValueAsString(feedback);
                haltCompletionLogger.info(logContent);
                logService.logReceive(topic, logContent);

                Map<String, Object> dataMap = objectMapper.convertValue(feedback, new TypeReference<Map<String, Object>>() {});
                cacheService.addData(topic, dataMap);

                // [新] 提交给异步服务处理
                log.info("为接收到的停产检修完成 (ID: {}) 派发异步任务...", feedback.getTaskId());
                asyncTaskService.submitHaltCompletion(feedback);
            }
        } catch (Exception e) {
            String errorMessage = "消息处理失败: " + e.getMessage() + ", 原始消息: " + message;
            haltCompletionLogger.error(errorMessage);
            logService.logReceiveError(topic, errorMessage);
        }
    }
}