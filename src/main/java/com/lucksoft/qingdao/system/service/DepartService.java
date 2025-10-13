package com.lucksoft.qingdao.system.service;

import com.lucksoft.qingdao.system.entity.Depart;
import com.lucksoft.qingdao.system.mapper.DepartMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 部门服务类
 *
 * @author Gemini
 */
@Service
public class DepartService {

    @Autowired
    private DepartMapper departMapper;

    public Depart getDepartById(String id) {
        return departMapper.findById(id);
    }

    public List<Depart> getAllDeparts(Map<String, Object> params) {
        return departMapper.findAll(params);
    }

    public long countDeparts(Map<String, Object> params) {
        return departMapper.countAll(params);
    }

    public boolean createDepart(Depart depart) {
        return departMapper.insert(depart) > 0;
    }

    public boolean updateDepart(Depart depart) {
        return departMapper.update(depart) > 0;
    }

    public boolean deleteDepart(String id) {
        return departMapper.deleteById(id) > 0;
    }
}
