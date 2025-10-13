package com.lucksoft.qingdao.system.controller;

import com.lucksoft.qingdao.system.entity.User;
import com.lucksoft.qingdao.system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户接口控制器
 *
 * @author Gemini
 */
@RestController
@RequestMapping("/api/system/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取用户列表（支持分页和条件查询）
     * @param params 包含查询条件和分页信息的Map
     * 例如: /api/system/users?name=张三&pageNum=1&pageSize=10
     * @return 包含列表和总数的分页结果
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listAllUsers(@RequestParam Map<String, Object> params) {
        // 将分页参数从 String 转为 Integer
        if (params.containsKey("pageNum") && params.containsKey("pageSize")) {
            params.put("pageNum", Integer.parseInt((String) params.get("pageNum")));
            params.put("pageSize", Integer.parseInt((String) params.get("pageSize")));
        }

        List<User> users = userService.getAllUsers(params);
        long total = userService.countUsers(params);

        Map<String, Object> response = new HashMap<>();
        response.put("list", users);
        response.put("total", total);

        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取单个用户
     * @param id 用户ID
     * @return 用户实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 创建一个新用户
     * @param user 用户实体
     * @return 创建后的用户实体
     */
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody User user) {
        boolean success = userService.createUser(user);
        if (success) {
            // 返回创建成功的实体，通常包含由数据库生成的ID
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新一个用户
     * @param id 用户ID
     * @param user 用户实体
     * @return 更新后的用户实体
     */
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id); // 确保更新的是正确的用户
        boolean success = userService.updateUser(user);
        if (success) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除一个用户
     * @param id 用户ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        boolean success = userService.deleteUser(id);
        if (success) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
