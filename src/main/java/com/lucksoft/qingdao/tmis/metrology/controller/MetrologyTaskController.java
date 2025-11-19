package com.lucksoft.qingdao.tmis.metrology.controller;

import com.lucksoft.qingdao.qdjl.mapper.MetrologyTaskMapper;
import com.lucksoft.qingdao.system.util.AuthUtil;
import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tmis.metrology.dto.MetrologyTaskDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.TaskQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.UpdateTaskRequestDTO;
import com.lucksoft.qingdao.tmis.metrology.service.MetrologyTaskService;
import com.lucksoft.qingdao.tmis.util.ExcelExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/metrology/task")
public class MetrologyTaskController {

    private static final Logger log = LoggerFactory.getLogger(MetrologyTaskController.class);

    private final MetrologyTaskService metrologyTaskService;
    private final MetrologyTaskMapper metrologyTaskMapper; // 直接注入Mapper

    @Autowired // 注入 AuthUtil
    private AuthUtil authUtil;

    public MetrologyTaskController(MetrologyTaskService metrologyTaskService, MetrologyTaskMapper metrologyTaskMapper) {
        this.metrologyTaskService = metrologyTaskService;
        this.metrologyTaskMapper = metrologyTaskMapper;
    }

    @GetMapping("/list")
    public ResponseEntity<PageResult<MetrologyTaskDTO>> getTasks(TaskQuery query, HttpServletRequest request) {
        log.info("接收到计量任务查询请求: {}", query);

        String loginId = authUtil.getCurrentUserLoginId(request); // 调用工具类方法
        if (loginId != null) {
            query.setLoginId(loginId);
            log.info("当前登录用户 LoginId: {}, 将用于过滤任务。", loginId);
        } else {
            log.warn("无法获取当前登录用户信息，将查询所有任务。");
        }

        PageResult<MetrologyTaskDTO> pageResult = metrologyTaskService.getTasksPage(query);
        return ResponseEntity.ok(pageResult);
    }

    @PostMapping("/export")
    public void exportTasks(@RequestBody TaskQuery query,HttpServletRequest request,  HttpServletResponse response) throws IOException {
        log.info("接收到计量任务导出请求: {}", query);

        // --- 修改：使用 AuthUtil 获取当前登录用户信息 ---
        String loginId = authUtil.getCurrentUserLoginId(request); // 调用工具类方法
        if (loginId != null) {
            query.setLoginId(loginId);
            log.info("导出任务将根据 LoginId: {} 进行过滤。", loginId);
        } else {
            log.warn("导出时无法获取当前登录用户信息，将导出所有任务。");
        }

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

    /**
     * 获取下拉补全选项
     * @param field 前端字段名 (deviceName, enterpriseId)
     * @return 字符串列表
     */
    @GetMapping("/options")
    public ResponseEntity<List<String>> getOptions(@RequestParam String field) {
        String columnName;
        switch (field) {
            case "deviceName": columnName = "SJNAME"; break;
            // 任务界面没有部门查询条件，但如果有企业编号的补全需求也可以加
            case "enterpriseId": columnName = "SJNO"; break;
            default: return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<String> options = metrologyTaskMapper.findDistinctValues(columnName);
        return ResponseEntity.ok(options);
    }
}
