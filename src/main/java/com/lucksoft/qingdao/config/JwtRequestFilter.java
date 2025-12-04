package com.lucksoft.qingdao.config;

import com.lucksoft.qingdao.system.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JWT 认证过滤器
 *
 * @author Gemini
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    // [新增] 定义一个要排除（跳过JWT检查）的路径列表
    // 这个列表应与 SecurityConfig 中的 permitAll() 保持一致
    private final List<String> excludedPaths = Arrays.asList(
            "/", "/index.html", "/assets/**", "/js/**", "/libs/**",
            "/sso.html", "/js/sso.js", "/sso/**",
            "/schedule_config.html",
            "/tims_simulator.html",
            "/api/tims/push",
            "/api/schedule/config",
            "/gen_token.html", "/login.html", "/simulate.html", "/oracle_test.html",
            "/my-websocket/**",
            "/stomp.min.js", "/sockjs.min.js", "/sockjs.min.js.map",
            "/api/oracle/task-status/**",
            "/api/system/auth/**",
            "/sso.html",
            "/api/maintainbook/**",
            "/api/oracle/**",
            "/api/tspm/simulate/**",
            "/api/tspm/generate-json/**",
            "/api/tspm/received-data/**",
            "/api/tspm/logs",
            // 新增测试页面
            "/tims_speed_test.html",
            "/tims_self_check_test.html",
            "/test_index.html",
            "/test_entry.html",
            // 新增 API
            "/api/tims/speed/**",
            "/api/tims/self-check/**",
            "/api/direct-test/**"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        final String requestURI = request.getRequestURI().substring(request.getContextPath().length());

        // [新增] 检查请求路径是否在排除列表中
        boolean isExcluded = excludedPaths.stream()
                .anyMatch(path -> pathMatcher.match(path, requestURI));

        if (isExcluded) {
            // 如果是排除的路径，则直接跳过JWT检查，放行
            chain.doFilter(request, response);
            return;
        }

        // --- 以下是原有的JWT检查逻辑，仅对非排除路径执行 ---

        final String requestTokenHeader = request.getHeader("Authorization");

        String username = null;
        String jwtToken = null;

        // JWT Token 通常以 "Bearer " 开头
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtTokenUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                logger.warn("JWT Token 解析失败", e);
            }
        } else {
            // [修改] 只有在需要认证的路径上才打印警告
            logger.warn("JWT Token does not begin with Bearer String, or is missing. URI: " + requestURI);
        }

        // 一旦我们得到令牌，就验证它
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 在这里，我们实际上并不需要从数据库加载用户，因为所有信息都在Redis中。
            // 我们只需构建一个 Authentication 对象并将其设置到上下文中。
            // 这表明对于当前请求，该用户是经过认证的。
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(username, "", new ArrayList<>());

            // 此处可以添加对Redis中Token的验证逻辑，如果需要的话

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            usernamePasswordAuthenticationToken
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // 在上下文中设置认证后，我们指定当前用户已通过认证。
            // 所以它会成功通过 Spring Security 的配置。
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        }

        chain.doFilter(request, response);
    }
}