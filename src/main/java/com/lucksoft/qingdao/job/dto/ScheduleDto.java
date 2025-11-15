package com.lucksoft.qingdao.job.dto;

import java.util.List;

/**
 * DTO (数据传输对象)
 * 用于映射 time.json 配置文件。
 */
public class ScheduleDto {

    /**
     * 一个包含一个或多个 Cron 表达式的列表。
     */
    private List<String> cronExpressions;

    public List<String> getCronExpressions() {
        return cronExpressions;
    }

    public void setCronExpressions(List<String> cronExpressions) {
        this.cronExpressions = cronExpressions;
    }
}