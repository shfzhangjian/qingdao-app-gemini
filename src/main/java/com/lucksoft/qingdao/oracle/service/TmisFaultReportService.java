package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.mapper.TmisFaultReportMapper;
import com.lucksoft.qingdao.tspm.dto.FaultReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * [新] TMIS 故障报告服务
 * 负责处理故障报告的创建。
 */
@Service
public class TmisFaultReportService {

    private static final Logger log = LoggerFactory.getLogger(TmisFaultReportService.class);

    @Autowired
    private TmisFaultReportMapper tmisFaultReportMapper;

    @Autowired
    private OracleDataService oracleDataService;

    /**
     * 创建一个新的故障报告，并返回生成的报告编码。
     * [重构] 确保事务只包裹数据库操作，不包含潜在的耗时网络操作。
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String createReport(FaultReportDTO reportDto) {
        try {
            sanitizeDateTime(reportDto);
            // 1. 调用存储过程保存到 Oracle (快速)
            tmisFaultReportMapper.createFaultReportViaSP(reportDto);

            String reportCode = reportDto.getCode();
            if (reportCode == null || reportCode.isEmpty()) {
                throw new RuntimeException("未能从存储过程获取新生成的故障报告编码 (p_new_code)。");
            }
            log.info("故障报告已存入数据库, 新编码: {}", reportCode);

            // [安全策略] 不要在这里进行同步 Kafka 推送！
            // 如果必须推送，请使用异步机制 (例如调用 asyncTaskService.submitTask(...))
            // 这里的 createReport 方法应该尽快结束事务并释放连接。

            return reportCode;

        } catch (Exception e) {
            log.error("创建故障报告失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建故障报告失败: " + e.getMessage(), e);
        }
    }

    /**
     * 异步消费处理入口 (保持事务，因为是在异步线程中执行)
     */
    @Transactional
    public FaultReportDTO createReportAndTriggerPush(FaultReportDTO reportDto) {
        // 复用 createReport 的逻辑
        sanitizeDateTime(reportDto);

        createReport(reportDto);
        return reportDto;
    }
    /**
     * 辅助方法：清洗时间字段中的 'T'
     */
    private void sanitizeDateTime(FaultReportDTO dto) {
        if (dto.getDebriefingTime() != null) {
            dto.setDebriefingTime(dto.getDebriefingTime().replace("T", " "));
        }
        if (dto.getHaltStartTime() != null) {
            dto.setHaltStartTime(dto.getHaltStartTime().replace("T", " "));
        }
        if (dto.getHaltEndTime() != null) {
            dto.setHaltEndTime(dto.getHaltEndTime().replace("T", " "));
        }
    }
}