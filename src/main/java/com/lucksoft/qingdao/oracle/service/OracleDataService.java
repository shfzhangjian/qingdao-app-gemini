package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.dto.*;
import com.lucksoft.qingdao.oracle.mapper.EqPlanLbMapper;
import com.lucksoft.qingdao.oracle.mapper.PmissionMapper;
import com.lucksoft.qingdao.oracle.mapper.PmMonthMapper;
import com.lucksoft.qingdao.oracle.mapper.VMaintenanceTasksMapper;
import com.lucksoft.qingdao.tspm.dto.MaintenanceTaskDTO;
import com.lucksoft.qingdao.tspm.dto.ProductionHaltTaskDTO;
import com.lucksoft.qingdao.tspm.dto.RotationalPlanDTO;
import com.lucksoft.qingdao.tspm.producer.TspmProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * [已重构 - JDK 1.8 兼容]
 * 业务服务层，用于处理来自 Oracle API Controller 的数据查询和过滤。
 * 1. 依赖 `V_MAINTENANCE_TASKS_RECENT` 视图来统一查询保养任务。
 * 2. 包含 Redis 防重逻辑。
 * 3. 包含 Kafka 消息分批推送逻辑。
 * 4. 本类所有方法都是同步的，由 AsyncTaskService 在后台线程中调用。
 * 5. 移除了 `EQ_PLANLB_ARCHIVED` 逻辑（已由 RotationalPlanPushJob 处理）。
 */
@Service
public class OracleDataService {

    private static final Logger log = LoggerFactory.getLogger(OracleDataService.class);
    private static final String PUSHED_TASK_KEY_PREFIX = "oracle:pushed_tasks:";
    private static final Duration KEY_EXPIRATION = Duration.ofDays(1);

    // Kafka 消息批量推送的大小
    private static final int KAFKA_BATCH_SIZE = 500;

    @Autowired
    private VMaintenanceTasksMapper vMaintenanceTasksMapper; // [新] 统一的保养任务Mapper

    @Autowired
    private PmMonthMapper pmMonthMapper;
    @Autowired
    private PmissionMapper pmissionMapper;
    // [删除] private EqPlanLbMapper eqPlanLbMapper; // 不再需要

    @Autowired
    private OracleDataTransformerService transformerService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private TspmProducerService producerService;

    // Kafka Topics
    @Value("${kafka.topics.sync-maintenance-task}")
    private String maintenanceTaskTopic;

    @Value("${kafka.topics.push-pm-month}")
    private String pushPmMonthTopic;

    // [删除] @Value("${kafka.topics.sync-rotational-plan}") // 不再需要
    // private String syncRotationalPlanTopic;

    @Value("${kafka.topics.push-pmission-zy-jm}")
    private String pushPmissionZyJmTopic;


