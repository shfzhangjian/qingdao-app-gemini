package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.tspm.dto.TaskScoreFeedbackDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

/**
 * Kafka 接口 3: 反馈保养、点检、润滑任务完成得分
 * 负责调用 'tmis.FEEDBACK_MAINTENANCE_TASK_SCORE' 存储过程。
 */
@Mapper
public interface TmisTaskScoreMapper {

    @Select(value = "{CALL tmis.FEEDBACK_MAINTENANCE_TASK_SCORE(" +
            "p_task_id => #{dto.taskId, jdbcType=VARCHAR, mode=IN}," +
            "p_score => #{dto.score, jdbcType=VARCHAR, mode=IN}," +
            "p_rectification_content => #{dto.rectificationContent, jdbcType=VARCHAR, mode=IN}" +
            ")}")
    @Options(statementType = StatementType.CALLABLE)
    void saveTaskScore(@Param("dto") TaskScoreFeedbackDTO dto);
}