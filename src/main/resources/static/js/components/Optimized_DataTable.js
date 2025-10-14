/**
 * 源码路径: js/components/Optimized_DataTable.js
 * 功能说明: 一个功能强大的数据表格组件
 * @version 3.2.0 - 2025-10-14: 补全所有方法实现，移除所有占位符，并添加了完整的中文注释。
 */
import Modal from './Modal.js';

export default class DataTable {
    /**
     * DataTable 组件构造函数
     * @param {object} config - 组件配置对象
     * @param {Array} config.columns - 列定义
     * @param {Array} config.data - 初始数据
     * @param {Array} config.actions - 工具栏操作按钮
     * @param {Array} config.filters - 工具栏筛选器
     * @param {object} config.options - 其他选项，如是否可配置、分页、排序等
     */
    constructor({ columns = [], data = [], actions = [], filters = [], options = {} }) {
        this.originalColumns = columns.map(c => ({...c, width: c.width || null, frozen: c.frozen || false, visible: c.visible !== false }));
        this.data = data || [];
        this.actions = actions;
        this.filters = filters;
        this.options = {
            resizable: true,
            defaultColumnWidth: 150,
            getRowClass: null,
            pagination: true,
            ...options
        };
        this.container = null;
        this.isConfigModalOpen = false; // 防止重复打开“配置列”模态框的状态标志
        this.scrollSyncAttached = false; // 防止重复绑定滚动同步事件的标志

        // 表格的核心状态，管理分页、排序和筛选
        this.state = {
            pageNum: 1,
            pageSize: 10,
            sortBy: this.options.defaultSortBy || null,
            sortOrder: this.options.defaultSortOrder || 'asc',
            filters: this._getInitialFilters()
        };

        this.columns = this._loadConfig() || this.originalColumns;
    }

    /**
     * 从筛选器配置中获取初始的筛选值
     * @private
     */
    _getInitialFilters() {
        const initialFilters = {};
        this.filters.forEach(field => {
            if (field.type === 'pills' && field.options) {
                const checkedOption = field.options.find(opt => opt.checked);
                initialFilters[field.name] = checkedOption ? checkedOption.value : field.options[0]?.value;
            }
        });
        return initialFilters;
    }

    /**
     * 从 localStorage 加载用户保存的列配置（顺序、宽度、可见性、锁定状态）
     * @private
     */
    _loadConfig() {
        if (!this.options.storageKey) return null;
        try {
            const savedConfig = localStorage.getItem(this.options.storageKey);
            if (savedConfig) {
                const parsedConfig = JSON.parse(savedConfig);
                const originalColsMap = new Map(this.originalColumns.map(c => [c.key, c]));
                const loadedColumns = parsedConfig.map(savedCol => {
                    const originalCol = originalColsMap.get(savedCol.key);
                    // 以保存的配置为基础，并从原始配置中恢复 render 等函数属性
                    return originalCol ? { ...originalCol, ...savedCol } : null;
                }).filter(Boolean);
                const loadedKeys = new Set(loadedColumns.map(c => c.key));
                // 将代码中新增的、但本地存储中没有的列追加到末尾
                this.originalColumns.forEach(originalCol => {
                    if (!loadedKeys.has(originalCol.key)) loadedColumns.push(originalCol);
                });
                return loadedColumns;
            }
        } catch (error) { console.error("加载表格配置失败:", error); }
        return null;
    }

    /**
     * 将当前列配置保存到 localStorage
     * @private
     */
    _saveConfig() {
        if (!this.options.storageKey) return;
        try {
            // 只保存可序列化的关键属性
            const configToSave = this.columns.map(({ key, title, visible, width, frozen, sortable }) => ({ key, title, visible, width, frozen, sortable }));
            localStorage.setItem(this.options.storageKey, JSON.stringify(configToSave));
        } catch (error) { console.error("保存表格配置失败:", error); }
    }

    /**
     * 获取当前所有可见的列，并根据需要添加“选择框”列
     * @private
     */
    _getVisibleColumns() {
        let visibleCols = this.columns.filter(col => col.visible !== false);
        if (this.options.selectable === 'multiple') {
            visibleCols.unshift({ key: '__selection', title: '<input type="checkbox" class="form-check-input" data-select="all">', render: (val, row) => `<input type="checkbox" class="form-check-input" data-row-id="${row.id || ''}">`, width: 40, frozen: 'left' });
        }
        return visibleCols;
    }

