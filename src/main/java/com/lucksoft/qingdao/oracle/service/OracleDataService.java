package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.dto.PmissionBoardBaoYangDTO;
import com.lucksoft.qingdao.oracle.dto.PmissionBoardDTO;
import com.lucksoft.qingdao.oracle.dto.PmissionBoardDayDTO;
import com.lucksoft.qingdao.oracle.dto.PmMonthDTO;
import com.lucksoft.qingdao.oracle.dto.PmMonthItemDTO;
import com.lucksoft.qingdao.oracle.mapper.PmissionBoardBaoYangMapper;
import com.lucksoft.qingdao.oracle.mapper.PmissionBoardMapper;
import com.lucksoft.qingdao.oracle.mapper.PmissionBoardDayMapper;
import com.lucksoft.qingdao.oracle.mapper.PmMonthMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 业务服务层，用于处理来自 Oracle 的数据并执行防重检查
 */
@Service
public class OracleDataService {

    private static final Logger log = LoggerFactory.getLogger(OracleDataService.class);

    @Autowired
    private PmissionBoardDayMapper pmissionBoardDayMapper;

    @Autowired
    private PmissionBoardMapper pmissionBoardMapper;

    @Autowired
    private PmissionBoardBaoYangMapper pmissionBoardBaoYangMapper;

    @Autowired
    private PmMonthMapper pmMonthMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // Redis 键的统一定义
    private static final String REDIS_KEY_PMISSION_DAY = "oracle:pushed_tasks:pmission_day";
    private static final String REDIS_KEY_PMISSION_LB = "oracle:pushed_tasks:pmissionboard_lb";
    private static final String REDIS_KEY_PMISSION_BAOYANG = "oracle:pushed_tasks:pmissionbaoyang";
    private static final String REDIS_KEY_PM_MONTH = "oracle:pushed_tasks:pm_month";


    /**
     * 1. 获取并过滤 SP_GENDAYTASK (PMISSIONBOARDDAY) 的新任务
     * @return 仅包含未推送过的新任务的列表
     */
    public List<PmissionBoardDayDTO> getAndFilterNewDayTasks() {
        // 1. 从数据库获取最近5分钟的任务
        List<PmissionBoardDayDTO> recentTasks = pmissionBoardDayMapper.findRecentTasks();
        if (recentTasks.isEmpty()) {
            return recentTasks; // 返回空列表
        }

        log.info("[PMISSIONBOARDDAY] 从 DB 查询到 {} 条最近的任务，开始 Redis 防重过滤...", recentTasks.size());
        List<PmissionBoardDayDTO> newTasks = new ArrayList<>();

        for (PmissionBoardDayDTO task : recentTasks) {
            String taskId = String.valueOf(task.getIdocid());

            // 2. 尝试将 ID 添加到 Redis Set
            // [V2 修复] SADD 返回 Long, 1L=成功, 0L=已存在
            Long addedCount = redisTemplate.opsForSet().add(REDIS_KEY_PMISSION_DAY, taskId);

            // 3. 仅当 ID 是新添加的时，才将其加入返回列表
            if (addedCount != null && addedCount > 0) {
                newTasks.add(task);
                // 设置一个过期时间 (例如 1 天)，防止 Redis Set 无限增大
                redisTemplate.expire(REDIS_KEY_PMISSION_DAY, 1, TimeUnit.DAYS);
            }
        }

        log.info("[PMISSIONBOARDDAY] 过滤后，发现 {} 条新任务需要推送。", newTasks.size());
        return newTasks;
    }

