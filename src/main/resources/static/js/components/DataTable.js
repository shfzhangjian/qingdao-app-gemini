/**
 * 源码路径: js/components/DataTable.js
 * 功能说明: 一个功能强大的数据表格组件
 * - 支持通过配置动态生成
 * - 支持列的显示/隐藏/排序，并能本地持久化
 * - 集成了统一的工具栏（操作按钮、筛选器）
 * - 解决了数据少时滚动条位置不佳的问题
 * 版本变动:
 * v2.0.0 - 2025-10-13: 组件化重构，实现基础渲染功能。
 * v2.1.0 - 2025-10-13: 增加列配置（显隐/排序）和本地存储功能。
 * v2.2.0 - 2025-10-13: 集成统一工具栏。
 * v2.3.0 - 2025-10-13: 优化flex布局，彻底解决滚动条位置问题。
 */

import Modal from './Modal.js';
import QueryForm from './QueryForm.js';

export default class DataTable {
    /**
     * @param {object} config
     * @param {Array<object>} config.columns - Column definitions.
     * @param {Array<object>} config.data - The data to display.
     * @param {Array<object>} [config.actions=[]] - Toolbar action buttons.
     * @param {Array<object>} [config.filters=[]] - Toolbar filter pills.
     * @param {object} [config.options={}] - Additional table options.
     */
    constructor({ columns = [], data = [], actions = [], filters = [], options = {} }) {
        this.originalColumns = JSON.parse(JSON.stringify(columns));
        this.data = data;
        this.actions = actions;
        this.filters = filters;
        this.options = options;
        this.container = null;
        this.columns = this._loadConfig() || this.originalColumns;
    }

    _loadConfig() {
        if (!this.options.storageKey) return null;
        try {
            const savedConfig = localStorage.getItem(this.options.storageKey);
            if (savedConfig) {
                const parsedConfig = JSON.parse(savedConfig);
                const loadedKeys = new Set(parsedConfig.map(c => c.key));
                const newColumns = this.originalColumns.filter(c => !loadedKeys.has(c.key));
                return [...parsedConfig, ...newColumns];
            }
        } catch (error) {
            console.error("Failed to load table configuration:", error);
        }
        return null;
    }

    _saveConfig() {
        if (!this.options.storageKey) return;
        try {
            localStorage.setItem(this.options.storageKey, JSON.stringify(this.columns));
        } catch (error) {
            console.error("Failed to save table configuration:", error);
        }
    }

    _getVisibleColumns() {
        return this.columns.filter(col => col.visible !== false);
    }

    render(container) {
        if (!container) {
            console.error("DataTable requires a container element to render.");
            return;
        }
        this.container = container;

        // 【核心修改】移除内联样式，依赖于style.css中更健壮的flex布局规则
        this.container.innerHTML = `
            ${this._createToolbar()}
            <div class="table-responsive">
                <table class="table table-bordered table-sm text-center table-hover">
                    <thead>${this._createHeader()}</thead>
                    <tbody>${this._createBody()}</tbody>
                </table>
            </div>
        `;

        this._attachEventListeners();
    }

    _createToolbar() {
        const actionsHtml = new QueryForm({ actions: this.actions })._createActionsHtml();
        const filtersHtml = new QueryForm({ fields: this.filters })._createFieldsHtml();

        let configButtonHtml = '';
        if (this.options.configurable) {
            configButtonHtml = `
                <button class="btn btn-sm btn-outline-secondary" data-action="configure-columns">
                    <i class="bi bi-gear"></i> 配置列
                </button>
            `;
        }

        return `
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div class="d-flex justify-content-start gap-2">${actionsHtml}</div>
                <div class="d-flex align-items-center gap-4">
                    ${filtersHtml}
                    ${configButtonHtml}
                </div>
            </div>
        `;
    }

    _createHeader() {
        const visibleColumns = this._getVisibleColumns();
        return `<tr>${visibleColumns.map(col => `<th>${col.title}</th>`).join('')}</tr>`;
    }