    /**
     * 核心渲染方法，构建表格的 DOM 结构
     * @param {HTMLElement} container - 渲染表格的目标容器
     */
    render(container) {
        if (!container) return;
        this.container = container;
        this.scrollSyncAttached = false;

        const visibleColumns = this._getVisibleColumns();
        const leftFrozenCols = visibleColumns.filter(c => c.frozen === 'left');
        const rightFrozenCols = visibleColumns.filter(c => c.frozen === 'right');
        const scrollableCols = visibleColumns.filter(c => !c.frozen);

        this.container.innerHTML = `
            <div style="display: flex; flex-direction: column; height: 100%;" class="position-relative">
                <div id="dt-loading-overlay" class="position-absolute top-0 start-0 w-100 h-100 d-none justify-content-center align-items-center" style="background-color: rgba(0,0,0,0.3); z-index: 10;">
                    <div class="spinner-border text-light" role="status"></div>
                </div>
                ${this._createToolbar()}
                <div class="datatable-main-wrapper">
                    ${leftFrozenCols.length > 0 ? this._createPane('left', leftFrozenCols) : ''}
                    ${scrollableCols.length > 0 ? this._createPane('scroll', scrollableCols) : ''}
                    ${rightFrozenCols.length > 0 ? this._createPane('right', rightFrozenCols) : ''}
                </div>
                ${this._createFooter()}
            </div>
        `;

        this._attachEventListeners();
        this._syncRowHeights();
        this._attachScrollSync();

        // 派发 'ready' 事件，通知外部组件可以加载数据了
        setTimeout(() => this.container.dispatchEvent(new CustomEvent('ready', { bubbles: true })), 0);
    }

    /**
     * 创建左、中、右三个表格窗格之一
     * @private
     */
    _createPane(type, columns) {
        const totalWidth = columns.reduce((sum, col) => sum + (col.width || this.options.defaultColumnWidth), 0);
        let paneClass = '', tableId = '';
        if (type === 'left') { paneClass = 'datatable-frozen-pane-left'; tableId = 'dt-table-left'; }
        else if (type === 'right') { paneClass = 'datatable-frozen-pane-right'; tableId = 'dt-table-right'; }
        else { paneClass = 'datatable-scrollable-pane'; tableId = 'dt-table-scroll'; }

        return `
            <div class="${paneClass}" id="dt-pane-${type}">
                 <table class="table table-bordered table-sm text-center table-hover" id="${tableId}" style="width: ${totalWidth}px; min-width: ${totalWidth}px;">
                    <colgroup>${this._createColGroup(columns)}</colgroup>
                    <thead>${this._createHeader(columns)}</thead>
                    <tbody>${this._createBody(columns)}</tbody>
                </table>
            </div>
        `;
    }

    /**
     * 创建表格顶部的工具栏（操作按钮、筛选器、配置按钮）
     * @private
     */
    _createToolbar() {
        const actionsHtml = this.actions.map(a => `<button class="btn btn-sm ${a.class || 'btn-primary'}" data-action="${a.name}">${a.text}</button>`).join('');
        const filtersHtml = this.filters.map(f => {
            if (f.type === 'pills') {
                const opts = f.options.map(o => `<input type="radio" class="btn-check" name="${f.name}" id="${f.name}-${o.value}" value="${o.value}" autocomplete="off" ${this.state.filters[f.name] === o.value ? 'checked' : ''}><label class="btn btn-sm btn-outline-secondary" for="${f.name}-${o.value}">${o.label}</label>`).join('');
                return `<div class="d-flex align-items-center gap-2"><label class="form-label mb-0 flex-shrink-0">${f.label}:</label><div class="btn-group btn-group-sm" role="group">${opts}</div></div>`;
            } return '';
        }).join('');
        const configBtn = this.options.configurable ? `<button class="btn btn-sm btn-outline-secondary" data-action="configure-columns"><i class="bi bi-gear"></i> 配置列</button>` : '';
        return `<div class="d-flex justify-content-between align-items-center mb-3">
                    <div class="d-flex justify-content-start gap-2">${actionsHtml}</div>
                    <div class="d-flex align-items-center gap-4">${filtersHtml}${configBtn}</div>
                </div>`;
    }

    /**
     * 创建表格的 <colgroup> 用于定义列宽
     * @private
     */
    _createColGroup(columns) { return columns.map(c => `<col data-col-key="${c.key}" style="width: ${c.width || this.options.defaultColumnWidth}px">`).join(''); }

