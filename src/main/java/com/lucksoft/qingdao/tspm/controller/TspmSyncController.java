package com.lucksoft.qingdao.tspm.controller;

import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tspm.dto.*;
import com.lucksoft.qingdao.tspm.dto.sync.*;
import com.lucksoft.qingdao.tspm.service.TspmSyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * TsPM 系统侧对账/同步独立接口控制器
 * 提供特定的 GET 接口供 TIMS 系统主动拉取数据
 */
@RestController
@RequestMapping("/api")
public class TspmSyncController {

    private static final Logger log = LoggerFactory.getLogger(TspmSyncController.class);

    @Autowired
    private TspmSyncService syncService;

    /**
     * 1. 获取保养、点检、润滑任务
     * Path: /tims/sync/maintenance/task
     */
    @GetMapping("/tims/sync/maintenance/task")
    public ResponseEntity<?> getMaintenanceTasks(MaintenanceTaskReq req) {
        log.info("收到接口1(保养任务)同步请求: type={}, lastSync={}", req.getType(), req.getLastSyncDateTime());

        if (req.getType() == null) return ResponseEntity.badRequest().body(createError("参数 'type' 不能为空"));
        if (req.getLastSyncDateTime() == null || req.getLastSyncDateTime().trim().isEmpty()) return ResponseEntity.badRequest().body(createError("参数 'lastSyncDateTime' 不能为空"));

        try {
            PageResult<MaintenanceTaskDTO> result = syncService.getMaintenanceTasks(req);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("接口1同步失败", e);
            return ResponseEntity.internalServerError().body(createError(e.getMessage()));
        }
    }

    /**
     * 5. 获取轮保计划排期
     * Path: /tims/sync/rotational/plan
     */
    @GetMapping("/tims/sync/rotational/plan")
    public ResponseEntity<?> getRotationalPlans(RotationalPlanReq req) {
        log.info("收到接口5(轮保计划)同步请求: lastSync={}", req.getLastSyncDateTime());

        if (req.getLastSyncDateTime() == null || req.getLastSyncDateTime().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createError("参数 'lastSyncDateTime' 不能为空"));
        }

        try {
            PageResult<RotationalPlanDTO> result = syncService.getRotationalPlans(req);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("接口5同步失败", e);
            return ResponseEntity.internalServerError().body(createError(e.getMessage()));
        }
    }

    /**
     * 7. 获取TsPM筛选后轮保任务
     * Path: /tims/sync/rotational/task
     * Method: GET
     */
    @GetMapping("/tims/sync/rotational/task")
    public ResponseEntity<?> getRotationalTasks(RotationalTaskReq req) {
        log.info("收到接口7(轮保任务)同步请求: lastSync={}", req.getLastSyncDateTime());

        if (req.getLastSyncDateTime() == null || req.getLastSyncDateTime().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createError("参数 'lastSyncDateTime' 不能为空"));
        }

        try {
            PageResult<ScreenedRotationalTaskDTO> result = syncService.getRotationalTasks(req);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("接口7同步失败", e);
            return ResponseEntity.internalServerError().body(createError(e.getMessage()));
        }
    }

    private Map<String, String> createError(String msg) {
        Map<String, String> map = new HashMap<>();
        map.put("error", msg);
        return map;
    }

    /**
     * [新增] 13. 获取停产检修计划任务
     * Path: /tims/sync/production/halt/maintenance/task
     * Method: GET
     */
    @GetMapping("/tims/sync/production/halt/maintenance/task")
    public ResponseEntity<?> getProductionHaltTasks(ProductionHaltTaskReq req) {
        log.info("收到接口13(停产检修)同步请求: lastSync={}", req.getLastSyncDateTime());

        if (req.getLastSyncDateTime() == null || req.getLastSyncDateTime().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createError("参数 'lastSyncDateTime' 不能为空"));
        }

        try {
            PageResult<ProductionHaltTaskDTO> result = syncService.getProductionHaltTasks(req);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("接口13同步失败", e);
            return ResponseEntity.internalServerError().body(createError(e.getMessage()));
        }
    }

    /**
     * [新增] 7. 获取包机信息
     * Path: /tims/sync/user/equipment
     * Method: GET
     */
    @GetMapping("/tims/sync/user/equipment")
    public ResponseEntity<?> getUserEquipments(UserEquipmentReq req) {
        log.info("收到接口7(包机信息)同步请求: lastSync={}", req.getLastSyncDateTime());

        if (req.getLastSyncDateTime() == null || req.getLastSyncDateTime().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(createError("参数 'lastSyncDateTime' 不能为空"));
        }

        try {
            PageResult<UserEquipmentDTO> result = syncService.getUserEquipments(req);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("接口7同步失败", e);
            return ResponseEntity.internalServerError().body(createError(e.getMessage()));
        }
    }
}