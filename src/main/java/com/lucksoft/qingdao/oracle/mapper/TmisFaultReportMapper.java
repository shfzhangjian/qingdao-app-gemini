package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.tspm.dto.FaultReportDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

/**
 * [已修改]
 * 负责调用 'tmis.CREATE_FAULT_REPORT' 存储过程。
 * 所有参数现在都作为 VARCHAR2 (String) 类型传递，以匹配新的存储过程定义。
 */
@Mapper
public interface TmisFaultReportMapper {

    /**
     * [已修改] 调用 'tmis.CREATE_FAULT_REPORT' 存储过程来插入一条新的故障报告。
     *
     * @param report 故障报告DTO。所有字段都将作为字符串传递。
     */
    @Select(value = "{CALL tmis.CREATE_FAULT_REPORT(" +
            // 对应 FaultReportDTO.id (TIMS 系统主键)
            "p_tims_id => #{report.id, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.name
            "p_name => #{report.name, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.equipmentCode
            "p_equipment_code => #{report.equipmentCode, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.major
            "p_major => #{report.major, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.faultSource
            "p_fault_source => #{report.faultSource, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.equipmentStatus
            "p_equipment_status => #{report.equipmentStatus, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.teamName
            "p_team_name => #{report.teamName, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.reporter
            "p_reporter => #{report.reporter, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.debriefingTime
            "p_debriefing_time => #{report.debriefingTime, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.equipmentPart
            "p_equipment_part => #{report.equipmentPart, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.faultPhenomenon
            "p_fault_phenomenon => #{report.faultPhenomenon, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.faultReason
            "p_fault_reason => #{report.faultReason, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.measures
            "p_measures => #{report.measures, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.haltStartTime
            "p_halt_start_time => #{report.haltStartTime, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.haltEndTime
            "p_halt_end_time => #{report.haltEndTime, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.haltDuration (作为字符串传递)
            "p_halt_duration => #{report.haltDuration, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.replacementRequired (作为字符串 "true"/"false" 传递)
            "p_replacement_required => #{report.replacementRequired, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.solution
            "p_solution => #{report.solution, jdbcType=VARCHAR, mode=IN}," +
            // 对应 FaultReportDTO.specificDesc
            "p_specific_desc => #{report.specificDesc, jdbcType=VARCHAR, mode=IN}" +
            ")}")
    @Options(statementType = StatementType.CALLABLE)
    void createFaultReportViaSP(@Param("report") FaultReportDTO report);
}