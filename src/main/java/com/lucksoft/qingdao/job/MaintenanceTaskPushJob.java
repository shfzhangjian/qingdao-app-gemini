package com.lucksoft.qingdao.job;

import com.lucksoft.qingdao.oracle.service.OracleDataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * [新功能] 轮保任务定时生成与推送 Job
 */
@Component
public class MaintenanceTaskPushJob {

    private static final Logger log = LoggerFactory.getLogger(MaintenanceTaskPushJob.class);

    @Autowired
    private OracleDataService oracleDataService;

    public void runJob() {
        log.info("--- [定时任务] 开始执行：生成并推送轮保任务 (tmis.genlb -> view_lb_task) ---");
        try {
            oracleDataService.generateAndPushLbTasks();
        } catch (Exception e) {
            log.error("[定时任务] 轮保任务生成推送失败: {}", e.getMessage(), e);
        }
        log.info("--- [定时任务] 轮保任务流程结束 ---");
    }
}