    _createBody() {
        const visibleColumns = this._getVisibleColumns();
        if (this.data.length === 0) {
            return `<tr><td colspan="${visibleColumns.length || 1}" class="text-center p-4">没有数据</td></tr>`;
        }

        return this.data.map(row => `
            <tr>
                ${visibleColumns.map(col => {
            const cellValue = row[col.key] !== undefined && row[col.key] !== null ? row[col.key] : '-';
            return `<td>${typeof col.render === 'function' ? col.render(cellValue, row) : cellValue}</td>`;
        }).join('')}
            </tr>
        `).join('');
    }

    _attachEventListeners() {
        const toolbar = this.container.querySelector('.d-flex.justify-content-between');
        if (toolbar) {
            toolbar.addEventListener('click', (e) => {
                const button = e.target.closest('button[data-action]');
                if (!button) return;

                const action = button.dataset.action;
                if (action === 'configure-columns') {
                    this._showColumnConfigModal();
                } else {
                    console.log(`Action button clicked: ${action}`);
                }
            });
        }
    }

    _showColumnConfigModal() {
        const modalBody = document.createElement('div');
        modalBody.innerHTML = `
            <ul class="list-group" id="column-config-list">
                ${this.columns.map((col, index) => `
                    <li class="list-group-item d-flex align-items-center" draggable="true" data-index="${index}">
                        <i class="bi bi-grip-vertical me-3" style="cursor: move;"></i>
                        <span class="flex-grow-1">${col.title}</span>
                        <div class="form-check form-switch">
                            <input class="form-check-input" type="checkbox" role="switch" ${col.visible !== false ? 'checked' : ''}>
                        </div>
                    </li>
                `).join('')}
            </ul>
        `;

        const footer = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" id="save-column-config">确定</button>
        `;

        const configModal = new Modal({ title: '列表字段设置', body: modalBody, footer: footer, size: 'lg' });

        this._attachDragAndDropHandlers(modalBody.querySelector('#column-config-list'));

        modalBody.querySelector('#save-column-config').addEventListener('click', () => {
            const reorderedColumns = Array.from(modalBody.querySelectorAll('#column-config-list li')).map(item => {
                const originalIndex = parseInt(item.dataset.index, 10);
                const column = this.columns.find((c, i) => i === originalIndex);
                if (column) {
                    column.visible = item.querySelector('input[type="checkbox"]').checked;
                }
                return column;
            }).filter(Boolean);

            this.columns = reorderedColumns;
            this._saveConfig();
            this.render(this.container);
            configModal.hide();
        });

        configModal.show();
    }

    _attachDragAndDropHandlers(list) {
        let draggedItem = null;

        list.addEventListener('dragstart', e => {
            draggedItem = e.target;
            setTimeout(() => e.target.style.opacity = '0.5', 0);
        });

        list.addEventListener('dragend', e => {
            setTimeout(() => e.target.style.opacity = '', 0);
            draggedItem = null;
        });

        list.addEventListener('dragover', e => {
            e.preventDefault();
            const afterElement = this._getDragAfterElement(list, e.clientY);
            if (afterElement == null) {
                list.appendChild(draggedItem);
            } else {
                list.insertBefore(draggedItem, afterElement);
            }
        });
    }

    _getDragAfterElement(container, y) {
        const draggableElements = [...container.querySelectorAll('li:not(.dragging)')];
        return draggableElements.reduce((closest, child) => {
            const box = child.getBoundingClientRect();
            const offset = y - box.top - box.height / 2;
            if (offset < 0 && offset > closest.offset) {
                return { offset: offset, element: child };
            } else {
                return closest;
            }
        }, { offset: Number.NEGATIVE_INFINITY }).element;
    }

    updateData(newData) {
        this.data = newData;
        if (this.container) {
            const tbody = this.container.querySelector('tbody');
            if (tbody) {
                tbody.innerHTML = this._createBody();
            } else {
                this.render(this.container);
            }
        }
    }
}

