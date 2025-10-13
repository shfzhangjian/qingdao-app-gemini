/**
 * 源码路径: js/components/DataTable.js
 * 功能说明: 一个功能强大的数据表格组件
 * - 支持通过配置动态生成
 * - 支持列的显示/隐藏/排序/宽度调整/锁定，并能本地持久化
 * - 集成了统一的工具栏和底部分页
 * - 通过Flexbox完美解决滚动条高度自适应问题
 * - 原生支持单选和多选（带全选）功能
 * 版本变动:
 * v2.8.2 - 2025-10-13: 修复了因不正确的深拷贝导致列的render函数丢失的bug。
 */

import Modal from './Modal.js';
import QueryForm from './QueryForm.js';

export default class DataTable {
    constructor({ columns = [], data = [], actions = [], filters = [], pagination = true, options = {} }) {
        // 【核心修复】使用 function-safe 的方式复制列配置，避免 render 函数丢失
        this.originalColumns = columns.map(c => ({...c, width: c.width || null, frozen: c.frozen || false }));
        this.data = data;
        this.actions = actions;
        this.filters = filters;
        this.pagination = pagination;
        this.options = { resizable: true, defaultColumnWidth: 150, getRowClass: null, ...options };
        this.container = null;
        this.columns = this._loadConfig() || this.originalColumns;
    }

    _loadConfig() {
        if (!this.options.storageKey) return null;
        try {
            const savedConfig = localStorage.getItem(this.options.storageKey);
            if (savedConfig) {
                let parsedConfig = JSON.parse(savedConfig);
                const loadedKeys = new Set(parsedConfig.map(c => c.key));

                // Merge saved config with any new columns from original config
                const newColumns = this.originalColumns.filter(c => !loadedKeys.has(c.key));
                parsedConfig.push(...newColumns);

                // Re-apply render functions from original config to the loaded config
                return parsedConfig.map(savedCol => {
                    const originalCol = this.originalColumns.find(c => c.key === savedCol.key);
                    return { ...originalCol, ...savedCol };
                });
            }
        } catch (error) {
            console.error("Failed to load table configuration:", error);
        }
        return null;
    }

    _saveConfig() {
        if (!this.options.storageKey) return;
        try {
            // Only save serializable properties
            const configToSave = this.columns.map(({ key, title, visible, width, frozen }) => ({ key, title, visible, width, frozen }));
            localStorage.setItem(this.options.storageKey, JSON.stringify(configToSave));
        } catch (error) {
            console.error("Failed to save table configuration:", error);
        }
    }

    _getVisibleColumns() {
        let visibleCols = this.columns.filter(col => col.visible !== false);
        if (this.options.selectable === 'multiple') {
            visibleCols.unshift({
                key: '__selection',
                title: '<input type="checkbox" class="form-check-input" data-select="all">',
                render: (val, row) => `<input type="checkbox" class="form-check-input" data-row-id="${row.id || ''}">`,
                width: 40,
                frozen: 'left'
            });
        }
        return visibleCols;
    }

    render(container) {
        if (!container) return;
        this.container = container;

        const visibleColumns = this._getVisibleColumns();

        let leftOffset = 0;
        let rightOffset = 0;
        const processedColumns = visibleColumns.map(col => ({ ...col, style: '', classes: '' }));

        processedColumns.forEach(col => {
            if (col.frozen === 'left') {
                col.style = `left: ${leftOffset}px;`;
                col.classes = 'dt-cell-frozen-left';
                leftOffset += col.width || this.options.defaultColumnWidth;
            }
        });

        [...processedColumns].reverse().forEach(col => {
            if (col.frozen === 'right') {
                col.style = `right: ${rightOffset}px;`;
                col.classes = 'dt-cell-frozen-right';
                rightOffset += col.width || this.options.defaultColumnWidth;
            }
        });

        const totalWidth = visibleColumns.reduce((sum, col) => sum + (col.width || this.options.defaultColumnWidth), 0);
        console.log(`[DataTable] Calculated table min-width: ${totalWidth}px`);

        this.container.innerHTML = `
            <div style="display: flex; flex-direction: column; height: 100%;">
                ${this._createToolbar()}
                <div class="table-responsive">
                    <table class="table table-bordered table-sm text-center table-hover" style="min-width: ${totalWidth}px;">
                        ${this._createColGroup(processedColumns)}
                        <thead>${this._createHeader(processedColumns)}</thead>
                        <tbody>${this._createBody(processedColumns)}</tbody>
                    </table>
                </div>
                ${this._createFooter()}
            </div>
        `;

        this._attachEventListeners();
    }

