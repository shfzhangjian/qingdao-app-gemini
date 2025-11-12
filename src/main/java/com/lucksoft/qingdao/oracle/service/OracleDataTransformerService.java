package com.lucksoft.qingdao.oracle.service;

import com.lucksoft.qingdao.oracle.dto.EqPlanLbDTO;
import com.lucksoft.qingdao.oracle.dto.EqPlanLbDtDTO;
import com.lucksoft.qingdao.oracle.dto.PmissionBoardBaoYangDTO;
import com.lucksoft.qingdao.oracle.dto.PmissionBoardDTO;
import com.lucksoft.qingdao.oracle.dto.PmissionBoardDayDTO;
import com.lucksoft.qingdao.oracle.dto.PmMonthDTO;
import com.lucksoft.qingdao.oracle.dto.PmMonthItemDTO;
import com.lucksoft.qingdao.tspm.dto.MaintenanceTaskDTO;
import com.lucksoft.qingdao.tspm.dto.ProductionHaltTaskDTO;
import com.lucksoft.qingdao.tspm.dto.RotationalPlanDTO;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 新增服务：
 * 负责将从 Oracle 接收到的 DTOs 转换为标准的 TIMS DTOs (即 simulate.html 使用的实体)。
 */
@Service
public class OracleDataTransformerService {

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 转换 "日保" 任务 (PmissionBoardDayDTO)
     * @param oracleTask Oracle DTO
     * @return TIMS MaintenanceTaskDTO
     */
    public MaintenanceTaskDTO transformDayTask(PmissionBoardDayDTO oracleTask) {
        MaintenanceTaskDTO timsTask = new MaintenanceTaskDTO();
        String taskId = "T-DAY-" + oracleTask.getIdocid();
        String planId = "P-DAY-" + oracleTask.getIdocid();

        timsTask.setPlanId(planId);
        timsTask.setTaskId(taskId);
        // PmissionBoardDayDTO 中缺少 equipmentCode，我们暂时使用 equipmentName 作为替代
        timsTask.setEquipmentCode(oracleTask.getMakeFlName());
        timsTask.setType(1); // 1-日保
        timsTask.setProject(oracleTask.getMiTitle());
        timsTask.setContent(oracleTask.getMiTitle()); // 日保任务中，项目、内容、标准相同
        timsTask.setStandard(oracleTask.getMiTitle());
        timsTask.setFullScore(10); // 默认分值
        timsTask.setCreateDateTime(formatDate(oracleTask.getdRegt()));
        timsTask.setPlanStartTime(formatDate(oracleTask.getdBegin()));
        timsTask.setOperator(oracleTask.getsDuty());

        return timsTask;
    }

    /**
     * 转换 "轮保/月保" 任务 (PmissionBoardDTO)
     * @param oracleTask Oracle DTO
     * @return TIMS MaintenanceTaskDTO
     */
    public MaintenanceTaskDTO transformBoardLbTask(PmissionBoardDTO oracleTask) {
        MaintenanceTaskDTO timsTask = new MaintenanceTaskDTO();
        String taskId = "T-LB-" + oracleTask.getIdocid();
        String planId = "P-LB-" + oracleTask.getIdocid();

        timsTask.setPlanId(planId);
        timsTask.setTaskId(taskId);
        // PmissionBoardDTO 中缺少 equipmentCode，我们使用 ieqno (设备内码) 作为替代
        timsTask.setEquipmentCode(String.valueOf(oracleTask.getIeqno()));

        // 31-轮保, 32-月保
        if (oracleTask.getDjClass() != null && oracleTask.getDjClass() == 31) {
            timsTask.setType(3); // 3-轮保
        } else {
            timsTask.setType(2); // 2-月保 (默认)
        }

        timsTask.setProject(oracleTask.getMititle());
        timsTask.setContent(oracleTask.getMititle());
        timsTask.setStandard(oracleTask.getMititle());
        timsTask.setFullScore(10); // 默认分值
        timsTask.setCreateDateTime(formatDate(oracleTask.getMakedate()));
        timsTask.setPlanStartTime(formatDate(oracleTask.getMidate()));
        timsTask.setOperator(oracleTask.getSduty());

        return timsTask;
    }

