/**
 * 源码路径: js/app.js
 * 功能说明: 应用的主入口文件，负责初始化、路由管理、页面调度和主框架渲染。
 * v1.7.0 - 2025-10-15: Integrated with AuthManager for re-authentication and UI updates.
 * v1.8.0 - 2025-11-14: [新增] IP白名单自动登录逻辑
 * v1.9.0 - 2025-11-15: [修改] 切换到 HTML5 History 模式路由
 * v1.9.1 - 2025-11-16: [修复] 修复了 IP 登录后白屏的问题
 */
import menuConfig from './config/menu.js';
import Breadcrumb from './components/Breadcrumb.js';
import Modal from './components/Modal.js';

class App {
    constructor(menuConfig) {
        this.menuConfig = menuConfig;
        this.topNav = document.getElementById('top-nav');
        this.sidebarMenu = document.getElementById('sidebar-menu');
        this.contentArea = document.getElementById('content-area');
        this.sidebarToggle = document.getElementById('sidebar-toggle');
        this.sidebar = document.getElementById('sidebar');
        this.sidebarHeaderTitle = document.getElementById('sidebar-header-title');
        this.appBody = document.getElementById('app-body');
        this.viewInstances = new Map();
        this.breadcrumb = new Breadcrumb();
        this.themeToggler = document.getElementById('theme-toggler');

        this.init(); // init 现在是 async
    }

    /**
     * [修改] init() 方法现在是 async，用于处理IP登录
     */
    async init() {
        // 1. 在执行任何操作前，首先检查 URL 参数
        const urlParams = new URLSearchParams(window.location.search);
        const loginid = urlParams.get('userCode');
        const view = urlParams.get('view');
        const theme = urlParams.get('theme');
        const currentLoginId = localStorage.getItem('current_loginid');

        let ipLoginUser = null; // 用于存储IP登录成功后的用户信息

        // 2. [新增] 执行你的核心需求：
        // 仅当 URL 提供了 loginid，且该 loginid 与 localStorage 中已存的 loginid 不一致时，
        // 才触发 IP 自动登录流程。
        if (loginid && loginid !== currentLoginId) {
            console.log(`[App] 检测到 URL loginid (${loginid}) 与当前用户 (${currentLoginId}) 不符。`);
            console.log("[App] 正在尝试 IP 白名单自动登录...");
            try {
                // 3. 调用新的后端端点
                const response = await fetch(`/tmis/api/system/auth/ip-login?loginid=${loginid}`);

                if (response.ok) {
                    // 4. IP登录成功 (IP在白名单中, 用户有效)
                    const data = await response.json(); // LoginResponse { token, user }
                    localStorage.setItem('jwt_token', data.token);
                    localStorage.setItem('current_loginid', data.user.loginid);
                    ipLoginUser = data.user; // 存储用户信息，以便后续更新UI
                    console.log(`[App] IP 登录成功: ${loginid}。已存储新 Token。`);

                    // 5. 清理 URL，防止下次刷新时再次触发登录
                    this.removeLoginParamsFromURL();

                } else {
                    // 6. IP登录失败 (IP不在白名单, 或用户无效)
                    const errorData = await response.json();
                    console.error(`[App] IP 自动登录失败 (HTTP ${response.status}):`, errorData);
                    // 弹窗提示用户，然后继续走标准流程
                    Modal.alert(`IP 自动登录失败: ${errorData.message || errorData.error}`);
                    // 清理URL，防止刷新时再次失败
                    this.removeLoginParamsFromURL();
                }

            } catch (error) {
                console.error("[App] IP 自动登录请求本身失败:", error);
                Modal.alert(`IP 自动登录请求失败: ${error.message}`);
            }
        }

        // 7. 继续执行标准的应用初始化
        this.continueInit(view, theme, ipLoginUser);
    }

    /**
     * [新增] 从 URL 中移除 loginid 参数，防止刷新时重复登录
     */
    removeLoginParamsFromURL() {
        const url = new URL(window.location);
        url.searchParams.delete('loginid');
        // 保留 view 等其他参数
        window.history.replaceState({}, document.title, url.pathname + url.search + url.hash);
    }

