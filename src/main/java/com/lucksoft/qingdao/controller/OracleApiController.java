package com.lucksoft.qingdao.controller;

import com.lucksoft.qingdao.oracle.dto.PmissionBoardBaoYangDTO;
import com.lucksoft.qingdao.oracle.dto.PmissionBoardDTO;
import com.lucksoft.qingdao.oracle.dto.PmissionBoardDayDTO;
import com.lucksoft.qingdao.oracle.dto.PmMonthDTO;
import com.lucksoft.qingdao.oracle.service.OracleDataService;
import com.lucksoft.qingdao.tspm.producer.TspmProducerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 专门用于接收来自 Oracle 数据库 UTL_HTTP 请求的控制器。
 *
 * @author Gemini
 */
@RestController
@RequestMapping("/api/oracle")
public class OracleApiController {

    private static final Logger log = LoggerFactory.getLogger(OracleApiController.class);

    @Autowired
    private OracleDataService oracleDataService;

    @Autowired
    private TspmProducerService kafkaProducer;

    // 从 application.properties 注入 Kafka topic 名称
    @Value("${kafka.topics.push-pmission-day}")
    private String topicPmissionDay;

    @Value("${kafka.topics.push-pmission-lb}")
    private String topicPmissionLb;

    @Value("${kafka.topics.push-pmission-baoyang}")
    private String topicPmissionBaoYang;

    @Value("${kafka.topics.push-pm-month}")
    private String topicPmMonth;

    /**
     * 接收来自 Oracle 过程的 JSON 推送。
     *
     * @param payload 包含 "stype" 的 JSON 负载
     * @return 一个简单的 JSON 响应，表示成功
     */
    @PostMapping("/receive")
    public ResponseEntity<Map<String, Object>> receiveOraclePush(@RequestBody OracleRequestPayload payload) {

        // 1. 打印接收时间和参数值
        LocalDateTime receptionTime = LocalDateTime.now();
        String receivedStype = (payload != null) ? payload.getStype() : "null";

        log.info("--- Oracle 推送接收成功 ---");
        log.info("接收时间: {}", receptionTime);
        log.info("接收参数 (stype): {}", receivedStype);
        log.info("--------------------------");

        String stypeKey = receivedStype;
        String stypeValue = null;

        // 检查是否为带参数的 stype (例如 'PM_MONTH_ARCHIVED:123456')
        if (receivedStype != null && receivedStype.contains(":")) {
            String[] parts = receivedStype.split(":", 2);
            stypeKey = parts[0];
            stypeValue = parts[1];
        }

        try {
            // 2. [新增] 根据 stype 执行不同的业务逻辑
            if ("SP_GENDAYTASK".equals(stypeKey)) {
                log.info("识别到 stype [SP_GENDAYTASK]，开始处理(日保)数据推送...");
                List<PmissionBoardDayDTO> newTasks = oracleDataService.getAndFilterNewDayTasks();
                if (!newTasks.isEmpty()) {
                    kafkaProducer.sendMessage(topicPmissionDay, newTasks);
                    log.info("成功推送 {} 条新(日保)任务到 Kafka Topic: {}", newTasks.size(), topicPmissionDay);
                }

            } else if ("PMBOARD.SP_QD_PLANBOARD_LB".equals(stypeKey)) {
                log.info("识别到 stype [PMBOARD.SP_QD_PLANBOARD_LB]，开始处理(轮保/月保)数据推送...");
                List<PmissionBoardDTO> newTasks = oracleDataService.getAndFilterNewPmissionBoardTasks();
                if (!newTasks.isEmpty()) {
                    kafkaProducer.sendMessage(topicPmissionLb, newTasks);
                    log.info("成功推送 {} 条新(轮保/月保)任务到 Kafka Topic: {}", newTasks.size(), topicPmissionLb);
                }

            } else if ("JOB_GEN_BAOYANG_TASKS".equals(stypeKey)) {
                log.info("识别到 stype [JOB_GEN_BAOYANG_TASKS]，开始处理(例保)数据推送...");
                List<PmissionBoardBaoYangDTO> newTasks = oracleDataService.getAndFilterNewPmissionBoardBaoYangTasks();
                if (!newTasks.isEmpty()) {
                    kafkaProducer.sendMessage(topicPmissionBaoYang, newTasks);
                    log.info("成功推送 {} 条新(例保)任务到 Kafka Topic: {}", newTasks.size(), topicPmissionBaoYang);
                }

            } else if ("PM_MONTH_ARCHIVED".equals(stypeKey) && stypeValue != null) {
                log.info("识别到 stype [PM_MONTH_ARCHIVED]，主键 ID: {}。开始处理(维修计划)数据推送...", stypeValue);
                Long indocno = Long.parseLong(stypeValue);
                PmMonthDTO pmMonthData = oracleDataService.getAndFilterPmMonthData(indocno);

                if (pmMonthData != null) {
                    // 推送单个主从DTO对象 (Kafka Producer 会将其序列化为 JSON)
                    kafkaProducer.sendMessage(topicPmMonth, pmMonthData);
                    log.info("成功推送 1 条(维修计划)主从数据 (ID: {}) 到 Kafka Topic: {}", indocno, topicPmMonth);
                } else {
                    log.info("维修计划 (ID: {}) 无需推送 (可能已推送或未找到)。", indocno);
                }

            } else {
                log.warn("收到未配置的 stype: {}", receivedStype);
            }

        } catch (Exception e) {
            log.error("处理 stype: {} 时发生内部错误: {}", receivedStype, e.getMessage(), e);
            // 即使 Java 处理失败，也要告诉 Oracle "已收到"，防止 Oracle 重试
        }

        // 3. 构建一个响应返回给 Oracle
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("receivedAt", receptionTime.toString());
        response.put("receivedStype", receivedStype);

        return ResponseEntity.ok(response);
    }

    /**
     * 用于映射 Oracle 发送的 JSON 负载 {"stype": "..."} 的简单 DTO。
     * 作为一个静态内部类，它不需要单独的文件。
     */
    static class OracleRequestPayload implements Serializable {
        private static final long serialVersionUID = 1L;

        private String stype;

        public String getStype() {
            return stype;
        }

        public void setStype(String stype) {
            this.stype = stype;
        }
    }
}