package com.lucksoft.qingdao.system.service;

import com.lucksoft.qingdao.system.dto.LoginResponse;
import com.lucksoft.qingdao.system.dto.UserInfo;
import com.lucksoft.qingdao.system.entity.Menu;
import com.lucksoft.qingdao.system.entity.Role;
import com.lucksoft.qingdao.system.entity.User;
import com.lucksoft.qingdao.system.mapper.MenuMapper;
import com.lucksoft.qingdao.system.mapper.UserMapper;
import com.lucksoft.qingdao.system.mapper.UserRoleMapper;
import com.lucksoft.qingdao.system.util.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


/**
 * 用户服务类
 *
 * @author Gemini
 */
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserRoleMapper userRoleMapper;
    @Autowired
    private MenuMapper menuMapper;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    /**
     * 【新增】根据Token从Redis获取用户信息
     * @param token JWT Token
     * @return 包含用户、角色和菜单的详细信息对象
     */
    public UserInfo getUserInfoByToken(String token) {
        String redisKey = "user_token:" + token;
        return (UserInfo) redisTemplate.opsForValue().get(redisKey);
    }

    /**
     * 【新增】用户登录逻辑
     */
    public LoginResponse login(String loginid, String password) {
        // 1. 验证用户
        User user = userMapper.findByLoginId(loginid);
        if (user == null || !user.getPasswd().equals(password)) { // 注意：实际项目应使用加密密码比对
            throw new RuntimeException("用户名或密码错误");
        }
        if (!"1".equals(user.getStatus())) {
            throw new RuntimeException("用户已被禁用");
        }

        // 2. 获取角色
        List<Role> roles = userRoleMapper.findRolesByUserId(user.getId());
        if (roles.isEmpty()) {
            throw new RuntimeException("用户未分配任何角色");
        }

        // 3. 获取所有角色的ID
        List<Long> roleIds = roles.stream().map(Role::getId).collect(Collectors.toList());

        // 4. 根据角色获取所有可访问的菜单
        List<Menu> accessibleMenus = menuMapper.findMenusByRoleIds(roleIds);

        // 5. 将菜单列表转换为树形结构
        List<Menu> menuTree = buildMenuTree(accessibleMenus);

        // 6. 生成JWT Token
        String token = jwtTokenUtil.generateToken(user);

        // 7. 将完整的用户信息（包括树形菜单）存入Redis
        UserInfo userInfo = new UserInfo();
        userInfo.setUser(user);
        userInfo.setRoles(roles);
        userInfo.setMenuTree(menuTree);
        redisTemplate.opsForValue().set("user_token:" + token, userInfo, 3600, TimeUnit.SECONDS); // 缓存1小时

        // 8. 返回Token和基本用户信息给前端
        return new LoginResponse(token, user);
    }

    private List<Menu> buildMenuTree(List<Menu> allMenus) {
        List<Menu> rootMenus = allMenus.stream()
                .filter(menu -> menu.getParentId() == null || menu.getParentId() == 0)
                .collect(Collectors.toList());
        rootMenus.forEach(root -> root.setChildren(findChildren(root, allMenus)));
        return rootMenus;
    }

    private List<Menu> findChildren(Menu parent, List<Menu> allMenus) {
        return allMenus.stream()
                .filter(menu -> parent.getId().equals(menu.getParentId()))
                .peek(child -> child.setChildren(findChildren(child, allMenus)))
                .collect(Collectors.toList());
    }


    /**
     * 根据ID获取用户详情
     * @param id 用户ID
     * @return 用户实体
     */
    public User getUserById(Long id) {
        return userMapper.findById(id);
    }

    /**
     * 根据登录账号获取用户
     * @param loginid 登录账号
     * @return 用户实体
     */
    public User getUserByLoginId(String loginid) {
        return userMapper.findByLoginId(loginid);
    }

    /**
     * 根据动态条件和分页参数获取用户列表
     * @param params 查询参数
     * @return 用户列表
     */
    public List<User> getAllUsers(Map<String, Object> params) {
        // 计算分页起始位置
        // 如果前端传来 pageNum 和 pageSize
        if (params.get("pageNum") != null && params.get("pageSize") != null) {
            int pageNum = (Integer) params.get("pageNum");
            int pageSize = (Integer) params.get("pageSize");
            // Oracle ROWNUM 分页需要 offset
            int offset = (pageNum - 1) * pageSize;
            params.put("offset", offset);
        }
        return userMapper.findAll(params);
    }

    /**
     * 根据动态条件获取用户总数
     * @param params 查询参数
     * @return 记录总数
     */
    public long countUsers(Map<String, Object> params) {
        return userMapper.countAll(params);
    }


    /**
     * 创建新用户
     * @param user 用户实体
     * @return 是否成功
     */
    public boolean createUser(User user) {
        // 在这里可以添加密码加密等业务逻辑
        return userMapper.insert(user) > 0;
    }

    /**
     * 更新用户信息
     * @param user 用户实体
     * @return 是否成功
     */
    public boolean updateUser(User user) {
        return userMapper.update(user) > 0;
    }

    /**
     * 删除用户
     * @param id 用户ID
     * @return 是否成功
     */
    public boolean deleteUser(Long id) {
        return userMapper.deleteById(id) > 0;
    }
}
