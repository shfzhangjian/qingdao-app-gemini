package com.lucksoft.qingdao.oracle.tmis.controller;

import com.lucksoft.qingdao.job.TmisDynamicJob;
import com.lucksoft.qingdao.oracle.tmis.dto.TmisDataQueryDTO;
import com.lucksoft.qingdao.oracle.tmis.service.TmisDataQueryService;
import com.lucksoft.qingdao.system.entity.TmisData;
import com.lucksoft.qingdao.system.mapper.TmisDataMapper;
import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tspm.service.TmisCompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * TMIS 通用数据查询与配置管理接口
 * 1. 提供通用数据查询能力 (Provider): 供外部系统(如 TIMS)调用，拉取 TsPM 的数据。
 * 2. 提供补漏配置管理能力 (Manager): 供内部前端管理页面调用，管理 TMIS_DATA 配置表。
 */
@RestController
@RequestMapping("/api/tmis/data")
public class TmisDataController {

    private static final Logger log = LoggerFactory.getLogger(TmisDataController.class);

    @Autowired
    private TmisDataQueryService queryService;

    @Autowired
    private TmisDataMapper tmisDataMapper;

    @Autowired
    private TmisCompensationService compensationService;


    // ==================================================================================
    // 1. 通用数据查询接口 (Provider Role)
    // ==================================================================================

    /**
     * 通用查询接口 (Provider)
     * 用于响应外部系统（如定时补漏任务或其他系统的对账请求）的数据拉取。
     *
     * URL: POST /api/tmis/data/query
     * Payload Example:
     * {
     * "topic": "tims.sync.maintenance.task",
     * "updateTime": "2025-11-26 00:00:00",
     * "pageNum": 1,
     * "pageSize": 50,
     * "body": { "equipmentCode": "EQ001" }
     * }
     *
     * @param queryDTO 查询参数对象，包含主题、时间戳、分页参数和业务过滤条件
     * @return PageResult 分页结果对象
     */
    @PostMapping("/query")
    public ResponseEntity<?> queryData(@RequestBody TmisDataQueryDTO queryDTO) {
        log.info("收到 TMIS 数据查询请求 - Topic: {}, UpdateTime: {}", queryDTO.getTopic(), queryDTO.getUpdateTime());

        if (queryDTO.getTopic() == null || queryDTO.getTopic().isEmpty()) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Topic is required"));
        }

        try {
            // 调用 Service 执行查询逻辑 (路由分发 -> Mapper 查询 -> DTO 转换)
            PageResult<?> result = queryService.queryData(queryDTO);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            log.warn("查询参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        } catch (Exception e) {
            log.error("数据查询发生系统错误", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Internal Server Error");
            error.put("message", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==================================================================================
    // 2. 补漏配置管理接口 (Manager Role) - 对应 tmis_config.html 页面
    // ==================================================================================

    /**
     * 获取所有 TMIS 接口配置及状态
     * 用于前端展示列表。
     *
     * @return 配置列表
     */
    @GetMapping("/config/list")
    public ResponseEntity<List<TmisData>> listConfigs() {
        return ResponseEntity.ok(tmisDataMapper.findAll());
    }

    @Autowired
    private TmisDynamicJob tmisDynamicJob; // [新增] 注入动态调度器



    /**
     * 更新接口启用状态 (启用/禁用)
     *
     * Payload: { "topic": "...", "isEnabled": 1/0 }
     */
    @PostMapping("/config/status")
    public ResponseEntity<?> updateStatus(@RequestBody Map<String, Object> params) {
        String topic = (String) params.get("topic");
        Integer enabled = (Integer) params.get("enabled");

        if (topic == null || enabled == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid parameters"));
        }

        tmisDataMapper.updateStatus(topic, enabled);
        // [新增] 状态更新后，重载调度器
        tmisDynamicJob.reloadTasks();
        return ResponseEntity.ok(Collections.singletonMap("message", "Status updated and tasks reloaded"));
    }

    /**
     * [新增] 更新 Cron 表达式
     * Payload: { "topic": "...", "cron": "0 0 14 * * ?" }
     */
    @PostMapping("/config/cron")
    public ResponseEntity<?> updateCron(@RequestBody Map<String, String> params) {
        String topic = params.get("topic");
        String cron = params.get("cron");

        if (topic == null || cron == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid parameters"));
        }

        try {
            // 简单校验 Cron 格式 (依赖 Spring 的 CronTrigger 校验比较麻烦，这里只做判空，运行时报错会记日志)
            tmisDataMapper.updateCron(topic, cron);
            // 重载调度器
            tmisDynamicJob.reloadTasks();
            return ResponseEntity.ok(Collections.singletonMap("message", "Cron updated and tasks reloaded"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * 重置最后更新时间（水位线）
     * 用于强制系统在下次定时任务时重新拉取历史数据。
     *
     * Payload: { "topic": "...", "time": "yyyy-MM-dd HH:mm:ss" }
     */
    @PostMapping("/config/reset-time")
    public ResponseEntity<?> resetTime(@RequestBody Map<String, Object> params) {
        String topic = (String) params.get("topic");
        String time = (String) params.get("time"); // 格式 yyyy-MM-dd HH:mm:ss

        if (topic == null || time == null) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Invalid parameters"));
        }

        tmisDataMapper.resetLastTime(topic, time);
        return ResponseEntity.ok(Collections.singletonMap("message", "Time reset successfully"));
    }

    /**
     * 手动触发一次补漏任务
     * 不必等待定时任务，立即执行一次异步拉取。
     *
     * Payload: { "topic": "..." }
     */
    @PostMapping("/config/trigger")
    public ResponseEntity<?> triggerCompensation(@RequestBody Map<String, String> params) {
        String topic = params.get("topic");
        TmisData config = tmisDataMapper.findByTopic(topic);

        if (config == null) {
            return ResponseEntity.notFound().build();
        }

        // 异步触发补漏逻辑 (非阻塞)
        compensationService.compensateTopic(config);

        return ResponseEntity.ok(Collections.singletonMap("message", "Task triggered successfully"));
    }
}