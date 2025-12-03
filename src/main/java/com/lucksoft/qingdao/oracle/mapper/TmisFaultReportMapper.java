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
 * 增加 p_new_id OUT 参数以支持 ID 回填。
 */
@Mapper
public interface TmisFaultReportMapper {

    @Select(value = "{CALL tmis.CREATE_FAULT_REPORT(" +
            "p_tims_id => #{report.id, jdbcType=VARCHAR, mode=IN}," +
            "p_name => #{report.name, jdbcType=VARCHAR, mode=IN}," +
            "p_equipment_code => #{report.equipmentCode, jdbcType=VARCHAR, mode=IN}," +
            "p_major => #{report.major, jdbcType=VARCHAR, mode=IN}," +
            "p_fault_source => #{report.faultSource, jdbcType=VARCHAR, mode=IN}," +
            "p_equipment_status => #{report.equipmentStatus, jdbcType=VARCHAR, mode=IN}," +
            "p_team_name => #{report.teamName, jdbcType=VARCHAR, mode=IN}," +
            "p_reporter => #{report.reporter, jdbcType=VARCHAR, mode=IN}," +
            "p_debriefing_time => #{report.debriefingTime, jdbcType=VARCHAR, mode=IN}," +
            "p_equipment_part => #{report.equipmentPart, jdbcType=VARCHAR, mode=IN}," +
            "p_fault_phenomenon => #{report.faultPhenomenon, jdbcType=VARCHAR, mode=IN}," +
            "p_fault_reason => #{report.faultReason, jdbcType=VARCHAR, mode=IN}," +
            "p_measures => #{report.measures, jdbcType=VARCHAR, mode=IN}," +
            "p_halt_start_time => #{report.haltStartTime, jdbcType=VARCHAR, mode=IN}," +
            "p_halt_end_time => #{report.haltEndTime, jdbcType=VARCHAR, mode=IN}," +
            "p_halt_duration => #{report.haltDuration, jdbcType=VARCHAR, mode=IN}," +
            "p_replacement_required => #{report.replacementRequired, jdbcType=VARCHAR, mode=IN}," +
            "p_solution => #{report.solution, jdbcType=VARCHAR, mode=IN}," +
            "p_specific_desc => #{report.specificDesc, jdbcType=VARCHAR, mode=IN}," +
            // [修正] 将 OUT 参数类型改为 VARCHAR，以匹配存储过程定义并提高兼容性
            "p_new_code => #{report.code, jdbcType=VARCHAR, mode=OUT}" +
            ")}")
    @Options(statementType = StatementType.CALLABLE)
    void createFaultReportViaSP(@Param("report") FaultReportDTO report);
}