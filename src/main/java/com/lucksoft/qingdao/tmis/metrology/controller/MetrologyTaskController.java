package com.lucksoft.qingdao.tmis.metrology.controller;

import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tmis.metrology.ExportColumn;
import com.lucksoft.qingdao.tmis.metrology.dto.MetrologyTaskDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.TaskQuery;
import com.lucksoft.qingdao.tmis.util.ExcelExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/metrology/task")
public class MetrologyTaskController {

    private static final Logger log = LoggerFactory.getLogger(MetrologyTaskController.class);
    private static final List<MetrologyTaskDTO> allTaskData;

    static {
        allTaskData = IntStream.rangeClosed(1, 48).mapToObj(i -> {
            String pointCheckStatus;
            boolean isAbnormal;
            String abc;
            switch (i % 3) {
                case 0: pointCheckStatus = "未点检"; isAbnormal = false; abc = "A"; break;
                case 1: pointCheckStatus = "已点检"; isAbnormal = false; abc = "B"; break;
                default: pointCheckStatus = "未点检"; isAbnormal = true; abc = "C"; break;
            }
            return new MetrologyTaskDTO(
                    i, "2025-08-" + (i % 28 + 1), "012000" + String.format("%02d", i),
                    "JL00" + (9218 + i), "电子台秤-" + i, "TCS-B", "FAC" + (1339743 + i),
                    "0-1000kg", "车间A-" + i, "III级",
                    isAbnormal ? "维修中" : "正常", pointCheckStatus, isAbnormal, abc
            );
        }).collect(Collectors.toList());
    }

    private <T> PageResult<T> paginate(List<T> sourceList, int pageNum, int pageSize) {
        long total = sourceList.size();
        int pages = (int) Math.ceil((double) total / pageSize);
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, sourceList.size());
        List<T> pagedList = (fromIndex >= sourceList.size()) ? Collections.emptyList() : sourceList.subList(fromIndex, toIndex);
        return new PageResult<>(pagedList, pageNum, pageSize, total, pages);
    }

    private List<MetrologyTaskDTO> filterData(TaskQuery query) {
        return allTaskData.stream()
                .filter(task -> query.getDeviceName() == null || query.getDeviceName().isEmpty() || task.getDeviceName().contains(query.getDeviceName()))
                .filter(task -> query.getEnterpriseId() == null || query.getEnterpriseId().isEmpty() || task.getEnterpriseId().contains(query.getEnterpriseId()))
                .filter(task -> query.getAbcCategory() == null || "all".equalsIgnoreCase(query.getAbcCategory()) || query.getAbcCategory().equalsIgnoreCase(task.getAbc()))
                .filter(task -> {
                    String taskStatus = query.getTaskStatus();
                    if (taskStatus == null || taskStatus.isEmpty() || "all".equalsIgnoreCase(taskStatus)) {
                        return true;
                    }
                    switch (taskStatus) {
                        case "unchecked":
                            return "未点检".equals(task.getPointCheckStatus()) && !task.isAbnormal();
                        case "checked":
                            return "已点检".equals(task.getPointCheckStatus());
                        case "abnormal":
                            return task.isAbnormal();
                        default:
                            return true;
                    }
                })
                // Note: Date range filter logic would be added here
                .collect(Collectors.toList());
    }

    @GetMapping("/list")
    public ResponseEntity<PageResult<MetrologyTaskDTO>> getTasks(TaskQuery query) {
        log.info("接收到计量任务查询请求: {}", query);
        List<MetrologyTaskDTO> filteredList = filterData(query);
        PageResult<MetrologyTaskDTO> pageResult = paginate(filteredList, query.getPageNum(), query.getPageSize());
        return ResponseEntity.ok(pageResult);
    }

    @PostMapping("/export")
    public void exportTasks(@RequestBody TaskQuery query, HttpServletResponse response) throws IOException {
        log.info("接收到计量任务导出请求: {}", query);
        List<MetrologyTaskDTO> dataToExport = filterData(query);
        List<ExportColumn> columns = query.getColumns();
        ExcelExportUtil.export(response, "计量任务", columns, dataToExport, MetrologyTaskDTO.class);
    }
}

