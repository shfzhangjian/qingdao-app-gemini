package com.lucksoft.qingdao.tmis.metrology.controller;

import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tmis.metrology.ExportColumn;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckListDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckStatsDTO;
import com.lucksoft.qingdao.tmis.util.ExcelExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/metrology/point-check")
public class PointCheckController {

    private static final Logger log = LoggerFactory.getLogger(PointCheckController.class);
    private static final List<PointCheckListDTO> allPointCheckListData = new ArrayList<>();

    static {
        String[] departments = {"卷材一厂", "制丝车间", "卷包车间", "能源动力处", "行政管理处", "安全保卫处", "工艺质量处", "生产供应处", "物资管理处", "设备管理处"};
        String[] categories = {"A", "B", "C"};
        int totalRecords = 300;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 1; i <= totalRecords; i++) {
            String department = departments[i % departments.length];
            String category = categories[i % categories.length];
            int month = ThreadLocalRandom.current().nextInt(1, 13);
            int day = ThreadLocalRandom.current().nextInt(1, 29);
            String checkDate = LocalDate.of(2025, month, day).format(formatter);

            String planStatus = Math.random() > 0.2 ? "已检" : "未检";
            String resultStatus;
            if ("未检".equals(planStatus)) {
                resultStatus = "未检";
            } else {
                resultStatus = Math.random() > 0.1 ? "正常" : "异常";
            }

            allPointCheckListData.add(new PointCheckListDTO(
                    i, department, "设备名-" + i, category,
                    checkDate, planStatus, resultStatus
            ));
        }
    }

    private Stream<PointCheckListDTO> getFilteredStream(PointCheckQuery query) {
        Stream<PointCheckListDTO> stream = allPointCheckListData.stream();

        // 时间范围过滤
        if (query.getDateRange() != null && !query.getDateRange().isEmpty()) {
            try {
                String[] dates = query.getDateRange().split(" to ");
                LocalDate startDate = LocalDate.parse(dates[0]);
                LocalDate endDate = LocalDate.parse(dates[1]);
                stream = stream.filter(item -> {
                    LocalDate itemDate = LocalDate.parse(item.getCheckDate());
                    return !itemDate.isBefore(startDate) && !itemDate.isAfter(endDate);
                });
            } catch (Exception e) {
                log.error("日期范围解析失败: {}", query.getDateRange(), e);
            }
        }

        // ABC 分类过滤
        if (query.getCategory() != null && !query.getCategory().isEmpty() && !"all".equalsIgnoreCase(query.getCategory())) {
            stream = stream.filter(item -> query.getCategory().equalsIgnoreCase(item.getDeviceType()));
        }

        // 部门过滤
        if (query.getDepartment() != null && !query.getDepartment().isEmpty()) {
            stream = stream.filter(item -> query.getDepartment().equals(item.getDepartment()));
        }

        // 计划状态过滤
        if (query.getPlanStatus() != null && !query.getPlanStatus().isEmpty() && !"all".equalsIgnoreCase(query.getPlanStatus())) {
            stream = stream.filter(item -> query.getPlanStatus().equals(item.getPlanStatus()));
        }

        // 结果状态过滤
        if (query.getResultStatus() != null && !query.getResultStatus().isEmpty() && !"all".equalsIgnoreCase(query.getResultStatus())) {
            stream = stream.filter(item -> query.getResultStatus().equals(item.getResultStatus()));
        }

        return stream;
    }

    private List<PointCheckStatsDTO> calculateStatistics(PointCheckQuery query) {
        List<PointCheckListDTO> filteredData = getFilteredStream(query).collect(Collectors.toList());

        Map<String, List<PointCheckListDTO>> groupedByDept = filteredData.stream()
                .collect(Collectors.groupingBy(PointCheckListDTO::getDepartment));

        List<PointCheckStatsDTO> result = new ArrayList<>();

        for (String dept : groupedByDept.keySet()) {
            List<PointCheckListDTO> items = groupedByDept.get(dept);
            Map<Boolean, List<PointCheckListDTO>> partitionedByHalf = items.stream()
                    .collect(Collectors.partitioningBy(item -> LocalDate.parse(item.getCheckDate()).getMonthValue() <= 6));

            List<PointCheckListDTO> h1Items = partitionedByHalf.get(true);
            List<PointCheckListDTO> h2Items = partitionedByHalf.get(false);

            long yingJian1 = h1Items.size();
            long yiJian1 = h1Items.stream().filter(i -> "已检".equals(i.getPlanStatus())).count();
            String zhiXingLv1 = yingJian1 > 0 ? String.format("%.0f%%", (double) yiJian1 * 100 / yingJian1) : "0%";
            long zhengChang1 = h1Items.stream().filter(i -> "正常".equals(i.getResultStatus())).count();
            long yiChang1 = yiJian1 - zhengChang1;


            long yingJian2 = h2Items.size();
            long yiJian2 = h2Items.stream().filter(i -> "已检".equals(i.getPlanStatus())).count();
            String zhiXingLv2 = yingJian2 > 0 ? String.format("%.0f%%", (double) yiJian2 * 100 / yingJian2) : "0%";
            long zhengChang2 = h2Items.stream().filter(i -> "正常".equals(i.getResultStatus())).count();
            long yiChang2 = yiJian2 - zhengChang2;


            result.add(new PointCheckStatsDTO(
                    dept,
                    yingJian1, yiJian1, yingJian1 - yiJian1, zhiXingLv1, zhengChang1, yiChang1,
                    yingJian2, yiJian2, yingJian2 - yiJian2, zhiXingLv2, zhengChang2, yiChang2
            ));
        }
        result.sort(Comparator.comparing(PointCheckStatsDTO::getDept));
        return result;
    }


    @GetMapping("/statistics")
    public ResponseEntity<List<Map<String, Object>>> getStatistics(PointCheckQuery query) {
        log.info("接收到点检统计查询: {}", query);
        List<PointCheckStatsDTO> stats = calculateStatistics(query);
        // 为了保持与旧版接口的兼容性，这里将 DTO 转换为 Map
        List<Map<String, Object>> result = stats.stream().map(s -> {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("dept", s.getDept());
            map.put("yingJianShu1", s.getYingJianShu1());
            map.put("yiJianShu1", s.getYiJianShu1());
            map.put("weiJianShu1", s.getWeiJianShu1());
            map.put("zhiXingLv1", s.getZhiXingLv1());
            map.put("zhengChangShu1", s.getZhengChangShu1());
            map.put("yiChangShu1", s.getYiChangShu1());
            map.put("yingJianShu2", s.getYingJianShu2());
            map.put("yiJianShu2", s.getYiJianShu2());
            map.put("weiJianShu2", s.getWeiJianShu2());
            map.put("zhiXingLv2", s.getZhiXingLv2());
            map.put("zhengChangShu2", s.getZhengChangShu2());
            map.put("yiChangShu2", s.getYiChangShu2());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/list")
    public ResponseEntity<PageResult<PointCheckListDTO>> getList(PointCheckQuery query) {
        log.info("接收到点检列表查询: {}", query);
        List<PointCheckListDTO> filteredData = getFilteredStream(query).collect(Collectors.toList());

        int pageNum = query.getPageNum();
        int pageSize = query.getPageSize();
        long total = filteredData.size();
        int pages = (int) Math.ceil((double) total / pageSize);
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, filteredData.size());
        List<PointCheckListDTO> pagedList = (fromIndex >= filteredData.size()) ? Collections.emptyList() : filteredData.subList(fromIndex, toIndex);

        return ResponseEntity.ok(new PageResult<>(pagedList, pageNum, pageSize, total, pages));
    }

    @PostMapping("/export")
    public void exportPointCheck(@RequestBody PointCheckQuery query, HttpServletResponse response) throws IOException {
        log.info("接收到点检导出请求: {}", query);

        String viewMode = query.getViewMode();

        if ("list".equals(viewMode)) {
            // --- 导出列表 ---
            List<PointCheckListDTO> dataToExport = getFilteredStream(query).collect(Collectors.toList());
            ExcelExportUtil.export(response, "点检列表", query.getColumns(), dataToExport, PointCheckListDTO.class);
        } else {
            // --- 导出统计 (默认) ---
            List<PointCheckStatsDTO> statsData = calculateStatistics(query);
            // 为统计报表创建固定的列定义
            List<ExportColumn> statsColumns = new ArrayList<>();
            statsColumns.add(createColumn("dept", "部门名称"));
            statsColumns.add(createColumn("yingJianShu1", "上半年应检数量"));
            statsColumns.add(createColumn("yiJianShu1", "上半年已检数量"));
            statsColumns.add(createColumn("weiJianShu1", "上半年未检数量"));
            statsColumns.add(createColumn("zhiXingLv1", "上半年执行率"));
            statsColumns.add(createColumn("zhengChangShu1", "上半年正常数量"));
            statsColumns.add(createColumn("yiChangShu1", "上半年异常数量"));
            statsColumns.add(createColumn("yingJianShu2", "下半年应检数量"));
            statsColumns.add(createColumn("yiJianShu2", "下半年已检数量"));
            statsColumns.add(createColumn("weiJianShu2", "下半年未检数量"));
            statsColumns.add(createColumn("zhiXingLv2", "下半年执行率"));
            statsColumns.add(createColumn("zhengChangShu2", "下半年正常数量"));
            statsColumns.add(createColumn("yiChangShu2", "下半年异常数量"));

            ExcelExportUtil.export(response, "点检统计", statsColumns, statsData, PointCheckStatsDTO.class);
        }
    }

    private ExportColumn createColumn(String key, String title) {
        ExportColumn col = new ExportColumn();
        col.setKey(key);
        col.setTitle(title);
        return col;
    }
}
