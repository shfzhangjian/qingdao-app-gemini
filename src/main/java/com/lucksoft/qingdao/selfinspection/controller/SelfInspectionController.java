package com.lucksoft.qingdao.selfinspection.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucksoft.qingdao.selfinspection.dto.ArchiveReportDTO;
import com.lucksoft.qingdao.selfinspection.dto.GenerateTaskReq;
import com.lucksoft.qingdao.selfinspection.entity.ZjzkStandardFile;
import com.lucksoft.qingdao.selfinspection.entity.ZjzkTask;
import com.lucksoft.qingdao.selfinspection.entity.ZjzkTaskDetail;
import com.lucksoft.qingdao.selfinspection.entity.ZjzkTool;
import com.lucksoft.qingdao.selfinspection.service.SelfInspectionService;
import com.lucksoft.qingdao.system.dto.UserInfo;
import com.lucksoft.qingdao.system.entity.User;
import com.lucksoft.qingdao.system.util.AuthUtil;
import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tmis.metrology.ExportColumn;
import com.lucksoft.qingdao.tmis.util.ExcelExportUtil;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 自检自控管理控制器 (重构版 - 独立标准附件表)
 */
@RestController
@RequestMapping("/api/si")
public class SelfInspectionController {

    private static final Logger log = LoggerFactory.getLogger(SelfInspectionController.class);

    @Autowired
    private SelfInspectionService siService;

    @Autowired
    private AuthUtil authUtil;

    @Autowired
    private ObjectMapper objectMapper;

    // ==================================================================================
    // 1. 自检自控台账 (Ledger) 接口
    // ==================================================================================

