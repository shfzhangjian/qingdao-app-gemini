package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.mapper.TmisRecommendTaskMapper;
import com.lucksoft.qingdao.tspm.dto.RecommendedRotationalTaskDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Kafka 接口 6: 推荐任务 - 工作单元服务
 */
@Service
public class TmisRecommendTaskService {

    private static final Logger log = LoggerFactory.getLogger(TmisRecommendTaskService.class);

    @Autowired
    private TmisRecommendTaskMapper mapper;

    @Transactional
    public void processRecommendTask(RecommendedRotationalTaskDTO dto) {
        try {
            log.info("调用存储过程 [tmis.RECOMMEND_ROTATIONAL_TASK] for planId: {}", dto.getPlanId());
            mapper.saveRecommendTask(dto);
        } catch (Exception e) {
            log.error("存储过程 [tmis.RECOMMEND_ROTATIONAL_TASK] 执行失败: {}", e.getMessage(), e);
            throw new RuntimeException("存储过程执行失败", e);
        }
    }
}