    _createToolbar() {
        const actionsHtml = new QueryForm({ actions: this.actions })._createActionsHtml();
        const filtersHtml = new QueryForm({ fields: this.filters })._createFieldsHtml();

        let configButtonHtml = '';
        if (this.options.configurable) {
            configButtonHtml = `<button class="btn btn-sm btn-outline-secondary" data-action="configure-columns"><i class="bi bi-gear"></i> 配置列</button>`;
        }

        return `<div class="d-flex justify-content-between align-items-center mb-3">
                    <div class="d-flex justify-content-start gap-2">${actionsHtml}</div>
                    <div class="d-flex align-items-center gap-4">${filtersHtml}${configButtonHtml}</div>
                </div>`;
    }

    _createColGroup(processedColumns) {
        const colsHtml = processedColumns.map(col => `<col data-col-key="${col.key}" ${col.width ? `style="width: ${col.width}px"` : `style="width: ${this.options.defaultColumnWidth}px"`}>`).join('');
        return `<colgroup>${colsHtml}</colgroup>`;
    }

    _createHeader(processedColumns) {
        const headerCells = processedColumns.map(col => {
            const resizer = this.options.resizable && col.key !== '__selection' ? '<div class="resizer"></div>' : '';
            return `<th data-col-key="${col.key}" class="${col.classes}" style="${col.style}">${col.title}${resizer}</th>`;
        }).join('');
        return `<tr>${headerCells}</tr>`;
    }

    _createBody(processedColumns) {
        if (this.data.length === 0) {
            return `<tr><td colspan="${processedColumns.length || 1}" class="text-center p-4">没有数据</td></tr>`;
        }

        return this.data.map(row => {
            const rowClass = typeof this.options.getRowClass === 'function' ? this.options.getRowClass(row) : '';
            return `<tr data-row-id="${row.id || ''}" class="${rowClass}">
                ${processedColumns.map(col => {
                const cellValue = row[col.key] !== undefined && row[col.key] !== null ? row[col.key] : '-';
                const cellContent = typeof col.render === 'function' ? col.render(cellValue, row) : cellValue;
                return `<td class="${col.classes}" style="${col.style}">${cellContent}</td>`;
            }).join('')}
            </tr>`
        }).join('');
    }

    _createFooter() {
        if (!this.pagination) return '';
        return `<nav class="d-flex justify-content-end pt-2 mt-auto" style="border-top: 1px solid var(--border-color);">
                    <ul class="pagination pagination-sm mb-0">
                        <li class="page-item disabled"><a class="page-link" href="#">&laquo;</a></li>
                        <li class="page-item"><a class="page-link" href="#">1</a></li>
                        <li class="page-item active"><a class="page-link" href="#">2</a></li>
                        <li class="page-item"><a class="page-link" href="#">3</a></li>
                        <li class="page-item"><a class="page-link" href="#">...</a></li>
                        <li class="page-item"><a class="page-link" href="#">10</a></li>
                        <li class="page-item"><a class="page-link" href="#">&raquo;</a></li>
                    </ul>
                </nav>`;
    }

    _attachEventListeners() {
        const toolbar = this.container.querySelector('.d-flex.justify-content-between');
        if (toolbar) {
            toolbar.addEventListener('click', e => {
                const button = e.target.closest('button[data-action]');
                if (button && button.dataset.action === 'configure-columns') this._showColumnConfigModal();
            });
        }
        this._attachSelectionListeners();
        if (this.options.resizable) this._attachResizeListeners();
    }

