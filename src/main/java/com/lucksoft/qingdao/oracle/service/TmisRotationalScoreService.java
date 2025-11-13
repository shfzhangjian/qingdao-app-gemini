package com.lucksoft.qingdao.tmis.service;

import com.lucksoft.qingdao.oracle.mapper.TmisRotationalScoreMapper;
import com.lucksoft.qingdao.tspm.dto.RotationalTaskScoreFeedbackDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka 接口 9: 轮保得分 - 工作单元服务
 */
@Service
public class TmisRotationalScoreService {

    private static final Logger log = LoggerFactory.getLogger(TmisRotationalScoreService.class);

    @Autowired
    private TmisRotationalScoreMapper mapper;

    @Transactional
    public void processRotationalScore(RotationalTaskScoreFeedbackDTO dto) {
        try {
            log.info("调用存储过程 [tmis.FEEDBACK_ROTATIONAL_TASK_SCORE] for taskId: {}", dto.getTaskId());
            mapper.saveRotationalScore(dto);
        } catch (Exception e) {
            log.error("存储过程 [tmis.FEEDBACK_ROTATIONAL_TASK_SCORE] 执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储过程执行失败", e);
        }
    }
}