package com.lucksoft.qingdao.system.service;

import com.lucksoft.qingdao.system.entity.Menu;
import com.lucksoft.qingdao.system.mapper.MenuMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 菜单服务类
 *
 * @author Gemini
 */
@Service
public class MenuService {

    @Autowired
    private MenuMapper menuMapper;

    public Menu getMenuById(Long id) {
        return menuMapper.findById(id);
    }

    public List<Menu> getAllMenus(Map<String, Object> params) {
        return menuMapper.findAll(params);
    }

    /**
     * 获取菜单的树形结构
     * @return 树形结构的菜单列表
     */
    public List<Menu> getMenuTree() {
        // 1. 获取所有菜单
        List<Menu> allMenus = menuMapper.findAll(null);

        // 2. 筛选出所有根节点 (parentId为null或0)
        List<Menu> rootMenus = allMenus.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .collect(Collectors.toList());

        // 3. 递归为每个根节点设置子节点
        rootMenus.forEach(root -> root.setChildren(findChildren(root, allMenus)));

        return rootMenus;
    }

    /**
     * 递归查找子菜单
     * @param parent 父菜单
     * @param allMenus 所有菜单的列表
     * @return 子菜单列表
     */
    private List<Menu> findChildren(Menu parent, List<Menu> allMenus) {
        return allMenus.stream()
                // 筛选出父ID为当前节点ID的菜单
                .filter(menu -> parent.getId().equals(menu.getParentId()))
                .peek(child -> {
                    // 为每个子节点递归查找其子节点
                    child.setChildren(findChildren(child, allMenus));
                })
                .collect(Collectors.toList());
    }

    public boolean createMenu(Menu menu) {
        return menuMapper.insert(menu) > 0;
    }

    public boolean updateMenu(Menu menu) {
        return menuMapper.update(menu) > 0;
    }

    public boolean deleteMenu(Long id) {
        // 注意：在实际项目中，删除父菜单前需要处理其子菜单
        return menuMapper.deleteById(id) > 0;
    }
}
