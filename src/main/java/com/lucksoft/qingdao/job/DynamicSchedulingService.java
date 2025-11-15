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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * [新功能]
 * 动态调度服务。
 * 负责在应用启动时读取 time.json 文件，并根据 Cron 表达式注册定时任务。
 * 提供了 API 来热重载配置。
 */
@Service
public class DynamicSchedulingService {

    private static final Logger log = LoggerFactory.getLogger(DynamicSchedulingService.class);

    // 位于 JAR 包同级目录的配置文件名
    private static final String CONFIG_FILE = "time.json";

    @Autowired
    private TaskScheduler taskScheduler;

    @Autowired
    private UserEquipmentPushJob userEquipmentPushJob; // 注入包含业务逻辑的 Job

    @Autowired
    private ObjectMapper objectMapper;

    // 存储当前正在运行的定时任务，以便后续取消
    private final Map<String, ScheduledFuture<?>> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * 应用启动时执行一次，加载初始配置
     */
    @PostConstruct
    public void init() {
        log.info("动态调度服务初始化...");
        reloadSchedules();
    }

    /**
     * (公开) API调用的入口：更新配置并重新加载
     */
    public synchronized ScheduleDto updateAndReload(ScheduleDto newConfig) throws IOException {
        writeConfigToFile(newConfig);
        reloadSchedules();
        return newConfig;
    }

    /**
     * (公开) API调用的入口：读取当前配置
     */
    public synchronized ScheduleDto readConfig() throws IOException {
        return readConfigFromFile();
    }

    /**
     * 核心方法：重新加载所有定时任务
     */
    public synchronized void reloadSchedules() {
        // 1. 取消所有已存在的定时任务
        cancelAllTasks();

        // 2. 读取 time.json 配置文件
        ScheduleDto config = null;
        try {
            config = readConfigFromFile();
        } catch (IOException e) {
            log.error("读取 time.json 配置文件失败！将使用空配置。错误: {}", e.getMessage());
            config = new ScheduleDto();
            config.setCronExpressions(new ArrayList<>());
        }

        // 3. 打印并注册新的定时任务
        log.info("--- [动态调度器] 加载 time.json 配置 ---");
        if (config.getCronExpressions() == null || config.getCronExpressions().isEmpty()) {
            log.warn("[动态调度器] time.json 中未配置 'cronExpressions'，未启动任何定时推送任务。");
            return;
        }

        for (String cron : config.getCronExpressions()) {
            if (cron == null || cron.trim().isEmpty()) {
                continue;
            }

            try {
                // 创建一个 Cron 触发器
                CronTrigger trigger = new CronTrigger(cron);

                // 创建一个指向 Job 业务逻辑的任务
                Runnable task = () -> userEquipmentPushJob.runPushJob();

                // 调度任务
                ScheduledFuture<?> future = taskScheduler.schedule(task, trigger);

                // 存储任务句柄，以便将来取消
                String taskId = "PushJob-" + cron;
                scheduledTasks.put(taskId, future);

                // 打印日志（用户要求）
                log.info("[动态调度器] 已成功加载定时推送任务。 Cron: [{}]", cron);

            } catch (IllegalArgumentException e) {
                log.error("[动态调度器] Cron 表达式 '{}' 格式无效，跳过此任务。", cron, e);
            }
        }
        log.info("--- [动态调度器] 共加载了 {} 个定时推送任务 ---", scheduledTasks.size());
    }

    /**
     * 取消所有当前正在调度的任务
     */
    private void cancelAllTasks() {
        if (scheduledTasks.isEmpty()) {
            return;
        }
        log.info("正在取消 {} 个旧的定时推送任务...", scheduledTasks.size());
        scheduledTasks.forEach((id, task) -> {
            task.cancel(false); // false 表示不中断正在执行的任务，仅取消未来的调度
        });
        scheduledTasks.clear();
        log.info("所有旧任务已取消。");
    }

    /**
     * 从 time.json 读取配置
     */
    private ScheduleDto readConfigFromFile() throws IOException {
        Path path = Paths.get(CONFIG_FILE);

        if (!Files.exists(path)) {
            log.warn("配置文件 {} 不存在，将创建默认配置。", CONFIG_FILE);
            return createDefaultConfigFile(path);
        }

        log.debug("正在从 {} 读取配置...", path.toAbsolutePath());
        byte[] bytes = Files.readAllBytes(path);
        return objectMapper.readValue(bytes, ScheduleDto.class);
    }

    /**
     * 将配置写入 time.json
     */
    private void writeConfigToFile(ScheduleDto config) throws IOException {
        Path path = Paths.get(CONFIG_FILE);
        log.info("正在将新配置写入 {}...", path.toAbsolutePath());
        byte[] bytes = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(config);
        Files.write(path, bytes);
    }

    /**
     * 创建一个默认的 time.json 配置文件
     */
    private ScheduleDto createDefaultConfigFile(Path path) throws IOException {
        ScheduleDto defaultConfig = new ScheduleDto();
        // 默认配置为每天凌晨1点
        defaultConfig.setCronExpressions(java.util.Collections.singletonList("0 0 1 * * ?"));

        writeConfigToFile(defaultConfig);
        log.info("已创建默认配置文件: {}", path.toAbsolutePath());
        return defaultConfig;
    }
}