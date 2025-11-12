package com.lucksoft.qingdao.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucksoft.qingdao.tspm.dto.UserEquipmentDTO;
import com.lucksoft.qingdao.tspm.producer.TspmProducerService;
import com.lucksoft.qingdao.tspm.service.DynamicQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 专门用于手动触发全量包机信息推送的控制器
 */
@RestController
@RequestMapping("/api/push")
public class UserEquipmentPushController {

    private static final Logger log = LoggerFactory.getLogger(UserEquipmentPushController.class);

    @Autowired
    private DynamicQueryService dynamicQueryService;

    @Autowired
    private TspmProducerService producerService;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${kafka.topics.sync-user-equipment}")
    private String userEquipmentTopic;

    /**
     * [修改]
     * SQL查询已被重构为 Oracle 数据库视图 V_USER_EQUIPMENT_TODAY。
     * Java代码现在只需查询这个视图即可。
     * 视图的创建脚本请见 'create_view_user_equipment.sql'。
     */
    private static final String ALL_USER_EQUIPMENT_SQL =
            "SELECT * FROM V_USER_EQUIPMENT_TODAY";

    /**
     * 手动触发推送所有包机信息
     * @return 推送结果
     */
    @PostMapping("/user-equipment-all")
    public ResponseEntity<Map<String, Object>> pushAllUserEquipment() {
        log.info("收到手动推送所有包机信息的请求...");

        try {
            // 1. 执行全量查询 (设置一个合理的上限，例如10000条)
            List<Map<String, Object>> queryResult = dynamicQueryService.executeQuery(ALL_USER_EQUIPMENT_SQL, 10000, Collections.emptyList());
            log.info("从数据库查询到 {} 条包机信息。", queryResult.size());

            // 2. 将 Map 列表转换为 DTO 列表
            // 注意：simulate.html中的DTO字段与UserEquipmentDTO不完全匹配
            // 我们将使用ObjectMapper灵活转换匹配的字段
            List<UserEquipmentDTO> dtoList = queryResult.stream()
                    .map(row -> objectMapper.convertValue(row, UserEquipmentDTO.class))
                    .collect(java.util.stream.Collectors.toList());

            // 3. 推送到 Kafka
            if (!dtoList.isEmpty()) {
                producerService.sendMessage(userEquipmentTopic, dtoList);
                log.info("成功将 {} 条包机信息推送到 Kafka 主题: {}", dtoList.size(), userEquipmentTopic);
            }

            // [修改] 将推送的数据和消息一起返回
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("message", "成功查询并推送 " + dtoList.size() + " 条包机信息。");
            response.put("pushedData", dtoList); // <--- 新增此行，将数据返回
            return ResponseEntity.ok(response); // <--- 修改返回对象

        } catch (Exception e) {
            log.error("推送包机信息时发生错误: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}