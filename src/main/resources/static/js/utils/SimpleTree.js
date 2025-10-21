/**
 * @file /js/utils/SimpleTree.js
 * @description 一个简单的树状视图组件，支持图标和连接线。
 * @version 2.0.0 - 2025-10-17
 */
export default class SimpleTree {
    /**
     * @param {HTMLElement} container - 渲染树的容器。
     * @param {Array<object>} data - 树状结构数据。
     * @param {function} onNodeSelect - 节点被选中时的回调函数。
     */
    constructor(container, data, onNodeSelect) {
        this.container = container;
        this.data = data;
        this.onNodeSelect = onNodeSelect;
        this.render();
        this._attachEventListeners();
    }

    render() {
        this.container.innerHTML = `<ul class="simple-tree">${this._buildTreeHtml(this.data)}</ul>`;
    }

    _buildTreeHtml(nodes) {
        return nodes.map(node => `
            <li data-id="${node.id}">
                <div class="tree-node">
                    <i class="bi ${node.children ? 'bi-chevron-right tree-toggle' : 'tree-node-placeholder'}"></i>
                    <i class="bi ${node.icon || 'bi-folder2'} tree-icon"></i>
                    <span class="tree-label">${node.label}</span>
                </div>
                ${node.children ? `<ul class="tree-children" style="display: none;">${this._buildTreeHtml(node.children)}</ul>` : ''}
            </li>
        `).join('');
    }

    _attachEventListeners() {
        this.container.addEventListener('click', e => {
            const node = e.target.closest('.tree-node');
            if (!node) return;

            const listItem = node.closest('li');
            const children = listItem.querySelector('.tree-children');
            const toggleIcon = node.querySelector('.tree-toggle');

            // --- 修复点击区域问题：点击整个节点区域都触发选择 ---
            this.container.querySelectorAll('.tree-node.active').forEach(n => n.classList.remove('active'));
            node.classList.add('active');
            if (this.onNodeSelect) {
                this.onNodeSelect(listItem.dataset.id);
            }

            // --- 折叠/展开逻辑 ---
            if (children && toggleIcon) {
                const isVisible = children.style.display !== 'none';
                children.style.display = isVisible ? 'none' : 'block';
                toggleIcon.classList.toggle('bi-chevron-down', !isVisible);
                toggleIcon.classList.toggle('bi-chevron-right', isVisible);
            }
        });
    }
}