    /**
     * 2. 获取并过滤 SP_QD_PLANBOARD_LB (PMISSIONBOARD) 的新任务
     * @return 仅包含未推送过的新任务的列表
     */
    public List<PmissionBoardDTO> getAndFilterNewPmissionBoardTasks() {
        List<PmissionBoardDTO> recentTasks = pmissionBoardMapper.findRecentTasks();
        if (recentTasks.isEmpty()) {
            return recentTasks;
        }

        log.info("[PMISSIONBOARD] 从 DB 查询到 {} 条最近的任务，开始 Redis 防重过滤...", recentTasks.size());
        List<PmissionBoardDTO> newTasks = new ArrayList<>();

        for (PmissionBoardDTO task : recentTasks) {
            String taskId = String.valueOf(task.getIdocid());
            Long addedCount = redisTemplate.opsForSet().add(REDIS_KEY_PMISSION_LB, taskId);

            if (addedCount != null && addedCount > 0) {
                newTasks.add(task);
                redisTemplate.expire(REDIS_KEY_PMISSION_LB, 1, TimeUnit.DAYS);
            }
        }

        log.info("[PMISSIONBOARD] 过滤后，发现 {} 条新任务需要推送。", newTasks.size());
        return newTasks;
    }

    /**
     * 3. 获取并过滤 JOB_GENERATE_ALL_BAOYANG_TASKS (PMISSIONBOARDBAOYANG) 的新任务
     * @return 仅包含未推送过的新任务的列表
     */
    public List<PmissionBoardBaoYangDTO> getAndFilterNewPmissionBoardBaoYangTasks() {
        List<PmissionBoardBaoYangDTO> recentTasks = pmissionBoardBaoYangMapper.findRecentTasks();
        if (recentTasks.isEmpty()) {
            return recentTasks;
        }

        log.info("[PMISSIONBOARDBAOYANG] 从 DB 查询到 {} 条最近的任务，开始 Redis 防重过滤...", recentTasks.size());
        List<PmissionBoardBaoYangDTO> newTasks = new ArrayList<>();

        for (PmissionBoardBaoYangDTO task : recentTasks) {
            // [修改] 使用 IIDCID (表主键) 作为幂等性判断依据
            String taskId = String.valueOf(task.getIidc());
            Long addedCount = redisTemplate.opsForSet().add(REDIS_KEY_PMISSION_BAOYANG, taskId);

            if (addedCount != null && addedCount > 0) {
                newTasks.add(task);
                redisTemplate.expire(REDIS_KEY_PMISSION_BAOYANG, 1, TimeUnit.DAYS);
            }
        }

        log.info("[PMISSIONBOARDBAOYANG] 过滤后，发现 {} 条新任务需要推送。", newTasks.size());
        return newTasks;
    }

    /**
     * 4. 获取并过滤 PM_MONTH (维修计划) 的数据
     * @param indocno 要查询的主表 ID
     * @return 组装好的主从 DTO，如果需要推送 (Redis 检查为新)；否则返回 null
     */
    public PmMonthDTO getAndFilterPmMonthData(Long indocno) {
        if (indocno == null) {
            return null;
        }

        String taskId = String.valueOf(indocno);
        String redisKey = REDIS_KEY_PM_MONTH;

        // 1. 检查 Redis 幂等性
        Long addedCount = redisTemplate.opsForSet().add(redisKey, taskId);
        if (addedCount == null || addedCount <= 0) {
            log.warn("[PM_MONTH] 维修计划 ID: {} 已被推送过，跳过。", indocno);
            return null; // 已存在，不再推送
        }

        // 2. 设置 Redis Key 过期时间
        redisTemplate.expire(redisKey, 90, TimeUnit.DAYS); // 维修计划的归档推送周期可能很长，给90天

        // 3. 从数据库查询主表
        log.info("[PM_MONTH] 发现新归档的维修计划 ID: {}，开始从数据库查询...", indocno);
        PmMonthDTO mainData = pmMonthMapper.findMainByIndocno(indocno);
        if (mainData == null) {
            log.error("[PM_MONTH] 收到 ID: {} 的归档通知，但在数据库中未找到该主记录！", indocno);
            return null;
        }

        // 4. 从数据库查询子表
        List<PmMonthItemDTO> items = pmMonthMapper.findItemsByIlinkno(indocno);

        // 5. 组装 DTO
        mainData.setItems(items);
        log.info("[PM_MONTH] 查询成功，主表记录 1 条，明细 {} 条。准备推送。", items.size());

        return mainData;
    }

}