package com.lucksoft.qingdao.system.service;

import com.lucksoft.qingdao.system.entity.Field;
import com.lucksoft.qingdao.system.mapper.FieldMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 数据字典主表服务类
 *
 * @author Gemini
 */
@Service
public class FieldService {

    @Autowired
    private FieldMapper fieldMapper;

    public Field getFieldById(Long id) {
        return fieldMapper.findById(id);
    }

    public List<Field> getAllFields(Map<String, Object> params) {
        return fieldMapper.findAll(params);
    }

    public long countFields(Map<String, Object> params) {
        return fieldMapper.countAll(params);
    }

    public boolean createField(Field field) {
        return fieldMapper.insert(field) > 0;
    }

    public boolean updateField(Field field) {
        return fieldMapper.update(field) > 0;
    }

    public boolean deleteField(Long id) {
        // 注意：在实际应用中，删除主表记录前应先处理其所有子表记录
        return fieldMapper.deleteById(id) > 0;
    }
}
