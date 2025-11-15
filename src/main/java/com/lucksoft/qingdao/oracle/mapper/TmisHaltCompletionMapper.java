package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.tspm.dto.ProductionHaltCompletionFeedbackDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

/**
 * Kafka 接口 14: 反馈停产检修计划任务完成情况
 * 负责调用 'tmis.FB_COMPLETED_HALT_TASK' 存储过程。
 */
@Mapper
public interface TmisHaltCompletionMapper {

    @Select(value = "{CALL tmis.FB_COMPLETED_HALT_TASK(" +
            "p_task_id => #{dto.taskId, jdbcType=VARCHAR, mode=IN}," +
            "p_complete_user => #{dto.completeUser, jdbcType=VARCHAR, mode=IN}," +
            "p_complete_date_time => #{dto.completeDateTime, jdbcType=VARCHAR, mode=IN}" +
            ")}")
    @Options(statementType = StatementType.CALLABLE)
    void saveHaltCompletion(@Param("dto") ProductionHaltCompletionFeedbackDTO dto);
}