    _attachResizeListeners() {
        const header = this.container.querySelector('thead');
        if (!header) return;

        let th, col, startX, startWidth;

        const onMouseMove = (e) => {
            e.preventDefault();
            const newWidth = startWidth + (e.clientX - startX);
            if (newWidth > 30 && col) col.style.width = `${newWidth}px`;
        };

        const onMouseUp = () => {
            document.removeEventListener('mousemove', onMouseMove);
            document.removeEventListener('mouseup', onMouseUp);
            document.body.style.cursor = 'default';

            if (th && col) {
                const colKey = th.dataset.colKey;
                const targetColumn = this.columns.find(c => c.key === colKey);
                const finalWidth = th.getBoundingClientRect().width;

                if (targetColumn) {
                    targetColumn.width = Math.round(finalWidth);
                    this._saveConfig();
                    this.render(this.container);
                }
            }
        };

        header.addEventListener('mousedown', e => {
            if (e.target.classList.contains('resizer')) {
                e.preventDefault();
                th = e.target.parentElement;
                const colKey = th.dataset.colKey;
                col = this.container.querySelector(`col[data-col-key="${colKey}"]`);

                if (col) {
                    startX = e.clientX;
                    startWidth = col.offsetWidth;
                    document.body.style.cursor = 'col-resize';
                    document.addEventListener('mousemove', onMouseMove);
                    document.addEventListener('mouseup', onMouseUp);
                }
            }
        });
    }

    _attachSelectionListeners() {
        if (this.options.selectable === 'none') return;
        const table = this.container.querySelector('table');
        if (!table) return;

        if (this.options.selectable === 'multiple') {
            table.querySelector('thead')?.addEventListener('change', e => {
                if(e.target.dataset.select === 'all') {
                    const isChecked = e.target.checked;
                    table.querySelectorAll('tbody input[data-row-id]').forEach(cb => {
                        cb.checked = isChecked;
                        cb.closest('tr').classList.toggle('table-active-custom', isChecked);
                    });
                }
            });
        }

        table.querySelector('tbody')?.addEventListener('click', e => {
            const row = e.target.closest('tr');
            if (!row || !row.parentElement) return;
            if (e.target.closest('button')) return;

            if (this.options.selectable === 'single') {
                const currentlyActive = row.parentElement.querySelector('.table-active-custom');
                if (currentlyActive) currentlyActive.classList.remove('table-active-custom');
                row.classList.add('table-active-custom');
            } else if (this.options.selectable === 'multiple') {
                const checkbox = row.querySelector('input[type="checkbox"]');
                if (checkbox && e.target !== checkbox) checkbox.checked = !checkbox.checked;
                row.classList.toggle('table-active-custom', checkbox.checked);
                this._updateSelectAllState();
            }
        });
    }


    _updateSelectAllState() {
        const selectAllCheckbox = this.container.querySelector('input[data-select="all"]');
        if (!selectAllCheckbox) return;

        const rowCheckboxes = Array.from(this.container.querySelectorAll('tbody input[data-row-id]'));
        const totalRows = rowCheckboxes.length;
        if(totalRows === 0) return;

        const checkedRows = rowCheckboxes.filter(cb => cb.checked).length;

        if (totalRows === checkedRows) {
            selectAllCheckbox.checked = true;
            selectAllCheckbox.indeterminate = false;
        } else if (checkedRows > 0) {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = true;
        } else {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = false;
        }
    }



