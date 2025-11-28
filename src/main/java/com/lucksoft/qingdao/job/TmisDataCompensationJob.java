package com.lucksoft.qingdao.job;

import com.lucksoft.qingdao.system.entity.TmisData;
import com.lucksoft.qingdao.system.mapper.TmisDataMapper;
import com.lucksoft.qingdao.tspm.service.TmisCompensationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 每天定时执行数据补漏任务
 * 负责遍历 TMIS_DATA 配置表，触发 TmisCompensationService 进行异步数据同步。
 */
@Component
public class TmisDataCompensationJob {

    private static final Logger log = LoggerFactory.getLogger(TmisDataCompensationJob.class);

    @Autowired
    private TmisDataMapper tmisDataMapper;

    @Autowired
    private TmisCompensationService compensationService;

    /**
     * 每天凌晨 02:00 执行
     * Cron 表达式: 秒 分 时 日 月 周
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void runCompensationJob() {
        log.info("=== [定时补漏] 开始执行数据遗漏检查任务 ===");

        try {
            // 1. 获取所有启用的接口配置
            List<TmisData> configs = tmisDataMapper.findAllEnabled();

            if (configs == null || configs.isEmpty()) {
                log.info("[定时补漏] 未发现启用的接口配置，任务结束。");
                return;
            }

            log.info("[定时补漏] 共发现 {} 个启用配置，准备进行异步调度...", configs.size());

            // 2. 遍历配置，为每个主题启动异步处理
            // 注意：compensationService.compensateTopic 被 @Async 注解修饰
            // 所以这里的循环调用是非阻塞的，会迅速完成，实际的 HTTP 请求和数据处理将在线程池中并发执行。
            for (TmisData config : configs) {
                try {
                    log.info("[定时补漏] 正在触发主题: {} (上次更新: {})", config.getTopic(), config.getLastUpdateTime());
                    compensationService.compensateTopic(config);
                } catch (Exception e) {
                    // 单个任务触发失败不应影响其他任务
                    log.error("[定时补漏] 触发主题 {} 失败", config.getTopic(), e);
                }
            }
        } catch (Exception e) {
            log.error("[定时补漏] 获取配置或执行调度时发生严重错误", e);
        }

        log.info("=== [定时补漏] 所有任务已提交至线程池 ===");
    }
}