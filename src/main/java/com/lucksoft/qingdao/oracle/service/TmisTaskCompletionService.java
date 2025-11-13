package com.lucksoft.qingdao.tmis.service;

import com.lucksoft.qingdao.oracle.mapper.TmisTaskCompletionMapper;
import com.lucksoft.qingdao.tspm.dto.TaskCompletionFeedbackDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka 接口 2: 任务完成情况 - 工作单元服务
 */
@Service
public class TmisTaskCompletionService {

    private static final Logger log = LoggerFactory.getLogger(TmisTaskCompletionService.class);

    @Autowired
    private TmisTaskCompletionMapper mapper;

    @Transactional
    public void processTaskCompletion(TaskCompletionFeedbackDTO dto) {
        try {
            log.info("调用存储过程 [tmis.FEEDBACK_COMPLETED_MAINTENANCE_TASK] for taskId: {}", dto.getTaskId());
            mapper.saveTaskCompletion(dto);
        } catch (Exception e) {
            log.error("存储过程 [tmis.FEEDBACK_COMPLETED_MAINTENANCE_TASK] 执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储过程执行失败", e);
        }
    }
}