    @GetMapping("/ledger/list")
    public ResponseEntity<PageResult<ZjzkTool>> getLedgerList(@RequestParam Map<String, Object> params) {
        PageResult<ZjzkTool> result = siService.getLedgerPage(params);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/ledger/save")
    public ResponseEntity<?> saveLedger(@RequestBody ZjzkTool tool) {
        try {
            siService.saveLedger(tool);
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @DeleteMapping("/ledger/delete/{id}")
    public ResponseEntity<?> deleteLedger(@PathVariable Long id) {
        try {
            siService.deleteLedger(id);
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/ledger/options")
    public ResponseEntity<List<String>> getLedgerOptions(@RequestParam String field) {
        String dbField;
        switch (field) {
            case "sdept": dbField = "SDEPT"; break;
            case "sjx": dbField = "SJX"; break;
            case "sbname": dbField = "SBNAME"; break;
            case "sfname": dbField = "SFNAME"; break;
            case "checker": dbField = "SSTEPOPERNM"; break;
            case "confirmer": dbField = "SSTEPOPERNM"; break;
            case "device": dbField = "SFNAME"; break;
            default: return ResponseEntity.badRequest().body(Collections.emptyList());
        }
        return ResponseEntity.ok(siService.getLedgerOptions(dbField));
    }

    // ==================================================================================
    // 2. 标准附件 (File) 接口
    // ==================================================================================

    @GetMapping("/file/list")
    public ResponseEntity<List<ZjzkStandardFile>> getFileList() {
        return ResponseEntity.ok(siService.getAllStandardFiles());
    }

    @PostMapping("/file/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file,
                                        HttpServletRequest request) {
        UserInfo userInfo = authUtil.getCurrentUserInfo(request);
        String uploader = (userInfo != null && userInfo.getUser() != null) ? userInfo.getUser().getName() : "Unknown";

        try {
            siService.uploadStandardFile(file, uploader);
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @DeleteMapping("/file/delete/{id}")
    public ResponseEntity<?> deleteFile(@PathVariable Long id) {
        try {
            siService.deleteStandardFile(id);
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // --- [新增] 专用接口：获取任务生成时的设备列表 (分组去重) ---
    @GetMapping("/ledger/group-list")
    public ResponseEntity<PageResult<ZjzkTool>> getDeviceGroupList(@RequestParam Map<String, Object> params) {
        return ResponseEntity.ok(siService.getTaskGenerationDeviceList(params));
    }

    @GetMapping("/file/preview/{id}")
    public ResponseEntity<Resource> previewFile(@PathVariable Long id) {
        ZjzkStandardFile fileRecord = siService.getFileById(id);
        if (fileRecord == null) return ResponseEntity.notFound().build();

        File file = new File(fileRecord.getFilePath());
        if (!file.exists()) return ResponseEntity.notFound().build();

        Resource resource = new FileSystemResource(file);

        try {
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + URLEncoder.encode(fileRecord.getFileName(), StandardCharsets.UTF_8.toString()) + "\"")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch(Exception e) { return ResponseEntity.internalServerError().build(); }
    }


    // --- [新增] 任务生成 ---
    @PostMapping("/task/generate")
    public ResponseEntity<?> generateTasks(@RequestBody GenerateTaskReq req) {
        try {
            siService.generateTasks(req);
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (Exception e) {
            log.error("生成任务失败", e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    // --- 任务 (Task) & 明细 ---
    @GetMapping("/task/list")
    public ResponseEntity<PageResult<ZjzkTask>> getTaskList(@RequestParam Map<String, Object> params) {
        return ResponseEntity.ok(siService.getTaskPage(params));
    }

    @GetMapping("/task/details/{taskId}")
    public ResponseEntity<List<ZjzkTaskDetail>> getTaskDetails(@PathVariable Long taskId) {
        return ResponseEntity.ok(siService.getTaskDetails(taskId));
    }

    @PostMapping("/task/submit/{taskId}")
    public ResponseEntity<?> submitTask(
            @PathVariable Long taskId,
            @RequestBody List<ZjzkTaskDetail> details,
            @RequestHeader(value = "X-Role", defaultValue = "inspector") String role,
            HttpServletRequest request) {
        UserInfo userInfo = authUtil.getCurrentUserInfo(request);
        User currentUser = (userInfo != null) ? userInfo.getUser() : null;
        try {
            siService.submitTaskDetails(taskId, details, role, currentUser);
            return ResponseEntity.ok(Collections.singletonMap("success", true));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    /**
     * 获取点检统计列表
     * [修正] 之前错误地调用了 getLedgerPage (查询 ZJZK_TOOL)，现在改为调用 getStatsPage (联表查询 ZJZK_TASK + DETAIL)
     */
    @GetMapping("/stats/list")
    public ResponseEntity<PageResult<Map<String, Object>>> getStatsList(@RequestParam Map<String, Object> params) {
        log.info("收到统计列表查询请求 /api/si/stats/list. 参数: {}", params);
        PageResult<Map<String, Object>> result = siService.getStatsPage(params);
        return ResponseEntity.ok(result);
    }

    /**
     * [新增] 导出点检统计报表
     * 支持按前端传入的列和查询条件导出Excel
     */
    @PostMapping("/stats/export")
    public void exportStatsList(@RequestBody Map<String, Object> payload, HttpServletResponse response) throws IOException {
        log.info("收到统计导出请求 /api/si/stats/export. Payload: {}", payload);

        // 1. 解析列配置
        List<ExportColumn> columns = null;
        if (payload.containsKey("columns")) {
            columns = objectMapper.convertValue(payload.get("columns"), new TypeReference<List<ExportColumn>>() {});
            payload.remove("columns"); // 移除以防止干扰查询参数
        }

        if (columns == null || columns.isEmpty()) {
            throw new IllegalArgumentException("导出列配置不能为空");
        }

        // 2. 查询数据 (不分页)
        List<Map<String, Object>> dataList = siService.getStatsListForExport(payload);

        // 3. 导出 Excel
        // [修复] 这里将 List<Map<String, Object>> 强转为 List<Map>，或者直接传入 raw type
        // Java 编译器无法自动将 List<Map<String, Object>> 视为 List<Map> (泛型不变性)
        // 传入 (List) dataList 是最直接的兼容方式
        ExcelExportUtil.export(response, "点检统计", columns, (List) dataList, Map.class);
    }

    /**
     * 归档操作 (模拟)
     */
    @PostMapping("/archive")
    public ResponseEntity<?> archiveData(@RequestBody Map<String, Object> params) {
        log.info("收到归档请求: {}", params);
        return ResponseEntity.ok(Collections.singletonMap("message", "归档成功 (模拟)"));
    }


    // [新增] 导出台账
    @GetMapping("/ledger/export")
    public void exportLedger(@RequestParam Map<String, Object> params, HttpServletResponse response) throws IOException {
        List<ZjzkTool> list = siService.getLedgerListForExport(params);

        List<ExportColumn> columns = Arrays.asList(
                new ExportColumn("sdept", "车间"),
                new ExportColumn("sname", "名称"),
                new ExportColumn("sjx", "所属机型"),
                new ExportColumn("sfname", "所属设备"),
                new ExportColumn("sbname", "主数据名称"),
                new ExportColumn("spmcode", "PM编码"),
                new ExportColumn("sazwz", "安装位置"),
                new ExportColumn("scj", "厂家"),
                new ExportColumn("sxh", "规格型号"),
                new ExportColumn("syl", "测量原理"),
                new ExportColumn("sddno", "订单号"),
                new ExportColumn("szcno", "资产编码"),
                new ExportColumn("dtime", "初次使用时间"),
                new ExportColumn("ssm", "使用寿命"),
                new ExportColumn("sstepstate", "状态")
        );

        ExcelExportUtil.export(response, "自检自控台账", columns, list, ZjzkTool.class);
    }

// ==========================================
    // [新增] 导入相关接口
    // ==========================================

    @PostMapping("/ledger/import")
    public ResponseEntity<Map<String, Object>> importLedger(@RequestParam("file") MultipartFile file) {
        Map<String, Object> result = siService.importLedgerData(file);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/ledger/import/error-report/{fileId}")
    public ResponseEntity<Resource> downloadErrorReport(@PathVariable String fileId) throws UnsupportedEncodingException {
        File file = siService.getErrorFile(fileId);
        if (file == null || !file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String fileName = URLEncoder.encode("导入校验失败报告.xlsx", "UTF-8");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    /**
     * [新增] 手动触发全量车速更新
     */
    @PostMapping("/speed/refresh-all")
    public ResponseEntity<?> refreshAllSpeeds() {
        // 异步执行，立即返回
        siService.refreshAllDeviceSpeeds();
        return ResponseEntity.ok(Collections.singletonMap("message", "全量车速刷新任务已在后台启动，请稍后刷新台账列表查看。"));
    }

    @GetMapping("/speed/list")
    public ResponseEntity<List<Map<String, Object>>> getSpeedRecords() {
        return ResponseEntity.ok(siService.getRecentSpeedRecords());
    }



    // ==========================================
    // [新增] 归档报表接口
    // ==========================================

    @PostMapping("/report/preview")
    public ResponseEntity<ArchiveReportDTO.Response> previewArchiveReport(@RequestBody ArchiveReportDTO.Request req) {
        log.info("收到归档报表预览请求: Device={}, Type={}, Date={}", req.getDeviceName(), req.getTaskType(), req.getDateRange());
        return ResponseEntity.ok(siService.generateArchiveReportData(req));
    }

    @PostMapping("/report/export")
    public void exportArchiveReport(@RequestBody ArchiveReportDTO.Request req, HttpServletResponse response) throws IOException {
        log.info("收到归档报表导出请求: Device={}", req.getDeviceName());

        try (Workbook workbook = siService.exportArchiveReportExcel(req)) {
            String fileName = URLEncoder.encode("自检自控记录表_" + req.getDeviceName() + ".xlsx", StandardCharsets.UTF_8.toString());
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");
            workbook.write(response.getOutputStream());
        } catch (Exception e) {
            log.error("导出报表失败", e);
            response.sendError(500, "导出失败: " + e.getMessage());
        }
    }
}