/**
 * @file /js/views/Placeholder.js
 * @description 用于未开发页面的占位视图
 * @version 1.1.0 - 2025-10-13 - Gemini - 移除内置标题，适配面包屑
 */

export default class Placeholder {
    /**
     * Renders a placeholder content.
     * @param {HTMLElement} container - The main content container.
     * @param {HTMLElement} footerContainer - The footer container.
     * @param {object} route - The active route object from menu config.
     */
    render(container, footerContainer, route) {
        // The title is now handled by the breadcrumb, so we just render the content.
        container.innerHTML = `<p>这是一个占位页面，内容正在建设中。</p>`;

        // Clear the footer as this page doesn't need it
        footerContainer.innerHTML = '';
    }
}

