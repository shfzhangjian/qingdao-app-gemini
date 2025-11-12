package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.dto.*;
import com.lucksoft.qingdao.tspm.dto.FaultReportCodeFeedbackDTO;
import com.lucksoft.qingdao.tspm.dto.MaintenanceTaskDTO;
import com.lucksoft.qingdao.tspm.dto.ProductionHaltTaskDTO;
import com.lucksoft.qingdao.tspm.dto.RotationalPlanDTO;
import com.lucksoft.qingdao.tspm.dto.ScreenedRotationalTaskDTO;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * [已重构]
 * Oracle DTO 转换服务
 * 1. [已移除] 保养任务 (type 0,1,2,3) 的转换逻辑已移至 `V_MAINTENANCE_TASKS_RECENT` 视图。
 * 2. [保留] 维修计划 (type 13) 的转换逻辑。
 * 3. [保留] 轮保计划 (type 5) 的转换逻辑。
 * 4. [新增] 轮保任务 (type 7) 的视图 DTO 转换方法。
 * 5. [新增] 故障编码 (type 12) 的视图 DTO 转换方法。
 */
@Service
public class OracleDataTransformerService {

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    /**
     * 接口 1: (保养任务) 将视图 DTO 转换为标准 DTO
     * (这是一个 1:1 的直接转换，因为视图已完成所有工作)
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
     * 接口 5: (轮保计划) 将 EQ_PLANLB (主) 和 EQ_PLANLBDT (从) 转换为 DTO 列表
     * @param mainData 包含主从数据的 DTO
     * @return 转换后的 RotationalPlanDTO 列表
     */
    public List<RotationalPlanDTO> transformEqPlanLbTasks(EqPlanLbDTO mainData) {
        List<RotationalPlanDTO> plans = new ArrayList<>();
        if (mainData == null || mainData.getItems() == null) {
            return plans;
        }

        String createDate = (mainData.getDregt() != null) ? dtf.format(mainData.getDregt().toInstant()) : null;

        for (EqPlanLbDtDTO item : mainData.getItems()) {
            RotationalPlanDTO plan = new RotationalPlanDTO();
            plan.setPlanId(String.valueOf(item.getIndocno())); // 使用子表ID作为唯一计划ID
            plan.setEquipmentCode(item.getSfcode());
            plan.setPlanDate((item.getDbegin() != null) ? dtf.format(item.getDbegin().toInstant()).substring(0, 10) : null);
            plan.setCreateDate(createDate != null ? createDate.substring(0, 10) : null);
            plans.add(plan);
        }
        return plans;
    }

    /**
     * [新增] 接口 7: (轮保任务) 将视图 DTO 转换为标准 DTO
     * (这是一个 1:1 的直接转换)
     */
    public ScreenedRotationalTaskDTO transformVTaskToRotationalTask(VRotationalTaskDTO vTask) {
        ScreenedRotationalTaskDTO task = new ScreenedRotationalTaskDTO();
        task.setPlanId(vTask.getPlanId());
        task.setTaskId(vTask.getTaskId());
        task.setEquipmentCode(vTask.getEquipmentCode());
        task.setProject(vTask.getProject());
        task.setContent(vTask.getContent());
        task.setStandard(vTask.getStandard());
        task.setSource(vTask.getSource());
        task.setFullScore(vTask.getFullScore());
        task.setCreateDateTime(vTask.getCreateDateTime());
        task.setOperator(vTask.getOperator());
        return task;
    }

    /**
     * [新增] 接口 12: (故障编码) 将视图 DTO 转换为标准 DTO
     * (这是一个 1:1 的直接转换)
     */
    public FaultReportCodeFeedbackDTO transformVFaultReportCode(VFaultReportCodeDTO vCode) {
        FaultReportCodeFeedbackDTO dto = new FaultReportCodeFeedbackDTO();
        dto.setType(vCode.getType());
        dto.setId(vCode.getId());
        dto.setCode(vCode.getCode());
        return dto;
    }


    /**
     * 接口 13: (停产检修) 将 PM_MONTH (主) 和 PM_MONTH_ITEM (从) 转换为 DTO 列表
     * @param mainData 包含主从数据的 DTO
     * @return 转换后的 ProductionHaltTaskDTO 列表
     */
    public List<ProductionHaltTaskDTO> transformPmMonthTasks(PmMonthDTO mainData) {
        List<ProductionHaltTaskDTO> tasks = new ArrayList<>();
        if (mainData == null || mainData.getItems() == null) {
            return tasks;
        }

        for (PmMonthItemDTO item : mainData.getItems()) {
            ProductionHaltTaskDTO task = new ProductionHaltTaskDTO();
            task.setTaskId(String.valueOf(item.getIndocno())); // 使用子表ID作为唯一任务ID
            task.setEquipmentCode(item.getSfcode());
            task.setContent(item.getSitem() + " - " + item.getStodo()); // 检修项目 + 检修内容
            task.setHead(mainData.getSapplyer()); // 负责人 (使用主表提报人)
            task.setTeamName(item.getSdept()); // 班组 (使用子表承修单位)
            task.setExecutor(item.getSduty()); // 执行人 (使用子表维修负责人)
            task.setPlanStartTime((item.getDplanbegin() != null) ? dtf.format(item.getDplanbegin().toInstant()) : null);
            task.setPlanEndTime((item.getDplanend() != null) ? dtf.format(item.getDplanend().toInstant()) : null);
            tasks.add(task);
        }
        return tasks;
    }
}