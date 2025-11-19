package com.lucksoft.qingdao.job.dto;

import java.util.List;

/**
 * DTO (数据传输对象)
 * 用于映射 time.json 配置文件。
 */
public class ScheduleDto {

    /**
     * 默认的定时任务 (推送包机信息)
     */
    private List<String> cronExpressions;

    /**
     * [新增] 轮保任务生成与推送的定时任务
     * 默认: "0 30 6 * * ?", "0 30 14 * * ?", "0 30 22 * * ?"
     */
    private List<String> lbTaskCronExpressions;

    public List<String> getCronExpressions() {
        return cronExpressions;
    }

    public void setCronExpressions(List<String> cronExpressions) {
        this.cronExpressions = cronExpressions;
    }

    public List<String> getLbTaskCronExpressions() {
        return lbTaskCronExpressions;
    }

    public void setLbTaskCronExpressions(List<String> lbTaskCronExpressions) {
        this.lbTaskCronExpressions = lbTaskCronExpressions;
    }
}