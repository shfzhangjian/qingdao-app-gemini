package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.dto.*;
import com.lucksoft.qingdao.tspm.dto.MaintenanceTaskDTO;
import com.lucksoft.qingdao.tspm.dto.ProductionHaltTaskDTO;
import com.lucksoft.qingdao.tspm.dto.RotationalPlanDTO;
import com.lucksoft.qingdao.oracle.dto.VMaintenanceTaskDTO; // [修改] 引入视图 DTO
import com.lucksoft.qingdao.oracle.dto.VRotationalPlanDTO; // [修改] 引入视图 DTO
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * [已重构]
 * Oracle DTO 转换服务
 * 1. 保养任务(日保/月保/例保)的转换逻辑已移至 V_MAINTENANCE_TASKS_RECENT 视图。
 * 2. 轮保计划的转换逻辑已移至 V_ROTATIONAL_PLANS_RECENT 视图。
 * 3. 本类现在只负责同步转换 维修计划(PM_MONTH) 和简单的 DTO 传递。
 */
@Service
public class OracleDataTransformerService {

    private final SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * [新]
     * 将从 V_MAINTENANCE_TASKS_RECENT 视图查询到的 DTO 转换为 MaintenanceTaskDTO
     * (此类用于解耦，尽管目前两个DTO结构相同)
     *
     * @param vTask 从视图查询到的 DTO
     * @return 转换后的 MaintenanceTaskDTO
     */
    public MaintenanceTaskDTO transformVTaskToMaintenanceTask(VMaintenanceTaskDTO vTask) {
        MaintenanceTaskDTO task = new MaintenanceTaskDTO();
        task.setPlanId(vTask.getPlanId());
        task.setTaskId(vTask.getTaskId());
        task.setEquipmentCode(vTask.getEquipmentCode());
        task.setType(vTask.getType());
        task.setProject(vTask.getProject());
        task.setContent(vTask.getContent());
        task.setStandard(vTask.getStandard());
        task.setTool(vTask.getTool());
        task.setFullScore(vTask.getFullScore());
        task.setCreateDateTime(vTask.getCreateDateTime());
        task.setPlanStartTime(vTask.getPlanStartTime());
        task.setOperator(vTask.getOperator());
        task.setOilId(vTask.getOilId());
        task.setOilQuantity(vTask.getOilQuantity());
        return task;
    }

    /**
     * [已重构]
     * 将从 V_ROTATIONAL_PLANS_RECENT 视图查询到的 DTO 转换为 RotationalPlanDTO
     *
     * @param vPlan 从视图查询到的 DTO
     * @return 转换后的 RotationalPlanDTO
     */
    public RotationalPlanDTO transformVPlanToRotationalPlan(VRotationalPlanDTO vPlan) {
        RotationalPlanDTO plan = new RotationalPlanDTO();
        plan.setPlanId(vPlan.getPlanId());
        plan.setEquipmentCode(vPlan.getEquipmentCode());
        plan.setPlanDate(vPlan.getPlanDate());
        plan.setCreateDate(vPlan.getCreateDate());
        return plan;
    }


    /**
     * 4. 转换维修计划 (PM_MONTH_ARCHIVED)
     * 将一个 PM_MONTH 主表和多个 PM_MONTH_ITEM 子表记录转换为一个 ProductionHaltTaskDTO 列表
     *
     * @param pmMonthData 包含主表和子表数据的 DTO
     * @return 转换后的 ProductionHaltTaskDTO 列表
     */
    public List<ProductionHaltTaskDTO> transformPmMonthTasks(PmMonthDTO pmMonthData) {
        if (pmMonthData == null || pmMonthData.getItems() == null || pmMonthData.getItems().isEmpty()) {
            return new ArrayList<>();
        }

        // 维修计划 (PM_MONTH) 被视为停产检修 (ProductionHaltTask)
        return pmMonthData.getItems().stream()
                .map(item -> {
                    ProductionHaltTaskDTO task = new ProductionHaltTaskDTO();

                    // 关键字段映射
                    task.setTaskId(item.getIndocno().toString()); // 任务唯一标识 (使用子表主键)
                    task.setEquipmentCode(item.getSfcode()); // 设备编码
                    task.setContent(item.getStodo()); // 检修内容
                    task.setHead(item.getSduty()); // 负责人
                    task.setTeamName(item.getSdept()); // 班组 (承修单位)
                    task.setExecutor(item.getSduty()); // 执行人 (使用负责人)

                    // 日期格式化
                    task.setPlanStartTime(formatDate(item.getDplanbegin())); // 计划开始时间
                    task.setPlanEndTime(formatDate(item.getDplanend())); // 计划结束时间

                    return task;
                })
                .collect(Collectors.toList());
    }

    /**
     * 辅助方法，安全地格式化日期
     */
    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        // SimpleDateFormat 不是线程安全的，所以在调用时进行同步
        synchronized (dateTimeFormatter) {
            return dateTimeFormatter.format(date);
        }
    }
}