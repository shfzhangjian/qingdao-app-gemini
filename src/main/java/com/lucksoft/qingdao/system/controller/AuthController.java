package com.lucksoft.qingdao.system.controller;

import com.lucksoft.qingdao.system.dto.LoginRequest;
import com.lucksoft.qingdao.system.dto.LoginResponse;
import com.lucksoft.qingdao.system.dto.UserInfo;
import com.lucksoft.qingdao.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


/**
 * 认证授权控制器
 *
 * @author Gemini
 */
@RestController
@RequestMapping("/api/system/auth")
public class AuthController {

    @Autowired
    private UserService userService;

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
