package com.lucksoft.qingdao.selfinspection.controller;

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
import java.io.File;
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
        // 字段映射
        String dbField;
        switch (field) {
            case "sdept": dbField = "SDEPT"; break;
            case "sjx": dbField = "SJX"; break;
            case "sbname": dbField = "SBNAME"; break;
            case "sfname": dbField = "SFNAME"; break;
            // [修复] 添加对 checker 和 confirmer 的支持，避免 400 错误
            // 由于 ZJZK_TOOL 表可能没有完全对应的字段，这里暂时映射到 SSTEPOPERNM (办理人)
            // 或者如果只是为了让前端不报错，可以映射到一个存在的文本字段
            case "checker": dbField = "SSTEPOPERNM"; break;
            case "confirmer": dbField = "SSTEPOPERNM"; break;
            case "device": dbField = "SFNAME"; break; // 增加 device 映射到 SFNAME
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
        // [修改] 现在调用真正的任务查询
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
     */
    @GetMapping("/stats/list")
    public ResponseEntity<PageResult<ZjzkTool>> getStatsList(@RequestParam Map<String, Object> params) {
        log.info("收到统计列表查询请求 /api/si/stats/list. 参数: {}", params);
        // 同上，暂时复用台账查询以消除 404
        PageResult<ZjzkTool> result = siService.getLedgerPage(params);
        return ResponseEntity.ok(result);
    }

    /**
     * 归档操作 (模拟)
     */
    @PostMapping("/archive")
    public ResponseEntity<?> archiveData(@RequestBody Map<String, Object> params) {
        log.info("收到归档请求: {}", params);
        return ResponseEntity.ok(Collections.singletonMap("message", "归档成功 (模拟)"));
    }
}