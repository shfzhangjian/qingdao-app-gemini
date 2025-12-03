package com.lucksoft.qingdao.tspm.service;

import com.github.pagehelper.PageHelper;
import com.lucksoft.qingdao.oracle.dto.VMaintenanceTaskDTO;
import com.lucksoft.qingdao.oracle.dto.VRotationalPlanDTO;
import com.lucksoft.qingdao.oracle.dto.VRotationalTaskDTO;
import com.lucksoft.qingdao.oracle.mapper.*;
import com.lucksoft.qingdao.oracle.service.OracleDataTransformerService;
// import com.lucksoft.qingdao.tmis.dto.PageResult; // [移除] 不再需要分页封装
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
 * [修改] 所有接口现在直接返回 List 数据，去除 list/total 包装。
 */
@Service
public class TspmSyncService {

    @Autowired
    private VMaintenanceTasksMapper vMaintenanceTasksMapper;

    @Autowired
    private VRotationalPlanMapper vRotationalPlanMapper;

    @Autowired
    private VRotationalTaskMapper vRotationalTaskMapper;

    @Autowired
    private PmMonthMapper pmMonthMapper;

    @Autowired
    private VUserEquipmentMapper vUserEquipmentMapper;

    @Autowired
    private OracleDataTransformerService transformerService;

    /**
     * 接口1: 获取保养、点检、润滑任务
     * [修改] 返回类型改为 List<MaintenanceTaskDTO>
     */
    public List<MaintenanceTaskDTO> getMaintenanceTasks(MaintenanceTaskReq req) {
        // 即使不返回分页信息，如果前端传了 limit，我们仍然在 SQL 层做限制以提升性能
        if (req.getPageNum() != null && req.getPageSize() != null) {
            PageHelper.startPage(req.getPageNum(), req.getPageSize());
        }
        Map<String, Object> params = new HashMap<>();
        if (req.getType() != null) {
            params.put("types", Collections.singletonList(req.getType()));
        }
        List<VMaintenanceTaskDTO> vTasks = vMaintenanceTasksMapper.findTasksByCondition(req.getLastSyncDateTime(), params);

        // 直接转换并返回 List，不再封装 PageInfo
        return vTasks.stream()
                .map(transformerService::transformVTaskToMaintenanceTask)
                .collect(Collectors.toList());
    }

    /**
     * 接口5: 获取轮保计划排期
     * [修改] 返回类型改为 List<RotationalPlanDTO>
     */
    public List<RotationalPlanDTO> getRotationalPlans(RotationalPlanReq req) {
        if (req.getPageNum() != null && req.getPageSize() != null) {
            PageHelper.startPage(req.getPageNum(), req.getPageSize());
        }

        List<VRotationalPlanDTO> vPlans = vRotationalPlanMapper.findPlansByCondition(req.getLastSyncDateTime(), null);

        return vPlans.stream()
                .map(this::convertVPlanToDto)
                .collect(Collectors.toList());
    }

    /**
     * 接口7: 获取筛选后轮保任务
     * [修改] 返回类型改为 List<ScreenedRotationalTaskDTO>
     */
    public List<ScreenedRotationalTaskDTO> getRotationalTasks(RotationalTaskReq req) {
        if (req.getPageNum() != null && req.getPageSize() != null) {
            PageHelper.startPage(req.getPageNum(), req.getPageSize());
        }

        List<VRotationalTaskDTO> vTasks = vRotationalTaskMapper.findTasksByCondition(req.getLastSyncDateTime(), null);

        return vTasks.stream()
                .map(transformerService::transformVTaskToRotationalTask)
                .collect(Collectors.toList());
    }

    private RotationalPlanDTO convertVPlanToDto(VRotationalPlanDTO vDto) {
        RotationalPlanDTO dto = new RotationalPlanDTO();
        dto.setPlanId(vDto.getPlanId());
        dto.setEquipmentCode(vDto.getEquipmentCode());
        dto.setPlanDate(vDto.getPlanDate());
        dto.setCreateDate(vDto.getCreateDate());
        return dto;
    }

    /**
     * 接口 13: 获取停产检修计划任务
     * [修改] 返回类型改为 List<ProductionHaltTaskDTO>
     */
    public List<ProductionHaltTaskDTO> getProductionHaltTasks(ProductionHaltTaskReq req) {
        if (req.getPageNum() != null && req.getPageSize() != null) {
            PageHelper.startPage(req.getPageNum(), req.getPageSize());
        }

        return pmMonthMapper.findHaltTasksByCondition(req.getLastSyncDateTime(), null);
    }

    /**
     * 接口 7 (实际为接口15): 获取包机信息
     * [修改] 返回类型改为 List<UserEquipmentDTO>
     */
    public List<UserEquipmentDTO> getUserEquipments(UserEquipmentReq req) {
        if (req.getPageNum() != null && req.getPageSize() != null) {
            PageHelper.startPage(req.getPageNum(), req.getPageSize());
        }

        return vUserEquipmentMapper.findUserEquipmentsByCondition(req.getLastSyncDateTime());
    }
}