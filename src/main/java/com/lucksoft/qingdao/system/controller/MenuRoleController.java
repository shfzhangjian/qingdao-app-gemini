package com.lucksoft.qingdao.system.controller;

import com.lucksoft.qingdao.system.entity.Menu;
import com.lucksoft.qingdao.system.service.MenuRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 角色菜单关联接口控制器
 *
 * @author Gemini
 */
@RestController
@RequestMapping("/api/system")
public class MenuRoleController {

    @Autowired
    private MenuRoleService menuRoleService;

    /**
     * 获取指定角色拥有的菜单列表
     * @param roleId 角色ID
     * @return 菜单列表
     */
    @GetMapping("/roles/{roleId}/menus")
    public ResponseEntity<List<Menu>> getMenusForRole(@PathVariable Long roleId) {
        List<Menu> menus = menuRoleService.getMenusByRoleId(roleId);
        return ResponseEntity.ok(menus);
    }

    /**
     * 为指定角色分配菜单权限
     * @param roleId  角色ID
     * @param menuIds 菜单ID的列表，在请求体中以JSON数组形式提供，例如 [101, 102]
     * @return 操作结果
     */
    @PostMapping("/roles/{roleId}/menus")
    public ResponseEntity<Void> assignMenusToRole(@PathVariable Long roleId, @RequestBody List<Long> menuIds) {
        boolean success = menuRoleService.assignMenusToRole(roleId, menuIds);
        if (success) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
}
