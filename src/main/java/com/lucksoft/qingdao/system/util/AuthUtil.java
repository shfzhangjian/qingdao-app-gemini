package com.lucksoft.qingdao.system.util;

import com.lucksoft.qingdao.system.dto.UserInfo;
import com.lucksoft.qingdao.system.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * 认证授权相关的工具类
 */
@Component // 使其成为 Spring Bean，方便注入 UserService
public class AuthUtil {

    private final UserService userService;

    // 通过构造函数注入 UserService
    public AuthUtil(UserService userService) {
        this.userService = userService;
    }

    /**
     * 从 HttpServletRequest 中提取 JWT，并获取当前登录用户的 LoginId。
     *
     * @param request HttpServletRequest 对象
     * @return 当前用户的 LoginId，如果无法获取则返回 null
     */
    public String getCurrentUserLoginId(HttpServletRequest request) {
        String tokenHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(tokenHeader) && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            UserInfo userInfo = userService.getUserInfoByToken(token); // 使用注入的 userService
            if (userInfo != null && userInfo.getUser() != null) {
                return userInfo.getUser().getLoginid(); // 返回用户的登录ID
            }
        }
        return null; // 获取失败返回 null
    }

    /**
     * 从 HttpServletRequest 中提取 JWT，并获取当前登录用户的 UserInfo 对象。
     *
     * @param request HttpServletRequest 对象
     * @return 当前用户的 UserInfo 对象，如果无法获取则返回 null
     */
    public UserInfo getCurrentUserInfo(HttpServletRequest request) {
        String tokenHeader = request.getHeader("Authorization");
        if (StringUtils.hasText(tokenHeader) && tokenHeader.startsWith("Bearer ")) {
            String token = tokenHeader.substring(7);
            return userService.getUserInfoByToken(token); // 使用注入的 userService
        }
        return null; // 获取失败返回 null
    }
}
