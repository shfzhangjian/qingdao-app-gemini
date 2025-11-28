package com.lucksoft.qingdao.tspm.controller;

import com.lucksoft.qingdao.oracle.service.TmisFaultAnalysisReportService;
import com.lucksoft.qingdao.tspm.dto.FaultAnalysisReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 接口 5 (对应 Kafka 接口 11 的主动调用版): 故障分析报告创建接口
 */
@RestController
@RequestMapping("/api/tims")
public class TimsFaultAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(TimsFaultAnalysisController.class);

    @Autowired
    private TmisFaultAnalysisReportService analysisService;

    /**
     * 创建故障分析报告
     * Path: /api/tims/fault-analysis-report
     * Method: POST
     */
    @PostMapping("/fault-analysis-report")
    public ResponseEntity<?> createFaultAnalysisReport(@RequestBody FaultAnalysisReportDTO reportDto) {
        log.info("收到故障分析报告创建请求: name={}, equipmentCode={}", reportDto.getName(), reportDto.getEquipmentCode());

        // 基础校验
        if (reportDto.getName() == null || reportDto.getTeamName() == null) {
            return ResponseEntity.badRequest().body("Required fields (name, teamName) are missing.");
        }

        try {
            // 调用服务层：写入数据库 -> 触发推送 -> 获取编码
            String reportCode = analysisService.createReportAndTriggerPush(reportDto);

            if (reportCode != null) {
                // 直接返回字符串编码
                return ResponseEntity.ok(reportCode);
            } else {
                return ResponseEntity.internalServerError().body("Report created but failed to retrieve code.");
            }

        } catch (Exception e) {
            log.error("故障分析报告创建失败", e);
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorMap);
        }
    }
}