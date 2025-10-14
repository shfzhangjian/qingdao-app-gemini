/**
 * 源码路径: js/components/DataTable.js
 * 功能说明: 一个功能强大的数据表格组件
 * - 支持动态生成、列配置持久化、分页、单选/多选等。
 * 版本变动:
 * v2.9.8 - 2025-10-14: 彻底修复了因类型处理不当导致的列锁定状态无法正确持久化的问题。
 */
import Modal from './Modal.js';
import QueryForm from './QueryForm.js';

export default class DataTable {
    constructor({ columns = [], data = [], actions = [], filters = [], options = {} }) {
        // 保证 originalColumns 包含所有原始信息，且 frozen 有默认值
        this.originalColumns = columns.map(c => ({...c, width: c.width || null, frozen: c.frozen || false, visible: c.visible !== false }));
        this.data = data || [];
        this.actions = actions;
        this.filters = filters;
        this.paginationInfo = null;
        this.options = {
            resizable: true,
            defaultColumnWidth: 150,
            getRowClass: null,
            pagination: true,
            ...options
        };
        this.container = null;
        // 构造时即加载配置，如果失败则使用原始配置
        this.columns = this._loadConfig() || this.originalColumns;
    }

    /**
     * 【已修复】重构加载逻辑，优先使用缓存的列顺序、可见性和锁定状态，
     * 仅从原始配置中补充 render 函数等不可序列化的属性。
     */
    _loadConfig() {
        if (!this.options.storageKey) return null;
        try {
            const savedConfig = localStorage.getItem(this.options.storageKey);
            if (savedConfig) {
                console.log("[DataTable] 从缓存加载列配置...");
                const parsedConfig = JSON.parse(savedConfig);
                const originalColsMap = new Map(this.originalColumns.map(c => [c.key, c]));

                // 1. 基于缓存的顺序和属性，恢复 render 函数
                const loadedColumns = parsedConfig.map(savedCol => {
                    const originalCol = originalColsMap.get(savedCol.key);
                    if (originalCol) {
                        // 【核心修复】确保 'frozen' 属性类型一致，使用非严格等于处理字符串 "false" 和布尔值 false
                        if (savedCol.frozen == 'false') {
                            savedCol.frozen = false;
                        }
                        // 以保存的列为基础，只补充 render 函数
                        return { ...originalCol, ...savedCol };
                    }
                    return null; // 如果原始列已不存在，则忽略
                }).filter(Boolean);

                // 2. 添加代码中新增的、缓存里没有的列
                const loadedKeys = new Set(loadedColumns.map(c => c.key));
                this.originalColumns.forEach(originalCol => {
                    if (!loadedKeys.has(originalCol.key)) {
                        loadedColumns.push(originalCol);
                    }
                });

                return loadedColumns;
            }
        } catch (error) {
            console.error("加载表格配置失败:", error);
        }
        return null;
    }

