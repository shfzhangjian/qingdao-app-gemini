package com.lucksoft.qingdao.system.service;

import com.lucksoft.qingdao.system.entity.Role;
import com.lucksoft.qingdao.system.mapper.RoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 角色服务类
 *
 * @author Gemini
 */
@Service
public class RoleService {

    @Autowired
    private RoleMapper roleMapper;

    public Role getRoleById(Long id) {
        return roleMapper.findById(id);
    }

    public List<Role> getAllRoles(Map<String, Object> params) {
        return roleMapper.findAll(params);
    }

    public long countRoles(Map<String, Object> params) {
        return roleMapper.countAll(params);
    }

    public boolean createRole(Role role) {
        return roleMapper.insert(role) > 0;
    }

    public boolean updateRole(Role role) {
        return roleMapper.update(role) > 0;
    }

    public boolean deleteRole(Long id) {
        return roleMapper.deleteById(id) > 0;
    }
}
