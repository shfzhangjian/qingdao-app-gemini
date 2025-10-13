package com.lucksoft.qingdao.system.controller;

import com.lucksoft.qingdao.system.entity.Role;
import com.lucksoft.qingdao.system.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 角色接口控制器
 *
 * @author Gemini
 */
@RestController
@RequestMapping("/api/system/roles")
public class RoleController {

    @Autowired
    private RoleService roleService;

    /**
     * 获取角色列表（支持条件查询）
     * @param params 查询参数，例如: /api/system/roles?name=管理员
     * @return 包含列表和总数的结果
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listAllRoles(@RequestParam Map<String, Object> params) {
        List<Role> roles = roleService.getAllRoles(params);
        long total = roleService.countRoles(params);

        Map<String, Object> response = new HashMap<>();
        response.put("list", roles);
        response.put("total", total);

        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取单个角色
     * @param id 角色ID
     * @return 角色实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<Role> getRoleById(@PathVariable Long id) {
        Role role = roleService.getRoleById(id);
        if (role != null) {
            return ResponseEntity.ok(role);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 创建一个新角色
     * @param role 角色实体
     * @return 创建后的角色实体
     */
    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Role role) {
        boolean success = roleService.createRole(role);
        if (success) {
            return ResponseEntity.ok(role);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新一个角色
     * @param id 角色ID
     * @param role 角色实体
     * @return 更新后的角色实体
     */
    @PutMapping("/{id}")
    public ResponseEntity<Role> updateRole(@PathVariable Long id, @RequestBody Role role) {
        role.setId(id);
        boolean success = roleService.updateRole(role);
        if (success) {
            return ResponseEntity.ok(role);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除一个角色
     * @param id 角色ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long id) {
        boolean success = roleService.deleteRole(id);
        if (success) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
