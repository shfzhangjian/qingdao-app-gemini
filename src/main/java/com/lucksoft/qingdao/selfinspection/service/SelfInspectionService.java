package com.lucksoft.qingdao.selfinspection.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lucksoft.qingdao.selfinspection.entity.SiLedger;
import com.lucksoft.qingdao.selfinspection.entity.SiTask;
import com.lucksoft.qingdao.selfinspection.entity.SiTaskDetail;
import com.lucksoft.qingdao.selfinspection.mapper.SiLedgerMapper;
import com.lucksoft.qingdao.selfinspection.mapper.SiTaskDetailMapper;
import com.lucksoft.qingdao.selfinspection.mapper.SiTaskMapper;
import com.lucksoft.qingdao.tmis.dto.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 自检自控业务逻辑服务
 */
@Service
public class SelfInspectionService {

    @Autowired
    private SiLedgerMapper ledgerMapper;

    @Autowired
    private SiTaskMapper taskMapper;

    @Autowired
    private SiTaskDetailMapper taskDetailMapper;

    // ==========================================
    // 台账管理 (Ledger)
    // ==========================================

    public PageResult<SiLedger> getLedgerPage(Map<String, Object> params) {
        int pageNum = Integer.parseInt(params.getOrDefault("pageNum", "1").toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "15").toString());

        PageHelper.startPage(pageNum, pageSize);
        List<SiLedger> list = ledgerMapper.findList(params);
        PageInfo<SiLedger> pageInfo = new PageInfo<>(list);

        return new PageResult<>(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    @Transactional
    public void saveLedger(SiLedger ledger) {
        if (ledger.getId() == null) {
            ledger.setAuditStatus("草稿");
            ledger.setHasStandard(0);
            ledgerMapper.insert(ledger);
        } else {
            ledgerMapper.update(ledger);
        }
    }

    @Transactional
    public void deleteLedger(Long id) {
        ledgerMapper.deleteById(id);
        // TODO: 关联删除 T_SI_STANDARD 等
    }

    public List<String> getLedgerOptions(String field) {
        // 防止 SQL 注入，这里应做白名单校验，简单起见省略
        return ledgerMapper.findDistinctValues(field);
    }

    // ==========================================
    // 任务管理 (Task)
    // ==========================================

    public PageResult<SiTask> getTaskPage(Map<String, Object> params) {
        int pageNum = Integer.parseInt(params.getOrDefault("pageNum", "1").toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "20").toString());

        PageHelper.startPage(pageNum, pageSize);
        List<SiTask> list = taskMapper.findList(params);
        PageInfo<SiTask> pageInfo = new PageInfo<>(list);

        return new PageResult<>(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    public List<SiTaskDetail> getTaskDetails(Long taskId) {
        return taskDetailMapper.findByTaskId(taskId);
    }

    /**
     * 提交任务详情 (检查人或操作工)
     * 自动判断更新任务主状态
     */
    @Transactional
    public void submitTaskDetails(Long taskId, List<SiTaskDetail> details, String userRole) {
        SiTask task = taskMapper.findById(taskId);
        if (task == null) throw new RuntimeException("任务不存在");

        boolean isInspector = "inspector".equals(userRole);

        // 1. 更新明细
        for (SiTaskDetail detail : details) {
            taskDetailMapper.update(detail);
        }

        // 2. 更新主任务状态
        if (isInspector) {
            task.setCheckStatus("已检");
            // checker 和 checkTime 应由 Controller 层从 Session 获取并传入，这里简化
            task.setChecker("Current User");
            task.setCheckTime(new java.util.Date());
        } else {
            // 检查是否所有明细都已确认
            boolean allConfirmed = details.stream().allMatch(d -> d.getIsConfirmed() != null && d.getIsConfirmed() == 1);
            if (allConfirmed) {
                task.setConfirmStatus("已确认");
                task.setConfirmer("Operator User");
                task.setConfirmTime(new java.util.Date());
            }
        }
        taskMapper.updateStatus(task);
    }
}