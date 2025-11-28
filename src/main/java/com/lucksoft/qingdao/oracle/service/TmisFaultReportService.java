package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.mapper.TmisFaultReportMapper;
import com.lucksoft.qingdao.oracle.service.OracleDataService;
import com.lucksoft.qingdao.tspm.dto.FaultReportCodeFeedbackDTO;
import com.lucksoft.qingdao.tspm.dto.FaultReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * [新] TMIS 故障报告服务
 * 负责处理故障报告的创建，并触发 "接口 12" 的 Kafka 推送。
 */
@Service
public class TmisFaultReportService {

    private static final Logger log = LoggerFactory.getLogger(TmisFaultReportService.class);

    // 1. 注入用于 *写入* 故障报告到主数据库的 Mapper
    // (注意：此 Mapper 必须配置为使用 'primaryDataSource')
    @Autowired
    private TmisFaultReportMapper tmisFaultReportMapper;

    // 2. 注入用于 *触发* "接口 12" Kafka 推送的服务
    @Autowired
    private OracleDataService oracleDataService;

    /**
     * 创建一个新的故障报告，触发推送，并返回生成的报告编码。
     *
     * @param reportDto 从 API 接收的故障报告数据
     * @return 生成的故障报告编码 (例如 "JB-E-20250716")
     */
    @Transactional
    public String createReport(FaultReportDTO reportDto) {
        try {
            // 1. 调用存储过程保存到 Oracle
            tmisFaultReportMapper.createFaultReportViaSP(reportDto);

            Integer newReportId = reportDto.getId();
            if (newReportId == null) {
                throw new RuntimeException("未能从序列获取新生成的故障报告 ID。");
            }
            log.info("故障报告已存入数据库, 新 ID: {}", newReportId);

            // 2. 触发 '接口 12' 逻辑 (查询编码并推送 Kafka)
            // result 包含 { "pushedData": [FaultReportCodeFeedbackDTO], ... }
            Map<String, Object> result = oracleDataService.getAndFilterFaultReportCode(newReportId);

            // 3. 从结果中提取 Report Code
            List<?> pushedData = (List<?>) result.get("pushedData");
            if (pushedData != null && !pushedData.isEmpty()) {
                Object firstItem = pushedData.get(0);
                if (firstItem instanceof FaultReportCodeFeedbackDTO) {
                    String code = ((FaultReportCodeFeedbackDTO) firstItem).getCode();
                    log.info("成功获取报告编码: {}", code);
                    return code;
                }
            }

            log.warn("未能获取到报告编码 (ID: {})", newReportId);
            return null;

        } catch (Exception e) {
            log.error("创建故障报告失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建故障报告失败: " + e.getMessage(), e);
        }
    }

    /**
     * 创建一个新的故障报告，并立即触发 "接口 12" 推送。
     *
     * @param reportDto 从 API 接收的故障报告数据
     * @return 包含新 ID 的故障报告 DTO
     */
    @Transactional // 确保写入和触发在同一个事务中
    public FaultReportDTO createReportAndTriggerPush(FaultReportDTO reportDto) {
        try {
            // 步骤 1: [已修改] 调用存储过程保存到 Oracle 主数据库
            // (TmisFaultReportMapper.java 会将OUT参数 p_new_id 自动回填到 reportDto.id)
            tmisFaultReportMapper.createFaultReportViaSP(reportDto);

            Integer newReportId = reportDto.getId();
            if (newReportId == null) {
                throw new RuntimeException("未能从序列获取新生成的故障报告 ID。");
            }
            log.info("故障报告已通过存储过程存入主数据库, 新 ID: {}", newReportId);

            // 步骤 2: 立即调用 OracleDataService 中的 "接口 12" 逻辑
            // 此方法将使用 newReportId 查询视图 V_TMIS_REPORT_CODE，
            // 并将获取到的编码推送到 Kafka 主题 'tims.receive.fault.report.code'
            log.info("正在触发 '接口 12' (TIMS_PUSH_FAULT_REPORT_CODE) 逻辑, ID: {}", newReportId);
            oracleDataService.getAndFilterFaultReportCode(newReportId);

            log.info("成功创建并触发推送, ID: {}", newReportId);
            return reportDto;

        } catch (Exception e) {
            log.error("创建故障报告并触发推送失败: {}", e.getMessage(), e);
            // 抛出运行时异常以触发事务回滚
            throw new RuntimeException("创建故障报告失败", e);
        }
    }
}