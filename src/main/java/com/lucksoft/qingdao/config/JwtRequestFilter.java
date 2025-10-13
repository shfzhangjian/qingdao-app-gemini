package com.lucksoft.qingdao.config;

import com.lucksoft.qingdao.system.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/**
 * JWT 认证过滤器
 *
 * @author Gemini
 */
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

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
            logger.warn("JWT Token does not begin with Bearer String");
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