    /**
     * 创建表格的 <thead>
     * @private
     */
    _createHeader(columns) {
        const cells = columns.map(col => {
            let sortIndicator = '';
            let sortableClass = '';
            if (col.sortable) {
                sortableClass = 'dt-sortable';
                if (this.state.sortBy === col.key) {
                    sortIndicator = this.state.sortOrder === 'asc' ? '<i class="bi bi-sort-up ms-1"></i>' : '<i class="bi bi-sort-down ms-1"></i>';
                }
            }
            const resizer = this.options.resizable && col.key !== '__selection' ? '<div class="resizer"></div>' : '';
            return `<th data-col-key="${col.key}" class="${sortableClass}">${col.title}${sortIndicator}${resizer}</th>`;
        }).join('');
        return `<tr>${cells}</tr>`;
    }

    /**
     * 创建表格的 <tbody>
     * @private
     */
    _createBody(columns) {
        if (!this.data || this.data.length === 0) {
            return `<tr><td colspan="${columns.length || 1}" class="text-center p-4">没有数据</td></tr>`;
        }
        const fragment = document.createDocumentFragment();
        this.data.forEach(row => {
            const tr = document.createElement('tr');
            tr.dataset.rowId = String(row.id || Math.random());
            const rowClass = typeof this.options.getRowClass === 'function' ? this.options.getRowClass(row) : '';
            if(rowClass) tr.className = rowClass;

            columns.forEach(col => {
                const td = document.createElement('td');
                const cellValue = row[col.key] !== undefined && row[col.key] !== null ? row[col.key] : '-';
                td.innerHTML = typeof col.render === 'function' ? col.render(cellValue, row) : cellValue;
                tr.appendChild(td);
            });
            fragment.appendChild(tr);
        });
        const tempDiv = document.createElement('div');
        tempDiv.appendChild(fragment);
        return tempDiv.innerHTML;
    }

    /**
     * 创建表格底部的分页和统计信息
     * @private
     */
    _createFooter() {
        if (!this.options.pagination || !this.paginationInfo) return '';
        const { pageNum, pages, total } = this.paginationInfo;
        let paginationHtml = '';
        if (pages > 1) {
            const createPageItem = (p, label, isDisabled = false, isActive = false) => `<li class="page-item ${isDisabled ? 'disabled' : ''} ${isActive ? 'active' : ''}"><a class="page-link" href="#" data-page="${p}">${label}</a></li>`;
            paginationHtml += createPageItem(pageNum - 1, '&laquo;', pageNum <= 1);
            let startPage, endPage;
            if (pages <= 7) { [startPage, endPage] = [1, pages]; }
            else { if (pageNum <= 4) { [startPage, endPage] = [1, 5]; }
            else if (pageNum + 3 >= pages) { [startPage, endPage] = [pages - 4, pages]; }
            else { [startPage, endPage] = [pageNum - 2, pageNum + 2]; } }
            if (startPage > 1) { paginationHtml += createPageItem(1, '1'); if (startPage > 2) { paginationHtml += `<li class="page-item disabled"><span class="page-link">...</span></li>`; } }
            for (let i = startPage; i <= endPage; i++) { paginationHtml += createPageItem(i, i, false, i === pageNum); }
            if (endPage < pages) { if (endPage < pages - 1) { paginationHtml += `<li class="page-item disabled"><span class="page-link">...</span></li>`; } paginationHtml += createPageItem(pages, pages); }
            paginationHtml += createPageItem(pageNum + 1, '&raquo;', pageNum >= pages);
        }
        const totalCountHtml = `<span class="text-secondary me-3">共 ${total} 条</span>`;
        return `<nav class="d-flex justify-content-between align-items-center pt-2 mt-auto" style="border-top: 1px solid var(--border-color);"><div>${totalCountHtml}</div><ul class="pagination pagination-sm mb-0">${paginationHtml}</ul></nav>`;
    }

    /**
     * 绑定所有交互事件
     * @private
     */
    _attachEventListeners() {
        this.container.addEventListener('click', e => {
            const button = e.target.closest('button[data-action]');
            if (button && button.dataset.action === 'configure-columns') this._showColumnConfigModal();

            const pageLink = e.target.closest('.page-link[data-page]');
            if (pageLink && !pageLink.parentElement.classList.contains('disabled')) {
                e.preventDefault();
                this.state.pageNum = parseInt(pageLink.dataset.page, 10);
                this.container.dispatchEvent(new CustomEvent('queryChange', { detail: { pageNum: this.state.pageNum }, bubbles: true }));
            }

            const sortableHeader = e.target.closest('th.dt-sortable');
            if (sortableHeader) {
                const key = sortableHeader.dataset.colKey;
                this.state.sortOrder = (this.state.sortBy === key && this.state.sortOrder === 'asc') ? 'desc' : 'asc';
                this.state.sortBy = key;
                this.state.pageNum = 1;
                this.container.dispatchEvent(new CustomEvent('queryChange', { detail: { sortBy: this.state.sortBy, sortOrder: this.state.sortOrder }, bubbles: true }));
            }
        });

        this.container.addEventListener('change', e => {
            const radio = e.target.closest('input[type="radio"][name]');
            if (radio) {
                this.state.filters[radio.name] = radio.value;
                this.state.pageNum = 1;
                this.container.dispatchEvent(new CustomEvent('queryChange', { detail: { filters: this.state.filters }, bubbles: true }));
            }
        });

        this._attachResizeListeners();
        this._attachSelectionAndHoverSync();
    }

