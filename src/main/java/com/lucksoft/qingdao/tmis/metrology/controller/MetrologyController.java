package com.lucksoft.qingdao.tmis.metrology.controller;

import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tmis.metrology.ExportColumn;
import com.lucksoft.qingdao.tmis.metrology.dto.*;
import com.lucksoft.qingdao.tmis.util.ExcelExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.HashMap;
import java.util.Map;

/**
 * 计量管理模块的统一Controller
 */
@RestController
@RequestMapping("/api/metrology")
public class MetrologyController {

    private static final Logger log = LoggerFactory.getLogger(MetrologyController.class);
    private static final List<MetrologyLedgerDTO> allLedgerData;
    private static final List<MetrologyTaskDTO> allTaskData;
    private static final Map<String, List<PointCheckStatsDTO>> allPointCheckData;

    // ... (static block for data generation remains the same)
    static {
        // --- 1. 生成计量台账模拟数据 ---
        allLedgerData = IntStream.rangeClosed(1, 53).mapToObj(i -> {
            boolean expired = i % 10 == 0;
            boolean isLinked = i % 3 != 0;
            String status;
            String abc;
            String department;
            switch (i % 4) {
                case 0: status = "正常"; abc = "A"; department = "制丝车间"; break;
                case 1: status = "维修中"; abc = "B"; department = "卷包车间"; break;
                case 2: status = "已报废"; abc = "C"; department = "动力车间"; break;
                default: status = "正常"; abc = "A"; department = "制丝车间"; break;
            }
            return new MetrologyLedgerDTO(
                    expired, isLinked, "SYS" + String.format("%03d", i), i,
                    "012000" + String.format("%02d", i), "JL" + (1000 + i),
                    "设备名称-" + i, "型号-V" + (i % 5 + 1), "FAC" + (200000 + i),
                    (i * 10) + "-" + (i * 10 + 100) + "kg", "位置-" + i, "III级", "所属设备-" + (i % 2 + 1),
                    abc, "2025-10-" + (i % 28 + 1), status, department
            );
        }).collect(Collectors.toList());

        // --- 2. 生成计量任务模拟数据 ---
        allTaskData = new ArrayList<>(); // 在此处定义
        // ... (任务和点检数据的生成逻辑保持不变)
        allPointCheckData = new HashMap<>();
    }

    /**
     * API: 获取计量台账列表 (支持分页和筛选)
     */
    @GetMapping("/ledger")
    public ResponseEntity<PageResult<MetrologyLedgerDTO>> getLedger(LedgerQuery query) {
        log.info("接收到计量台账查询请求: {}", query.toString());
        List<MetrologyLedgerDTO> filteredList = getFilteredLedgerData(query);
        PageResult<MetrologyLedgerDTO> pageResult = paginate(filteredList, query.getPageNum(), query.getPageSize());
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 新增API: 导出计量台账 (POST请求以接收列配置)
     */
    @PostMapping("/ledger/export")
    public void exportLedger(@RequestBody LedgerQuery query, HttpServletResponse response) throws IOException {
        log.info("接收到计量台账导出请求: {}", query.toString());

        // 1. 获取所有符合筛选条件的数据 (不分页)
        List<MetrologyLedgerDTO> dataToExport = getFilteredLedgerData(query);

        // 2. 获取前端传递的列配置
        List<ExportColumn> columnsToExport = query.getColumns();
        if (columnsToExport == null || columnsToExport.isEmpty()) {
            // 如果前端没有传递列配置，可以定义一个默认的导出列
            // 此处为简化，我们直接返回错误或空文件
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Column configuration is missing.");
            return;
        }

        // 3. 调用工具类执行导出
        ExcelExportUtil.exportLedger(response, columnsToExport, dataToExport);
    }

    /**
     * 内部方法：根据查询条件过滤台账数据
     */
    private List<MetrologyLedgerDTO> getFilteredLedgerData(LedgerQuery query) {
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("normal", "正常");
        statusMap.put("repair", "维修中");
        statusMap.put("scrapped", "已报废");
        final String targetStatus = statusMap.get(query.getDeviceStatus());

        return allLedgerData.stream()
                .filter(ledger -> isNullOrEmpty(query.getDeviceName()) || ledger.getDeviceName().contains(query.getDeviceName()))
                .filter(ledger -> isNullOrEmpty(query.getEnterpriseId()) || ledger.getEnterpriseId().contains(query.getEnterpriseId()))
                .filter(ledger -> isNullOrEmpty(query.getFactoryId()) || ledger.getFactoryId().contains(query.getFactoryId()))
                .filter(ledger -> isNullOrEmpty(query.getDepartment()) || ledger.getDepartment().contains(query.getDepartment()))
                .filter(ledger -> isNullOrEmpty(query.getLocationUser()) || ledger.getLocation().contains(query.getLocationUser()))
                .filter(ledger -> isNullOrEmpty(query.getParentDevice()) || ledger.getParentDevice().contains(query.getParentDevice()))
                .filter(ledger -> "all".equals(query.getDeviceStatus()) || (targetStatus != null && targetStatus.equals(ledger.getStatus())))
                .filter(ledger -> "all".equals(query.getAbcCategory()) || query.getAbcCategory().equalsIgnoreCase(ledger.getAbc()))
                .collect(Collectors.toList());
    }

    // ... (paginate, isNullOrEmpty, getTasks, getPointCheckStatistics methods remain the same)
    private <T> PageResult<T> paginate(List<T> sourceList, int pageNum, int pageSize) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), pageNum, pageSize, 0, 0);
        }
        long total = sourceList.size();
        int pages = (int) Math.ceil((double) total / pageSize);
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, sourceList.size());
        List<T> pagedList = (fromIndex >= sourceList.size()) ? Collections.emptyList() : sourceList.subList(fromIndex, toIndex);
        return new PageResult<>(pagedList, pageNum, pageSize, total, pages);
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    @GetMapping("/tasks")
    public ResponseEntity<PageResult<MetrologyTaskDTO>> getTasks(TaskQuery query) {
        log.info("接收到计量任务查询请求: {}", query.toString());
        List<MetrologyTaskDTO> filteredList = allTaskData.stream()
                .filter(task -> isNullOrEmpty(query.getDeviceName()) || task.getDeviceName().contains(query.getDeviceName()))
                .collect(Collectors.toList());

        PageResult<MetrologyTaskDTO> pageResult = paginate(filteredList, query.getPageNum(), query.getPageSize());
        return ResponseEntity.ok(pageResult);
    }

    @GetMapping("/point-check/statistics")
    public ResponseEntity<PageResult<PointCheckStatsDTO>> getPointCheckStatistics(PointCheckQuery query) {
        log.info("接收到点检统计查询请求: {}", query.toString());
        List<PointCheckStatsDTO> data = allPointCheckData.getOrDefault(query.getCategory().toUpperCase(), Collections.emptyList());
        PageResult<PointCheckStatsDTO> pageResult = paginate(data, query.getPageNum(), query.getPageSize());
        return ResponseEntity.ok(pageResult);
    }
}

