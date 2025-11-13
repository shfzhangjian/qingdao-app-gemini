package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.dto.*;
import com.lucksoft.qingdao.oracle.mapper.*;
import com.lucksoft.qingdao.tspm.dto.FaultReportCodeFeedbackDTO;
import com.lucksoft.qingdao.tspm.dto.MaintenanceTaskDTO;
import com.lucksoft.qingdao.tspm.dto.ProductionHaltTaskDTO;
import com.lucksoft.qingdao.tspm.dto.RotationalPlanDTO;
import com.lucksoft.qingdao.tspm.dto.ScreenedRotationalTaskDTO;
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
 * [已重构]
 * 业务服务层，用于处理来自 Oracle API Controller 的数据查询和过滤。
 * 1. 依赖 `V_..._RECENT` 系列视图来统一查询。
 * 2. [关键] 包含“先推送Kafka，成功后再写入Redis”的防重逻辑。
 * 3. 包含 Kafka 消息分批推送逻辑。
 * 4. 本类所有方法都是同步的，由 AsyncTaskService 在后台线程中调用。
 */
@Service
public class OracleDataService {

    private static final Logger log = LoggerFactory.getLogger(OracleDataService.class);
    private static final String PUSHED_TASK_KEY_PREFIX = "oracle:pushed_tasks:";
    private static final Duration KEY_EXPIRATION = Duration.ofDays(1);

    // Kafka 消息批量推送的大小
    private static final int KAFKA_BATCH_SIZE = 500;

    // --- Mappers ---
    @Autowired
    private VMaintenanceTasksMapper vMaintenanceTasksMapper; // 接口 1
    @Autowired
    private VRotationalTaskMapper vRotationalTaskMapper; // 接口 7
    @Autowired
    private VFaultReportCodeMapper vFaultReportCodeMapper; // 接口 12
    @Autowired
    private PmMonthMapper pmMonthMapper; // 接口 13
    @Autowired
    private EqPlanLbMapper eqPlanLbMapper; // 接口 5
    @Autowired
    private PmissionMapper pmissionMapper; // 旧接口(专业点检)

    // --- Services ---
    @Autowired
    private OracleDataTransformerService transformerService;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private TspmProducerService producerService;

    // --- Kafka Topics ---
    @Value("${kafka.topics.sync-maintenance-task}")
    private String maintenanceTaskTopic; // 接口 1
    @Value("${kafka.topics.sync-rotational-plan}")
    private String syncRotationalPlanTopic; // 接口 5
    @Value("${kafka.topics.sync-rotational-task}")
    private String syncRotationalTaskTopic; // 接口 7
    @Value("${kafka.topics.receive-fault-report-code}")
    private String receiveFaultReportCodeTopic; // 接口 12
    @Value("${kafka.topics.sync-production-halt-maintenance-task}")
    private String syncProductionHaltMaintenanceTaskTopic; // 接口 13
    @Value("${kafka.topics.push-pmission-zy-jm}")
    private String pushPmissionZyJmTopic; // 旧接口(专业点检)


