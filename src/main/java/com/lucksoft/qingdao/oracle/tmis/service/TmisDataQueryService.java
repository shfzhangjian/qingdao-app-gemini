package com.lucksoft.qingdao.oracle.tmis.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lucksoft.qingdao.oracle.dto.VMaintenanceTaskDTO;
import com.lucksoft.qingdao.oracle.dto.VRotationalPlanDTO;
import com.lucksoft.qingdao.oracle.dto.VRotationalTaskDTO;
import com.lucksoft.qingdao.oracle.mapper.*;
import com.lucksoft.qingdao.oracle.service.OracleDataTransformerService;
import com.lucksoft.qingdao.oracle.tmis.dto.TmisDataQueryDTO;
import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tspm.dto.ProductionHaltTaskDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TMIS 数据主动查询服务
 * (JDK 1.8 兼容版)
 */
@Service
public class TmisDataQueryService {

    @Autowired
    private VMaintenanceTasksMapper vMaintenanceTasksMapper;
    @Autowired
    private VRotationalPlanMapper vRotationalPlanMapper;
    @Autowired
    private VRotationalTaskMapper vRotationalTaskMapper;
    @Autowired
    private PmMonthMapper pmMonthMapper;
    @Autowired
    private VFaultReportCodeMapper vFaultReportCodeMapper;

    @Autowired
    private OracleDataTransformerService transformerService;

    /**
     * 根据请求参数查询数据
     */
    public PageResult<?> queryData(TmisDataQueryDTO query) {
        // 开启分页
        PageHelper.startPage(query.getPageNum(), query.getPageSize());

        String topic = query.getTopic();
        if (topic == null) {
            throw new IllegalArgumentException("Topic cannot be null");
        }

        List<?> resultList;
        long total;
        int pages;

        // 根据 Topic 路由
        switch (topic) {
            // 接口 1: 保养任务
            case "tims.sync.maintenance.task":
                List<VMaintenanceTaskDTO> vTasks = vMaintenanceTasksMapper.findTasksByCondition(query.getUpdateTime(), query.getBody());
                PageInfo<VMaintenanceTaskDTO> taskPageInfo = new PageInfo<>(vTasks);
                total = taskPageInfo.getTotal();
                pages = taskPageInfo.getPages();
                // 转换 DTO
                resultList = vTasks.stream()
                        .map(transformerService::transformVTaskToMaintenanceTask)
                        .collect(Collectors.toList());
                break;

            // 接口 5: 轮保计划
            case "tims.sync.rotational.plan":
                List<VRotationalPlanDTO> plans = vRotationalPlanMapper.findPlansByCondition(query.getUpdateTime(), query.getBody());
                PageInfo<VRotationalPlanDTO> planPageInfo = new PageInfo<>(plans);
                total = planPageInfo.getTotal();
                pages = planPageInfo.getPages();
                // 视图 DTO 与 Kafka DTO 结构一致，直接返回
                resultList = plans;
                break;

            // 接口 7: 轮保任务
            case "tims.sync.rotational.task":
                List<VRotationalTaskDTO> rotTasks = vRotationalTaskMapper.findTasksByCondition(query.getUpdateTime(), query.getBody());
                PageInfo<VRotationalTaskDTO> rotTaskPageInfo = new PageInfo<>(rotTasks);
                total = rotTaskPageInfo.getTotal();
                pages = rotTaskPageInfo.getPages();
                resultList = rotTasks.stream()
                        .map(transformerService::transformVTaskToRotationalTask)
                        .collect(Collectors.toList());
                break;

            // 接口 13: 停产检修
            case "tims.sync.production.halt.maintenance.task":
                List<ProductionHaltTaskDTO> haltTasks = pmMonthMapper.findHaltTasksByCondition(query.getUpdateTime(), query.getBody());
                PageInfo<ProductionHaltTaskDTO> haltPageInfo = new PageInfo<>(haltTasks);
                total = haltPageInfo.getTotal();
                pages = haltPageInfo.getPages();
                resultList = haltTasks; // DTO 结构一致
                break;

            default:
                throw new IllegalArgumentException("Unsupported topic for query: " + topic);
        }

        return new PageResult<>(resultList, query.getPageNum(), query.getPageSize(), total, pages);
    }
}