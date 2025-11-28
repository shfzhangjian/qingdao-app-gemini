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
            // 1. 调用存储过程保存到 Oracle (out 参数回填 ID)
            mapper.createFaultAnalysisReportViaSP(reportDto);

            Integer newReportId = reportDto.getId();
            if (newReportId == null) {
                throw new RuntimeException("未能从序列获取新生成的故障分析报告 ID。");
            }
            log.info("故障分析报告已存入数据库, 新 ID: {}", newReportId);

            // 2. 触发 '接口 12' 逻辑 (查询编码并推送 Kafka)
            // 注意: 接口12 (V_TMIS_REPORT_CODE) 包含 type 0 和 1 的数据，通过 ID 查询
            Map<String, Object> result = oracleDataService.getAndFilterFaultReportCode(newReportId);

            // 3. 从结果中提取 Report Code
            List<?> pushedData = (List<?>) result.get("pushedData");
            if (pushedData != null && !pushedData.isEmpty()) {
                Object firstItem = pushedData.get(0);
                if (firstItem instanceof FaultReportCodeFeedbackDTO) {
                    String code = ((FaultReportCodeFeedbackDTO) firstItem).getCode();
                    log.info("成功获取故障分析报告编码: {}", code);
                    return code;
                }
            }

            log.warn("未能获取到故障分析报告编码 (ID: {})", newReportId);
            return null;

        } catch (Exception e) {
            log.error("创建故障分析报告失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建故障分析报告失败: " + e.getMessage(), e);
        }
    }
}