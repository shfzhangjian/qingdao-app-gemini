package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.mapper.TmisFaultReportMapper;
import com.lucksoft.qingdao.oracle.service.OracleDataService;
import com.lucksoft.qingdao.tspm.dto.FaultReportDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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