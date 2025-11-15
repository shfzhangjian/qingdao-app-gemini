package com.lucksoft.qingdao.controller;

import com.lucksoft.qingdao.job.DynamicSchedulingService;
import com.lucksoft.qingdao.job.dto.ScheduleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * [新功能]
 * 用于管理动态定时任务的 API 控制器。
 */
@RestController
@RequestMapping("/api/schedule")
public class ScheduleController {

    private static final Logger log = LoggerFactory.getLogger(ScheduleController.class);

    @Autowired
    private DynamicSchedulingService schedulingService;

    /**
     * 获取当前 time.json 的配置内容
     * @return ScheduleDto
     */
    @GetMapping("/config")
    public ResponseEntity<?> getCurrentScheduleConfig() {
        try {
            ScheduleDto config = schedulingService.readConfig();
            return ResponseEntity.ok(config);
        } catch (IOException e) {
            log.error("读取 time.json 失败: {}", e.getMessage(), e);
            // [修复] 使用 JDK 1.8 兼容的 Map 创建方式
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "读取配置文件失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    /**
     * 更新 time.json 的配置内容并重新加载所有定时任务
     * @param newConfig 新的配置
     * @return 操作结果
     */
    @PostMapping("/config")
    public ResponseEntity<?> updateScheduleConfig(@RequestBody ScheduleDto newConfig) {
        if (newConfig == null || newConfig.getCronExpressions() == null) {
            // [修复] 使用 JDK 1.8 兼容的 Map 创建方式
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "请求体无效，'cronExpressions' 不能为空。");
            return ResponseEntity.badRequest().body(errorResponse);
        }

        try {
            log.warn("收到 API 请求：更新定时任务配置...");
            schedulingService.updateAndReload(newConfig);
            log.info("定时任务配置已更新并重新加载。");

            Map<String, Object> response = new HashMap<>();
            response.put("message", "定时任务配置已更新并成功重新加载。");
            response.put("newConfig", newConfig);
            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("写入 time.json 失败: {}", e.getMessage(), e);
            // [修复] 使用 JDK 1.8 兼容的 Map 创建方式
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "写入配置文件失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        } catch (Exception e) {
            log.error("重新加载定时任务时发生未知错误: {}", e.getMessage(), e);
            // [修复] 使用 JDK 1.8 兼容的 Map 创建方式
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "重新加载任务失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}