package com.lucksoft.qingdao.tmis.metrology.controller;

import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckListDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/statistics")
    public ResponseEntity<List<Map<String, Object>>> getStatistics(PointCheckQuery query) {
        log.info("接收到点检统计查询: {}", query);

        List<PointCheckListDTO> filteredData = getFilteredStream(query).collect(Collectors.toList());

        Map<String, List<PointCheckListDTO>> groupedByDept = filteredData.stream()
                .collect(Collectors.groupingBy(PointCheckListDTO::getDepartment));

        List<Map<String, Object>> result = new ArrayList<>();

        for (String dept : groupedByDept.keySet()) {
            List<PointCheckListDTO> items = groupedByDept.get(dept);
            Map<String, Object> deptStats = new LinkedHashMap<>();
            deptStats.put("dept", dept);

            // 【核心修复】按月份分区，分别计算上半年和下半年
            Map<Boolean, List<PointCheckListDTO>> partitionedByHalf = items.stream()
                    .collect(Collectors.partitioningBy(item -> LocalDate.parse(item.getCheckDate()).getMonthValue() <= 6));

            List<PointCheckListDTO> h1Items = partitionedByHalf.get(true);
            List<PointCheckListDTO> h2Items = partitionedByHalf.get(false);

            // 上半年统计
            long yingJian1 = h1Items.size();
            long yiJian1 = h1Items.stream().filter(i -> "已检".equals(i.getPlanStatus())).count();
            long weiJian1 = yingJian1 - yiJian1;
            String zhiXingLv1 = yingJian1 > 0 ? String.format("%.0f%%", (double) yiJian1 * 100 / yingJian1) : "0%";
            long zhengChang1 = h1Items.stream().filter(i -> "正常".equals(i.getResultStatus())).count();
            long yiChang1 = h1Items.stream().filter(i -> "异常".equals(i.getResultStatus())).count();
            deptStats.put("yingJianShu1", yingJian1);
            deptStats.put("yiJianShu1", yiJian1);
            deptStats.put("weiJianShu1", weiJian1);
            deptStats.put("zhiXingLv1", zhiXingLv1);
            deptStats.put("zhengChangShu1", zhengChang1);
            deptStats.put("yiChangShu1", yiChang1);

            // 下半年统计
            long yingJian2 = h2Items.size();
            long yiJian2 = h2Items.stream().filter(i -> "已检".equals(i.getPlanStatus())).count();
            long weiJian2 = yingJian2 - yiJian2;
            String zhiXingLv2 = yingJian2 > 0 ? String.format("%.0f%%", (double) yiJian2 * 100 / yingJian2) : "0%";
            long zhengChang2 = h2Items.stream().filter(i -> "正常".equals(i.getResultStatus())).count();
            long yiChang2 = h2Items.stream().filter(i -> "异常".equals(i.getResultStatus())).count();
            deptStats.put("yingJianShu2", yingJian2);
            deptStats.put("yiJianShu2", yiJian2);
            deptStats.put("weiJianShu2", weiJian2);
            deptStats.put("zhiXingLv2", zhiXingLv2);
            deptStats.put("zhengChangShu2", zhengChang2);
            deptStats.put("yiChangShu2", yiChang2);

            result.add(deptStats);
        }

        result.sort(Comparator.comparing(m -> (String) m.get("dept")));
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
}

