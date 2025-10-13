package com.lucksoft.qingdao.system.controller;

import com.lucksoft.qingdao.system.entity.FieldValue;
import com.lucksoft.qingdao.system.service.FieldValueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典明细接口控制器
 *
 * @author Gemini
 */
@RestController
@RequestMapping("/api/system")
public class FieldValueController {

    @Autowired
    private FieldValueService fieldValueService;

    /**
     * 获取指定字典下的所有明细值
     * @param fieldId 字典主表ID
     * @return 明细列表
     */
    @GetMapping("/fields/{fieldId}/values")
    public ResponseEntity<List<FieldValue>> listValuesByFieldId(@PathVariable Long fieldId) {
        List<FieldValue> values = fieldValueService.getValuesByFieldId(fieldId);
        return ResponseEntity.ok(values);
    }

    /**
     * 获取单个明细值
     * @param id 明细ID
     * @return 明细实体
     */
    @GetMapping("/values/{id}")
    public ResponseEntity<FieldValue> getValueById(@PathVariable Integer id) {
        FieldValue value = fieldValueService.getValueById(id);
        return value != null ? ResponseEntity.ok(value) : ResponseEntity.notFound().build();
    }

    /**
     * 为指定字典创建一个新的明细值
     * @param fieldId 字典主表ID
     * @param fieldValue 明细实体
     * @return 创建后的明细实体
     */
    @PostMapping("/fields/{fieldId}/values")
    public ResponseEntity<FieldValue> createValue(@PathVariable Long fieldId, @RequestBody FieldValue fieldValue) {
        fieldValue.setFsid(fieldId); // 确保关联正确
        return fieldValueService.createValue(fieldValue) ? ResponseEntity.ok(fieldValue) : ResponseEntity.badRequest().build();
    }

    /**
     * 更新一个明细值
     * @param id 明细ID
     * @param fieldValue 明细实体
     * @return 更新后的明细实体
     */
    @PutMapping("/values/{id}")
    public ResponseEntity<FieldValue> updateValue(@PathVariable Integer id, @RequestBody FieldValue fieldValue) {
        fieldValue.setId(id);
        return fieldValueService.updateValue(fieldValue) ? ResponseEntity.ok(fieldValue) : ResponseEntity.badRequest().build();
    }

    /**
     * 删除一个明细值
     * @param id 明细ID
     * @return 无内容
     */
    @DeleteMapping("/values/{id}")
    public ResponseEntity<Void> deleteValue(@PathVariable Integer id) {
        return fieldValueService.deleteValue(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
