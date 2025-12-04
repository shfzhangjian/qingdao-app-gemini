package com.lucksoft.qingdao.tspm.controller;

import com.lucksoft.qingdao.tspm.dto.tims.CreateSelfCheckTaskReq;
import com.lucksoft.qingdao.tspm.dto.tims.CreateSelfCheckTaskResp;
import com.lucksoft.qingdao.tspm.dto.tims.GetAvgSpeedReq;
import com.lucksoft.qingdao.tspm.service.TimsServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TIMS 系统侧对账接口 (主动调用部分)
 * 实现文档 "二、TIMS系统侧对账接口" 中的接口 7 和 8
 */
@RestController
@RequestMapping("/api/tims")
public class TimsController {

    private static final Logger log = LoggerFactory.getLogger(TimsController.class);

    @Autowired
    private TimsServiceClient timsServiceClient;

    /**
     * 7. 获取设备指定时间段内平均车速
     * Path: /api/tims/speed/avg
     * Method: GET
     */
    @GetMapping("/speed/avg")
    public ResponseEntity<?> getAverageSpeed(
            @RequestParam String equipmentCode,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime) {

        log.info("收到获取平均车速请求: equipmentCode={}, startTime={}, endTime={}", equipmentCode, startTime, endTime);

        if (equipmentCode == null || equipmentCode.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createError("equipmentCode 不能为空"));
        }

        GetAvgSpeedReq req = new GetAvgSpeedReq(equipmentCode, startTime, endTime);

        try {
            Double speed = timsServiceClient.getAverageSpeed(req);
            // 直接返回 Double 值 (如 320.0)
            return ResponseEntity.ok(speed);
        } catch (Exception e) {
            log.error("获取平均车速失败", e);
            return ResponseEntity.internalServerError().body(createError(e.getMessage()));
        }
    }

    /**
     * 8. 创建自检自控待办任务
     * Path: /api/tims/self-check/task
     * Method: POST
     */
    @PostMapping("/self-check/task")
    public ResponseEntity<?> createSelfCheckTask(@RequestBody List<CreateSelfCheckTaskReq> tasks) {
        log.info("收到创建自检自控任务请求，共 {} 条", tasks != null ? tasks.size() : 0);

        if (tasks == null || tasks.isEmpty()) {
            return ResponseEntity.badRequest().body(createError("请求体不能为空"));
        }

        // 简单的必填校验
        for (int i = 0; i < tasks.size(); i++) {
            CreateSelfCheckTaskReq task = tasks.get(i);
            if (task.getEquipmentCode() == null || task.getTaskId() == null) {
                return ResponseEntity.badRequest().body(createError("第 " + (i + 1) + " 条数据缺少必填字段 (equipmentCode, taskId)"));
            }
        }

        try {
            List<CreateSelfCheckTaskResp> result = timsServiceClient.createSelfCheckTask(tasks);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("创建自检自控任务失败", e);
            return ResponseEntity.internalServerError().body(createError(e.getMessage()));
        }
    }

    private Map<String, String> createError(String msg) {
        Map<String, String> map = new HashMap<>();
        map.put("error", msg);
        return map;
    }
}