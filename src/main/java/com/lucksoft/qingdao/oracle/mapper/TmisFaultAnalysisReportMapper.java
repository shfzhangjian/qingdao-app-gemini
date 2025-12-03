package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.tspm.dto.FaultAnalysisReportDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

/**
 * [新] Kafka 接口 11: 故障分析报告创建
 * 负责调用 'tmis.CREATE_FAULT_ANALYSIS_REPORT' 存储过程。
 * 增加 p_new_id OUT 参数以支持 ID 回填。
 */
@Mapper
public interface TmisFaultAnalysisReportMapper {

    @Select(value = "{CALL tmis.CREATE_FAULT_ANALYSIS_REPORT(" +
            "p_tims_id => #{dto.id, jdbcType=VARCHAR, mode=IN}," +
            "p_name => #{dto.name, jdbcType=VARCHAR, mode=IN}," +
            "p_equipment_code => #{dto.equipmentCode, jdbcType=VARCHAR, mode=IN}," +
            "p_attribute => #{dto.attribute, jdbcType=VARCHAR, mode=IN}," +
            "p_category => #{dto.category, jdbcType=VARCHAR, mode=IN}," +
            "p_fault_nature => #{dto.faultNature, jdbcType=VARCHAR, mode=IN}," +
            "p_team_name => #{dto.teamName, jdbcType=VARCHAR, mode=IN}," +
            "p_reporter => #{dto.reporter, jdbcType=VARCHAR, mode=IN}," +
            "p_debriefing_time => #{dto.debriefingTime, jdbcType=VARCHAR, mode=IN}," +
            "p_equipment_part => #{dto.equipmentPart, jdbcType=VARCHAR, mode=IN}," +
            "p_fault_phenomenon => #{dto.faultPhenomenon, jdbcType=VARCHAR, mode=IN}," +
            "p_fault_reason => #{dto.faultReason, jdbcType=VARCHAR, mode=IN}," +
            "p_measures => #{dto.measures, jdbcType=VARCHAR, mode=IN}," +
            "p_halt_start_time => #{dto.haltStartTime, jdbcType=VARCHAR, mode=IN}," +
            "p_halt_end_time => #{dto.haltEndTime, jdbcType=VARCHAR, mode=IN}," +
            "p_halt_duration => #{dto.haltDuration, jdbcType=VARCHAR, mode=IN}," +
            "p_fault_site_desc => #{dto.faultSiteDesc, jdbcType=VARCHAR, mode=IN}," +
            "p_fault_causes_analysis => #{dto.faultCausesAnalysis, jdbcType=VARCHAR, mode=IN}," +

            "p_new_code => #{report.code, jdbcType=VARCHAR, mode=OUT}" +
            ")}")
    @Options(statementType = StatementType.CALLABLE)
    void createFaultAnalysisReportViaSP(@Param("dto") FaultAnalysisReportDTO dto);
}