package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.tspm.dto.TaskCompletionFeedbackDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

/**
 * Kafka 接口 2: 反馈保养、点检、润滑任务完成情况
 * 负责调用 'tmis.FB_COMPLETED_MAINT_TASK' 存储过程。
 */
@Mapper
public interface TmisTaskCompletionMapper {

    @Select(value = "{CALL tmis.FB_COMPLETED_MAINT_TASK(" +
            "p_task_id => #{dto.taskId, jdbcType=VARCHAR, mode=IN}," +
            "p_task_type => #{dto.type, jdbcType=INTEGER, mode=IN}," +
            "p_complete_user => #{dto.completeUser, jdbcType=VARCHAR, mode=IN}," +
            "p_complete_date_time => #{dto.completeDateTime, jdbcType=VARCHAR, mode=IN}," +
            "p_inspection_actual_value => #{dto.inspectionActualValue, jdbcType=VARCHAR, mode=IN}" +
            ")}")
    @Options(statementType = StatementType.CALLABLE)
    void saveTaskCompletion(@Param("dto") TaskCompletionFeedbackDTO dto);
}