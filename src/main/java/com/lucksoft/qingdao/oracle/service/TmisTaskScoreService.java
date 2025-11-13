package com.lucksoft.qingdao.tmis.service;

import com.lucksoft.qingdao.oracle.mapper.TmisTaskScoreMapper;
import com.lucksoft.qingdao.tspm.dto.TaskScoreFeedbackDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka 接口 3: 任务得分 - 工作单元服务
 */
@Service
public class TmisTaskScoreService {

    private static final Logger log = LoggerFactory.getLogger(TmisTaskScoreService.class);

    @Autowired
    private TmisTaskScoreMapper mapper;

    @Transactional
    public void processTaskScore(TaskScoreFeedbackDTO dto) {
        try {
            log.info("调用存储过程 [tmis.FEEDBACK_MAINTENANCE_TASK_SCORE] for taskId: {}", dto.getTaskId());
            mapper.saveTaskScore(dto);
        } catch (Exception e) {
            log.error("存储过程 [tmis.FEEDBACK_MAINTENANCE_TASK_SCORE] 执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储过程执行失败", e);
        }
    }
}