package com.lucksoft.qingdao.controller;

import com.lucksoft.qingdao.oracle.service.AsyncTaskService;
import com.lucksoft.qingdao.tspm.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * [新功能]
 * 直接测试控制器 (绕过 Kafka)
 * 接收来自 direct_test.html 的请求，并直接调用 AsyncTaskService，
 * 模拟 TimsFeedbackConsumer 的行为。
 */
@RestController
@RequestMapping("/api/direct-test")
public class DirectTestController {

    private static final Logger log = LoggerFactory.getLogger(DirectTestController.class);

    @Autowired
    private AsyncTaskService asyncTaskService;

    /**
     * 辅助方法，用于创建标准化的成功响应
     * @param count 处理的 DTO 数量
     * @param taskType 任务类型描述
     * @return 标准 ResponseEntity
     */
    private ResponseEntity<Map<String, Object>> createResponse(int count, String taskType) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "Tasks submitted directly to service");
        response.put("count", count);
        response.put("message", "已直接提交 " + count + " 个 " + taskType + " 任务到异步服务。");
        log.info("已直接提交 {} 个 {} 任务。", count, taskType);
        return ResponseEntity.ok(response);
    }

    /**
     * 接口 2: 接收任务完成情况
     */
    @PostMapping("/task-completion")
    public ResponseEntity<Map<String, Object>> taskCompletion(@RequestBody List<TaskCompletionFeedbackDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) return ResponseEntity.badRequest().build();
        for (TaskCompletionFeedbackDTO dto : dtoList) {
            asyncTaskService.submitTaskCompletion(dto);
        }
        return createResponse(dtoList.size(), "TaskCompletion");
    }

    /**
     * 接口 3: 接收任务得分
     */
    @PostMapping("/task-score")
    public ResponseEntity<Map<String, Object>> taskScore(@RequestBody List<TaskScoreFeedbackDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) return ResponseEntity.badRequest().build();
        for (TaskScoreFeedbackDTO dto : dtoList) {
            asyncTaskService.submitTaskScore(dto);
        }
        return createResponse(dtoList.size(), "TaskScore");
    }

    /**
     * 接口 4/10: 接收故障报告
     */
    @PostMapping("/fault-report")
    public ResponseEntity<Map<String, Object>> faultReport(@RequestBody List<FaultReportDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) return ResponseEntity.badRequest().build();
        for (FaultReportDTO dto : dtoList) {
            asyncTaskService.submitFaultReportTask(dto);
        }
        return createResponse(dtoList.size(), "FaultReport");
    }

    /**
     * 接口 6: 接收推荐任务
     */
    @PostMapping("/recommend-task")
    public ResponseEntity<Map<String, Object>> recommendTask(@RequestBody List<RecommendedRotationalTaskDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) return ResponseEntity.badRequest().build();
        for (RecommendedRotationalTaskDTO dto : dtoList) {
            asyncTaskService.submitRecommendTask(dto);
        }
        return createResponse(dtoList.size(), "RecommendTask");
    }

    /**
     * 接口 8: 接收轮保完成
     */
    @PostMapping("/rotational-completion")
    public ResponseEntity<Map<String, Object>> rotationalCompletion(@RequestBody List<RotationalTaskCompletionFeedbackDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) return ResponseEntity.badRequest().build();
        for (RotationalTaskCompletionFeedbackDTO dto : dtoList) {
            asyncTaskService.submitRotationalCompletion(dto);
        }
        return createResponse(dtoList.size(), "RotationalCompletion");
    }

    /**
     * 接口 9: 接收轮保得分
     */
    @PostMapping("/rotational-score")
    public ResponseEntity<Map<String, Object>> rotationalScore(@RequestBody List<RotationalTaskScoreFeedbackDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) return ResponseEntity.badRequest().build();
        for (RotationalTaskScoreFeedbackDTO dto : dtoList) {
            asyncTaskService.submitRotationalScore(dto);
        }
        return createResponse(dtoList.size(), "RotationalScore");
    }

    /**
     * 接口 11: 接收故障分析
     */
    @PostMapping("/fault-analysis")
    public ResponseEntity<Map<String, Object>> faultAnalysis(@RequestBody List<FaultAnalysisReportDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) return ResponseEntity.badRequest().build();
        for (FaultAnalysisReportDTO dto : dtoList) {
            asyncTaskService.submitFaultAnalysisReport(dto);
        }
        return createResponse(dtoList.size(), "FaultAnalysis");
    }

    /**
     * 接口 14: 接收停产检修完成
     */
    @PostMapping("/halt-completion")
    public ResponseEntity<Map<String, Object>> haltCompletion(@RequestBody List<ProductionHaltCompletionFeedbackDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) return ResponseEntity.badRequest().build();
        for (ProductionHaltCompletionFeedbackDTO dto : dtoList) {
            asyncTaskService.submitHaltCompletion(dto);
        }
        return createResponse(dtoList.size(), "HaltCompletion");
    }
}