    /**
     * 1. [重构] 查询、防重并推送所有新保养任务（来自视图）
     * 此方法现在是同步的，由 AsyncTaskService 异步调用。
     * [修改] 增加了 Kafka 分批推送逻辑。
     *
     * @param taskId 异步任务ID，用于日志跟踪
     * @return 包含推送计数和数据的Map
     */
    public Map<String, Object> findAndPushNewMaintenanceTasks(String taskId) {
        // 1. 从统一视图查询所有近期任务
        List<VMaintenanceTaskDTO> recentTasks = vMaintenanceTasksMapper.findRecentTasks();
        if (recentTasks.isEmpty()) {
            log.info("[Task {}][MaintenanceTasks] V_MAINTENANCE_TASKS_RECENT 视图中未发现近期任务。", taskId);
            // [JDK 1.8] 替换 Map.of()
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("pushedCount", 0);
            emptyResult.put("pushedData", new ArrayList<>());
            return emptyResult;
        }

        String redisKey = PUSHED_TASK_KEY_PREFIX + "maintenance_tasks_v2";
        List<MaintenanceTaskDTO> newTasksToPush = new ArrayList<>();

        // 2. 过滤掉已经推送过的
        for (VMaintenanceTaskDTO vTask : recentTasks) {
            String deDupeKey = vTask.getDeDupeKey(); // 使用视图中定义的防重键
            if (deDupeKey == null || deDupeKey.isEmpty()) {
                log.warn("[Task {}] 跳过任务，因为 deDupeKey 为空: taskId={}", taskId, vTask.getTaskId());
                continue;
            }

            Long addedCount = redisTemplate.opsForSet().add(redisKey, deDupeKey);
            if (addedCount != null && addedCount > 0) {
                // 这是一个新任务，转换并添加到推送列表
                newTasksToPush.add(transformerService.transformVTaskToMaintenanceTask(vTask));
            }
        }

        // 3. 如果我们添加了新任务, 就刷新 Key 的过期时间并推送
        if (!newTasksToPush.isEmpty()) {
            redisTemplate.expire(redisKey, KEY_EXPIRATION);
            log.info("[Task {}][MaintenanceTasks] 查询到 {} 条近期任务, 过滤后新增 {} 条。", taskId, recentTasks.size(), newTasksToPush.size());

            // 4. [新] 分批推送到 Kafka
            int totalTasks = newTasksToPush.size();
            for (int i = 0; i < totalTasks; i += KAFKA_BATCH_SIZE) {
                int end = Math.min(i + KAFKA_BATCH_SIZE, totalTasks);
                List<MaintenanceTaskDTO> batchList = newTasksToPush.subList(i, end);

                log.info("[Task {}] 正在推送批次 {}/{} ({} 条任务)...", taskId, (i / KAFKA_BATCH_SIZE) + 1, (totalTasks + KAFKA_BATCH_SIZE - 1) / KAFKA_BATCH_SIZE, batchList.size());
                producerService.sendMessage(maintenanceTaskTopic, batchList);
            }
            log.info("[Task {}] 成功分批推送 {} 条新保养任务到 Kafka Topic: {}", taskId, totalTasks, maintenanceTaskTopic);

        } else {
            log.info("[Task {}][MaintenanceTasks] 查询到 {} 条近期任务, 但全部已推送过。", taskId, recentTasks.size());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("pushedCount", newTasksToPush.size());
        result.put("pushedData", newTasksToPush); // 仍然返回完整列表供日志记录
        return result;
    }


    /**
     * 4. PM_MONTH_ARCHIVED (维修计划归档 - 触发器)
     * [修改] 此方法现在由 Controller 直接调用（因为它不是高并发源）
     *
     * @param indocno 主键
     * @return 包含推送计数和数据的Map
     */
    public Map<String, Object> getAndFilterPmMonthData(Long indocno) {
        String redisKey = PUSHED_TASK_KEY_PREFIX + "pm_month";
        Long addedCount = redisTemplate.opsForSet().add(redisKey, indocno.toString());
        if (addedCount == null || addedCount == 0) {
            log.warn("[{}] INDOCNO: {} 触发, 但 Redis 中显示已推送过, 将跳过。", "PM_MONTH_ARCHIVED", indocno);
            // [JDK 1.8] 替换 Map.of()
            Map<String, Object> skippedResult = new HashMap<>();
            skippedResult.put("pushedCount", 0);
            skippedResult.put("pushedData", new ArrayList<>());
            skippedResult.put("message", "Skipped (already pushed)");
            return skippedResult;
        }
        redisTemplate.expire(redisKey, KEY_EXPIRATION);

        PmMonthDTO mainData = pmMonthMapper.findMainByIndocno(indocno);
        if (mainData == null) {
            log.error("[{}] INDOCNO: {} 触发, 但在 PM_MONTH 中未查询到数据!", "PM_MONTH_ARCHIVED", indocno);
            // [JDK 1.8] 替换 Map.of()
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("pushedCount", 0);
            errorResult.put("pushedData", new ArrayList<>());
            errorResult.put("message", "Error (Data not found)");
            return errorResult;
        }
        List<PmMonthItemDTO> items = pmMonthMapper.findItemsByIlinkno(indocno);
        mainData.setItems(items);
        log.info("[{}] INDOCNO: {} 触发, 成功查询到主表及 {} 条子项, 准备转换。", "PM_MONTH_ARCHIVED", indocno, items.size());

        List<ProductionHaltTaskDTO> tasksToPush = transformerService.transformPmMonthTasks(mainData);

        // 推送 (维修计划通常不大，不需要分批)
        producerService.sendMessage(pushPmMonthTopic, tasksToPush);
        log.info("成功推送 {} 条维修计划任务到 Kafka Topic: {}", tasksToPush.size(), pushPmMonthTopic);

        Map<String, Object> result = new HashMap<>();
        result.put("pushedCount", tasksToPush.size());
        result.put("pushedData", tasksToPush);
        result.put("message", "Success");
        return result;
    }

    /**
     * 5. EQ_PLANLB_ARCHIVED (轮保计划归档 - 触发器)
     * [已删除]
     * 此逻辑已于 2025-11-12 移除，并由 RotationalPlanPushJob 定时任务替代。
     */
    // public Map<String, Object> getAndFilterEqPlanLbData(Long indocno) { ... }


    /**
     * [重构] 6. PD_ZY_JM (专业/精密点检)
     *
     * @param taskId 异步任务ID，用于日志跟踪
     * @return 包含推送计数和数据的Map
     */
    public Map<String, Object> findAndPushNewPmissionTasks(String taskId) {
        List<PmissionDTO> recentTasks = pmissionMapper.findRecentTasks();
        if (recentTasks.isEmpty()) {
            log.info("[Task {}][PD_ZY_JM] 未发现近期任务。", taskId);
            // [JDK 1.8] 替换 Map.of()
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("pushedCount", 0);
            emptyResult.put("pushedData", new ArrayList<>());
            return emptyResult;
        }
        String redisKey = PUSHED_TASK_KEY_PREFIX + "pmission_zy_jm";

        List<PmissionDTO> newTasksToPush = recentTasks.stream()
                .filter(task -> {
                    Long addedCount = redisTemplate.opsForSet().add(redisKey, task.getIdocid().toString());
                    return addedCount != null && addedCount > 0;
                })
                .collect(Collectors.toList());

        if (!newTasksToPush.isEmpty()) {
            redisTemplate.expire(redisKey, KEY_EXPIRATION);
            log.info("[Task {}][PD_ZY_JM] 查询到 {} 条近期任务, 过滤后新增 {} 条。", taskId, recentTasks.size(), newTasksToPush.size());

            // 推送专业点检任务 (假设数量可控，不分批)
            producerService.sendMessage(pushPmissionZyJmTopic, newTasksToPush);
            log.info("[Task {}] 成功推送 {} 条专业/精密点检任务到 Kafka Topic: {}", taskId, newTasksToPush.size(), pushPmissionZyJmTopic);

        } else {
            log.info("[Task {}][PD_ZY_JM] 查询到 {} 条近期任务, 但全部已推送过。", taskId, recentTasks.size());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("pushedCount", newTasksToPush.size());
        result.put("pushedData", newTasksToPush);
        return result;
    }

    /**
     * 调试功能: 清空所有 Oracle 推送相关的 Redis 缓存
     * @return 被删除的 key 的集合
     */
    public Set<String> clearAllPushTaskCache() {
        log.warn("--- [调试] 正在清空所有 Oracle 推送缓存 ({}*) ---", PUSHED_TASK_KEY_PREFIX);

        Set<String> keys = redisTemplate.keys(PUSHED_TASK_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            log.warn("--- [调试] 未找到匹配的 Redis 键 ---");
            return new HashSet<>();
        }

        Long deleteCount = redisTemplate.delete(keys);
        log.warn("--- [调试] 成功删除 {} 个键: {} ---", deleteCount, keys);
        return keys;
    }
}