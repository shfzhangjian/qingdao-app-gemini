package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.tspm.dto.RecommendedRotationalTaskDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

/**
 * Kafka 接口 6: TIMS智能推荐预测性维修任务
 * 负责调用 'tmis.RECOMMEND_ROTATIONAL_TASK' 存储过程。
 */
@Mapper
public interface TmisRecommendTaskMapper {

    @Select(value = "{CALL tmis.RECOMMEND_ROTATIONAL_TASK(" +
            "p_plan_id => #{dto.planId, jdbcType=VARCHAR, mode=IN}," +
            "p_equipment_code => #{dto.equipmentCode, jdbcType=VARCHAR, mode=IN}," +
            "p_project => #{dto.project, jdbcType=VARCHAR, mode=IN}," +
            "p_content => #{dto.content, jdbcType=VARCHAR, mode=IN}," +
            "p_standard => #{dto.standard, jdbcType=VARCHAR, mode=IN}," +
            "p_create_date_time => #{dto.createDateTime, jdbcType=VARCHAR, mode=IN}" +
            ")}")
    @Options(statementType = StatementType.CALLABLE)
    void saveRecommendTask(@Param("dto") RecommendedRotationalTaskDTO dto);
}