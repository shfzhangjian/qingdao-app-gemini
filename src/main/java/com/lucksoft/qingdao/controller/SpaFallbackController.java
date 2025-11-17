package com.lucksoft.qingdao.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * [新] 单页面应用 (SPA) 路由回退控制器
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * CSS and API calls.
 * This ensures that refreshing the page on a path like "/performance_opt/metrology_mgmt/tasks"
 * will correctly load the index.html page, allowing the JavaScript router to take over.
 */
@Controller
public class SpaFallbackController {

    /**
     * 捕获所有与 menu.js 中定义的主路由匹配的深层链接。
     * 这会将所有前端路由请求转发到 /index.html。
     * * @return 转发到 index.html
     */
    @GetMapping(value = {
            "/performance_opt/**",
            "/execution_board/**",
            "/running_opt/**",
            "/lifecycle_mgmt/**",
            "/maintenance_mgmt/**"
    })
    public String spaFallback() {
        return "forward:/index.html";
    }
}