    /**
     * 绑定多表格之间的垂直滚动同步事件
     * @private
     */
    _attachScrollSync() {
        if (this.scrollSyncAttached) return;
        const scrollables = [this.container.querySelector('.datatable-scrollable-pane'), this.container.querySelector('.datatable-frozen-pane-left'), this.container.querySelector('.datatable-frozen-pane-right')].filter(Boolean);
        if(scrollables.length < 2) return;

        let isSyncing = false;
        scrollables.forEach(el => {
            el.addEventListener('scroll', () => {
                if (!isSyncing) {
                    isSyncing = true;
                    const scrollTop = el.scrollTop;
                    scrollables.forEach(otherEl => { if (otherEl !== el) { otherEl.scrollTop = scrollTop; } });
                    requestAnimationFrame(() => { isSyncing = false; });
                }
            });
        });
        this.scrollSyncAttached = true;
    }

    /**
     * 绑定多表格之间的行高亮和选择同步事件
     * @private
     */
    _attachSelectionAndHoverSync() {
        const tables = this.container.querySelectorAll('table');
        tables.forEach(table => {
            table.addEventListener('mouseover', e => {
                const row = e.target.closest('tr');
                if (row && row.dataset.rowId) this.container.querySelectorAll(`tr[data-row-id="${row.dataset.rowId}"]`).forEach(r => r.classList.add('table-hover-custom'));
            });
            table.addEventListener('mouseout', e => {
                const row = e.target.closest('tr');
                if (row && row.dataset.rowId) this.container.querySelectorAll(`tr[data-row-id="${row.dataset.rowId}"]`).forEach(r => r.classList.remove('table-hover-custom'));
            });
            table.addEventListener('click', e => {
                const row = e.target.closest('tr');
                if (!row || !row.dataset.rowId || e.target.closest('button')) return;

                const allRows = this.container.querySelectorAll(`tr[data-row-id="${row.dataset.rowId}"]`);
                if (this.options.selectable === 'single') {
                    this.container.querySelectorAll('.table-active-custom').forEach(r => r.classList.remove('table-active-custom'));
                    allRows.forEach(r => r.classList.add('table-active-custom'));
                }
            });
        });
    }

