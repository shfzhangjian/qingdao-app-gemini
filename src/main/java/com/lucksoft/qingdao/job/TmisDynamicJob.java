package com.lucksoft.qingdao.job;

import com.lucksoft.qingdao.system.entity.TmisData;
import com.lucksoft.qingdao.system.mapper.TmisDataMapper;
import com.lucksoft.qingdao.tspm.service.TmisCompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * TMIS 动态任务调度器 (支持复合 Cron)
 */
@Component
public class TmisDynamicJob {

    private static final Logger log = LoggerFactory.getLogger(TmisDynamicJob.class);

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private TmisDataMapper tmisDataMapper;

    @Autowired
    private TmisCompensationService compensationService;

    // 存储已注册的任务 (Key: "topic-index")
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("=== [TMIS 动态调度器] 初始化 ===");
        reloadTasks();
    }

    /**
     * 重新加载所有任务
     */
    public synchronized void reloadTasks() {
        log.info("[TMIS 动态调度器] 正在重新加载所有任务...");

        cancelAllTasks();

        List<TmisData> configs = tmisDataMapper.findAllEnabled();
        if (configs == null || configs.isEmpty()) {
            log.info("[TMIS 动态调度器] 无启用的接口配置。");
            return;
        }

        int count = 0;
        for (TmisData config : configs) {
            String cronStr = config.getCronExpression();
            String topic = config.getTopic();

            if (cronStr == null || cronStr.trim().isEmpty()) {
                continue;
            }

            // [新增] 支持用 || 分隔的多个 Cron 表达式
            String[] crons = cronStr.split("\\|\\|");

            for (int i = 0; i < crons.length; i++) {
                String cron = crons[i].trim();
                if (cron.isEmpty()) continue;

                try {
                    Runnable task = () -> {
                        try {
                            compensationService.compensateTopic(config);
                        } catch (Exception e) {
                            log.error("任务执行异常: " + topic, e);
                        }
                    };

                    // 注册调度，使用唯一 key (topic + 索引)
                    ScheduledFuture<?> future = taskScheduler.schedule(task, new CronTrigger(cron));
                    scheduledTasks.put(topic + "-" + i, future);
                    log.info("已注册任务: [{}], Cron: [{}]", topic, cron);
                    count++;

                } catch (Exception e) {
                    log.error("注册任务失败: [{}] Cron: [{}]", topic, cron, e);
                }
            }
        }
        log.info("[TMIS 动态调度器] 加载完成，共注册 {} 个触发器。", count);
    }

    private void cancelAllTasks() {
        if (!scheduledTasks.isEmpty()) {
            scheduledTasks.forEach((key, future) -> {
                if (future != null) future.cancel(false);
            });
            scheduledTasks.clear();
        }
    }
}