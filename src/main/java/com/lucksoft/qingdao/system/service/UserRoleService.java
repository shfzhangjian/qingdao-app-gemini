package com.lucksoft.qingdao.system.service;

import com.lucksoft.qingdao.system.entity.Role;
import com.lucksoft.qingdao.system.entity.UserRole;
import com.lucksoft.qingdao.system.mapper.UserRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户角色关联服务类
 *
 * @author Gemini
 */
@Service
public class UserRoleService {

    @Autowired
    private UserRoleMapper userRoleMapper;

    /**
     * 获取指定用户的所有角色
     * @param userId 用户ID
     * @return 角色列表
     */
    public List<Role> getRolesByUserId(Long userId) {
        return userRoleMapper.findRolesByUserId(userId);
    }

    /**
     * 为用户重新分配角色
     * @param userId  用户ID
     * @param roleIds 新的角色ID列表
     * @return 是否成功
     */
    @Transactional // 添加事务管理，确保操作的原子性
    public boolean assignRolesToUser(Long userId, List<Long> roleIds) {
        // 1. 先删除该用户的所有旧角色
        userRoleMapper.deleteByUserId(userId);

        // 2. 如果新的角色列表不为空，则逐一添加
        if (roleIds != null && !roleIds.isEmpty()) {
            for (Long roleId : roleIds) {
                UserRole userRole = new UserRole();
                userRole.setUserId(userId);
                userRole.setRoleId(roleId);
                userRole.setIflag(1); // 默认为已授权
                userRoleMapper.insert(userRole);
            }
        }
        return true;
    }
}