    _showColumnConfigModal() {
        // Create a temporary copy for live editing in the modal
        let tempColumns = JSON.parse(JSON.stringify(this.columns));

        const modalBody = document.createElement('div');
        modalBody.innerHTML = `
            <div class="d-flex justify-content-between align-items-center mb-2 pb-2 border-bottom border-secondary">
                <input type="text" class="form-control form-control-sm" placeholder="过滤列名..." id="column-config-filter" style="width: 200px;">
                <div class="d-flex gap-2 ms-3">
                    <button class="btn btn-sm btn-outline-secondary py-0" id="column-config-select-all">全选</button>
                    <button class="btn btn-sm btn-outline-secondary py-0" id="column-config-invert">反选</button>
                </div>
            </div>
            <ul class="list-group mt-2" id="column-config-list" style="max-height: 60vh; overflow-y: auto;"></ul>`;

        const listContainer = modalBody.querySelector('#column-config-list');

        const renderList = () => {
            listContainer.innerHTML = tempColumns.map((col, index) => `
                <li class="list-group-item d-flex align-items-center" draggable="true" data-key="${col.key}">
                    <i class="bi bi-grip-vertical me-2" style="cursor: move;"></i>
                    <span class="flex-grow-1">${col.title}</span>
                    <div class="btn-group btn-group-sm me-3" role="group">
                        <button type="button" class="btn btn-outline-secondary ${col.frozen === 'left' ? 'active' : ''}" data-action="pin" data-value="left" title="左固定"><i class="bi bi-arrow-left-square"></i></button>
                        <button type="button" class="btn btn-outline-secondary ${!col.frozen ? 'active' : ''}" data-action="pin" data-value="false" title="不固定"><i class="bi bi-x-circle"></i></button>
                        <button type="button" class="btn btn-outline-secondary ${col.frozen === 'right' ? 'active' : ''}" data-action="pin" data-value="right" title="右固定"><i class="bi bi-arrow-right-square"></i></button>
                    </div>
                    <div class="form-check form-switch">
                        <input class="form-check-input" type="checkbox" role="switch" ${col.visible !== false ? 'checked' : ''}>
                    </div>
                </li>
            `).join('');
        };
        renderList();

        const footer = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" id="save-column-config">确定</button>
        `;

        const configModal = new Modal({ title: '列表字段设置', body: modalBody, footer: footer, size: 'lg' });

        // --- Event Listeners ---
        const filterInput = configModal.modalElement.querySelector('#column-config-filter');

        filterInput.addEventListener('input', (e) => {
            const filterText = e.target.value.toLowerCase();
            configModal.modalElement.querySelectorAll('#column-config-list li').forEach(item => {
                const title = item.querySelector('span').textContent.toLowerCase();
                item.classList.toggle('d-none', !title.includes(filterText));
            });
        });

        configModal.modalElement.querySelector('#column-config-select-all').addEventListener('click', () => {
            configModal.modalElement.querySelectorAll('#column-config-list li:not(.d-none) input[type="checkbox"]').forEach(cb => cb.checked = true);
        });

        configModal.modalElement.querySelector('#column-config-invert').addEventListener('click', () => {
            configModal.modalElement.querySelectorAll('#column-config-list li:not(.d-none) input[type="checkbox"]').forEach(cb => cb.checked = !cb.checked);
        });

        listContainer.addEventListener('click', (e) => {
            const pinButton = e.target.closest('button[data-action="pin"]');
            if (pinButton) {
                const key = pinButton.closest('li').dataset.key;
                const value = pinButton.dataset.value === 'false' ? false : pinButton.dataset.value;
                const col = tempColumns.find(c => c.key === key);
                if (col) col.frozen = value;
                renderList(); // Re-render to update active states
            }
        });

        this._attachDragAndDropHandlers(listContainer, tempColumns);

        configModal.modalElement.querySelector('#save-column-config').addEventListener('click', () => {
            const reorderedKeys = Array.from(configModal.modalElement.querySelectorAll('#column-config-list li')).map(item => item.dataset.key);

            tempColumns.forEach(col => {
                const li = configModal.modalElement.querySelector(`li[data-key="${col.key}"]`);
                if(li) col.visible = li.querySelector('input[type="checkbox"]').checked;
            });

            // Apply new order
            this.columns = reorderedKeys.map(key => tempColumns.find(c => c.key === key));

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

