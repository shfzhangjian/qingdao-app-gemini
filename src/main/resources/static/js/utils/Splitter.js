/**
 * @file /js/utils/Splitter.js
 * @description 一个可拖拽的分隔条工具，用于创建可调整大小的双栏布局。
 * @version 1.0.0 - 2025-10-17
 */

export default class Splitter {
    /**
     * @param {HTMLElement} container - 包含左右两个面板的父容器。
     * @param {HTMLElement} leftPanel - 左侧面板。
     * @param {HTMLElement} rightPanel - 右侧面板。
     */
    constructor(container, leftPanel, rightPanel) {
        this.container = container;
        this.leftPanel = leftPanel;
        this.rightPanel = rightPanel;
        this.handle = null;
        this.isDragging = false;
        this.isCollapsed = false;
        this.lastWidth = this.leftPanel.style.width || '250px';

        this._createHandle();
        this._attachEventListeners();
    }

    _createHandle() {
        this.handle = document.createElement('div');
        this.handle.className = 'splitter-handle';
        this.handle.innerHTML = `<div class="splitter-icon-wrapper"><i class="bi bi-grip-vertical"></i></div>`;
        this.container.insertBefore(this.handle, this.rightPanel);

        const collapseBtn = document.createElement('button');
        collapseBtn.className = 'splitter-collapse-btn';
        collapseBtn.innerHTML = '<i class="bi bi-chevron-left"></i>';
        collapseBtn.title = '折叠/展开';
        this.handle.appendChild(collapseBtn);
    }

    _attachEventListeners() {
        this.handle.addEventListener('mousedown', e => {
            // 防止点击折叠按钮时触发拖拽
            if (e.target.closest('.splitter-collapse-btn')) return;
            e.preventDefault();
            this.isDragging = true;
            this.container.classList.add('is-dragging');
        });

        document.addEventListener('mousemove', e => {
            if (!this.isDragging) return;
            e.preventDefault();
            const containerRect = this.container.getBoundingClientRect();
            let newLeftWidth = e.clientX - containerRect.left;

            if (newLeftWidth < 50) newLeftWidth = 50; // 最小宽度
            if (newLeftWidth > containerRect.width - 50) newLeftWidth = containerRect.width - 50; // 最大宽度

            this.leftPanel.style.width = `${newLeftWidth}px`;
            this.lastWidth = `${newLeftWidth}px`;
        });

        document.addEventListener('mouseup', () => {
            if (this.isDragging) {
                this.isDragging = false;
                this.container.classList.remove('is-dragging');
            }
        });

        this.handle.querySelector('.splitter-collapse-btn').addEventListener('click', () => {
            this.toggleCollapse();
        });
    }

    toggleCollapse() {
        this.isCollapsed = !this.isCollapsed;
        this.container.classList.toggle('collapsed', this.isCollapsed);
        const icon = this.handle.querySelector('.splitter-collapse-btn i');
        if (this.isCollapsed) {
            this.leftPanel.style.width = '0px';
            icon.classList.remove('bi-chevron-left');
            icon.classList.add('bi-chevron-right');
        } else {
            this.leftPanel.style.width = this.lastWidth;
            icon.classList.remove('bi-chevron-right');
            icon.classList.add('bi-chevron-left');
        }
    }
}
