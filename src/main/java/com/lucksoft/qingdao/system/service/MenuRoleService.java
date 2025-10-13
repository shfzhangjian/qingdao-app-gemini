package com.lucksoft.qingdao.system.service;

import com.lucksoft.qingdao.system.entity.Menu;
import com.lucksoft.qingdao.system.entity.MenuRole;
import com.lucksoft.qingdao.system.mapper.MenuRoleMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 角色菜单关联服务类
 *
 * @author Gemini
 */
@Service
public class MenuRoleService {

    @Autowired
    private MenuRoleMapper menuRoleMapper;

    /**
     * 获取指定角色的所有菜单
     * @param roleId 角色ID
     * @return 菜单列表
     */
    public List<Menu> getMenusByRoleId(Long roleId) {
        return menuRoleMapper.findMenusByRoleId(roleId);
    }

    /**
     * 为角色重新分配菜单权限
     * @param roleId  角色ID
     * @param menuIds 新的菜单ID列表
     * @return 是否成功
     */
    @Transactional // 添加事务管理，确保操作的原子性
    public boolean assignMenusToRole(Long roleId, List<Long> menuIds) {
        // 1. 先删除该角色的所有旧菜单权限
        menuRoleMapper.deleteByRoleId(roleId);

        // 2. 如果新的菜单列表不为空，则逐一添加
        if (menuIds != null && !menuIds.isEmpty()) {
            for (Long menuId : menuIds) {
                MenuRole menuRole = new MenuRole();
                menuRole.setRoleId(roleId);
                menuRole.setMenuId(menuId);
                menuRoleMapper.insert(menuRole);
            }
        }
        return true;
    }
}
