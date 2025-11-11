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
                        .antMatchers("/login.html", "/stomp.min.js", "/sockjs.min.js","/main.html","/query-template.html", "/simulate.html").permitAll()
                        .antMatchers("/my-websocket/**").permitAll()
                        .antMatchers("/api/system/auth/**").permitAll()
                        // Existing public endpoints
                        .antMatchers("/api/maintainbook/**", "/api/tspm/simulate/**","/api/tspm/generate-json/**","/api/tspm/received-data/**", "/api/tspm/logs").permitAll()
                        // Secure new kanban endpoints
                        .antMatchers("/api/kb/**").authenticated()
                        .antMatchers("/api/trigger/**", "/api/oracle/**").hasRole("API")
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
