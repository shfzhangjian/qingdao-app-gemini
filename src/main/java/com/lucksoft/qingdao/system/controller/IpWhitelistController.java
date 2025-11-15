package com.lucksoft.qingdao.system.controller;

import com.lucksoft.qingdao.config.IpWhitelistService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap; // [新增] 导入 HashMap
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * [新] IP白名单配置接口
 * 负责提供API供前端读取和更新 ip-whitelist.json 文件
 */
@RestController
@RequestMapping("/api/system/whitelist")
public class IpWhitelistController {

    private static final Logger log = LoggerFactory.getLogger(IpWhitelistController.class);

    @Autowired
    private IpWhitelistService ipWhitelistService;

    /**
     * 获取当前加载的 IP 白名单列表
     * @return IP 列表
     */
    @GetMapping
    public ResponseEntity<Set<String>> getWhitelist() {
        return ResponseEntity.ok(ipWhitelistService.getWhitelistedIps());
    }

    /**
     * 保存并覆盖 IP 白名单列表
     * @param ips 前端提交的新 IP 列表
     * @return 操作结果
     */
    @PostMapping
    public ResponseEntity<?> saveWhitelist(@RequestBody List<String> ips) {
        if (ips == null) {
            // [修改] JDK 1.8 兼容
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("error", "IP list cannot be null.");
            return ResponseEntity.badRequest().body(errorBody);
        }

        try {
            log.warn("正在通过 API 更新 IP 白名单，操作者需要有管理员权限。");
            ipWhitelistService.saveWhitelistedIps(ips);
            log.info("IP 白名单已成功更新。");
            // [修改] JDK 1.8 兼容
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("message", "IP whitelist saved successfully.");
            responseBody.put("count", ips.size());
            return ResponseEntity.ok(responseBody);
        } catch (IOException e) {
            log.error("保存 IP 白名单失败:", e);
            // [修改] JDK 1.8 兼容
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("error", "Failed to save file");
            errorBody.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorBody);
        } catch (Exception e) {
            log.error("更新 IP 白名单时发生未知错误:", e);
            // [修改] JDK 1.8 兼容
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("error", "An unexpected error occurred.");
            errorBody.put("message", e.getMessage());
            return ResponseEntity.internalServerError().body(errorBody);
        }
    }
}