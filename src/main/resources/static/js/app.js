/**
 * @file /js/app.js
 * @description 应用主入口文件，负责框架初始化、路由和视图调度
 * @version 1.2.0 - 2025-10-13 - Gemini - 集成面包屑组件，优化视图渲染逻辑
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
        this.pageFooter = document.getElementById('page-footer');
        this.viewInstances = new Map(); // Cache view instances
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
        const [topMenuId, ...restPath] = pathParts;

        const breadcrumbPath = this.findPathToRoute(pathParts);

        // Update UI only if not in content-only mode
        if (!this.appBody.classList.contains('content-only-mode')) {
            const topMenuItem = this.menuConfig.find(item => item.id === topMenuId);
            if (topMenuItem) {
                this.sidebarHeaderTitle.textContent = topMenuItem.title;
            }
            this.updateNavActiveState(this.topNav, topMenuId, true);
            this.renderSidebar(topMenuId);

            const activeLeafId = pathParts[pathParts.length - 1];
            this.updateSidebarActiveState(activeLeafId);
        }

        // Clear content and render breadcrumb first
        this.contentArea.innerHTML = '';
        this.pageFooter.innerHTML = '';

        const breadcrumbContainer = document.createElement('div');
        this.contentArea.appendChild(breadcrumbContainer);
        this.breadcrumb.render(breadcrumbContainer, breadcrumbPath);

        const activeRoute = breadcrumbPath.length > 0 ? breadcrumbPath[breadcrumbPath.length - 1] : null;

        if (activeRoute && activeRoute.component) {
            try {
                const viewModule = await import(activeRoute.component);
                const ViewClass = viewModule.default;

                if (!this.viewInstances.has(ViewClass)) {
                    this.viewInstances.set(ViewClass, new ViewClass());
                }
                const viewInstance = this.viewInstances.get(ViewClass);

                // Create a new container for the view content itself, after the breadcrumb
                const viewContainer = document.createElement('div');
                this.contentArea.appendChild(viewContainer);
                viewInstance.render(viewContainer, this.pageFooter, activeRoute);

            } catch (error) {
                console.error("Failed to load or render view:", error);
                this.contentArea.innerHTML += `<div>Error loading page: ${error.message}</div>`;
            }
        } else if (hash !== (defaultRoute ? defaultRoute.url : '')) {
            this.contentArea.innerHTML += '<h4>404 - Page Not Found</h4>';
        }
    }

    findPathToRoute(pathParts, menu = this.menuConfig, currentPath = []) {
        if (pathParts.length === 0) return currentPath;

        const currentId = pathParts[0];
        const foundItem = menu.find(item => item.id === currentId);

        if (!foundItem) return currentPath;

        const newPath = [...currentPath, foundItem];

        if (foundItem.children && pathParts.length > 1) {
            return this.findPathToRoute(pathParts.slice(1), foundItem.children, newPath);
        }

        return newPath;
    }

    updateNavActiveState(navList, activeId, isTopNav = false) {
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

// Start the app
new App(menuConfig);

