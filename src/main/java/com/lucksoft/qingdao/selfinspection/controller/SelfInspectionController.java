package com.lucksoft.qingdao.selfinspection.controller;

import com.lucksoft.qingdao.selfinspection.entity.SiLedger;
import com.lucksoft.qingdao.selfinspection.entity.SiTask;
import com.lucksoft.qingdao.selfinspection.entity.SiTaskDetail;
import com.lucksoft.qingdao.selfinspection.service.SelfInspectionService;
import com.lucksoft.qingdao.system.dto.UserInfo;
import com.lucksoft.qingdao.system.entity.User;
import com.lucksoft.qingdao.system.util.AuthUtil;
import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tspm.service.DynamicQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 自检自控管理控制器
 * 对应前端 selfInspectionApi.js 中的接口调用
 * 负责接收 HTTP 请求，调用 Service 层逻辑，并返回 JSON 结果
 */
@RestController
@RequestMapping("/api/si")
public class SelfInspectionController {

    private static final Logger log = LoggerFactory.getLogger(SelfInspectionController.class);

    @Autowired
    private SelfInspectionService siService;

    @Autowired
    private DynamicQueryService dynamicQueryService; // 用于执行临时SQL查询（如标准详情）

    @Autowired
    private AuthUtil authUtil;

    // ==================================================================================
    // 1. 自检自控台账 (Ledger) 接口
    // ==================================================================================

    /**
     * 获取左侧设备树
     * 目前返回模拟的树结构，后续可替换为查询 T_DEPART 或 EQ_CLASS 表
     */
    @GetMapping("/ledger/tree")
    public ResponseEntity<List<Map<String, Object>>> getLedgerTree() {
        log.info("收到获取设备树请求 /api/si/ledger/tree");
        List<Map<String, Object>> tree = new ArrayList<>();

        // 模拟根节点：青岛卷烟厂
        Map<String, Object> root = new HashMap<>();
        root.put("id", "f_qingdao");
        root.put("label", "青岛卷烟厂");
        root.put("icon", "bi-building");

        // 模拟二级节点：卷包车间
        Map<String, Object> workshop = new HashMap<>();
        workshop.put("id", "w_juanbao");
        workshop.put("label", "卷包车间");
        workshop.put("icon", "bi-house-gear");

        // 模拟三级节点：包装机组
        Map<String, Object> machineGroup = new HashMap<>();
        machineGroup.put("id", "m_pack");
        machineGroup.put("label", "包装机组");
        machineGroup.put("icon", "bi-boxes");

        // 模拟四级节点：具体设备
        List<Map<String, Object>> devices = new ArrayList<>();
        devices.add(createNode("e_gdgy20", "GDGY 20#高速包装机组", "bi-hdd-rack"));
        devices.add(createNode("e_gdgy21", "GDGY 21#高速包装机组", "bi-hdd-rack"));

        machineGroup.put("children", devices);
        workshop.put("children", Collections.singletonList(machineGroup));
        root.put("children", Collections.singletonList(workshop));
        tree.add(root);

        log.info("设备树构建完成，返回根节点: {}", root.get("label"));
        return ResponseEntity.ok(tree);
    }

    private Map<String, Object> createNode(String id, String label, String icon) {
        Map<String, Object> node = new HashMap<>();
        node.put("id", id);
        node.put("label", label);
        node.put("icon", icon);
        return node;
    }

    /**
     * 分页查询台账列表
     * @param params 查询参数 map
     */
    @GetMapping("/ledger/list")
    public ResponseEntity<PageResult<SiLedger>> getLedgerList(@RequestParam Map<String, Object> params) {
        log.info("收到台账列表查询请求 /api/si/ledger/list. 参数: {}", params);
        PageResult<SiLedger> result = siService.getLedgerPage(params);
        log.info("台账查询成功，返回记录数: {}, 总页数: {}", result.getList().size(), result.getPages());
        return ResponseEntity.ok(result);
    }

