package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.dto.*;
import com.lucksoft.qingdao.oracle.mapper.*;
import com.lucksoft.qingdao.tspm.dto.MaintenanceTaskDTO;
import com.lucksoft.qingdao.tspm.dto.ProductionHaltTaskDTO;
import com.lucksoft.qingdao.tspm.dto.RotationalPlanDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 业务服务层，用于处理来自 Oracle API Controller 的数据查询和过滤
 */
@Service
public class OracleDataService {

    private static final Logger log = LoggerFactory.getLogger(OracleDataService.class);

    // Redis key 的前缀
    private static final String PUSHED_TASK_KEY_PREFIX = "oracle:pushed_tasks:";
    // Redis key 的过期时间 (1 天)
    private static final Duration KEY_EXPIRATION = Duration.ofDays(1);

    @Autowired
    private PmissionBoardDayMapper pmissionBoardDayMapper;

    @Autowired
    private PmissionBoardMapper pmissionBoardMapper;

    @Autowired
    private PmissionBoardBaoYangMapper pmissionBoardBaoYangMapper;

    @Autowired
    private PmMonthMapper pmMonthMapper;

    @Autowired
    private EqPlanLbMapper eqPlanLbMapper;

    @Autowired
    private PmissionMapper pmissionMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * [新增] 注入转换服务
     */
    @Autowired
    private OracleDataTransformerService transformerService;


    /**
     * 1. SP_GENDAYTASK (精益日保)
     * 获取、过滤并**转换**新生成的任务
     * @return 仅包含新任务的 **MaintenanceTaskDTO** 列表
     */
    public List<MaintenanceTaskDTO> getAndFilterNewDayTasks() {
        // 1. 从数据库查询过去5分钟内的所有任务
        List<PmissionBoardDayDTO> recentTasks = pmissionBoardDayMapper.findRecentTasks();
        if (recentTasks.isEmpty()) {
            return new ArrayList<>();
        }

        String redisKey = PUSHED_TASK_KEY_PREFIX + "pmissionday";

        // 2. 过滤掉已经推送过的
        List<PmissionBoardDayDTO> newOracleTasks = new ArrayList<>();
        for (PmissionBoardDayDTO task : recentTasks) {
            // SADD 命令 (add) 是原子的。
            // 如果 ID 是新的, 返回 1 (Long); 如果 ID 已存在, 返回 0 (Long)
            Long addedCount = redisTemplate.opsForSet().add(redisKey, task.getIdocid().toString());

            if (addedCount != null && addedCount > 0) {
                // 这是一个新任务
                newOracleTasks.add(task);
            }
        }

        // 3. 如果我们添加了新任务, 就刷新 Key 的过期时间
        if (!newOracleTasks.isEmpty()) {
            redisTemplate.expire(redisKey, KEY_EXPIRATION);
        }

        log.info("[{}] 查询到 {} 条近期任务, 过滤后新增 {} 条。", "SP_GENDAYTASK", recentTasks.size(), newOracleTasks.size());

        // 4. [修改] 转换 DTO
        return newOracleTasks.stream()
                .map(transformerService::transformDayTask)
                .collect(Collectors.toList());
    }

    /**
     * 2. SP_QD_PLANBOARD_LB (轮保/月保)
     * 获取、过滤并**转换**新生成的任务
     * @return 仅包含新任务的 **MaintenanceTaskDTO** 列表
     */
    public List<MaintenanceTaskDTO> getAndFilterNewPmissionBoardTasks() {
        List<PmissionBoardDTO> recentTasks = pmissionBoardMapper.findRecentTasks();
        if (recentTasks.isEmpty()) {
            return new ArrayList<>();
        }

        String redisKey = PUSHED_TASK_KEY_PREFIX + "pmissionboard_lb";

        List<PmissionBoardDTO> newOracleTasks = recentTasks.stream()
                .filter(task -> {
                    Long addedCount = redisTemplate.opsForSet().add(redisKey, task.getIdocid().toString());
                    return addedCount != null && addedCount > 0;
                })
                .collect(Collectors.toList());

        if (!newOracleTasks.isEmpty()) {
            redisTemplate.expire(redisKey, KEY_EXPIRATION);
        }

        log.info("[{}] 查询到 {} 条近期任务, 过滤后新增 {} 条。", "SP_QD_PLANBOARD_LB", recentTasks.size(), newOracleTasks.size());

        // [修改] 转换 DTO
        return newOracleTasks.stream()
                .map(transformerService::transformBoardLbTask)
                .collect(Collectors.toList());
    }


