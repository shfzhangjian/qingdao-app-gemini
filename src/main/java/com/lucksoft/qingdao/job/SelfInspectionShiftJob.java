package com.lucksoft.qingdao.job;

import com.lucksoft.qingdao.selfinspection.service.SelfInspectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SelfInspectionShiftJob {

    private static final Logger log = LoggerFactory.getLogger(SelfInspectionShiftJob.class);

    @Autowired
    private SelfInspectionService siService;

    @Scheduled(cron = "0 0 7 * * ?")
    public void generateMorningShiftTasks() {
        log.info(">>> 定时任务触发: [早班] 自动生成");
        siService.runAutoGenerationJob("早班");
    }

    @Scheduled(cron = "0 0 15 * * ?")
    public void generateMiddleShiftTasks() {
        log.info(">>> 定时任务触发: [中班] 自动生成");
        siService.runAutoGenerationJob("中班");
    }

    @Scheduled(cron = "0 0 23 * * ?")
    public void generateNightShiftTasks() {
        log.info(">>> 定时任务触发: [夜班] 自动生成");
        siService.runAutoGenerationJob("夜班");
    }
}