    /**
     * 保存台账 (新增/修改)
     */
    @PostMapping("/ledger/save")
    public ResponseEntity<?> saveLedger(@RequestBody SiLedger ledger, HttpServletRequest request) {
        // 获取当前登录人（如果需要记录创建人/修改人）
        // String currentUser = authUtil.getCurrentUserLoginId(request);
        log.info("收到保存台账请求 /api/si/ledger/save. 台账名称: {}, ID: {}", ledger.getName(), ledger.getId());
        try {
            siService.saveLedger(ledger);
            log.info("台账保存成功 ID: {}", ledger.getId());
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (Exception e) {
            log.error("保存台账失败", e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * 删除台账
     */
    @DeleteMapping("/ledger/delete/{id}")
    public ResponseEntity<?> deleteLedger(@PathVariable Long id) {
        log.info("收到删除台账请求 /api/si/ledger/delete/{}. ID: {}", id, id);
        try {
            siService.deleteLedger(id);
            log.info("台账删除成功 ID: {}", id);
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (Exception e) {
            log.error("删除台账失败", e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * 获取下拉补全选项 (自动补全)
     */
    @GetMapping("/ledger/options")
    public ResponseEntity<List<String>> getLedgerOptions(@RequestParam String field) {
        log.info("收到获取台账选项请求 /api/si/ledger/options. 字段: {}", field);

        // 简单的字段名映射，防止SQL注入
        // 前端传 workshop, model, mainDevice -> 映射为数据库字段名
        String dbField;
        switch (field) {
            case "workshop": dbField = "WORKSHOP"; break;
            case "model": dbField = "MODEL"; break;
            case "mainDevice": dbField = "MAIN_DEVICE"; break;
            case "device": dbField = "DEVICE"; break;
            case "factory": dbField = "FACTORY"; break;
            default:
                log.warn("非法的选项字段请求: {}", field);
                return ResponseEntity.badRequest().body(Collections.emptyList());
        }

        List<String> options = siService.getLedgerOptions(dbField);
        log.info("获取选项成功，返回 {} 条数据", options.size());
        return ResponseEntity.ok(options);
    }

    /**
     * 获取点检标准详情列表
     * 直接查询 T_SI_STANDARD 表
     */
    @GetMapping("/ledger/standard/{id}")
    public ResponseEntity<List<Map<String, Object>>> getStandardDetails(@PathVariable Long id) {
        log.info("收到获取标准详情请求 /api/si/ledger/standard/{}. 台账ID: {}", id, id);

        // 使用动态 SQL 查询标准详情
        // 注意：DynamicQueryService.executeQuery 内部会自动处理分页 (添加 ROWNUM <= ?)
        // 这里我们设置一个较大的 limit (例如 1000) 来获取该台账下的所有标准
        String sql = "SELECT DEVICE_PART as \"device\", ITEM_NAME as \"item\", " +
                "STANDARD_DESC as \"standard\", EXECUTOR_ROLE as \"executor\", " +
                "CHECK_CYCLE as \"cycle\", TO_CHAR(NEXT_EXEC_DATE, 'yyyy-MM-dd') as \"nextDate\" " +
                "FROM T_SI_STANDARD WHERE LEDGER_ID = ? ORDER BY ID ASC";

        try {
            List<Object> params = new ArrayList<>();
            params.add(id);

            log.debug("执行SQL: {}", sql);
            log.debug("参数: {}", params);

            // 使用 DynamicQueryService 执行查询
            List<Map<String, Object>> list = dynamicQueryService.executeQuery(sql, 1000, params);
            log.info("获取标准详情成功，返回 {} 条记录", list.size());

            return ResponseEntity.ok(list);
        } catch (Exception e) {
            log.error("获取标准详情失败", e);
            return ResponseEntity.status(500).body(Collections.emptyList());
        }
    }

    // ==================================================================================
    // 2. 点检任务 (Task) 接口
    // ==================================================================================

    /**
     * 分页查询任务列表
     */
    @GetMapping("/task/list")
    public ResponseEntity<PageResult<SiTask>> getTaskList(@RequestParam Map<String, Object> params) {
        log.info("收到任务列表查询请求 /api/si/task/list. 参数: {}", params);
        PageResult<SiTask> result = siService.getTaskPage(params);
        log.info("任务查询成功，返回记录数: {}, 总页数: {}", result.getList().size(), result.getPages());
        return ResponseEntity.ok(result);
    }

    /**
     * 获取任务执行详情 (明细列表)
     */
    @GetMapping("/task/details/{taskId}")
    public ResponseEntity<List<SiTaskDetail>> getTaskDetails(@PathVariable Long taskId) {
        log.info("收到获取任务详情请求 /api/si/task/details/{}. 任务ID: {}", taskId, taskId);
        List<SiTaskDetail> details = siService.getTaskDetails(taskId);
        log.info("获取任务详情成功，返回 {} 条明细", details.size());
        return ResponseEntity.ok(details);
    }

    @PostMapping("/task/submit/{taskId}")
    public ResponseEntity<?> submitTask(
            @PathVariable Long taskId,
            @RequestBody List<SiTaskDetail> details,
            @RequestHeader(value = "X-Role", defaultValue = "inspector") String role,
            HttpServletRequest request) { // 增加 Request 参数

        // [新增] 从 Token 获取当前登录用户
        UserInfo userInfo = authUtil.getCurrentUserInfo(request);
        User currentUser = (userInfo != null) ? userInfo.getUser() : null;

        if (currentUser != null) {
            log.info("当前操作用户: {} (ID: {}, 工号: {})", currentUser.getName(), currentUser.getId(), currentUser.getGh());
        } else {
            log.warn("未获取到当前登录用户信息 (可能是匿名或Token失效)");
        }

        try {
            // 将 User 对象传递给 Service
            siService.submitTaskDetails(taskId, details, role, currentUser);
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // ==================================================================================
    // 3. 统计与归档 (Stats) 接口
    // ==================================================================================

    /**
     * 获取点检统计列表
     * 目前复用任务列表的查询逻辑，未来可扩展为专门的统计Service方法
     * [修复] 获取点检统计列表 (扁平化明细数据)
     */
    @GetMapping("/stats/list")
    public ResponseEntity<PageResult<Map<String, Object>>> getStatsList(@RequestParam Map<String, Object> params) {
        log.info("收到统计列表查询请求 /api/si/stats/list. 参数: {}", params);
        // [修改] 调用新的 getStatsPage 方法
        PageResult<Map<String, Object>> result = siService.getStatsPage(params);
        log.info("统计查询成功，返回记录数: {}, 总页数: {}", result.getList().size(), result.getPages());
        return ResponseEntity.ok(result);
    }

    /**
     * 归档操作
     */
    @PostMapping("/archive")
    public ResponseEntity<?> archiveData(@RequestBody Map<String, Object> params) {
        String device = (String) params.get("device");
        String taskType = (String) params.get("taskType");
        String dateRange = (String) params.get("dateRange");

        log.info("收到归档请求 /api/si/archive. 设备: {}, 类型: {}, 范围: {}", device, taskType, dateRange);

        // TODO: 调用 Service 执行实际的数据迁移或状态更新逻辑
        // 这里模拟成功返回，实际业务中可能涉及将 T_SI_TASK 数据移动到历史表

        try {
            // 模拟耗时操作
            Thread.sleep(500);
            String message = String.format("已成功归档设备 [%s] 的 [%s] 数据 (%s)", device, taskType, dateRange);
            log.info("归档操作完成: {}", message);
            return ResponseEntity.ok(Collections.singletonMap("message", message));
        } catch (InterruptedException e) {
            log.error("归档操作被中断", e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", "归档操作被中断"));
        }
    }

    // --- 辅助方法 ---
    private String camelToUnderscore(String camel) {
        if (camel == null || camel.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : camel.toCharArray()) {
            if (Character.isUpperCase(c)) {
                sb.append('_').append(c);
            } else {
                sb.append(Character.toUpperCase(c));
            }
        }
        return sb.toString();
    }

    // [修改] 批量导入标准
    // 改为接收 ids 字符串 (逗号分隔)
    @PostMapping("/ledger/import-standard")
    public ResponseEntity<?> importStandard(@RequestParam("ids") String ids, @RequestParam("file") MultipartFile file) {
        try {
            if (ids == null || ids.isEmpty()) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "未选择任何设备"));
            }
            List<Long> ledgerIds = Arrays.stream(ids.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Long::parseLong)
                    .collect(Collectors.toList());

            siService.importStandardBatch(ledgerIds, file);
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (Exception e) {
            log.error("导入标准失败", e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }


    // [新增] 下载标准 (附件/模板)
    @PostMapping("/ledger/download-standard/{ledgerId}")
    public void downloadStandard(@PathVariable Long ledgerId, HttpServletResponse response) throws IOException {
        try {
            siService.downloadStandard(ledgerId, response);
        } catch (Exception e) {
            log.error("下载标准失败", e);
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(500);
            response.getWriter().write("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}