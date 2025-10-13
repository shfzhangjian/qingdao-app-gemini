
package com.lucksoft.qingdao.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class SimpleScheduledJob {
    private static final Logger log = LoggerFactory.getLogger(SimpleScheduledJob.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    // 每10秒执行一次
    //@Scheduled(fixedRateString = "${scheduled.report-time.fixed-rate}")
    public void reportCurrentTime() {
        log.info("定时任务执行 - 现在时间: {}", dateFormat.format(new Date()));
    }
}
