package com.lucksoft.qingdao.system.service;

import com.lucksoft.qingdao.system.entity.FieldValue;
import com.lucksoft.qingdao.system.mapper.FieldValueMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 数据字典明细服务类
 *
 * @author Gemini
 */
@Service
public class FieldValueService {

    @Autowired
    private FieldValueMapper fieldValueMapper;

    public List<FieldValue> getValuesByFieldId(Long fieldId) {
        return fieldValueMapper.findByFieldId(fieldId);
    }

    public FieldValue getValueById(Integer id) {
        return fieldValueMapper.findById(id);
    }

    public boolean createValue(FieldValue fieldValue) {
        return fieldValueMapper.insert(fieldValue) > 0;
    }

    public boolean updateValue(FieldValue fieldValue) {
        return fieldValueMapper.update(fieldValue) > 0;
    }

    public boolean deleteValue(Integer id) {
        return fieldValueMapper.deleteById(id) > 0;
    }
}
