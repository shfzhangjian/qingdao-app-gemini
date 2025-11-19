package com.lucksoft.qingdao.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucksoft.qingdao.job.dto.ScheduleDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays; // Added import
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * [已更新] 动态调度服务。
 * 支持多种类型的定时任务配置。
 */
@Service
public class DynamicSchedulingService {

    private static final Logger log = LoggerFactory.getLogger(DynamicSchedulingService.class);

    // 位于 JAR 包同级目录的配置文件名
    private static final String CONFIG_FILE = "time.json";

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private UserEquipmentPushJob userEquipmentPushJob;

    @Autowired
    private MaintenanceTaskPushJob maintenanceTaskPushJob; // [新增] 注入新 Job

    @Autowired
    private ObjectMapper objectMapper;

    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("动态调度服务初始化...");
        reloadSchedules();
    }

    public synchronized ScheduleDto updateAndReload(ScheduleDto newConfig) throws IOException {
        writeConfigToFile(newConfig);
        reloadSchedules();
        return newConfig;
    }

    public synchronized ScheduleDto readConfig() throws IOException {
        return readConfigFromFile();
    }

    public synchronized void reloadSchedules() {
        cancelAllTasks();

        ScheduleDto config = null;
        try {
            config = readConfigFromFile();
        } catch (IOException e) {
            log.error("读取 time.json 配置文件失败！将使用空配置。错误: {}", e.getMessage());
            config = new ScheduleDto();
        }

        log.info("--- [动态调度器] 加载 time.json 配置 ---");

        // 1. 注册包机信息推送任务 (原有)
        registerTasks(config.getCronExpressions(), "UserEquipmentPush", () -> userEquipmentPushJob.runPushJob());

        // 2. [新增] 注册轮保任务生成推送任务
        registerTasks(config.getLbTaskCronExpressions(), "LbTaskPush", () -> maintenanceTaskPushJob.runJob());

        log.info("--- [动态调度器] 所有任务加载完毕, 当前活动任务数: {} ---", scheduledTasks.size());
    }

    /**
     * 辅助方法：批量注册同一类型的任务
     */
    private void registerTasks(List<String> crons, String taskType, Runnable runnable) {
        if (crons == null || crons.isEmpty()) {
            log.warn("[动态调度器] 未配置 '{}' 类型的 Cron 表达式。", taskType);
            return;
        }
        for (String cron : crons) {
            if (cron == null || cron.trim().isEmpty()) continue;
            try {
                CronTrigger trigger = new CronTrigger(cron);
                ScheduledFuture<?> future = taskScheduler.schedule(runnable, trigger);
                String taskId = taskType + "-" + cron + "-" + System.nanoTime(); // 确保唯一ID
                scheduledTasks.put(taskId, future);
                log.info("[动态调度器] 已注册任务 [{}], Cron: [{}]", taskType, cron);
            } catch (IllegalArgumentException e) {
                log.error("[动态调度器] 任务 [{}] 的 Cron 表达式 '{}' 无效。", taskType, cron, e);
            }
        }
    }

    private void cancelAllTasks() {
        if (scheduledTasks.isEmpty()) return;
        log.info("正在取消 {} 个旧的定时任务...", scheduledTasks.size());
        scheduledTasks.forEach((id, task) -> task.cancel(false));
        scheduledTasks.clear();
    }

    private ScheduleDto readConfigFromFile() throws IOException {
        Path path = Paths.get(CONFIG_FILE);
        if (!Files.exists(path)) {
            log.warn("配置文件 {} 不存在，将创建默认配置。", CONFIG_FILE);
            return createDefaultConfigFile(path);
        }
        byte[] bytes = Files.readAllBytes(path);
        return objectMapper.readValue(bytes, ScheduleDto.class);
    }

    private void writeConfigToFile(ScheduleDto config) throws IOException {
        Path path = Paths.get(CONFIG_FILE);
        byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(config);
        Files.write(path, bytes);
    }

    private ScheduleDto createDefaultConfigFile(Path path) throws IOException {
        ScheduleDto defaultConfig = new ScheduleDto();
        defaultConfig.setCronExpressions(Arrays.asList("0 0 1 * * ?"));
        // [新增] 默认配置
        defaultConfig.setLbTaskCronExpressions(Arrays.asList("0 30 6 * * ?", "0 30 14 * * ?", "0 30 22 * * ?"));
        writeConfigToFile(defaultConfig);
        return defaultConfig;
    }
}