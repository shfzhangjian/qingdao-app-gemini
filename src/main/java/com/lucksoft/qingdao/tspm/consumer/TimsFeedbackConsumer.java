package com.lucksoft.qingdao.tspm.consumer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucksoft.qingdao.tspm.dto.*;
import com.lucksoft.qingdao.tspm.service.TspmLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 监听并处理所有来自TIMS系统的消息
 * (已根据Excel重构并集成日志)
 */
@Service
public class TimsFeedbackConsumer {

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
                // 1. 写入物理日志文件
                maintenanceCompletionLogger.info(logContent);
                // 2. 推送到前端UI
                logService.logReceive(topic, logContent);
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
            }
        } catch (Exception e) {
            String errorMessage = "消息处理失败: " + e.getMessage() + ", 原始消息: " + message;
            rotationalScoreLogger.error(errorMessage);
            logService.logReceiveError(topic, errorMessage);
        }
    }

    /**
     * 接口 11: 消费“故障分析报告创建”
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
            }
        } catch (Exception e) {
            String errorMessage = "消息处理失败: " + e.getMessage() + ", 原始消息: " + message;
            haltCompletionLogger.error(errorMessage);
            logService.logReceiveError(topic, errorMessage);
        }
    }
}

