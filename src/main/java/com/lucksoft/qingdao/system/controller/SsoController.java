package com.lucksoft.qingdao.system.controller;

import com.lucksoft.qingdao.system.dto.LoginResponse;
import com.lucksoft.qingdao.system.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 单点登录（SSO）控制器，用于处理来自父系统的票据验证。
 */
@Controller
@RequestMapping("/sso")
public class SsoController {

    private static final Logger log = LoggerFactory.getLogger(SsoController.class);

    @Autowired
    private UserService userService;

    /**
     * 验证来自父系统的票据(Ticket)。
     * @param ticket 一次性票据。
     * @param response HttpServletResponse 用于重定向。
     * @throws IOException 重定向异常。
     */
    @GetMapping("/validate")
    public void validateTicket(@RequestParam("ticket") String ticket, HttpServletResponse response) throws IOException {
        log.info("接收到用于验证的票据: {}", ticket);

        // --- 模拟父系统票据校验 ---
        // 在真实场景中，这里应该通过HTTP客户端调用父系统的后端接口来验证 ticket 的有效性，
        // 并获取对应的用户信息（如用户名）。
        String username = null;
        if ("VALID_TICKET_FOR_ADMIN".equals(ticket)) {
            username = "admin"; // 假设这个票据对应我们系统中的 'admin' 用户
            log.info("票据 '{}' 验证成功，对应用户: {}", ticket, username);
        } else {
            log.warn("票据 '{}' 无效", ticket);
            // 票据无效，可以重定向到错误页或统一登录页
            response.sendRedirect("/login.html?error=invalid_ticket");
            return;
        }

        // --- 票据有效，为用户生成系统内部的Token ---
        try {
            // 我们复用现有的登录服务，它会处理所有JWT生成和缓存逻辑。
            // 在实际项目中，可以为此创建一个免密登录的专用服务方法。
            // 假设 'admin' 用户的密码是 '123456' 用于模拟登录。
            LoginResponse loginResponse = userService.login(username, "123456");

            String token = loginResponse.getToken();
            log.info("为用户 '{}' 生成了内部JWT: {}", username, token);

            // --- 重定向到中转页面，并通过URL hash安全地传递Token ---
            // 使用 hash 可以防止 token 出现在服务器日志或浏览器历史记录中。
            String redirectUrl = "/sso.html#" + token;
            response.sendRedirect(redirectUrl);

        } catch (Exception e) {
            log.error("为用户 '{}' 执行SSO登录流程失败", username, e);
            response.sendRedirect("/login.html?error=sso_failed");
        }
    }
}
