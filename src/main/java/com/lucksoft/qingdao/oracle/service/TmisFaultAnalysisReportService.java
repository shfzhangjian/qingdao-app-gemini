package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.mapper.TmisFaultAnalysisReportMapper;
import com.lucksoft.qingdao.tspm.dto.FaultAnalysisReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * [新] Kafka 接口 11: 故障分析报告 - 工作单元服务
 */
@Service
public class TmisFaultAnalysisReportService {

    private static final Logger log = LoggerFactory.getLogger(TmisFaultAnalysisReportService.class);

    @Autowired
    private TmisFaultAnalysisReportMapper mapper;

    @Transactional
    public void processFaultAnalysisReport(FaultAnalysisReportDTO dto) {
        try {
            log.info("调用存储过程 [tmis.CREATE_FAULT_ANALYSIS_REPORT] for TIMS ID: {}", dto.getId());
            mapper.createFaultAnalysisReportViaSP(dto);
        } catch (Exception e) {
            log.error("存储过程 [tmis.CREATE_FAULT_ANALYSIS_REPORT] 执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储过程执行失败", e);
        }
    }
}