    /**
     * [重构] 原始的 init() 方法被重构为 continueInit()
     * @param {string} viewParam - 'view' URL 参数
     * @param {string} themeParam - 'theme' URL 参数 [新增]
     * @param {object | null} preloadedUser - 如果IP登录成功，则传入用户信息
     */
    continueInit(viewParam, themeParam, preloadedUser = null) {
        this._initTheme(themeParam);

        const isContentOnly = viewParam === 'content';

        if (isContentOnly) {
            this.appBody.classList.add('content-only-mode');
        } else {
            this.renderTopNav(); // 创建 Header DOM
            this.sidebarToggle.addEventListener('click', () => this.toggleSidebar());
            this._attachHeaderListeners(preloadedUser); // 附加监听器并加载用户信息
        }

        // [修改] 监听 'popstate' (浏览器前进/后退)
        window.addEventListener('popstate', () => this.handleRouteChange());

        // [修改] 监听全局点击事件，以拦截 <a href> 导航
        document.addEventListener('click', (e) => {
            const link = e.target.closest('a');

            // 检查链接是否存在、是否有 href，以及是否是 SPA 内部链接 (以 /tmis/ 开头)
            // 确保它不是一个外部链接或一个 data-bs-toggle 按钮
            if (link && link.href && link.pathname.startsWith('/tmis/') && !link.dataset.bsToggle) {
                e.preventDefault(); // 阻止浏览器默认导航

                // 使用 History API 更新 URL
                window.history.pushState(null, '', link.href);

                // 手动触发路由处理
                this.handleRouteChange();
            }
        });


        // 监听 AuthManager 发出的事件 (用于多标签页同步)
        window.addEventListener('userSwitched', (e) => {
            this.updateUserInfo(e.detail.user);
        });

        // [关键修复]
        // 无论IP登录是否发生，都在 continueInit 的末尾
        // 立即调用 handleRouteChange() 来解析当前 URL 并渲染视图。
        this.handleRouteChange();
    }

    /**
     * [修改] 接收 preloadedUser，避免重复发起 /info 请求
     */
    _attachHeaderListeners(preloadedUser = null) {
        // Theme Toggler
        this.themeToggler.addEventListener('click', (e) => {
            e.preventDefault();
            this._toggleTheme();
        });

        // Logout Button
        const logoutBtn = document.getElementById('logout-btn');
        if (logoutBtn) {
            logoutBtn.addEventListener('click', async (e) => {
                e.preventDefault();
                const confirmed = await Modal.confirm('退出系统', '您确定要退出当前系统吗？');
                if (confirmed) {
                    console.log("User confirmed logout. Clearing token...");
                    localStorage.removeItem('jwt_token');
                    localStorage.removeItem('current_loginid'); // [新增] 清除已存用户
                    Modal.alert('您已成功退出。');
                    window.location.href = 'login.html'; // [修改] 强制重定向到登录页
                }
            });
        }

        // [修改] 用户信息加载逻辑
        if (preloadedUser) {
            // 1. 如果通过 IP 登录成功，直接使用该用户信息更新UI
            console.log("[App] 使用IP登录的预加载用户信息更新UI。");
            this.updateUserInfo(preloadedUser);
        } else {
            // 2. 否则，按原流程检查 localStorage 中是否有 token
            const token = localStorage.getItem('jwt_token');
            if (token) {
                // 3. 如果有 token，异步加载 /info
                (async () => {
                    try {
                        console.log("[App] 检测到现有 Token，正在获取用户信息...");
                        // 动态导入 apiFetch
                        const api = (await import('./services/api.js')).apiFetch;
                        const userInfo = await api('api/system/auth/info');
                        if (userInfo && userInfo.user) {
                            this.updateUserInfo(userInfo.user);
                            // 确保 localStorage 与 token 同步
                            localStorage.setItem('current_loginid', userInfo.user.loginid);
                        }
                    } catch (error) {
                        console.error("加载用户信息失败:", error);
                        // api.js 中的 AuthManager 会自动处理 401/403 错误并弹出登录框
                    }
                })();
            }
        }
    }

    /**
     * [修改] 更新 updateUserInfo 来查询正确的元素
     */
    updateUserInfo(user) {
        // Header 中的用户名元素
        const userNameEl = document.querySelector('.dropdown .ms-2');
        if (userNameEl && user) {
            userNameEl.textContent = user.name || user.loginid;
        }
    }

