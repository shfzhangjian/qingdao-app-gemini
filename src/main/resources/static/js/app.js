/**
 * 源码路径: js/app.js
 * 功能说明: 应用的主入口文件，负责初始化、路由管理、页面调度和主框架渲染。
 * 版本变动:
 * v1.0.0 - 2025-10-13: 初始版本，实现基础框架和路由。
 * v1.1.0 - 2025-10-13: 集成面包屑组件，优化内容区布局。
 * v1.4.0 - 2025-10-13: 【最终方案】移除所有JS高度计算和page-footer，回归纯CSS Flexbox终极布局方案。
 */
import menuConfig from './config/menu.js';
import Breadcrumb from './components/Breadcrumb.js';

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
        // 【核心修改】移除 this.pageFooter
        this.viewInstances = new Map();
        this.breadcrumb = new Breadcrumb();

        this.init();
    }

    init() {
        const urlParams = new URLSearchParams(window.location.search);
        const isContentOnly = urlParams.get('view') === 'content';

        if (isContentOnly) {
            this.appBody.classList.add('content-only-mode');
        } else {
            this.renderTopNav();
            this.sidebarToggle.addEventListener('click', () => this.toggleSidebar());
        }

        window.addEventListener('hashchange', () => this.handleRouteChange());
        window.addEventListener('load', () => this.handleRouteChange());
        // 移除 resize listener
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

                // 【核心修改】render方法不再传递 footerContainer
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

