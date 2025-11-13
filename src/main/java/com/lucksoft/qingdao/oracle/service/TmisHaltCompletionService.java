package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.mapper.TmisHaltCompletionMapper;
import com.lucksoft.qingdao.tspm.dto.ProductionHaltCompletionFeedbackDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka 接口 14: 停产检修完成情况 - 工作单元服务
 */
@Service
public class TmisHaltCompletionService {

    private static final Logger log = LoggerFactory.getLogger(TmisHaltCompletionService.class);

    @Autowired
    private TmisHaltCompletionMapper mapper;

    @Transactional
    public void processHaltCompletion(ProductionHaltCompletionFeedbackDTO dto) {
        try {
            log.info("调用存储过程 [tmis.FEEDBACK_COMPLETED_PRODUCTION_HALT_MAINTENANCE_TASK] for taskId: {}", dto.getTaskId());
            mapper.saveHaltCompletion(dto);
        } catch (Exception e) {
            log.error("存储过程 [tmis.FEEDBACK_COMPLETED_PRODUCTION_HALT_MAINTENANCE_TASK] 执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储过程执行失败", e);
        }
    }
}