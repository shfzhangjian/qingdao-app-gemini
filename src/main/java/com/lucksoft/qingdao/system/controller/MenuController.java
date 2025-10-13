package com.lucksoft.qingdao.system.controller;

import com.lucksoft.qingdao.system.entity.Menu;
import com.lucksoft.qingdao.system.service.MenuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 菜单接口控制器
 *
 * @author Gemini
 */
@RestController
@RequestMapping("/api/system/menus")
public class MenuController {

    @Autowired
    private MenuService menuService;

    /**
     * 获取扁平的菜单列表（支持条件查询）
     * @param params 查询参数
     * @return 菜单列表
     */
    @GetMapping("/list")
    public ResponseEntity<List<Menu>> listAllMenus(@RequestParam Map<String, Object> params) {
        List<Menu> menus = menuService.getAllMenus(params);
        return ResponseEntity.ok(menus);
    }

    /**
     * 获取树形结构的菜单列表
     * @return 树形菜单
     */
    @GetMapping("/tree")
    public ResponseEntity<List<Menu>> getMenuTree() {
        List<Menu> menuTree = menuService.getMenuTree();
        return ResponseEntity.ok(menuTree);
    }

    /**
     * 根据ID获取单个菜单
     * @param id 菜单ID
     * @return 菜单实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<Menu> getMenuById(@PathVariable Long id) {
        Menu menu = menuService.getMenuById(id);
        if (menu != null) {
            return ResponseEntity.ok(menu);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 创建一个新菜单
     * @param menu 菜单实体
     * @return 创建后的菜单实体
     */
    @PostMapping
    public ResponseEntity<Menu> createMenu(@RequestBody Menu menu) {
        boolean success = menuService.createMenu(menu);
        if (success) {
            return ResponseEntity.ok(menu);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新一个菜单
     * @param id 菜单ID
     * @param menu 菜单实体
     * @return 更新后的菜单实体
     */
    @PutMapping("/{id}")
    public ResponseEntity<Menu> updateMenu(@PathVariable Long id, @RequestBody Menu menu) {
        menu.setId(id);
        boolean success = menuService.updateMenu(menu);
        if (success) {
            return ResponseEntity.ok(menu);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除一个菜单
     * @param id 菜单ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenu(@PathVariable Long id) {
        boolean success = menuService.deleteMenu(id);
        if (success) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
