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
                        // 允许所有用户访问根URL和主HTML文件
                        .antMatchers("/", "/index.html").permitAll()
                        // 【更新】允许访问所有在 /assets/ 和 /js/ 目录下的静态资源
                        .antMatchers("/assets/**", "/js/**").permitAll()
                        .antMatchers("/assets/**", "/js/components/**","/js/views/**","/js/services/**").permitAll()
                        // 保留您原有的其他公共端点
                        .antMatchers("/login.html", "/stomp.min.js", "/sockjs.min.js","/main.html","/query-template.html", "/simulate.html").permitAll()
                        .antMatchers("/my-websocket/**").permitAll()
                        .antMatchers("/api/system/auth/**","/api/maintainbook/**", "/api/tspm/simulate/**","/api/tspm/generate-json/**","/api/tspm/received-data/**", "/api/tspm/logs").permitAll()
                        .antMatchers("/api/trigger/**").hasRole("API")
                        // 其他所有请求都需要JWT认证
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