    /**
     * 3. JOB_GEN_BAOYANG_TASKS (例保)
     * 获取、过滤并**转换**新生成的任务
     * @return 仅包含新任务的 **MaintenanceTaskDTO** 列表
     */
    public List<MaintenanceTaskDTO> getAndFilterNewPmissionBoardBaoYangTasks() {
        List<PmissionBoardBaoYangDTO> recentTasks = pmissionBoardBaoYangMapper.findRecentTasks();
        if (recentTasks.isEmpty()) {
            return new ArrayList<>();
        }

        String redisKey = PUSHED_TASK_KEY_PREFIX + "pmissionbaoyang";

        // 注意: 例保任务使用 IIDCID 作为Redis防重键, IDOCID (任务ID) 作为Kafka推送内容
        List<PmissionBoardBaoYangDTO> newOracleTasks = recentTasks.stream()
                .filter(task -> {
                    Long addedCount = redisTemplate.opsForSet().add(redisKey, task.getIidc().toString());
                    return addedCount != null && addedCount > 0;
                })
                .collect(Collectors.toList());

        if (!newOracleTasks.isEmpty()) {
            redisTemplate.expire(redisKey, KEY_EXPIRATION);
        }

        log.info("[{}] 查询到 {} 条近期任务, 过滤后新增 {} 条。", "JOB_GEN_BAOYANG_TASKS", recentTasks.size(), newOracleTasks.size());

        // [修改] 转换 DTO
        return newOracleTasks.stream()
                .map(transformerService::transformBaoYangTask)
                .collect(Collectors.toList());
    }

    /**
     * 4. PM_MONTH_ARCHIVED (维修计划归档 - 触发器)
     * 获取、过滤并**转换** PM_MONTH + PM_MONTH_ITEM 的主从数据
     * @param indocno 触发器传入的主键 ID
     * @return 包含 **ProductionHaltTaskDTO** 的列表 (如果它是新的), 否则返回空列表
     */
    public List<ProductionHaltTaskDTO> getAndFilterPmMonthData(Long indocno) {
        String redisKey = PUSHED_TASK_KEY_PREFIX + "pm_month";

        // 1. 检查 Redis 中是否已推送过
        Long addedCount = redisTemplate.opsForSet().add(redisKey, indocno.toString());
        if (addedCount == null || addedCount == 0) {
            log.warn("[{}] INDOCNO: {} 触发, 但 Redis 中显示已推送过, 将跳过。", "PM_MONTH_ARCHIVED", indocno);
            return new ArrayList<>(); // [修改] 返回空列表
        }

        // 2. 这是一个新 ID, 刷新过期时间
        redisTemplate.expire(redisKey, KEY_EXPIRATION);

        // 3. 查询主表
        PmMonthDTO mainData = pmMonthMapper.findMainByIndocno(indocno);
        if (mainData == null) {
            log.error("[{}] INDOCNO: {} 触发, 但在 PM_MONTH 中未查询到数据!", "PM_MONTH_ARCHIVED", indocno);
            return new ArrayList<>(); // [修改] 返回空列表
        }

        // 4. 查询子表并组合
        List<PmMonthItemDTO> items = pmMonthMapper.findItemsByIlinkno(indocno);
        mainData.setItems(items);

        log.info("[{}] INDOCNO: {} 触发, 成功查询到主表及 {} 条子项, 准备推送。", "PM_MONTH_ARCHIVED", indocno, items.size());

        // 5. [修改] 转换 DTO
        return transformerService.transformPmMonthTasks(mainData);
    }