    /**
     * 转换 "例保" 任务 (PmissionBoardBaoYangDTO)
     * @param oracleTask Oracle DTO
     * @return TIMS MaintenanceTaskDTO
     */
    public MaintenanceTaskDTO transformBaoYangTask(PmissionBoardBaoYangDTO oracleTask) {
        MaintenanceTaskDTO timsTask = new MaintenanceTaskDTO();
        String taskId = "T-BY-" + oracleTask.getIdocid();
        String planId = "P-BY-" + oracleTask.getIdocid();

        timsTask.setPlanId(planId);
        timsTask.setTaskId(taskId);
        timsTask.setEquipmentCode(oracleTask.getMakeflcode());
        timsTask.setType(0); // 0-保养(例保)
        timsTask.setProject(oracleTask.getMititle());
        timsTask.setContent(oracleTask.getMititle());
        timsTask.setStandard(oracleTask.getMititle());
        timsTask.setFullScore(10); // 默认分值
        timsTask.setCreateDateTime(formatDate(oracleTask.getMakedate()));
        timsTask.setPlanStartTime(formatDate(oracleTask.getDbegin()));
        timsTask.setOperator(oracleTask.getSduty());

        return timsTask;
    }

    /**
     * 转换 "维修计划" (PmMonthDTO) 为停产检修任务列表
     * @param mainData 包含主表和子项的 Oracle DTO
     * @return TIMS ProductionHaltTaskDTO 列表
     */
    public List<ProductionHaltTaskDTO> transformPmMonthTasks(PmMonthDTO mainData) {
        if (mainData == null || mainData.getItems() == null) {
            return Collections.emptyList();
        }

        return mainData.getItems().stream()
                .map(item -> {
                    ProductionHaltTaskDTO timsTask = new ProductionHaltTaskDTO();
                    timsTask.setTaskId(String.valueOf(item.getIndocno())); // 任务ID = 维修计划明细ID
                    timsTask.setEquipmentCode(item.getSfcode());

                    // 组合检修项目和内容
                    String content = (item.getSitem() != null ? item.getSitem() : "") +
                            (item.getStodo() != null ? ": " + item.getStodo() : "");
                    timsTask.setContent(content);

                    timsTask.setHead(mainData.getSapplyer()); // 负责人 = 主表提报人
                    timsTask.setTeamName(item.getSdept());    // 班组 = 子项承修单位
                    timsTask.setExecutor(item.getSduty());    // 执行人 = 子项维修负责人

                    timsTask.setPlanStartTime(formatDate(item.getDplanbegin()));
                    timsTask.setPlanEndTime(formatDate(item.getDplanend()));

                    return timsTask;
                })
                .collect(Collectors.toList());
    }

    /**
     * [新增] 转换 "轮保计划" (EqPlanLbDTO) 为轮保计划排期列表
     * @param mainData 包含主表和子项的 Oracle DTO
     * @return TIMS RotationalPlanDTO 列表
     */
    public List<RotationalPlanDTO> transformEqPlanLbTasks(EqPlanLbDTO mainData) {
        if (mainData == null || mainData.getItems() == null) {
            return Collections.emptyList();
        }

        // 轮保计划 (TIMS DTO) 是一个扁平列表，
        // Oracle (EAM DTO) 是一个主-从结构。
        // 我们将 Oracle 的 *每一个子项* 转换为一个 TIMS 计划项。
        return mainData.getItems().stream()
                .map(item -> {
                    RotationalPlanDTO timsPlan = new RotationalPlanDTO();

                    // 计划ID = Oracle 主表SNO + 子表ID
                    String planId = (mainData.getSno() != null ? mainData.getSno() : mainData.getIndocno())
                            + "-" + item.getIndocno();

                    timsPlan.setPlanId(planId);
                    timsPlan.setEquipmentCode(item.getSfcode());
                    timsPlan.setPlanDate(formatDate(item.getDbegin()));
                    timsPlan.setCreateDate(formatDate(mainData.getDregt()));

                    return timsPlan;
                })
                .collect(Collectors.toList());
    }


    /**
     * 辅助方法：安全地格式化日期
     */
    private String formatDate(Date date) {
        if (date == null) {
            return null;
        }
        synchronized (SDF) {
            return SDF.format(date);
        }
    }
}