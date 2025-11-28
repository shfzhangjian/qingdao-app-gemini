package com.lucksoft.qingdao.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
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


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )
                .authorizeHttpRequests(authz -> authz
                        .antMatchers("/", "/index.html").permitAll()
                        .antMatchers("/assets/**", "/js/**","/libs/**").permitAll()
                        .antMatchers("/sso.html", "/js/sso.js", "/sso/**").permitAll()
                        .antMatchers("/gen_token.html").permitAll()
                        .antMatchers("/tims_simulator.html").permitAll()
                        .antMatchers("/api/system/auth/**").permitAll()
                        .antMatchers("/schedule_config.html").permitAll()
                        .antMatchers("/whitelist-config.html").permitAll() // [新增] 允许访问新的配置页面
                        .antMatchers("/api/tims/push").permitAll()
                        .antMatchers("/login.html", "/stomp.min.js", "/sockjs.min.js","/main.html","/query-template.html", "/simulate.html").permitAll()
                        .antMatchers("/my-websocket/**").permitAll()
                        .antMatchers("/maintenance_task_test.html").permitAll()
                        .antMatchers("/api/system/auth/**").permitAll()
                        .antMatchers("/api/push/**").permitAll()
                        .antMatchers("/api/oracle/task-status/**").permitAll()
                        .antMatchers("/api/schedule/**").permitAll()
                        // [新增] 放行新生成的测试页面
                        .antMatchers(
                                "/tmis_config.html",
                                "/test_entry.html",
                                "/maintenance_task_test.html",
                                "/rotational_plan_test.html",
                                "/rotational_task_test.html",
                                "/fault_report_test.html",
                                "/fault_report_create_test.html",
                                "/fault_analysis_create_test.html",
                                "/production_halt_task_test.html",
                                "/user_equipment_test.html"
                        ).permitAll()
                                .antMatchers("/test_index.html").permitAll()
                                .antMatchers("/direct_test.html").permitAll() // [新] 允许访问新的测试页面
                        .antMatchers("/api/direct-test/**").permitAll() // [新] 允许访问新的测试API

                        // [新增] 放行所有同步 API 接口 (方便直接测试)
                        .antMatchers("/api/tims/sync/**", "/api/tims/create/**", "/api/tims/fault-report", "/api/tims/fault-analysis-report").permitAll()

                        // [新增] 放行通用查询接口
                        .antMatchers("/api/tmis/data/query").permitAll()

                        // [关键修复] 放行配置管理接口，解决 403 问题
                        .antMatchers("/api/tmis/data/config/**").permitAll()

                        // Existing public endpoints
                        .antMatchers("/api/maintainbook/**","/api/oracle/**","/oracle_test.html", "/api/tspm/simulate/**","/api/tspm/generate-json/**","/api/tspm/received-data/**", "/api/tspm/logs").permitAll()
                        // Secure new kanban endpoints
                        .antMatchers("/api/kb/**").authenticated()
                        .antMatchers("/api/trigger/**", "/api/oracle/**").hasRole("API")
                        .antMatchers("/performance_opt/**", "/execution_board/**", "/running_opt/**", "/lifecycle_mgmt/**", "/maintenance_mgmt/**").permitAll()

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
