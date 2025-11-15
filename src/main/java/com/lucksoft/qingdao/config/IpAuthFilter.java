package com.lucksoft.qingdao.config;

import com.lucksoft.qingdao.system.dto.LoginResponse;
import com.lucksoft.qingdao.system.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 新的IP认证过滤器
 * 它会在 JwtRequestFilter 之前运行，专门处理来自白名单IP的自动登录请求。
 */
@Component
public class IpAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(IpAuthFilter.class);

    // 拦截的特定自动登录URL
    private static final String IP_AUTH_SSO_PATH = "/ip-auth-sso";

    @Autowired
    private IpWhitelistService ipWhitelistService;

    @Autowired
    private UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestUri = request.getRequestURI().substring(request.getContextPath().length());

        // 1. 检查是否是我们关心的自动登录URL
        if (IP_AUTH_SSO_PATH.equals(requestUri)) {
            String clientIp = ipWhitelistService.getClientIp(request);
            log.info("IPAuthFilter: 拦截到请求 {} 来自 IP: {}", requestUri, clientIp);

            // 2. 检查IP是否在白名单中
            if (!ipWhitelistService.isWhitelisted(clientIp)) {
                log.warn("IPAuthFilter: 拒绝访问。IP {} 不在白名单中。", clientIp);
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "IP not whitelisted");
                return;
            }

            // 3. IP 在白名单中，获取 loginid 参数
            String loginid = request.getParameter("loginid");
            if (!StringUtils.hasText(loginid)) {
                log.warn("IPAuthFilter: 拒绝访问。白名单IP {} 的请求缺少 'loginid' 参数。", clientIp);
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "loginid parameter is missing");
                return;
            }

            // 4. 执行免密登录
            try {
                log.info("IPAuthFilter: IP {} 通过。正在为 {} 执行免密登录...", clientIp, loginid);
                LoginResponse loginResponse = userService.loginWithoutPassword(loginid);
                String token = loginResponse.getToken();

                // 5. 重定向到 sso.html，并附上新 token
                // 这将触发 sso.js 将 token 存入 localStorage
                String redirectUrl = request.getContextPath() + "/sso.html#" + token;
                log.info("IPAuthFilter: 登录成功。重定向到 {}", redirectUrl);
                response.sendRedirect(redirectUrl);
                return; // 结束请求，不进入后续过滤器

            } catch (Exception e) {
                log.error("IPAuthFilter: 免密登录失败 (loginid: {})。原因: {}", loginid, e.getMessage());
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Login failed: " + e.getMessage());
                return;
            }
        }

        // 如果不是 /ip-auth-sso 请求，则放行给下一个过滤器 (JwtRequestFilter)
        filterChain.doFilter(request, response);
    }
}