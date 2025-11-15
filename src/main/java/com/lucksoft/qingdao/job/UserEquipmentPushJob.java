package com.lucksoft.qingdao.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucksoft.qingdao.tspm.dto.UserEquipmentDTO;
import com.lucksoft.qingdao.tspm.producer.TspmProducerService;
import com.lucksoft.qingdao.tspm.service.DynamicQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * [新功能]
 * 定时任务，用于每天自动推送全量包机信息。
 * 将 'UserEquipmentPushController' 的逻辑迁移至此。
 */
@Component
public class UserEquipmentPushJob {

    private static final Logger log = LoggerFactory.getLogger(UserEquipmentPushJob.class);

    @Autowired
    private DynamicQueryService dynamicQueryService;

    @Autowired
    private TspmProducerService producerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.topics.sync-user-equipment}")
    private String userEquipmentTopic;

    /**
     * SQL查询，基于 V_USER_EQUIPMENT_TODAY 视图。
     * 此视图在 'create_view_user_equipment.sql' 脚本中定义。
     */
    private static final String ALL_USER_EQUIPMENT_SQL =
            "SELECT * FROM V_USER_EQUIPMENT_TODAY";

    /**
     * 自动推送包机信息。
     * cron = "0 0 0 * * ?" 表示在每天凌晨 00:00:00 执行。
    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨0点执行
     */
    public void runPushJob() {
        log.info("--- [定时任务] 开始执行：推送每日包机信息 ---");

        try {
            // 1. 执行全量查询 (设置一个合理的上限，例如10000条)
            // 注意：定时任务中没有 http-request，所以 limit 是写死的
            List<Map<String, Object>> queryResult = dynamicQueryService.executeQuery(ALL_USER_EQUIPMENT_SQL, 10000, Collections.emptyList());
            log.info("[定时任务] 从视图 V_USER_EQUIPMENT_TODAY 查询到 {} 条包机信息。", queryResult.size());

            // 2. 将 Map 列表转换为 DTO 列表
            List<UserEquipmentDTO> dtoList = queryResult.stream()
                    .map(row -> objectMapper.convertValue(row, UserEquipmentDTO.class))
                    .collect(Collectors.toList());

            // 3. 推送到 Kafka
            if (!dtoList.isEmpty()) {
                producerService.sendMessage(userEquipmentTopic, dtoList);
                log.info("[定时任务] 成功将 {} 条包机信息推送到 Kafka 主题: {}", dtoList.size(), userEquipmentTopic);
            } else {
                log.warn("[定时任务] 查询到0条包机信息，未推送到Kafka。");
            }

            log.info("--- [定时任务] 执行完毕：推送每日包机信息 ---");

        } catch (Exception e) {
            log.error("[定时任务] 推送包机信息时发生严重错误: {}", e.getMessage(), e);
        }
    }
}