    /**
     * [重构] 接口 1: (保养任务) 查询、防重并推送所有新保养任务（来自视图）
     *
     * @param taskId 异步任务ID，用于日志跟踪
     * @return 包含推送计数和数据的Map
     * @throws Exception 如果 Kafka 推送失败
     */
    public Map<String, Object> findAndPushNewMaintenanceTasks(String taskId) throws Exception {
        // 1. 从统一视图查询所有近期任务
        List<VMaintenanceTaskDTO> recentTasks = vMaintenanceTasksMapper.findRecentTasks();
        if (recentTasks.isEmpty()) {
            log.info("[Task {}][MaintenanceTasks] V_MAINTENANCE_TASKS_RECENT 视图中未发现近期任务。", taskId);
            return createEmptyResult();
        }

        String redisKey = PUSHED_TASK_KEY_PREFIX + "maintenance_tasks_v2";
        List<MaintenanceTaskDTO> newTasksToPush = new ArrayList<>();
        Set<String> pushedDeDupeKeys = new HashSet<>();

        // 2. 过滤掉已经推送过的
        for (VMaintenanceTaskDTO vTask : recentTasks) {
            String deDupeKey = vTask.getDeDupeKey();
            if (deDupeKey == null || deDupeKey.isEmpty()) {
                log.warn("[Task {}] 跳过任务，因为 deDupeKey 为空: taskId={}", taskId, vTask.getTaskId());
                continue;
            }

            // [关键] 检查 Redis
            if (!redisTemplate.opsForSet().isMember(redisKey, deDupeKey)) {
                newTasksToPush.add(transformerService.transformVTaskToMaintenanceTask(vTask));
                pushedDeDupeKeys.add(deDupeKey); // 暂存准备推送的key
            }
        }

        // 3. [关键] 先推送 Kafka, 成功后再写入 Redis
        if (!newTasksToPush.isEmpty()) {
            log.info("[Task {}][MaintenanceTasks] 查询到 {} 条近期任务, 过滤后新增 {} 条。", taskId, recentTasks.size(), newTasksToPush.size());

            // 4. 分批推送到 Kafka (同步)
            pushTasksInBatches(taskId, maintenanceTaskTopic, newTasksToPush, KAFKA_BATCH_SIZE);

            // 5. [成功后] 批量写入 Redis
            log.info("[Task {}] Kafka 同步推送成功, 正在将 {} 个 deDupeKeys 写入 Redis...", taskId, pushedDeDupeKeys.size());
            redisTemplate.opsForSet().add(redisKey, pushedDeDupeKeys.toArray());
            redisTemplate.expire(redisKey, KEY_EXPIRATION);
            log.info("[Task {}] Redis 写入完毕。", taskId);

        } else {
            log.info("[Task {}][MaintenanceTasks] 查询到 {} 条近期任务, 但全部已推送过。", taskId, recentTasks.size());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("pushedCount", newTasksToPush.size());
        result.put("pushedData", newTasksToPush);
        return result;
    }

    /**
     * [重构] 接口 5: (轮保计划) 获取并过滤 EQ_PLANLB
     *
     * @param indocno 主键
     * @return 包含推送计数和数据的Map
     * @throws Exception 如果 Kafka 推送失败
     */
    public Map<String, Object> getAndFilterEqPlanLbData(Long indocno) throws Exception {
        String redisKey = PUSHED_TASK_KEY_PREFIX + "eq_planlb_v2";
        String indocnoStr = indocno.toString();

        // [关键] 检查 Redis
        if (redisTemplate.opsForSet().isMember(redisKey, indocnoStr)) {
            log.warn("[{}] INDOCNO: {} 触发, 但 Redis 中显示已推送过, 将跳过。", "EQ_PLANLB_ARCHIVED", indocno);
            return createSkippedResult();
        }

        EqPlanLbDTO mainData = eqPlanLbMapper.findMainByIndocno(indocno);
        if (mainData == null) {
            log.error("[{}] INDOCNO: {} 触发, 但在 EQ_PLANLB 中未查询到数据!", "EQ_PLANLB_ARCHIVED", indocno);
            return createErrorResult("Data not found");
        }
        List<EqPlanLbDtDTO> items = eqPlanLbMapper.findItemsByIlinkno(indocno);
        mainData.setItems(items);
        log.info("[{}] INDOCNO: {} 触发, 成功查询到主表及 {} 条子项, 准备转换。", "EQ_PLANLB_ARCHIVED", indocno, items.size());

        List<RotationalPlanDTO> plansToPush = transformerService.transformEqPlanLbTasks(mainData);

        // [关键] 先推送 Kafka, 成功后再写入 Redis
        if (!plansToPush.isEmpty()) {
            producerService.sendSync(syncRotationalPlanTopic, plansToPush);
            log.info("成功推送 {} 条轮保计划到 Kafka Topic: {}", plansToPush.size(), syncRotationalPlanTopic);
        }

        // 写入 Redis
        redisTemplate.opsForSet().add(redisKey, indocnoStr);
        redisTemplate.expire(redisKey, KEY_EXPIRATION);

        return createSuccessResult(plansToPush);
    }

    /**
     * [重构] 接口 7: (轮保任务) 查询、防重并推送所有新轮保任务（来自视图）
     *
     * @param taskId 异步任务ID，用于日志跟踪
     * @return 包含推送计数和数据的Map
     * @throws Exception 如果 Kafka 推送失败
     */
    public Map<String, Object> findAndPushNewRotationalTasks(String taskId) throws Exception {
        // 1. 从视图查询
        List<VRotationalTaskDTO> recentTasks = vRotationalTaskMapper.findRecentTasks();
        if (recentTasks.isEmpty()) {
            log.info("[Task {}][RotationalTasks] V_ROTATIONAL_TASK_RECENT 视图中未发现近期任务。", taskId);
            return createEmptyResult();
        }

        String redisKey = PUSHED_TASK_KEY_PREFIX + "rotational_tasks_v1";
        List<ScreenedRotationalTaskDTO> newTasksToPush = new ArrayList<>();
        Set<String> pushedDeDupeKeys = new HashSet<>();

        // 2. 过滤
        for (VRotationalTaskDTO vTask : recentTasks) {
            String deDupeKey = vTask.getDeDupeKey();
            if (deDupeKey == null || deDupeKey.isEmpty()) {
                log.warn("[Task {}] 跳过轮保任务，因为 deDupeKey 为空: taskId={}", taskId, vTask.getTaskId());
                continue;
            }
            if (!redisTemplate.opsForSet().isMember(redisKey, deDupeKey)) {
                newTasksToPush.add(transformerService.transformVTaskToRotationalTask(vTask));
                pushedDeDupeKeys.add(deDupeKey);
            }
        }

        // 3. 推送
        if (!newTasksToPush.isEmpty()) {
            log.info("[Task {}][RotationalTasks] 查询到 {} 条近期任务, 过滤后新增 {} 条。", taskId, recentTasks.size(), newTasksToPush.size());

            // 4. 分批推送到 Kafka (同步)
            pushTasksInBatches(taskId, syncRotationalTaskTopic, newTasksToPush, KAFKA_BATCH_SIZE);

            // 5. [成功后] 批量写入 Redis
            log.info("[Task {}] Kafka 同步推送成功, 正在将 {} 个 deDupeKeys 写入 Redis...", taskId, pushedDeDupeKeys.size());
            redisTemplate.opsForSet().add(redisKey, pushedDeDupeKeys.toArray());
            redisTemplate.expire(redisKey, KEY_EXPIRATION);
            log.info("[Task {}] Redis 写入完毕。", taskId);

        } else {
            log.info("[Task {}][RotationalTasks] 查询到 {} 条近期任务, 但全部已推送过。", taskId, recentTasks.size());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("pushedCount", newTasksToPush.size());
        result.put("pushedData", newTasksToPush);
        return result;
    }

    /**
     * [重构] 接口 12: (故障编码) 获取并推送故障报告编码
     *
     * @param timsId TIMS系统报告数据记录主键
     * @return 包含推送计数和数据的Map
     * @throws Exception 如果 Kafka 推送失败
     */
    public Map<String, Object> getAndFilterFaultReportCode(Integer timsId) throws Exception {
        String redisKey = PUSHED_TASK_KEY_PREFIX + "fault_report_code_v1";
        String timsIdStr = timsId.toString();

        if (redisTemplate.opsForSet().isMember(redisKey, timsIdStr)) {
            log.warn("[{}] TIMS_ID: {} 触发, 但 Redis 中显示已推送过, 将跳过。", "FAULT_REPORT_CODE", timsId);
            return createSkippedResult();
        }

        // 1. 从视图查询
        VFaultReportCodeDTO reportCodeDTO = vFaultReportCodeMapper.findByTimsId(timsId);
        if (reportCodeDTO == null) {
            log.error("[{}] TIMS_ID: {} 触发, 但在 V_TMIS_REPORT_CODE 视图中未查询到数据!", "FAULT_REPORT_CODE", timsId);
            return createErrorResult("Data not found");
        }
        log.info("[{}] TIMS_ID: {} 触发, 成功查询到数据, 准备转换。", "FAULT_REPORT_CODE", timsId);

        // 2. 转换
        FaultReportCodeFeedbackDTO dtoToPush = transformerService.transformVFaultReportCode(reportCodeDTO);

        // 3. 推送 (故障编码是单条，且在数组中)
        List<FaultReportCodeFeedbackDTO> listToPush = new ArrayList<>();
        listToPush.add(dtoToPush);

        producerService.sendSync(receiveFaultReportCodeTopic, listToPush);
        log.info("成功推送 1 条故障编码到 Kafka Topic: {}", receiveFaultReportCodeTopic);

        // 4. [成功后] 写入 Redis
        redisTemplate.opsForSet().add(redisKey, timsIdStr);
        redisTemplate.expire(redisKey, KEY_EXPIRATION);

        return createSuccessResult(listToPush);
    }

    /**
     * [重构] 接口 13: (停产检修) 获取并过滤 PM_MONTH (使用视图)
     *
     * @param indocno 主键
     * @return 包含推送计数和数据的Map
     * @throws Exception 如果 Kafka 推送失败
     */
    public Map<String, Object> getAndFilterPmMonthData(Long indocno) throws Exception {
        String redisKey = PUSHED_TASK_KEY_PREFIX + "pm_month_v2";
        String indocnoStr = indocno.toString();

        if (redisTemplate.opsForSet().isMember(redisKey, indocnoStr)) {
            log.warn("[{}] INDOCNO: {} 触发, 但 Redis 中显示已推送过, 将跳过。", "PM_MONTH_ARCHIVED", indocno);
            return createSkippedResult();
        }

        // [核心修改] 直接从视图查询，不再需要 DTO 转换
        List<ProductionHaltTaskDTO> tasksToPush = pmMonthMapper.findTasksFromViewByIndocno(indocno);
        if (tasksToPush == null || tasksToPush.isEmpty()) {
            log.error("[{}] INDOCNO: {} 触发, 但在 v_pm_month_item 视图中未查询到数据!", "PM_MONTH_ARCHIVED", indocno);
            return createErrorResult("Data not found in view v_pm_month_item");
        }
        log.info("[{}] INDOCNO: {} 触发, 成功从视图 v_pm_month_item 查询到 {} 条任务, 准备推送。", "PM_MONTH_ARCHIVED", indocno, tasksToPush.size());

        // [关键] 先推送 Kafka
        if (!tasksToPush.isEmpty()) {
            producerService.sendSync(syncProductionHaltMaintenanceTaskTopic, tasksToPush);
            log.info("成功推送 {} 条停产检修任务到 Kafka Topic: {}", tasksToPush.size(), syncProductionHaltMaintenanceTaskTopic);
        }

        // 4. [成功后] 写入 Redis
        redisTemplate.opsForSet().add(redisKey, indocnoStr);
        redisTemplate.expire(redisKey, KEY_EXPIRATION);

        return createSuccessResult(tasksToPush);
    }


    /**
     * [重构] (旧) PD_ZY_JM (专业/精密点检)
     *
     * @param taskId 异步任务ID，用于日志跟踪
     * @return 包含推送计数和数据的Map
     * @throws Exception 如果 Kafka 推送失败
     */
    public Map<String, Object> findAndPushNewPmissionTasks(String taskId) throws Exception {
        List<PmissionDTO> recentTasks = pmissionMapper.findRecentTasks();
        if (recentTasks.isEmpty()) {
            log.info("[Task {}][PD_ZY_JM] 未发现近期任务。", taskId);
            return createEmptyResult();
        }
        String redisKey = PUSHED_TASK_KEY_PREFIX + "pmission_zy_jm";

        Set<String> pushedTaskIds = new HashSet<>();
        List<PmissionDTO> newTasksToPush = recentTasks.stream()
                .filter(task -> {
                    String taskIdStr = task.getIdocid().toString();
                    if (!redisTemplate.opsForSet().isMember(redisKey, taskIdStr)) {
                        pushedTaskIds.add(taskIdStr);
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());

        if (!newTasksToPush.isEmpty()) {
            log.info("[Task {}][PD_ZY_JM] 查询到 {} 条近期任务, 过滤后新增 {} 条。", taskId, recentTasks.size(), newTasksToPush.size());

            // [关键] 先推送 Kafka
            producerService.sendSync(pushPmissionZyJmTopic, newTasksToPush);
            log.info("[Task {}] 成功推送 {} 条专业/精密点检任务到 Kafka Topic: {}", taskId, newTasksToPush.size(), pushPmissionZyJmTopic);

            // [成功后] 写入 Redis
            redisTemplate.opsForSet().add(redisKey, pushedTaskIds.toArray());
            redisTemplate.expire(redisKey, KEY_EXPIRATION);

        } else {
            log.info("[Task {}][PD_ZY_JM] 查询到 {} 条近期任务, 但全部已推送过。", taskId, recentTasks.size());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("pushedCount", newTasksToPush.size());
        result.put("pushedData", newTasksToPush);
        return result;
    }

    /**
     * [新] 辅助方法：分批同步推送到 Kafka
     * @param taskId 任务ID
     * @param topic Topic
     * @param tasksToPush 任务列表
     * @param batchSize 批次大小
     * @throws Exception 如果任何批次推送失败
     */
    private <T> void pushTasksInBatches(String taskId, String topic, List<T> tasksToPush, int batchSize) throws Exception {
        int totalTasks = tasksToPush.size();
        for (int i = 0; i < totalTasks; i += batchSize) {
            int end = Math.min(i + batchSize, totalTasks);
            List<T> batchList = tasksToPush.subList(i, end);

            log.info("[Task {}] 正在同步推送批次 {}/{} ({} 条任务)...", taskId, (i / batchSize) + 1, (totalTasks + batchSize - 1) / batchSize, batchList.size());

            // [关键] 调用同步发送
            producerService.sendSync(topic, batchList);
        }
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

    // --- [新] 辅助方法，用于创建标准响应 ---

    private Map<String, Object> createEmptyResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("pushedCount", 0);
        result.put("pushedData", new ArrayList<>());
        return result;
    }

    private Map<String, Object> createSkippedResult() {
        Map<String, Object> result = new HashMap<>();
        result.put("pushedCount", 0);
        result.put("pushedData", new ArrayList<>());
        result.put("message", "Skipped (already pushed)");
        return result;
    }

    private Map<String, Object> createErrorResult(String message) {
        Map<String, Object> result = new HashMap<>();
        result.put("pushedCount", 0);
        result.put("pushedData", new ArrayList<>());
        result.put("message", message);
        return result;
    }

    private Map<String, Object> createSuccessResult(List<?> data) {
        Map<String, Object> result = new HashMap<>();
        result.put("pushedCount", data.size());
        result.put("pushedData", data);
        result.put("message", "Success");
        return result;
    }
}