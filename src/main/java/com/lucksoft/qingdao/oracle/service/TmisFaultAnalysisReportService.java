package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.mapper.TmisFaultAnalysisReportMapper;
import com.lucksoft.qingdao.tspm.dto.FaultAnalysisReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * [新] Kafka 接口 11: 故障分析报告 - 工作单元服务
 */
@Service
public class TmisFaultAnalysisReportService {

    private static final Logger log = LoggerFactory.getLogger(TmisFaultAnalysisReportService.class);

    @Autowired
    private OracleDataService oracleDataService;

    @Autowired
    private TmisFaultAnalysisReportMapper mapper;

    // 此方法主要供 AsyncTaskService 异步调用
    @Transactional
    public void processFaultAnalysisReport(FaultAnalysisReportDTO dto) {
        try {
            sanitizeDateTime(dto);
            log.info("调用存储过程 [tmis.CREATE_FAULT_ANALYSIS_REPORT] for TIMS ID: {}", dto.getId());
            mapper.createFaultAnalysisReportViaSP(dto);
        } catch (Exception e) {
            log.error("存储过程 [tmis.CREATE_FAULT_ANALYSIS_REPORT] 执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储过程执行失败", e);
        }
    }


    /**
     * [重构] 创建故障分析报告，并返回生成的报告编码。
     * 关键修改：
     * 1. 使用 REQUIRED 事务传播，确保数据库操作在事务内。
     * 2. 推送逻辑（如果有）应当在事务外部或异步执行，防止 Kafka 阻塞导致数据库连接耗尽。
     *
     * @param reportDto 从 API 接收的故障分析报告数据
     * @return 生成的报告编码
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String createReportAndTriggerPush(FaultAnalysisReportDTO reportDto) {
        // 1. 调用存储过程保存 (快速操作)
        try {
            sanitizeDateTime(reportDto);
            mapper.createFaultAnalysisReportViaSP(reportDto);
        } catch (Exception e) {
            log.error("调用存储过程失败", e);
            throw new RuntimeException("数据库保存失败: " + e.getMessage());
        }

        // [修改] 直接获取 Code
        String reportCode = reportDto.getCode();
        if (reportCode == null || reportCode.isEmpty()) {
            // 如果存储过程没返回 code，抛异常回滚事务
            throw new RuntimeException("未能从存储过程获取新生成的故障分析报告编码。");
        }
        log.info("故障分析报告已存入数据库, 新编码: {}", reportCode);

        // [注意] 此处不进行同步 Kafka 推送。
        // 如果业务要求“必须推送成功才返回”，则这里会面临连接耗尽风险。
        // 建议：如果需要推送，另起一个异步线程，或者使用 Spring ApplicationEventPublisher 发布事件。
        // 目前代码中没有看到对 oracleDataService 的调用，说明这里只是纯入库。
        // 如果后续加了推送，一定要注意！

        return reportCode;
    }
    /**
     * 辅助方法：清洗时间字段中的 'T'
     */
    private void sanitizeDateTime(FaultAnalysisReportDTO dto) {
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