    _initTheme(themeFromUrl = null) {
        let finalTheme = 'dark'; // 默认主题

        // 1. 检查 URL 参数 (最高优先级)
        if (themeFromUrl === 'light' || themeFromUrl === 'dark') {
            finalTheme = themeFromUrl;
            // 注意：我们不将 URL 参数保存到 localStorage，
            // 因为它应该是临时的，以便在下次正常访问时恢复用户保存的设置。
        } else {
            // 2. 如果 URL 没有指定，则检查 localStorage
            const savedTheme = localStorage.getItem('theme');
            if (savedTheme === 'light' || savedTheme === 'dark') {
                finalTheme = savedTheme;
            }
        }

        // 3. 应用主题
        // 先移除所有可能存在的主题类，以防冲突
        this.appBody.classList.remove('theme-light', 'theme-dark');
        this.appBody.classList.add(`theme-${finalTheme}`);

        // 4. 更新切换按钮的图标 (仅当按钮存在时)
        if (this.themeToggler) {
            this._updateThemeIcon(finalTheme);
        }
    }

    _toggleTheme() {
        const currentTheme = this.appBody.classList.contains('theme-light') ? 'light' : 'dark';
        const newTheme = currentTheme === 'light' ? 'dark' : 'light';

        this.appBody.classList.remove(`theme-${currentTheme}`);
        this.appBody.classList.add(`theme-${newTheme}`);
        localStorage.setItem('theme', newTheme);
        this._updateThemeIcon(newTheme);
    }

    _updateThemeIcon(theme) {
        const icon = this.themeToggler.querySelector('i');
        if (theme === 'light') {
            icon.classList.remove('bi-sun-fill');
            icon.classList.add('bi-moon-fill');
        } else {
            icon.classList.remove('bi-moon-fill');
            icon.classList.add('bi-sun-fill');
        }
    }


    toggleSidebar() {
        this.sidebar.classList.toggle('collapsed');
        this.appBody.classList.toggle('sidebar-collapsed');
    }

    renderTopNav() {
        this.menuConfig.forEach(item => {
            const li = document.createElement('li');
            li.className = 'nav-item';
            li.innerHTML = `<a class="nav-link" href="#" data-id="${item.id}">${item.title}</a>`;
            this.topNav.appendChild(li);
        });
        this.topNav.addEventListener('click', (e) => {
            if (e.target.tagName === 'A') {
                e.preventDefault();
                const topMenuId = e.target.dataset.id;
                const firstSubMenu = this.findFirstLeaf(this.menuConfig.find(m => m.id === topMenuId));
                if (firstSubMenu && firstSubMenu.url) {
                    // [修改] 使用 History API 导航，而不是设置 hash
                    window.history.pushState(null, '', firstSubMenu.url);
                    this.handleRouteChange();
                }
            }
        });
    }

    findFirstLeaf(menuItem) {
        if (!menuItem || !menuItem.children || menuItem.children.length === 0) {
            return menuItem;
        }
        return this.findFirstLeaf(menuItem.children[0]);
    }

    renderSidebar(topMenuId) {
        this.sidebarMenu.innerHTML = '';
        const topMenuItem = this.menuConfig.find(item => item.id === topMenuId);
        if (!topMenuItem || !topMenuItem.children) return;

        const createMenuItem = (item) => {
            const li = document.createElement('li');
            li.className = 'menu-item';
            li.dataset.id = item.id;

            if (item.children) {
                li.innerHTML = `
                    <a href="#" data-bs-toggle="collapse" data-bs-target="#submenu-${item.id}" aria-expanded="false">
                        <i class="menu-icon ${item.icon || 'bi-folder'}"></i>
                        <span class="menu-text">${item.title}</span>
                        <i class="bi bi-chevron-down arrow-icon ms-auto"></i>
                    </a>
                    <ul class="collapse submenu" id="submenu-${item.id}"></ul>
                `;
                const submenu = li.querySelector('.submenu');
                item.children.forEach(child => submenu.appendChild(createMenuItem(child)));
            } else {
                li.innerHTML = `
                     <a href="${item.url}">
                        <i class="menu-icon ${item.icon || 'bi-file-earmark'}"></i>
                        <span class="menu-text">${item.title}</span>
                    </a>
                `;
            }
            return li;
        };

        topMenuItem.children.forEach(child => this.sidebarMenu.appendChild(createMenuItem(child)));
    }

