/**
 * @file /js/components/Breadcrumb.js
 * @description 动态面包屑导航组件
 * @version 1.1.0 - 2025-10-13 - Gemini - 新增了面包屑图标
 */

/**
 * A component for rendering breadcrumb navigation.
 */
export default class Breadcrumb {
    /**
     * Renders the breadcrumb navigation.
     * @param {HTMLElement} container - The element to render the breadcrumb into.
     * @param {Array<object>} pathItems - An array of menu objects representing the path.
     */
    render(container, pathItems = []) {
        if (!container) return;

        if (pathItems.length === 0) {
            container.innerHTML = '';
            return;
        }

        const itemsHtml = pathItems.map((item, index) => {
            const isLast = index === pathItems.length - 1;
            if (isLast) {
                return `<li class="breadcrumb-item active" aria-current="page">${item.title}</li>`;
            } else {
                return `<li class="breadcrumb-item">${item.title}</li>`;
            }
        }).join('');

        container.innerHTML = `
            <nav aria-label="breadcrumb" class="d-flex align-items-center">
                <i class="bi bi-geo-alt-fill me-2" style="color: var(--bs-breadcrumb-divider-color);"></i>
                <ol class="breadcrumb mb-0">
                    ${itemsHtml}
                </ol>
            </nav>
        `;
    }
}

