package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.mapper.TmisRotationalCompletionMapper;
import com.lucksoft.qingdao.tspm.dto.RotationalTaskCompletionFeedbackDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka 接口 8: 轮保完成情况 - 工作单元服务
 */
@Service
public class TmisRotationalCompletionService {

    private static final Logger log = LoggerFactory.getLogger(TmisRotationalCompletionService.class);

    @Autowired
    private TmisRotationalCompletionMapper mapper;

    @Transactional
    public void processRotationalCompletion(RotationalTaskCompletionFeedbackDTO dto) {
        try {
            log.info("调用存储过程 [tmis.FEEDBACK_COMPLETED_ROTATIONAL_TASK] for taskId: {}", dto.getTaskId());
            mapper.saveRotationalCompletion(dto);
        } catch (Exception e) {
            log.error("存储过程 [tmis.FEEDBACK_COMPLETED_ROTATIONAL_TASK] 执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储过程执行失败", e);
        }
    }
}