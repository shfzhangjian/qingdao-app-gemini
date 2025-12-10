package com.lucksoft.qingdao.tspm.controller;

import com.lucksoft.qingdao.oracle.service.TmisFaultReportService;
import com.lucksoft.qingdao.tspm.dto.FaultReportDTO;
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
 * 接口 4/10: 故障维修报告创建接口
 */
@RestController
@RequestMapping("/api/tims")
public class TimsFaultReportController {

    private static final Logger log = LoggerFactory.getLogger(TimsFaultReportController.class);

    @Autowired
    private TmisFaultReportService faultReportService;

    /**
     * 创建故障报告
     * Path: /api/tims/fault-report
     * Method: POST
     */
    @PostMapping("/create/fault/report")
    public ResponseEntity<?> createFaultReport(@RequestBody FaultReportDTO reportDto) {
        log.info("收到故障报告创建请求: name={}, equipmentCode={}", reportDto.getName(), reportDto.getEquipmentCode());

        // 基础校验
        if (reportDto.getName() == null || reportDto.getTeamName() == null) {
            return ResponseEntity.badRequest().body("Required fields (name, teamName) are missing.");
        }

        try {
            // 调用服务层：写入数据库 -> 触发推送 -> 获取编码
            String reportCode = faultReportService.createReport(reportDto);

            if (reportCode != null) {
                // 直接返回字符串编码，符合响应示例 "JB-E-20250716"
                return ResponseEntity.ok(reportCode);
            } else {
                return ResponseEntity.internalServerError().body("Report created but failed to retrieve code.");
            }

        } catch (Exception e) {
            log.error("故障报告创建失败", e);
            Map<String, String> errorMap = new HashMap<>();
            errorMap.put("error", e.getMessage());
            return ResponseEntity.internalServerError().body(errorMap);
        }
    }
}