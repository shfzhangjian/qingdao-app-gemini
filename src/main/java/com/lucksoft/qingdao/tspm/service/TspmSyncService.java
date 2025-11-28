package com.lucksoft.qingdao.tspm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lucksoft.qingdao.oracle.dto.VMaintenanceTaskDTO;
import com.lucksoft.qingdao.oracle.dto.VRotationalPlanDTO;
import com.lucksoft.qingdao.oracle.dto.VRotationalTaskDTO;
import com.lucksoft.qingdao.oracle.mapper.*;
import com.lucksoft.qingdao.oracle.service.OracleDataTransformerService;
import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tspm.dto.*;
import com.lucksoft.qingdao.tspm.dto.sync.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 专门处理 TsPM 系统侧对账/同步接口的业务逻辑
 */
@Service
public class TspmSyncService {

    @Autowired
    private VMaintenanceTasksMapper vMaintenanceTasksMapper;

    @Autowired
    private VRotationalPlanMapper vRotationalPlanMapper;

    @Autowired
    private VRotationalTaskMapper vRotationalTaskMapper; // [新增] 注入 Mapper

    @Autowired
    private OracleDataTransformerService transformerService;

    /**
     * 接口1: 获取保养、点检、润滑任务
     */
    public PageResult<MaintenanceTaskDTO> getMaintenanceTasks(MaintenanceTaskReq req) {
        if (req.getPageNum() != null && req.getPageSize() != null) {
            PageHelper.startPage(req.getPageNum(), req.getPageSize());
        }
        Map<String, Object> params = new HashMap<>();
        if (req.getType() != null) {
            params.put("types", Collections.singletonList(req.getType()));
        }
        List<VMaintenanceTaskDTO> vTasks = vMaintenanceTasksMapper.findTasksByCondition(req.getLastSyncDateTime(), params);

        PageInfo<VMaintenanceTaskDTO> pageInfo = new PageInfo<>(vTasks);
        int pages = req.getPageNum() == null ? 1 : pageInfo.getPages();

        List<MaintenanceTaskDTO> resultList = vTasks.stream()
                .map(transformerService::transformVTaskToMaintenanceTask)
                .collect(Collectors.toList());

        return new PageResult<>(resultList, req.getPageNum() != null ? req.getPageNum() : 1, req.getPageSize() != null ? req.getPageSize() : resultList.size(), pageInfo.getTotal(), pages);
    }

    /**
     * 接口5: 获取轮保计划排期
     */
    public PageResult<RotationalPlanDTO> getRotationalPlans(RotationalPlanReq req) {
        if (req.getPageNum() != null && req.getPageSize() != null) {
            PageHelper.startPage(req.getPageNum(), req.getPageSize());
        }

        List<VRotationalPlanDTO> vPlans = vRotationalPlanMapper.findPlansByCondition(req.getLastSyncDateTime(), null);

        PageInfo<VRotationalPlanDTO> pageInfo = new PageInfo<>(vPlans);
        int pages = req.getPageNum() == null ? 1 : pageInfo.getPages();

        List<RotationalPlanDTO> resultList = vPlans.stream()
                .map(this::convertVPlanToDto)
                .collect(Collectors.toList());

        return new PageResult<>(
                resultList,
                req.getPageNum() != null ? req.getPageNum() : 1,
                req.getPageSize() != null ? req.getPageSize() : resultList.size(),
                pageInfo.getTotal(),
                pages
        );
    }

    /**
     * [新增] 接口7: 获取筛选后轮保任务
     */
    public PageResult<ScreenedRotationalTaskDTO> getRotationalTasks(RotationalTaskReq req) {
        // 1. 处理分页
        if (req.getPageNum() != null && req.getPageSize() != null) {
            PageHelper.startPage(req.getPageNum(), req.getPageSize());
        }

        // 2. 执行查询 (调用 Mapper 的 findTasksByCondition，使用 null 作为 params，因为没有额外过滤条件)
        List<VRotationalTaskDTO> vTasks = vRotationalTaskMapper.findTasksByCondition(req.getLastSyncDateTime(), null);

        // 3. 获取分页信息
        PageInfo<VRotationalTaskDTO> pageInfo = new PageInfo<>(vTasks);
        int pages = req.getPageNum() == null ? 1 : pageInfo.getPages();

        // 4. 转换为标准 DTO (ScreenedRotationalTaskDTO)
        List<ScreenedRotationalTaskDTO> resultList = vTasks.stream()
                .map(transformerService::transformVTaskToRotationalTask)
                .collect(Collectors.toList());

        // 5. 封装返回
        return new PageResult<>(
                resultList,
                req.getPageNum() != null ? req.getPageNum() : 1,
                req.getPageSize() != null ? req.getPageSize() : resultList.size(),
                pageInfo.getTotal(),
                pages
        );
    }

    private RotationalPlanDTO convertVPlanToDto(VRotationalPlanDTO vDto) {
        RotationalPlanDTO dto = new RotationalPlanDTO();
        dto.setPlanId(vDto.getPlanId());
        dto.setEquipmentCode(vDto.getEquipmentCode());
        dto.setPlanDate(vDto.getPlanDate());
        dto.setCreateDate(vDto.getCreateDate());
        return dto;
    }

    @Autowired
    private PmMonthMapper pmMonthMapper;

    /**
     * [新增] 接口 13: 获取停产检修计划任务
     */
    public PageResult<ProductionHaltTaskDTO> getProductionHaltTasks(ProductionHaltTaskReq req) {
        // 1. 处理分页
        if (req.getPageNum() != null && req.getPageSize() != null) {
            PageHelper.startPage(req.getPageNum(), req.getPageSize());
        }

        // 2. 执行查询 (调用 PmMonthMapper 的 findHaltTasksByCondition)
        // 假设在之前 turn 4 中已为 PmMonthMapper 增加了 findHaltTasksByCondition 方法
        List<ProductionHaltTaskDTO> tasks = pmMonthMapper.findHaltTasksByCondition(req.getLastSyncDateTime(), null);

        // 3. 获取分页信息
        PageInfo<ProductionHaltTaskDTO> pageInfo = new PageInfo<>(tasks);
        int pages = req.getPageNum() == null ? 1 : pageInfo.getPages();

        // 4. 封装返回 (DTO 结构与视图一致，无需额外转换)
        return new PageResult<>(
                tasks,
                req.getPageNum() != null ? req.getPageNum() : 1,
                req.getPageSize() != null ? req.getPageSize() : tasks.size(),
                pageInfo.getTotal(),
                pages
        );
    }

    @Autowired
    private VUserEquipmentMapper vUserEquipmentMapper;

    /**
     * [新增] 接口 7 (实际为接口15/7): 获取包机信息
     */
    public PageResult<UserEquipmentDTO> getUserEquipments(UserEquipmentReq req) {
        // 1. 处理分页
        if (req.getPageNum() != null && req.getPageSize() != null) {
            PageHelper.startPage(req.getPageNum(), req.getPageSize());
        }

        // 2. 执行查询
        List<UserEquipmentDTO> list = vUserEquipmentMapper.findUserEquipmentsByCondition(req.getLastSyncDateTime());

        // 3. 获取分页信息
        PageInfo<UserEquipmentDTO> pageInfo = new PageInfo<>(list);
        int pages = req.getPageNum() == null ? 1 : pageInfo.getPages();

        // 4. 封装返回
        return new PageResult<>(
                list,
                req.getPageNum() != null ? req.getPageNum() : 1,
                req.getPageSize() != null ? req.getPageSize() : list.size(),
                pageInfo.getTotal(),
                pages
        );
    }
}