package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.mapper.TmisFaultAnalysisReportMapper;
import com.lucksoft.qingdao.tspm.dto.FaultAnalysisReportDTO;
import com.lucksoft.qingdao.tspm.dto.FaultReportCodeFeedbackDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

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


    /**
     * [新增] 创建故障分析报告，触发推送，并返回生成的报告编码。
     *
     * @param reportDto 从 API 接收的故障分析报告数据
     * @return 生成的报告编码
     */
    @Transactional
    public String createReportAndTriggerPush(FaultAnalysisReportDTO reportDto) {
        try {
            // 1. 调用存储过程保存 (out 参数回填 Code)
            mapper.createFaultAnalysisReportViaSP(reportDto);

            // [修改] 直接获取 Code
            String reportCode = reportDto.getCode();
            if (reportCode == null || reportCode.isEmpty()) {
                throw new RuntimeException("未能从存储过程获取新生成的故障分析报告编码。");
            }
            log.info("故障分析报告已存入数据库, 新编码: {}", reportCode);

            // 2. 触发 '接口 12' 逻辑
            // 同上，如果需要推送，请确认 oracleDataService 需要的参数

            return reportCode;

        } catch (Exception e) {
            log.error("创建故障分析报告失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建故障分析报告失败: " + e.getMessage(), e);
        }
    }
}