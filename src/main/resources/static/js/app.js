/**
 * 源码路径: js/app.js
 * 功能说明: 应用的主入口文件，负责初始化、路由管理、页面调度和主框架渲染。
 * 版本变动:
 * v1.7.0 - 2025-10-15: Integrated with AuthManager for re-authentication and UI updates.
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
        this.userNameDisplay = document.getElementById('user-name-display'); // Added for username updates

        this.init();
    }

    init() {
        this._initTheme();

        const urlParams = new URLSearchParams(window.location.search);
        const isContentOnly = urlParams.get('view') === 'content';

        if (isContentOnly) {
            this.appBody.classList.add('content-only-mode');
        } else {
            this.renderTopNav();
            this.sidebarToggle.addEventListener('click', () => this.toggleSidebar());
            this._attachHeaderListeners();
        }

        window.addEventListener('hashchange', () => this.handleRouteChange());
        window.addEventListener('load', () => this.handleRouteChange());

        // [Key Change] Listen for user switch events to update UI
        window.addEventListener('userSwitched', (e) => {
            this.updateUserInfo(e.detail.user);
        });
    }

    _attachHeaderListeners() {
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
                    Modal.alert('您已成功退出。');
                    // In a real app, you might want to redirect:
                    // window.location.href = '/login.html';
                }
            });
        }
    }

    /**
     * [Key Change] Updates the username display in the header.
     * @param {object} user - The user object from the login response.
     */
    updateUserInfo(user) {
        if (this.userNameDisplay && user) {
            this.userNameDisplay.textContent = user.name || user.loginid;
        }
    }

    _initTheme() {
        const savedTheme = localStorage.getItem('theme') || 'dark';
        this.appBody.classList.add(`theme-${savedTheme}`);
        this._updateThemeIcon(savedTheme);
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
                    window.location.hash = firstSubMenu.url;
                }
            }
        });
    }

    findFirstLeaf(menuItem) {
        if (!menuItem.children || menuItem.children.length === 0) {
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
        const hash = window.location.hash || (defaultRoute ? defaultRoute.url : '');
        if (!hash.startsWith('#!/')) {
            if (defaultRoute && defaultRoute.url) window.location.hash = defaultRoute.url;
            return;
        };

        const pathParts = hash.substring(3).split('/');
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

        const path = this.findPath(pathParts);
        this.breadcrumb.render(breadcrumbContainer, path);

        const activeRoute = path[path.length - 1];

        if (!this.appBody.classList.contains('content-only-mode')) {
            const [topMenuId] = pathParts;
            const topMenuItem = this.menuConfig.find(item => item.id === topMenuId);
            if (topMenuItem) {
                this.sidebarHeaderTitle.textContent = topMenuItem.title;
            }
            this.updateNavActiveState(this.topNav, topMenuId);
            this.renderSidebar(topMenuId);
            this.updateSidebarActiveState(activeRoute.id);
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