    async handleRouteChange() {
        const defaultRoute = this.findFirstLeaf(this.menuConfig[0]);

        // [修改] 从 window.location.pathname 读取路由
        let path = window.location.pathname; // 例如: /tmis/performance_opt/metrology_mgmt/tasks

        // [修改] 检查路径是否是 /tmis/ 或 /tmis，如果是，则重定向到默认路由
        if (path === '/tmis/' || path === '/tmis') {
            if (defaultRoute && defaultRoute.url) {
                window.history.replaceState(null, '', defaultRoute.url);
                path = window.location.pathname; // 更新 path 为新路径
            } else {
                return; // 没有默认路由，不执行任何操作
            }
        }

        // [修改] 解析路径
        const contextPath = '/tmis';
        const routePath = path.startsWith(contextPath) ? path.substring(contextPath.length) : path; // -> /performance_opt/metrology_mgmt/tasks
        const pathParts = routePath.split('/').filter(p => p.length > 0); // -> ['performance_opt', 'metrology_mgmt', 'tasks']

        // 根据当前路由判断是否进入看板模式
        if (pathParts[0] === 'execution_board') {
            this.appBody.classList.add('board-mode');
        } else {
            this.appBody.classList.remove('board-mode');
        }
        this.contentArea.innerHTML = `
            <div id="breadcrumb-container"></div>
            <div id="view-container" style="flex-grow: 1; display: flex; flex-direction: column; min-height: 0;"></div>
        `;
        const breadcrumbContainer = this.contentArea.querySelector('#breadcrumb-container');
        const viewContainer = this.contentArea.querySelector('#view-container');

        const foundPath = this.findPath(pathParts);
        this.breadcrumb.render(breadcrumbContainer, foundPath);

        if (this.appBody.classList.contains('board-mode')) {
            breadcrumbContainer.style.display = 'none';
        } else {
            this.breadcrumb.render(breadcrumbContainer, foundPath);
        }

        const activeRoute = foundPath[foundPath.length - 1];

        if (!this.appBody.classList.contains('content-only-mode')) {
            const [topMenuId] = pathParts;
            const topMenuItem = this.menuConfig.find(item => item.id === topMenuId);
            if (topMenuItem) {
                this.sidebarHeaderTitle.textContent = topMenuItem.title;
            }
            this.updateNavActiveState(this.topNav, topMenuId);
            this.renderSidebar(topMenuId);
            if (activeRoute) {
                this.updateSidebarActiveState(activeRoute.id);
            }
        }

        if (activeRoute && activeRoute.component) {
            try {
                const viewModule = await import(activeRoute.component);
                const ViewClass = viewModule.default;

                if (!this.viewInstances.has(ViewClass)) {
                    this.viewInstances.set(ViewClass, new ViewClass());
                }
                const viewInstance = this.viewInstances.get(ViewClass);

                viewInstance.render(viewContainer, activeRoute);

            } catch (error) {
                console.error("Failed to load or render view:", error);
                viewContainer.innerHTML = `<div>Error loading page: ${error.message}</div>`;
            }
        } else {
            viewContainer.innerHTML = '<h4>404 - Page Not Found</h4>';
        }
    }

    findPath(pathParts, items = this.menuConfig, basePath = []) {
        if (!pathParts || pathParts.length === 0) return basePath;
        const currentId = pathParts[0];
        const foundItem = items.find(item => item.id === currentId);
        if (!foundItem) return basePath;

        const newPath = [...basePath, foundItem];

        if (foundItem.children) {
            return this.findPath(pathParts.slice(1), foundItem.children, newPath);
        }

        return newPath;
    }

    updateNavActiveState(navList, activeId) {
        navList.querySelectorAll('.nav-link').forEach(link => {
            if (link.dataset.id === activeId) {
                link.classList.add('active');
            } else {
                link.classList.remove('active');
            }
        });
    }

    updateSidebarActiveState(leafId) {
        this.sidebarMenu.querySelectorAll('.menu-item').forEach(el => el.classList.remove('active'));
        this.sidebarMenu.querySelectorAll('.submenu.show').forEach(el => new bootstrap.Collapse(el, {toggle: false}).hide());

        const activeLeaf = this.sidebarMenu.querySelector(`.menu-item[data-id="${leafId}"]`);

        if (activeLeaf) {
            activeLeaf.classList.add('active');
            const parentSubmenu = activeLeaf.closest('.submenu');
            if(parentSubmenu){
                new bootstrap.Collapse(parentSubmenu, {show: true});
                const parentMenuItem = parentSubmenu.closest('.menu-item');
                if(parentMenuItem) {
                    parentMenuItem.classList.add('active');
                }
            }
        }
    }
}

new App(menuConfig);