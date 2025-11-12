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
 * [已重构 - JDK 1.8 兼容]
 * 业务服务层，用于处理来自 Oracle API Controller 的数据查询和过滤。
 * 1. 依赖 `V_..._RECENT` 系列视图来统一查询。
 * 2. 包含 Redis 防重逻辑。
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
     * 接口 1: (保养任务) 查询、防重并推送所有新保养任务（来自视图）
     * 由 SP_GENDAYTASK, PMBOARD.SP_QD_PLANBOARD_LB, JOB_GEN_BAOYANG_TASKS 触发
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
     * 接口 5: (轮保计划) 获取并过滤 EQ_PLANLB (轮保计划归档 - 触发器)
     * 由 EQ_PLANLB_ARCHIVED:{id} 触发
     *
     * @param indocno 主键
     * @return 包含推送计数和数据的Map
     */
    public Map<String, Object> getAndFilterEqPlanLbData(Long indocno) {
        String redisKey = PUSHED_TASK_KEY_PREFIX + "eq_planlb_v2"; // 使用 v2 key
        Long addedCount = redisTemplate.opsForSet().add(redisKey, indocno.toString());
        if (addedCount == null || addedCount == 0) {
            log.warn("[{}] INDOCNO: {} 触发, 但 Redis 中显示已推送过, 将跳过。", "EQ_PLANLB_ARCHIVED", indocno);
            Map<String, Object> skippedResult = new HashMap<>();
            skippedResult.put("pushedCount", 0);
            skippedResult.put("pushedData", new ArrayList<>());
            skippedResult.put("message", "Skipped (already pushed)");
            return skippedResult;
        }
        redisTemplate.expire(redisKey, KEY_EXPIRATION);

        EqPlanLbDTO mainData = eqPlanLbMapper.findMainByIndocno(indocno);
        if (mainData == null) {
            log.error("[{}] INDOCNO: {} 触发, 但在 EQ_PLANLB 中未查询到数据!", "EQ_PLANLB_ARCHIVED", indocno);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("pushedCount", 0);
            errorResult.put("pushedData", new ArrayList<>());
            errorResult.put("message", "Error (Data not found)");
            return errorResult;
        }
        List<EqPlanLbDtDTO> items = eqPlanLbMapper.findItemsByIlinkno(indocno);
        mainData.setItems(items);
        log.info("[{}] INDOCNO: {} 触发, 成功查询到主表及 {} 条子项, 准备转换。", "EQ_PLANLB_ARCHIVED", indocno, items.size());

        List<RotationalPlanDTO> plansToPush = transformerService.transformEqPlanLbTasks(mainData);

        // 推送 (轮保计划通常不大，不需要分批)
        producerService.sendMessage(syncRotationalPlanTopic, plansToPush);
        log.info("成功推送 {} 条轮保计划到 Kafka Topic: {}", plansToPush.size(), syncRotationalPlanTopic);

        Map<String, Object> result = new HashMap<>();
        result.put("pushedCount", plansToPush.size());
        result.put("pushedData", plansToPush);
        result.put("message", "Success");
        return result;
    }

    /**
     * [新增] 接口 7: (轮保任务) 查询、防重并推送所有新轮保任务（来自视图）
     * 由 TIMS_PUSH_ROTATIONAL_TASK 触发
     *
     * @param taskId 异步任务ID，用于日志跟踪
     * @return 包含推送计数和数据的Map
     */
    public Map<String, Object> findAndPushNewRotationalTasks(String taskId) {
        // 1. 从视图查询
        List<VRotationalTaskDTO> recentTasks = vRotationalTaskMapper.findRecentTasks();
        if (recentTasks.isEmpty()) {
            log.info("[Task {}][RotationalTasks] V_ROTATIONAL_TASK_RECENT 视图中未发现近期任务。", taskId);
            Map<String, Object> emptyResult = new HashMap<>();
            emptyResult.put("pushedCount", 0);
            emptyResult.put("pushedData", new ArrayList<>());
            return emptyResult;
        }

        String redisKey = PUSHED_TASK_KEY_PREFIX + "rotational_tasks_v1";
        List<ScreenedRotationalTaskDTO> newTasksToPush = new ArrayList<>();

        // 2. 过滤
        for (VRotationalTaskDTO vTask : recentTasks) {
            String deDupeKey = vTask.getDeDupeKey();
            if (deDupeKey == null || deDupeKey.isEmpty()) {
                log.warn("[Task {}] 跳过轮保任务，因为 deDupeKey 为空: taskId={}", taskId, vTask.getTaskId());
                continue;
            }
            Long addedCount = redisTemplate.opsForSet().add(redisKey, deDupeKey);
            if (addedCount != null && addedCount > 0) {
                newTasksToPush.add(transformerService.transformVTaskToRotationalTask(vTask));
            }
        }

        // 3. 推送
        if (!newTasksToPush.isEmpty()) {
            redisTemplate.expire(redisKey, KEY_EXPIRATION);
            log.info("[Task {}][RotationalTasks] 查询到 {} 条近期任务, 过滤后新增 {} 条。", taskId, recentTasks.size(), newTasksToPush.size());

            // 4. 分批推送到 Kafka
            int totalTasks = newTasksToPush.size();
            for (int i = 0; i < totalTasks; i += KAFKA_BATCH_SIZE) {
                int end = Math.min(i + KAFKA_BATCH_SIZE, totalTasks);
                List<ScreenedRotationalTaskDTO> batchList = newTasksToPush.subList(i, end);
                log.info("[Task {}] 正在推送轮保任务批次 {}/{} ({} 条任务)...", taskId, (i / KAFKA_BATCH_SIZE) + 1, (totalTasks + KAFKA_BATCH_SIZE - 1) / KAFKA_BATCH_SIZE, batchList.size());
                producerService.sendMessage(syncRotationalTaskTopic, batchList);
            }
            log.info("[Task {}] 成功分批推送 {} 条新轮保任务到 Kafka Topic: {}", taskId, totalTasks, syncRotationalTaskTopic);

        } else {
            log.info("[Task {}][RotationalTasks] 查询到 {} 条近期任务, 但全部已推送过。", taskId, recentTasks.size());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("pushedCount", newTasksToPush.size());
        result.put("pushedData", newTasksToPush);
        return result;
    }

    /**
     * [新增] 接口 12: (故障编码) 获取并推送故障报告编码
     * 由 TIMS_PUSH_FAULT_REPORT_CODE:{id} 触发
     *
     * @param timsId TIMS系统报告数据记录主键
     * @return 包含推送计数和数据的Map
     */
    public Map<String, Object> getAndFilterFaultReportCode(Integer timsId) {
        String redisKey = PUSHED_TASK_KEY_PREFIX + "fault_report_code_v1";
        Long addedCount = redisTemplate.opsForSet().add(redisKey, timsId.toString());
        if (addedCount == null || addedCount == 0) {
            log.warn("[{}] TIMS_ID: {} 触发, 但 Redis 中显示已推送过, 将跳过。", "FAULT_REPORT_CODE", timsId);
            Map<String, Object> skippedResult = new HashMap<>();
            skippedResult.put("pushedCount", 0);
            skippedResult.put("pushedData", new ArrayList<>());
            skippedResult.put("message", "Skipped (already pushed)");
            return skippedResult;
        }
        redisTemplate.expire(redisKey, KEY_EXPIRATION);

        // 1. 从视图查询
        VFaultReportCodeDTO reportCodeDTO = vFaultReportCodeMapper.findByTimsId(timsId);
        if (reportCodeDTO == null) {
            log.error("[{}] TIMS_ID: {} 触发, 但在 V_TMIS_REPORT_CODE 视图中未查询到数据!", "FAULT_REPORT_CODE", timsId);
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("pushedCount", 0);
            errorResult.put("pushedData", new ArrayList<>());
            errorResult.put("message", "Error (Data not found)");
            return errorResult;
        }
        log.info("[{}] TIMS_ID: {} 触发, 成功查询到数据, 准备转换。", "FAULT_REPORT_CODE", timsId);

        // 2. 转换
        FaultReportCodeFeedbackDTO dtoToPush = transformerService.transformVFaultReportCode(reportCodeDTO);

        // 3. 推送 (故障编码是单条，且在数组中)
        List<FaultReportCodeFeedbackDTO> listToPush = new ArrayList<>();
        listToPush.add(dtoToPush);

        producerService.sendMessage(receiveFaultReportCodeTopic, listToPush);
        log.info("成功推送 1 条故障编码到 Kafka Topic: {}", receiveFaultReportCodeTopic);

        Map<String, Object> result = new HashMap<>();
        result.put("pushedCount", 1);
        result.put("pushedData", listToPush);
        result.put("message", "Success");
        return result;
    }

    /**
     * 接口 13: (停产检修) 获取并过滤 PM_MONTH (维修计划归档 - 触发器)
     * 由 PM_MONTH_ARCHIVED:{id} 触发
     *
     * @param indocno 主键
     * @return 包含推送计数和数据的Map
     */
    public Map<String, Object> getAndFilterPmMonthData(Long indocno) {
        String redisKey = PUSHED_TASK_KEY_PREFIX + "pm_month_v2"; // v2 key
        Long addedCount = redisTemplate.opsForSet().add(redisKey, indocno.toString());
        if (addedCount == null || addedCount == 0) {
            log.warn("[{}] INDOCNO: {} 触发, 但 Redis 中显示已推送过, 将跳过。", "PM_MONTH_ARCHIVED", indocno);
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
        producerService.sendMessage(syncProductionHaltMaintenanceTaskTopic, tasksToPush);
        log.info("成功推送 {} 条停产检修任务到 Kafka Topic: {}", tasksToPush.size(), syncProductionHaltMaintenanceTaskTopic);

        Map<String, Object> result = new HashMap<>();
        result.put("pushedCount", tasksToPush.size());
        result.put("pushedData", tasksToPush);
        result.put("message", "Success");
        return result;
    }


    /**
     * (旧) PD_ZY_JM (专业/精密点检) - [已废弃, 由接口7替代]
     *
     * @param taskId 异步任务ID，用于日志跟踪
     * @return 包含推送计数和数据的Map
     */
    public Map<String, Object> findAndPushNewPmissionTasks(String taskId) {
        List<PmissionDTO> recentTasks = pmissionMapper.findRecentTasks();
        if (recentTasks.isEmpty()) {
            log.info("[Task {}][PD_ZY_JM] 未发现近期任务。", taskId);
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