    /**
     * 绑定列宽拖拽功能
     * @private
     */
    _attachResizeListeners() {
        this.container.querySelectorAll('thead').forEach(header => {
            let th, col, startX, startWidth, table, tableWidth;
            const onMouseMove = (e) => {
                e.preventDefault();
                const dx = e.clientX - startX;
                const newWidth = startWidth + dx;
                if (newWidth > 30) {
                    col.style.width = `${newWidth}px`;
                    table.style.width = `${tableWidth + dx}px`;
                }
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
                    table = th.closest('table');
                    const colKey = th.dataset.colKey;
                    col = table.querySelector(`col[data-col-key="${colKey}"]`);
                    if (col) {
                        startX = e.clientX;
                        startWidth = col.offsetWidth;
                        tableWidth = table.offsetWidth;
                        document.body.style.cursor = 'col-resize';
                        document.addEventListener('mousemove', onMouseMove);
                        document.addEventListener('mouseup', onMouseUp);
                    }
                }
            });
        });
    }

    /**
     * 同步所有表格的行高，确保对齐
     * @private
     */
    _syncRowHeights() {
        const leftBody = this.container.querySelector('#dt-table-left tbody');
        const scrollBody = this.container.querySelector('#dt-table-scroll tbody');
        const rightBody = this.container.querySelector('#dt-table-right tbody');

        if (!scrollBody) return;
        const scrollRows = scrollBody.rows;
        if(scrollRows.length === 0) return;

        // 使用 requestAnimationFrame 确保在浏览器完成布局计算后执行
        requestAnimationFrame(() => {
            for (let i = 0; i < scrollRows.length; i++) {
                const rowElements = [scrollRows[i], leftBody ? leftBody.rows[i] : null, rightBody ? rightBody.rows[i] : null].filter(Boolean);
                if(rowElements.length < 2) continue;

                let maxHeight = 0;
                // 先重置高度，以便获取自然高度
                rowElements.forEach(r => { r.style.height = ''; });
                // 获取最大自然高度
                rowElements.forEach(r => { if(r.offsetHeight > maxHeight) maxHeight = r.offsetHeight; });
                // 设置统一高度
                rowElements.forEach(r => { r.style.height = `${maxHeight}px`; });
            }
        });
    }

    /**
     * 使用新的数据更新表格视图
     */
    updateView(pageResult) {
        this.data = pageResult && Array.isArray(pageResult.list) ? pageResult.list : [];
        this.paginationInfo = pageResult ? { pageNum: pageResult.pageNum, pages: pageResult.pages, total: pageResult.total } : { pageNum: 1, pages: 1, total: 0 };
        this.render(this.container);
    }

    /**
     * 控制加载遮罩的显示/隐藏
     */
    toggleLoading(isLoading) {
        const overlay = this.container.querySelector('#dt-loading-overlay');
        if (overlay) overlay.classList.toggle('d-none', !isLoading);
    }

    /**
     * 显示“配置列”模态框
     * @private
     */
    _showColumnConfigModal() {
        if (this.isConfigModalOpen) return;
        this.isConfigModalOpen = true;
        let shouldRerender = false;

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
                        <button type="button" class="btn btn-outline-secondary ${!col.frozen ? 'active' : ''}" data-action="pin" data-value="false" title="不固定"><i class="bi bi-x-circle"></i></button>
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

        modalElement.querySelector('#column-config-filter').addEventListener('input', (e) => {
            const filterText = e.target.value.toLowerCase();
            modalElement.querySelectorAll('#column-config-list li').forEach(item => {
                const title = item.querySelector('span').textContent.toLowerCase();
                item.classList.toggle('d-none', !title.includes(filterText));
            });
        });

        modalElement.querySelector('#column-config-list').addEventListener('click', (e) => {
            const pinButton = e.target.closest('button[data-action="pin"]');
            if (pinButton) {
                pinButton.parentElement.querySelector('.active')?.classList.remove('active');
                pinButton.classList.add('active');
            }
        });

        modalElement.querySelector('#column-config-select-all').addEventListener('click', () => {
            modalElement.querySelectorAll('#column-config-list li:not(.d-none) input[type="checkbox"]').forEach(cb => cb.checked = true);
        });

        modalElement.querySelector('#column-config-invert').addEventListener('click', () => {
            modalElement.querySelectorAll('#column-config-list li:not(.d-none) input[type="checkbox"]').forEach(cb => cb.checked = !cb.checked);
        });

        modalElement.querySelector('#save-column-config').addEventListener('click', () => {
            const newColumns = [];
            modalElement.querySelectorAll('#column-config-list li').forEach(item => {
                const key = item.dataset.key;
                const currentColumn = this.columns.find(c => c.key === key);
                if (currentColumn) {
                    currentColumn.visible = item.querySelector('input[type="checkbox"]').checked;
                    const frozenBtn = item.querySelector('.btn-group .active');
                    currentColumn.frozen = frozenBtn.dataset.value === 'false' ? false : frozenBtn.dataset.value;
                    newColumns.push(currentColumn);
                }
            });
            this.columns = newColumns;
            this._saveConfig();
            shouldRerender = true;
            configModal.hide();
        });

        modalElement.querySelector('#reset-column-config').addEventListener('click', async () => {
            const confirmed = await Modal.confirm('恢复默认设置', '您确定要将列的显示、排序、宽度和锁定状态恢复到初始默认设置吗？此操作不可撤销。');
            if (confirmed) {
                if (this.options.storageKey) localStorage.removeItem(this.options.storageKey);
                this.columns = this.originalColumns.map(c => ({...c}));
                shouldRerender = true;
                configModal.hide();
            }
        });

        modalElement.addEventListener('hidden.bs.modal', () => {
            this.isConfigModalOpen = false;
            if (shouldRerender) {
                setTimeout(() => this.render(this.container), 50);
            }
        }, { once: true });

        this._attachDragAndDropHandlers(listContainer);
        configModal.show();
    }

    /**
     * 绑定“配置列”模态框中的拖拽排序功能
     * @private
     */
    _attachDragAndDropHandlers(list) {
        let draggedItem = null;
        list.addEventListener('dragstart', e => {
            if(e.target.tagName === 'LI') {
                draggedItem = e.target;
                setTimeout(() => e.target.classList.add('dragging'), 0);
            }
        });
        list.addEventListener('dragend', () => {
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
                return (offset < 0 && offset > closest.offset) ? { offset: offset, element: child } : closest;
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
}

