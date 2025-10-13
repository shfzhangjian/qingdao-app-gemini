package com.lucksoft.qingdao.system.controller;

import com.lucksoft.qingdao.system.entity.Role;
import com.lucksoft.qingdao.system.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户角色关联接口控制器
 *
 * @author Gemini
 */
@RestController
@RequestMapping("/api/system")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    /**
     * 获取指定用户拥有的角色列表
     * @param userId 用户ID
     * @return 角色列表
     */
    @GetMapping("/users/{userId}/roles")
    public ResponseEntity<List<Role>> getRolesForUser(@PathVariable Long userId) {
        List<Role> roles = userRoleService.getRolesByUserId(userId);
        return ResponseEntity.ok(roles);
    }

    /**
     * 为指定用户分配角色
     * @param userId  用户ID
     * @param roleIds 角色ID的列表，在请求体中以JSON数组形式提供，例如 [1, 2, 3]
     * @return 操作结果
     */
    @PostMapping("/users/{userId}/roles")
    public ResponseEntity<Void> assignRolesToUser(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        boolean success = userRoleService.assignRolesToUser(userId, roleIds);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            // 在实际项目中，可以根据失败原因返回更具体的错误状态
            return ResponseEntity.badRequest().build();
        }
    }
}
