package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.tspm.dto.RotationalTaskScoreFeedbackDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

/**
 * Kafka 接口 9: 反馈轮保任务完成得分
 * 负责调用 'tmis.FEEDBACK_ROTATIONAL_TASK_SCORE' 存储过程。
 */
@Mapper
public interface TmisRotationalScoreMapper {

    @Select(value = "{CALL tmis.FEEDBACK_ROTATIONAL_TASK_SCORE(" +
            "p_task_id => #{dto.taskId, jdbcType=VARCHAR, mode=IN}," +
            "p_score => #{dto.score, jdbcType=VARCHAR, mode=IN}" +
            ")}")
    @Options(statementType = StatementType.CALLABLE)
    void saveRotationalScore(@Param("dto") RotationalTaskScoreFeedbackDTO dto);
}