package com.lucksoft.qingdao.system.controller;

import com.lucksoft.qingdao.system.entity.Depart;
import com.lucksoft.qingdao.system.service.DepartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部门接口控制器
 *
 * @author Gemini
 */
@RestController
@RequestMapping("/api/system/departs")
public class DepartController {

    @Autowired
    private DepartService departService;

    /**
     * 获取部门列表
     * @param params 查询参数, 例如: /api/system/departs?sdepName=财务
     * @return 包含列表和总数的结果
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> listAllDeparts(@RequestParam Map<String, Object> params) {
        List<Depart> departs = departService.getAllDeparts(params);
        long total = departService.countDeparts(params);

        Map<String, Object> response = new HashMap<>();
        response.put("list", departs);
        response.put("total", total);

        return ResponseEntity.ok(response);
    }

    /**
     * 根据ID获取单个部门
     * @param id 部门ID
     * @return 部门实体
     */
    @GetMapping("/{id}")
    public ResponseEntity<Depart> getDepartById(@PathVariable String id) {
        Depart depart = departService.getDepartById(id);
        if (depart != null) {
            return ResponseEntity.ok(depart);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 创建一个新部门
     * @param depart 部门实体
     * @return 创建后的部门实体
     */
    @PostMapping
    public ResponseEntity<Depart> createDepart(@RequestBody Depart depart) {
        boolean success = departService.createDepart(depart);
        if (success) {
            return ResponseEntity.ok(depart);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 更新一个部门
     * @param id 部门ID
     * @param depart 部门实体
     * @return 更新后的部门实体
     */
    @PutMapping("/{id}")
    public ResponseEntity<Depart> updateDepart(@PathVariable String id, @RequestBody Depart depart) {
        depart.setSdepId(id);
        boolean success = departService.updateDepart(depart);
        if (success) {
            return ResponseEntity.ok(depart);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 删除一个部门
     * @param id 部门ID
     * @return 无内容
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepart(@PathVariable String id) {
        boolean success = departService.deleteDepart(id);
        if (success) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
