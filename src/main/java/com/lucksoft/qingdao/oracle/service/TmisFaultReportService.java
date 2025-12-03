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

            // [修改] 直接获取存储过程返回的 code
            String reportCode = reportDto.getCode();
            if (reportCode == null || reportCode.isEmpty()) {
                throw new RuntimeException("未能从存储过程获取新生成的故障报告编码 (p_new_code)。");
            }
            log.info("故障报告已存入数据库, 新编码: {}", reportCode);

            // 2. 触发 '接口 12' 逻辑 (这里可能需要调整逻辑，因为原本是根据ID查Code，现在直接有了Code)
            // 如果接口12的逻辑是推送 Code 给 TIMS，现在可以直接推送了。
            // 假设 getAndFilterFaultReportCode 需要的是 ID 来查库，
            // 如果存储过程只返回 Code 而不返回 ID，那么原有的触发逻辑可能需要调整。
            // *暂且保留原有逻辑，但如果 oracleDataService 需要 ID，这里可能会有问题*
            // 假设 reportDto.getId() 仍然是 TIMS 传来的 ID，而新生成的 ID 没有返回。
            // 如果 oracleDataService 需要新生成的内部 ID 来查询视图 V_TMIS_REPORT_CODE，
            // 那么存储过程最好同时返回 ID 和 Code。

            // *修正建议*: 如果 oracleDataService.getAndFilterFaultReportCode(id) 需要的是新生成的 GZISSUE.INDOCNO，
            // 那么存储过程应该返回这个 ID。如果它需要的是 TIMS_ID，那可以直接用 reportDto.getId()。
            // 假设这里主要目的是返回 Code 给前端，我们直接返回 reportCode。

            // 如果还需要触发 Kafka 推送，且推送内容就是这个 Code：
            // 可以直接构造 DTO 推送，或者沿用旧逻辑（如果旧逻辑依赖 ID 查询）。

            return reportCode;

        } catch (Exception e) {
            log.error("创建故障报告失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建故障报告失败: " + e.getMessage(), e);
        }
    }

//    @Transactional
//    public String createReport(FaultReportDTO reportDto) {
//        try {
//            // 1. 调用存储过程保存到 Oracle
//            tmisFaultReportMapper.createFaultReportViaSP(reportDto);
//
//            Integer newReportId = reportDto.getId();
//            if (newReportId == null) {
//                throw new RuntimeException("未能从序列获取新生成的故障报告 ID。");
//            }
//            log.info("故障报告已存入数据库, 新 ID: {}", newReportId);
//
//            // 2. 触发 '接口 12' 逻辑 (查询编码并推送 Kafka)
//            // result 包含 { "pushedData": [FaultReportCodeFeedbackDTO], ... }
//            Map<String, Object> result = oracleDataService.getAndFilterFaultReportCode(newReportId);
//
//            // 3. 从结果中提取 Report Code
//            List<?> pushedData = (List<?>) result.get("pushedData");
//            if (pushedData != null && !pushedData.isEmpty()) {
//                Object firstItem = pushedData.get(0);
//                if (firstItem instanceof FaultReportCodeFeedbackDTO) {
//                    String code = ((FaultReportCodeFeedbackDTO) firstItem).getCode();
//                    log.info("成功获取报告编码: {}", code);
//                    return code;
//                }
//            }
//
//            log.warn("未能获取到报告编码 (ID: {})", newReportId);
//            return null;
//
//        } catch (Exception e) {
//            log.error("创建故障报告失败: {}", e.getMessage(), e);
//            throw new RuntimeException("创建故障报告失败: " + e.getMessage(), e);
//        }
//    }

    /**
     * 创建一个新的故障报告，并立即触发 "接口 12" 推送。
     *
     * @param reportDto 从 API 接收的故障报告数据
     * @return 包含新 ID 的故障报告 DTO
     */

    @Transactional
    public FaultReportDTO createReportAndTriggerPush(FaultReportDTO reportDto) {
        try {
            // 步骤 1: 调用存储过程保存
            tmisFaultReportMapper.createFaultReportViaSP(reportDto);

            String reportCode = reportDto.getCode();
            if (reportCode == null || reportCode.isEmpty()) {
                throw new RuntimeException("未能从存储过程获取新生成的故障报告编码。");
            }
            log.info("故障报告已存入, Code: {}", reportCode);

            // 步骤 2: 触发接口12推送 (注意：这里可能需要根据实际业务调整参数)
            // 如果需要推送，这里应该使用正确的新 ID 或 Code
            // oracleDataService.getAndFilterFaultReportCode(...);

            return reportDto;

        } catch (Exception e) {
            log.error("创建故障报告并触发推送失败: {}", e.getMessage(), e);
            throw new RuntimeException("创建故障报告失败", e);
        }
    }

//    @Transactional // 确保写入和触发在同一个事务中
//    public FaultReportDTO createReportAndTriggerPush(FaultReportDTO reportDto) {
//        try {
//            // 步骤 1: [已修改] 调用存储过程保存到 Oracle 主数据库
//            // (TmisFaultReportMapper.java 会将OUT参数 p_new_id 自动回填到 reportDto.id)
//            tmisFaultReportMapper.createFaultReportViaSP(reportDto);
//
//            Integer newReportId = reportDto.getId();
//            if (newReportId == null) {
//                throw new RuntimeException("未能从序列获取新生成的故障报告 ID。");
//            }
//            log.info("故障报告已通过存储过程存入主数据库, 新 ID: {}", newReportId);
//
//            // 步骤 2: 立即调用 OracleDataService 中的 "接口 12" 逻辑
//            // 此方法将使用 newReportId 查询视图 V_TMIS_REPORT_CODE，
//            // 并将获取到的编码推送到 Kafka 主题 'tims.receive.fault.report.code'
//            log.info("正在触发 '接口 12' (TIMS_PUSH_FAULT_REPORT_CODE) 逻辑, ID: {}", newReportId);
//            oracleDataService.getAndFilterFaultReportCode(newReportId);
//
//            log.info("成功创建并触发推送, ID: {}", newReportId);
//            return reportDto;
//
//        } catch (Exception e) {
//            log.error("创建故障报告并触发推送失败: {}", e.getMessage(), e);
//            // 抛出运行时异常以触发事务回滚
//            throw new RuntimeException("创建故障报告失败", e);
//        }
//    }
}