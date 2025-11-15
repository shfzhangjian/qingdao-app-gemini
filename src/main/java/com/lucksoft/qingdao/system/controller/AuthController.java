package com.lucksoft.qingdao.system.controller;

import com.lucksoft.qingdao.config.IpWhitelistService;
import com.lucksoft.qingdao.system.dto.LoginRequest;
import com.lucksoft.qingdao.system.dto.LoginResponse; // [新增]
import com.lucksoft.qingdao.system.dto.UserInfo;
import com.lucksoft.qingdao.system.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap; // [新增] 导入 HashMap
import java.util.Map;


/**
 * 认证授权控制器
 *
 * @author Gemini
 */
@RestController
@RequestMapping("/api/system/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class); // [新增]

    @Autowired
    private UserService userService;

    @Autowired
    private IpWhitelistService ipWhitelistService; // [新增]

    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = userService.login(loginRequest.getLoginid(), loginRequest.getPassword());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * [新增] IP 白名单自动登录端点
     * @param loginid 待登录的用户名
     * @param request HTTP请求，用于获取IP
     * @return 成功则返回 LoginResponse (含token)，失败则返回401/403
     */
    @GetMapping("/ip-login")
    public ResponseEntity<?> ipLogin(@RequestParam("loginid") String loginid, HttpServletRequest request) {
        String clientIp = ipWhitelistService.getClientIp(request);

        // 1. 检查IP是否在白名单中
        if (!ipWhitelistService.isWhitelisted(clientIp)) {
            log.warn("IPAuth: 拒绝访问。IP {} 不在白名单中。", clientIp);
            // [修改] JDK 1.8 兼容
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("error", "IP not whitelisted");
            errorBody.put("ip", clientIp);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorBody);
        }

        // 2. IP通过，执行免密登录
        try {
            log.info("IPAuth: IP {} 通过。正在为 {} 执行免密登录...", clientIp, loginid);
            LoginResponse loginResponse = userService.loginWithoutPassword(loginid);
            return ResponseEntity.ok(loginResponse); // 返回 LoginResponse (包含token)
        } catch (Exception e) {
            log.error("IPAuth: 免密登录失败 (loginid: {})。原因: {}", loginid, e.getMessage());
            // [修改] JDK 1.8 兼容
            Map<String, Object> errorBody = new HashMap<>();
            errorBody.put("error", "Login failed");
            errorBody.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorBody);
        }
    }

    /**
     * 【新增】获取当前登录用户的信息
     * @param request HTTP请求对象，用于获取Header
     * @return 包含用户、角色和菜单的详细信息
     */
    @GetMapping("/info")
    public ResponseEntity<UserInfo> getUserInfo(HttpServletRequest request) {
        // 从 Header 中获取 token
        String tokenHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(tokenHeader) && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            UserInfo userInfo = userService.getUserInfoByToken(token);
            if (userInfo != null) {
                return ResponseEntity.ok(userInfo);
            }
        }
        return ResponseEntity.status(401).build(); // Unauthorized
    }
}