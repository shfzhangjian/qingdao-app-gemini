package com.lucksoft.qingdao.system.controller;

import com.lucksoft.qingdao.system.entity.Field;
import com.lucksoft.qingdao.system.service.FieldService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据字典主表接口控制器
 *
 * @author Gemini
 */
@RestController
@RequestMapping("/api/system/fields")
public class FieldController {

    @Autowired
    private FieldService fieldService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> listAllFields(@RequestParam Map<String, Object> params) {
        List<Field> fields = fieldService.getAllFields(params);
        long total = fieldService.countFields(params);
        Map<String, Object> response = new HashMap<>();
        response.put("list", fields);
        response.put("total", total);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Field> getFieldById(@PathVariable Long id) {
        Field field = fieldService.getFieldById(id);
        return field != null ? ResponseEntity.ok(field) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Field> createField(@RequestBody Field field) {
        return fieldService.createField(field) ? ResponseEntity.ok(field) : ResponseEntity.badRequest().build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<Field> updateField(@PathVariable Long id, @RequestBody Field field) {
        field.setId(id);
        return fieldService.updateField(field) ? ResponseEntity.ok(field) : ResponseEntity.badRequest().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteField(@PathVariable Long id) {
        return fieldService.deleteField(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