    _saveConfig() {
        if (!this.options.storageKey) return;
        try {
            // 只保存可序列化的关键属性
            const configToSave = this.columns.map(({ key, title, visible, width, frozen }) => ({ key, title, visible, width, frozen }));
            localStorage.setItem(this.options.storageKey, JSON.stringify(configToSave));
            console.log("[DataTable] 列配置已保存到缓存。");
        } catch (error) {
            console.error("保存表格配置失败:", error);
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
        console.log(`[DataTable] 正在渲染表格...`);

        const visibleColumns = this._getVisibleColumns();
        const processedColumns = visibleColumns.map(col => ({ ...col, style: '', classes: '' }));

        // 分别计算左侧和右侧锁定的偏移量
        let leftOffset = 0;
        processedColumns.forEach(col => {
            if (col.frozen === 'left') {
                col.style = `left: ${leftOffset}px;`;
                col.classes = 'dt-cell-frozen-left';
                leftOffset += col.width || this.options.defaultColumnWidth;
            }
        });

        let rightOffset = 0;
        // 从后向前遍历，正确计算右侧偏移
        for (let i = processedColumns.length - 1; i >= 0; i--) {
            const col = processedColumns[i];
            if (col.frozen === 'right') {
                col.style = `right: ${rightOffset}px;`;
                col.classes = 'dt-cell-frozen-right';
                rightOffset += col.width || this.options.defaultColumnWidth;
            }
        }

        const totalWidth = visibleColumns.reduce((sum, col) => sum + (col.width || this.options.defaultColumnWidth), 0);

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
        if (!this.data || this.data.length === 0) {
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
        if (!this.options.pagination) return '';

        const { pageNum = 1, pages = 1, total = 0 } = this.paginationInfo || {};

        let paginationHtml = '';
        if (pages > 1) {
            const createPageItem = (p, label, isDisabled = false, isActive = false) => {
                const disabledClass = isDisabled ? 'disabled' : '';
                const activeClass = isActive ? 'active' : '';
                return `<li class="page-item ${disabledClass} ${activeClass}"><a class="page-link" href="#" data-page="${p}">${label}</a></li>`;
            };

            paginationHtml += createPageItem(pageNum - 1, '&laquo;', pageNum <= 1);

            let startPage, endPage;
            if (pages <= 7) {
                startPage = 1;
                endPage = pages;
            } else {
                if (pageNum <= 4) {
                    startPage = 1;
                    endPage = 5;
                } else if (pageNum + 3 >= pages) {
                    startPage = pages - 4;
                    endPage = pages;
                } else {
                    startPage = pageNum - 2;
                    endPage = pageNum + 2;
                }
            }

            if (startPage > 1) {
                paginationHtml += createPageItem(1, '1');
                if (startPage > 2) {
                    paginationHtml += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
                }
            }

            for (let i = startPage; i <= endPage; i++) {
                paginationHtml += createPageItem(i, i, false, i === pageNum);
            }

            if (endPage < pages) {
                if (endPage < pages - 1) {
                    paginationHtml += `<li class="page-item disabled"><span class="page-link">...</span></li>`;
                }
                paginationHtml += createPageItem(pages, pages);
            }

            paginationHtml += createPageItem(pageNum + 1, '&raquo;', pageNum >= pages);
        }

        const totalCountHtml = `<span class="text-secondary me-3">共 ${total} 条</span>`;

        return `<nav class="d-flex justify-content-between align-items-center pt-2 mt-auto" style="border-top: 1px solid var(--border-color);">
                    <div>${totalCountHtml}</div>
                    <ul class="pagination pagination-sm mb-0">${paginationHtml}</ul>
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
        this._attachPaginationListeners();
        if (this.options.resizable) this._attachResizeListeners();
    }

    _attachPaginationListeners() {
        this.container.addEventListener('click', e => {
            const link = e.target.closest('.page-link[data-page]');
            if (link && !link.parentElement.classList.contains('disabled')) {
                e.preventDefault();
                const pageNum = parseInt(link.dataset.page, 10);
                const event = new CustomEvent('pageChange', { detail: { pageNum } });
                this.container.dispatchEvent(event);
            }
        });
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
                if (targetColumn) {
                    targetColumn.width = Math.round(col.getBoundingClientRect().width);
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
            if (!row || !row.parentElement || !row.dataset.rowId) return;
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
        if(totalRows === 0) {
            selectAllCheckbox.checked = false;
            selectAllCheckbox.indeterminate = false;
            return;
        }

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

        const renderList = (columnsToList) => {
            listContainer.innerHTML = columnsToList.map(col => `
                <li class="list-group-item d-flex align-items-center" draggable="true" data-key="${col.key}">
                    <i class="bi bi-grip-vertical me-2" style="cursor: move;"></i>
                    <span class="flex-grow-1">${col.title}</span>
                    <div class="btn-group btn-group-sm me-3" role="group">
                        <button type="button" class="btn btn-outline-secondary ${col.frozen === 'left' ? 'active' : ''}" data-action="pin" data-value="left" title="左固定"><i class="bi bi-arrow-left-square"></i></button>
                        <button type="button" class="btn btn-outline-secondary ${col.frozen == false ? 'active' : ''}" data-action="pin" data-value="false" title="不固定"><i class="bi bi-x-circle"></i></button>
                        <button type="button" class="btn btn-outline-secondary ${col.frozen === 'right' ? 'active' : ''}" data-action="pin" data-value="right" title="右固定"><i class="bi bi-arrow-right-square"></i></button>
                    </div>
                    <div class="form-check form-switch">
                        <input class="form-check-input" type="checkbox" role="switch" ${col.visible ? 'checked' : ''}>
                    </div>
                </li>
            `).join('');
        };
        renderList(this.columns);

        const footer = `
            <button type="button" class="btn btn-outline-secondary me-auto" id="reset-column-config">恢复默认</button>
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" id="save-column-config">确定</button>
        `;

        const configModal = new Modal({ title: '列表字段设置', body: modalBody, footer: footer, size: 'lg' });
        const modalElement = configModal.modalElement;

        // 过滤功能
        modalElement.querySelector('#column-config-filter').addEventListener('input', (e) => {
            const filterText = e.target.value.toLowerCase();
            modalElement.querySelectorAll('#column-config-list li').forEach(item => {
                const title = item.querySelector('span').textContent.toLowerCase();
                const isMatch = title.includes(filterText);
                item.classList.toggle('d-none', !isMatch);
            });
        });

        // 锁定/Pin功能
        modalElement.querySelector('#column-config-list').addEventListener('click', (e) => {
            const pinButton = e.target.closest('button[data-action="pin"]');
            if (pinButton) {
                const group = pinButton.parentElement;
                group.querySelector('.active')?.classList.remove('active');
                pinButton.classList.add('active');
            }
        });

        // 全选/反选/保存/恢复默认 功能
        modalElement.querySelector('#column-config-select-all').addEventListener('click', () => {
            modalElement.querySelectorAll('#column-config-list li:not(.d-none) input[type="checkbox"]').forEach(cb => cb.checked = true);
        });
        modalElement.querySelector('#column-config-invert').addEventListener('click', () => {
            modalElement.querySelectorAll('#column-config-list li:not(.d-none) input[type="checkbox"]').forEach(cb => cb.checked = !cb.checked);
        });
        modalElement.querySelector('#save-column-config').addEventListener('click', () => {
            const newColumns = [];
            // 基于DOM中的顺序来构建新的列数组
            modalElement.querySelectorAll('#column-config-list li').forEach(item => {
                const key = item.dataset.key;
                const currentColumn = this.columns.find(c => c.key === key);
                if (currentColumn) {
                    currentColumn.visible = item.querySelector('input[type="checkbox"]').checked;
                    const frozenBtn = item.querySelector('.btn-group .active');
                    currentColumn.frozen = frozenBtn ? (frozenBtn.dataset.value === 'false' ? false : frozenBtn.dataset.value) : false;
                    newColumns.push(currentColumn);
                }
            });
            this.columns = newColumns;
            this._saveConfig();
            this.render(this.container);
            configModal.hide();
        });
        modalElement.querySelector('#reset-column-config').addEventListener('click', async () => {
            const confirmed = await Modal.confirm('恢复默认设置', '您确定要将列的显示、排序、宽度和锁定状态恢复到初始默认设置吗？此操作不可撤销。');
            if (confirmed) {
                if (this.options.storageKey) localStorage.removeItem(this.options.storageKey);
                this.columns = this.originalColumns.map(c => ({...c})); // 从原始配置重置
                this.render(this.container);
                configModal.hide();
            }
        });

        this._attachDragAndDropHandlers(listContainer);
        configModal.show();
    }

    _attachDragAndDropHandlers(list) {
        let draggedItem = null;
        list.addEventListener('dragstart', e => {
            if(e.target.tagName === 'LI') {
                draggedItem = e.target;
                setTimeout(() => e.target.classList.add('dragging'), 0);
            }
        });
        list.addEventListener('dragend', e => {
            if(draggedItem) {
                setTimeout(() => draggedItem.classList.remove('dragging'), 0);
                draggedItem = null;
            }
        });
        list.addEventListener('dragover', e => {
            e.preventDefault();
            const afterElement = [...list.querySelectorAll('li:not(.dragging)')].reduce((closest, child) => {
                const box = child.getBoundingClientRect();
                const offset = e.clientY - box.top - box.height / 2;
                if (offset < 0 && offset > closest.offset) {
                    return { offset: offset, element: child };
                } else {
                    return closest;
                }
            }, { offset: Number.NEGATIVE_INFINITY }).element;

            if (draggedItem) {
                if (afterElement == null) {
                    list.appendChild(draggedItem);
                } else {
                    list.insertBefore(draggedItem, afterElement);
                }
            }
        });
    }

    updateView(pageResult) {
        if (!pageResult) {
            console.error("[DataTable] updateView received invalid data:", pageResult);
            this.data = [];
            this.paginationInfo = { pageNum: 1, pages: 1, total: 0 };
        } else {
            this.data = Array.isArray(pageResult.list) ? pageResult.list : [];
            this.paginationInfo = {
                pageNum: pageResult.pageNum,
                pages: pageResult.pages,
                total: pageResult.total
            };
        }

        if (this.container) {
            const tbody = this.container.querySelector('tbody');
            if (tbody) {
                const visibleColumns = this._getVisibleColumns();
                tbody.innerHTML = this._createBody(visibleColumns);
            }
            const footer = this.container.querySelector('nav');
            if (footer) {
                const newFooter = document.createElement('div');
                newFooter.innerHTML = this._createFooter();
                if(newFooter.firstChild) {
                    footer.replaceWith(newFooter.firstChild);
                } else {
                    footer.innerHTML = '';
                }
            }
            this._updateSelectAllState();
        }
    }
}

