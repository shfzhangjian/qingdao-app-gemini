package com.lucksoft.qingdao.tmis.metrology.controller;

import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tmis.metrology.dto.MetrologyTaskDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.TaskQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.UpdateTaskRequestDTO;
import com.lucksoft.qingdao.tmis.metrology.service.MetrologyTaskService;
import com.lucksoft.qingdao.tmis.util.ExcelExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrology/task")
public class MetrologyTaskController {

    private static final Logger log = LoggerFactory.getLogger(MetrologyTaskController.class);

    private final MetrologyTaskService metrologyTaskService;

    public MetrologyTaskController(MetrologyTaskService metrologyTaskService) {
        this.metrologyTaskService = metrologyTaskService;
    }

    @GetMapping("/list")
    public ResponseEntity<PageResult<MetrologyTaskDTO>> getTasks(TaskQuery query) {
        log.info("接收到计量任务查询请求: {}", query);
        PageResult<MetrologyTaskDTO> pageResult = metrologyTaskService.getTasksPage(query);
        return ResponseEntity.ok(pageResult);
    }

    @PostMapping("/export")
    public void exportTasks(@RequestBody TaskQuery query, HttpServletResponse response) throws IOException {
        log.info("接收到计量任务导出请求: {}", query);
        List<MetrologyTaskDTO> dataToExport = metrologyTaskService.getTasksForExport(query);
        ExcelExportUtil.export(response, "计量点检任务", query.getColumns(), dataToExport, MetrologyTaskDTO.class);
    }

    /**
     * [核心修复] 将返回类型从 String 修改为 Map<String, Object> 以返回JSON
     */
    @PostMapping("/update")
    public ResponseEntity<Map<String, Object>> updateTask(@RequestBody UpdateTaskRequestDTO request) {
        try {
            int affectedRows = metrologyTaskService.updateTasks(request);
            Map<String, Object> response = new HashMap<>();
            response.put("message", "成功更新 " + affectedRows + " 条任务。");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("更新任务失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "更新失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