    /**
     * 5. EQ_PLANLB_ARCHIVED (轮保计划归档 - 触发器)
     * 获取、过滤并**转换** EQ_PLANLB + EQ_PLANLBDT 的主从数据
     * @param indocno 触发器传入的主键 ID
     * @return 包含 **RotationalPlanDTO** 的列表 (如果它是新的), 否则返回空列表
     */
    public List<RotationalPlanDTO> getAndFilterEqPlanLbData(Long indocno) {
        String redisKey = PUSHED_TASK_KEY_PREFIX + "eq_planlb";

        // 1. 检查 Redis 中是否已推送过
        Long addedCount = redisTemplate.opsForSet().add(redisKey, indocno.toString());
        if (addedCount == null || addedCount == 0) {
            log.warn("[{}] INDOCNO: {} 触发, 但 Redis 中显示已推送过, 将跳过。", "EQ_PLANLB_ARCHIVED", indocno);
            return new ArrayList<>(); // [修改] 返回空列表
        }

        // 2. 这是一个新 ID, 刷新过期时间
        redisTemplate.expire(redisKey, KEY_EXPIRATION);

        // 3. 查询主表
        EqPlanLbDTO mainData = eqPlanLbMapper.findMainByIndocno(indocno);
        if (mainData == null) {
            log.error("[{}] INDOCNO: {} 触发, 但在 EQ_PLANLB 中未查询到数据!", "EQ_PLANLB_ARCHIVED", indocno);
            return new ArrayList<>(); // [修改] 返回空列表
        }

        // 4. 查询子表并组合
        List<EqPlanLbDtDTO> items = eqPlanLbMapper.findItemsByIlinkno(indocno);
        mainData.setItems(items);

        log.info("[{}] INDOCNO: {} 触发, 成功查询到主表及 {} 条子项, 准备推送。", "EQ_PLANLB_ARCHIVED", indocno, items.size());

        // 5. [修改] 转换 DTO
        return transformerService.transformEqPlanLbTasks(mainData);
    }


    /**
     * 6. PD_ZY_JM (专业/精密点检)
     * 获取并过滤（防重） PMISSION 中新生成的任务
     * @return 仅包含新任务的 DTO 列表
     */
    public List<PmissionDTO> getAndFilterNewPmissionTasks() {
        List<PmissionDTO> recentTasks = pmissionMapper.findRecentTasks();
        if (recentTasks.isEmpty()) {
            return new ArrayList<>();
        }

        String redisKey = PUSHED_TASK_KEY_PREFIX + "pmission_zy_jm";

        List<PmissionDTO> newTasks = recentTasks.stream()
                .filter(task -> {
                    // 使用任务 ID (IDOCID) 进行防重
                    Long addedCount = redisTemplate.opsForSet().add(redisKey, task.getIdocid().toString());
                    return addedCount != null && addedCount > 0;
                })
                .collect(Collectors.toList());

        if (!newTasks.isEmpty()) {
            redisTemplate.expire(redisKey, KEY_EXPIRATION);
        }

        log.info("[{}] 查询到 {} 条近期任务, 过滤后新增 {} 条。", "PD_ZY_JM", recentTasks.size(), newTasks.size());
        return newTasks;
    }

    /**
     * [新增] 调试功能: 清空所有 Oracle 推送相关的 Redis 缓存
     * @return 被删除的 key 的集合
     */
    public Set<String> clearAllPushTaskCache() {
        log.warn("--- [调试] 正在清空所有 Oracle 推送缓存 ({}*) ---", PUSHED_TASK_KEY_PREFIX);

        // 查找所有匹配的 key
        Set<String> keys = redisTemplate.keys(PUSHED_TASK_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            log.warn("--- [调试] 未找到匹配的 Redis 键 ---");
            return new HashSet<>();
        }

        // 删除所有找到的 key
        Long deleteCount = redisTemplate.delete(keys);
        log.warn("--- [调试] 成功删除 {} 个键: {} ---", deleteCount, keys);
        return keys;
    }
}