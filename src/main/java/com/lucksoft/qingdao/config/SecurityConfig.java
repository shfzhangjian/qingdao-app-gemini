package com.lucksoft.qingdao.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Autowired
    private ApiKeyAuthFilter apiKeyAuthFilter;

    /**
     * 配置 WebSecurity 以完全绕过 Spring Security 过滤器链。
     * 适用于静态资源和绝对公开的 API。
     * 注意：配置在这里的路径将不再经过 JwtRequestFilter。
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().antMatchers(
                // 静态资源
                "/assets/**", "/js/**", "/libs/**", "/stomp.min.js", "/sockjs.min.js",
                // 登录与认证页面
                "/login.html", "/gen_token.html", "/sso.html",
                // 调试页面
                "/test_entry.html", "/test_index.html", "/direct_test.html",
                "/tims_simulator.html", "/schedule_config.html", "/whitelist-config.html",
                "/tmis_config.html", "/query-template.html", "/simulate.html", "/oracle_test.html",
                // 业务测试页面
                "/maintenance_task_test.html",
                "/rotational_plan_test.html",
                "/rotational_task_test.html",
                "/fault_report_test.html",
                "/fault_report_create_test.html",
                "/fault_analysis_create_test.html",
                "/production_halt_task_test.html",
                "/user_equipment_test.html"
        );
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )
                .authorizeHttpRequests(authz -> authz
                        // --- 基础放行 ---
                        .antMatchers("/", "/index.html", "/main.html").permitAll()
                        .antMatchers("/api/system/auth/**").permitAll()
                        .antMatchers("/my-websocket/**").permitAll()

                        // --- [核心修改] SPA 前端路由放行 ---
                        .antMatchers(
                                "/performance_opt/**",
                                "/execution_board/**",
                                "/running_opt/**",
                                "/lifecycle_mgmt/**",
                                "/maintenance_mgmt/**"
                        ).permitAll()

                        // --- [核心修改] 业务 API 放行 (解决 403 问题) ---
                        // 必须确保这些路径在 JwtRequestFilter 中也能通过，或者 JwtRequestFilter 对无 Token 请求放行。
                        // 如果 JwtRequestFilter 逻辑严格，建议将这些 API 移至上面的 webSecurityCustomizer() 中。
                        // 这里我们保持 permitAll 配置，假设 Filter 逻辑是“无 Token 则放行（匿名）”。

                        // 1. 同步与推送相关
                        .antMatchers("/api/tims/**", "/api/push/**", "/api/schedule/**", "/api/direct-test/**").permitAll()
                        .antMatchers("/api/tmis/data/**", "/api/tspm/**").permitAll()
                        .antMatchers("/api/oracle/**").permitAll()

                        // 2. 业务模块 API (计量、自检、保养等)
                        .antMatchers("/api/metrology/**").permitAll()       // 放行计量管理 API
                        .antMatchers("/api/si/**").permitAll()              // 放行自检自控 API
                        .antMatchers("/api/maintainbook/**").permitAll()    // 放行保养台账 API
                        .antMatchers("/api/maintainbookdt/**").permitAll()  // 放行保养明细 API

                        // 3. 看板相关
                        .antMatchers("/api/kb/**").permitAll()

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(formLogin -> formLogin.disable())
                .httpBasic(httpBasic -> httpBasic.disable());

        http.addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(jwtRequestFilter, ApiKeyAuthFilter.class);


        return http.build();
    }
}