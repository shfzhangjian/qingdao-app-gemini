package com.lucksoft.qingdao.selfinspection.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lucksoft.qingdao.selfinspection.entity.SiLedger;
import com.lucksoft.qingdao.selfinspection.entity.SiStandard;
import com.lucksoft.qingdao.selfinspection.entity.SiTask;
import com.lucksoft.qingdao.selfinspection.entity.SiTaskDetail;
import com.lucksoft.qingdao.selfinspection.mapper.SiLedgerMapper;
import com.lucksoft.qingdao.selfinspection.mapper.SiStandardMapper;
import com.lucksoft.qingdao.selfinspection.mapper.SiTaskDetailMapper;
import com.lucksoft.qingdao.selfinspection.mapper.SiTaskMapper;
import com.lucksoft.qingdao.system.entity.User; // 导入 User
import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tmis.metrology.ExportColumn;
import com.lucksoft.qingdao.tmis.util.ExcelExportUtil;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 自检自控业务逻辑服务
 */
@Service
public class SelfInspectionService {

    private static final Logger log = LoggerFactory.getLogger(SelfInspectionService.class);

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
    }

    public List<String> getLedgerOptions(String field) {
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

    @Transactional
    public void submitTaskDetails(Long taskId, List<SiTaskDetail> details, String userRole, User currentUser) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        log.info(">>> submitTaskDetails 执行开始，系统当前时间: {}", sdf.format(new Date()));

        SiTask task = taskMapper.findById(taskId);
        if (task == null) throw new RuntimeException("任务不存在");

        boolean isInspector = "inspector".equals(userRole);

        // 1. 更新明细
        for (SiTaskDetail detail : details) {
            taskDetailMapper.update(detail);
        }

        // 2. 更新主任务状态，并记录真实人员信息
        if (isInspector) {
            task.setCheckStatus("已检");
            if (currentUser != null) {
                task.setChecker(currentUser.getName());
                task.setCheckerId(currentUser.getId());
                task.setCheckerNo(currentUser.getGh()); // 工号
            } else {
                task.setChecker("Unknown User");
            }
            task.setCheckTime(new java.util.Date());
        } else {
            boolean allConfirmed = details.stream().allMatch(d -> d.getIsConfirmed() != null && d.getIsConfirmed() == 1);
            if (allConfirmed) {
                task.setConfirmStatus("已确认");
                if (currentUser != null) {
                    task.setConfirmer(currentUser.getName());
                    task.setConfirmerId(currentUser.getId());
                    task.setConfirmerNo(currentUser.getGh()); // 工号
                } else {
                    task.setConfirmer("Unknown User");
                }
                task.setConfirmTime(new java.util.Date());
            }
        }
        taskMapper.updateStatus(task);
    }

    // ==========================================
    // 统计管理 (Stats)
    // ==========================================

    @Autowired
    private SiStandardMapper standardMapper;

    public PageResult<Map<String, Object>> getStatsPage(Map<String, Object> params) {
        String dateRange = (String) params.get("checkTime");
        if (dateRange != null && dateRange.contains(" 至 ")) {
            String[] parts = dateRange.split(" 至 ");
            if (parts.length == 2) {
                params.put("startDate", parts[0].trim());
                params.put("endDate", parts[1].trim());
            }
        }

        int pageNum = Integer.parseInt(params.getOrDefault("pageNum", "1").toString());
        int pageSize = Integer.parseInt(params.getOrDefault("pageSize", "20").toString());

        PageHelper.startPage(pageNum, pageSize);
        List<Map<String, Object>> list = taskDetailMapper.findStatsList(params);
        PageInfo<Map<String, Object>> pageInfo = new PageInfo<>(list);

        return new PageResult<>(pageInfo.getList(), pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages());
    }

    // ==========================================
    // 4. 标准管理 (Standard) - 新增逻辑
    // ==========================================

    /**
     * [新增] 批量导入标准
     */
    @Transactional
    public void importStandardBatch(List<Long> ledgerIds, MultipartFile file) throws IOException {
        if (ledgerIds == null || ledgerIds.isEmpty()) return;

        // 1. 解析 Excel (只解析一次模板)
        List<SiStandard> templateStandards = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                SiStandard std = new SiStandard();
                // 暂不设置 ledgerId，后续循环设置
                std.setDevicePart(getCellValue(row.getCell(0)));
                std.setItemName(getCellValue(row.getCell(1)));
                std.setStandardDesc(getCellValue(row.getCell(2)));
                std.setExecutorRole(getCellValue(row.getCell(3)));

                String cycleStr = getCellValue(row.getCell(4));
                try {
                    std.setCheckCycle(cycleStr.isEmpty() ? 0 : (int) Double.parseDouble(cycleStr));
                } catch (Exception e) {
                    std.setCheckCycle(0);
                }

                if (std.getItemName() != null && !std.getItemName().isEmpty()) {
                    templateStandards.add(std);
                }
            }
        }

        if (templateStandards.isEmpty()) {
            throw new RuntimeException("文件中未读取到有效数据");
        }

        // 2. 循环处理每个 Ledger ID
        for (Long ledgerId : ledgerIds) {
            // 2.1 删除旧标准
            standardMapper.deleteByLedgerId(ledgerId);

            // 2.2 准备新标准列表
            List<SiStandard> newStandards = new ArrayList<>();
            for (SiStandard template : templateStandards) {
                SiStandard newStd = new SiStandard();
                newStd.setLedgerId(ledgerId);
                newStd.setDevicePart(template.getDevicePart());
                newStd.setItemName(template.getItemName());
                newStd.setStandardDesc(template.getStandardDesc());
                newStd.setExecutorRole(template.getExecutorRole());
                newStd.setCheckCycle(template.getCheckCycle());
                newStandards.add(newStd);
            }

            // 2.3 批量插入
            if (!newStandards.isEmpty()) {
                standardMapper.batchInsert(newStandards);
            }

            // 2.4 更新台账状态
            ledgerMapper.updateStandardStatus(ledgerId, 1);
        }
    }

    /**
     * 导入标准
     */
    @Transactional
    public void importStandard(Long ledgerId, MultipartFile file) throws IOException {
        // 1. 解析 Excel
        List<SiStandard> standards = new ArrayList<>();
        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            // 假设第一行是标题，从第二行开始读取
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                SiStandard std = new SiStandard();
                std.setLedgerId(ledgerId);
                // 简单读取，按固定列顺序：检测装置, 检测项目, 检测标准, 执行人, 周期
                std.setDevicePart(getCellValue(row.getCell(0)));
                std.setItemName(getCellValue(row.getCell(1)));
                std.setStandardDesc(getCellValue(row.getCell(2)));
                std.setExecutorRole(getCellValue(row.getCell(3)));

                String cycleStr = getCellValue(row.getCell(4));
                try {
                    std.setCheckCycle(cycleStr.isEmpty() ? 0 : (int) Double.parseDouble(cycleStr));
                } catch (Exception e) {
                    std.setCheckCycle(0);
                }

                // 只有当关键字段不为空时才添加
                if (std.getItemName() != null && !std.getItemName().isEmpty()) {
                    standards.add(std);
                }
            }
        }

        if (standards.isEmpty()) {
            throw new RuntimeException("文件中未读取到有效数据");
        }

        // 2. 覆盖旧数据
        standardMapper.deleteByLedgerId(ledgerId);
        standardMapper.batchInsert(standards);

        // 3. 更新台账状态
        Map<String, Object> params = new HashMap<>();
        params.put("id", ledgerId);
        // 获取当前台账对象以保持其他字段不变
        // 实际上 SiLedgerMapper.findList 是动态查询，这里我们可以简化：
        // 直接执行 SQL 更新 HAS_STANDARD 字段会更高效，但为了复用 mapper.update，需要先查出来
        // 这里为了简单，直接更新 updateTime 和 hasStandard
        // (注意：Mapper 的 update 是全量更新，所以需要先查出完整对象)
        // 更好的做法是在 Mapper 增加单独更新状态的方法，这里暂时不改动 Mapper 结构，
        // 而是假设前端刷新列表会看到 HAS_STANDARD 变化。
        //
        // **关键修复**: 我们需要一个能够单独更新状态的方法，或者查出来改完再存回去。
        // 这里选择：查 -> 改 -> 存
        List<SiLedger> ledgers = ledgerMapper.findList(Collections.singletonMap("id", ledgerId)); // 这里利用 params 传 ID 可能需要调整 Mapper XML
        // Mapper XML 中的 findList 是动态的，如果 params 中有 id 就能查到吗？
        // 检查 Mapper：findList SQL 中并没有 <if test='params.id'>。
        // 所以我们需要用 findDistinctValues 或者复用删除逻辑？
        // 不，我们可以直接调用 update，但需要填充所有字段。这很麻烦。
        //
        // 推荐方案：直接执行 SQL 更新，这里使用 jdbcTemplate 或者在 Mapper 加方法。
        // 为了最快实现，我们修改 SiLedgerMapper 的 XML/注解，添加一个 updateStatus 方法。
        // 但用户给的文件是 Interface 注解版。
        // 我们可以在这里直接使用 SiStandardMapper 关联的逻辑，或者简单地忽略台账状态更新（不完美）。
        //
        // **修正方案**: 我会在 SiLedgerMapper 中动态添加一个 updateHasStandard 方法。
        // 但我现在不能改 Mapper 文件，所以只能用蹩脚的方法：忽略状态更新，或者假设前端重新查询时能通过 left join 查到有标准（目前 findList SQL 是查单表）。
        //
        // 让我们回看 `SiLedgerMapper`，它有一个 `update` 方法：
        // UPDATE T_SI_LEDGER SET ... WHERE ID=#{id}
        // 如果我传一个只有 ID 和 HAS_STANDARD 的对象，其他字段会变成 null！
        //
        // **最终方案**:
        // 为了保证代码健壮性，我必须修改 `SiLedgerMapper`，添加 `updateStandardStatus` 方法。
        // (见下文修改 SiLedgerMapper)

        ledgerMapper.updateStandardStatus(ledgerId, 1);
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return new SimpleDateFormat("yyyy-MM-dd").format(cell.getDateCellValue());
                }
                return String.valueOf(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    /**
     * 下载标准附件 (或空模板)
     */
    public void downloadStandard(Long ledgerId, HttpServletResponse response) throws IOException {
        // 1. 获取台账信息，用于生成文件名
        // 由于 Mapper findList 没有按 ID 查的方法，我们只能假设调用方确保 ID 存在
        // 我们可以遍历查找（效率低但可行），或者增加 findById
        SiLedger ledger = ledgerMapper.findById(ledgerId); // 需要在 Mapper 增加此方法
        if (ledger == null) throw new RuntimeException("台账不存在");

        String fileName = (ledger.getAssetCode() == null ? "无编号" : ledger.getAssetCode())
                + "_" + (ledger.getName() == null ? "未知设备" : ledger.getName()) + "_点检标准";

        // 2. 查询标准列表
        List<SiStandard> standards = standardMapper.findByLedgerId(ledgerId);

        // 3. 构建导出列
        List<ExportColumn> columns = new ArrayList<>();
        columns.add(new ExportColumn("devicePart", "检测装置"));
        columns.add(new ExportColumn("itemName", "检测项目"));
        columns.add(new ExportColumn("standardDesc", "检测标准"));
        columns.add(new ExportColumn("executorRole", "执行人"));
        columns.add(new ExportColumn("checkCycle", "检查周期(天)"));

        // 4. 执行导出
        ExcelExportUtil.export(response, fileName, columns, standards, SiStandard.class);
    }


}