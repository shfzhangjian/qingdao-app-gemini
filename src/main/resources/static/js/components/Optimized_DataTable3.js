/**
 * 源码路径: js/components/Optimized_DataTable.js
 * 功能说明: 一个功能强大的数据表格组件
 * @version 3.5.8 - 2025-10-15: [新增] uniformRowHeight 选项，可根据内容统一所有数据行的高度。
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
            // [新增] 增加 uniformRowHeight 选项，用于控制是否统一所有数据行的高度。
            // 设置为 true 后，表格将计算所有可见数据行的最大高度，并将其应用到每一行，实现整齐划一的视觉效果。
            // 默认为 false，保持原有的自适应行高。
            uniformRowHeight: false,
            ...options
        };
        this.container = null;
        this.isConfigModalOpen = false;
        this.scrollSyncAttached = false;
        this.listenersAttached = false;
        // [新增] 选中状态管理
        if (this.options.selectable) {
            this.selectedRowId = null;
            this.selectedRows = new Set();
        }

        this.state = {
            pageNum: 1,
            pageSize: 10,
            sortBy: this.options.defaultSortBy || null,
            sortOrder: this.options.defaultSortOrder || 'asc',
            filters: this._getInitialFilters()
        };

        const loadedConfig = this._loadConfig();
        this.columns = loadedConfig || this.originalColumns;
        // [新增] 标记是否为首次加载（无本地配置）
        this.isInitialLoad = !loadedConfig;
    }

    /**
     * [新增] 绑定持久化容器的事件监听器，此方法只应执行一次
     * @private
     */
    _attachContainerEventListeners() {
        // 监听：点击事件（分页、排序、操作按钮等）
        this.container.addEventListener('click', e => {
            const button = e.target.closest('button[data-action]');
            if (button && button.dataset.action === 'configure-columns') this._showColumnConfigModal();

            const pageLink = e.target.closest('.page-link[data-page]');
            if (pageLink && !pageLink.parentElement.classList.contains('disabled')) {
                e.preventDefault();
                e.stopPropagation();
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

        // 监听：变更事件（仅限工具栏筛选器）
        this.container.addEventListener('change', e => {
            const radio = e.target.closest('input[type="radio"][name]');

            // [关键修复] 增加对父元素的检查，确保事件源是工具栏筛选器，而不是表格行
            if (radio && radio.closest('.d-flex.align-items-center.gap-4')) {
                this.state.filters[radio.name] = radio.value;
                this.state.pageNum = 1;
                this.container.dispatchEvent(new CustomEvent('queryChange', { detail: { filters: this.state.filters }, bubbles: true }));
            }
        });
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
                    return originalCol ? { ...originalCol, ...savedCol } : null;
                }).filter(Boolean);
                const loadedKeys = new Set(loadedColumns.map(c => c.key));
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
            const configToSave = this.columns.map(({ key, title, visible, width, frozen, sortable }) => ({ key, title, visible, width, frozen, sortable }));
            localStorage.setItem(this.options.storageKey, JSON.stringify(configToSave));
        } catch (error) { console.error("保存表格配置失败:", error); }
    }

    /**
     * [新增] 自动调整列宽以适应容器
     * @private
     */
    _autoFitColumns(containerWidth) {
        const visibleColumns = this._getVisibleColumns();
        const currentTotalWidth = visibleColumns.reduce((sum, col) => sum + (col.width || this.options.defaultColumnWidth), 0);

        if (currentTotalWidth >= containerWidth) {
            return; // 如果当前宽度已足够或超出，则不作处理
        }

        const spaceToFill = containerWidth - currentTotalWidth;
        const adaptableColumns = visibleColumns.filter(c => !c.frozen);
        const adaptableWidth = adaptableColumns.reduce((sum, col) => sum + (col.width || this.options.defaultColumnWidth), 0);

        if (adaptableWidth <= 0) {
            return; // 没有可供调整的列
        }

        let totalAdjustedWidth = 0;
        adaptableColumns.forEach(col => {
            const currentWidth = col.width || this.options.defaultColumnWidth;
            const adjustment = Math.floor(spaceToFill * (currentWidth / adaptableWidth));
            col.width = currentWidth + adjustment;
            totalAdjustedWidth += col.width;
        });

        // 将因取整导致的剩余像素加到最后一列
        const remainingSpace = containerWidth - visibleColumns.reduce((sum, col) => sum + (col.width || this.options.defaultColumnWidth), 0);
        if (remainingSpace > 0 && adaptableColumns.length > 0) {
            adaptableColumns[adaptableColumns.length - 1].width += remainingSpace;
        }
    }


    /**
     * 获取当前所有可见的列，并根据需要添加“选择框”列
     * @private
     */
    _getVisibleColumns() {
        let visibleCols = this.columns.filter(col => col.visible !== false);
        if (this.options.selectable === 'multiple' || this.options.selectable === 'single') { // 同时支持单选和多选
            const key = '__selection';
            const title = this.options.selectable === 'multiple' ? '<input type="checkbox" class="form-check-input" data-select="all">' : '';
            const render = (val, row) => {
                const inputType = this.options.selectable === 'multiple' ? 'checkbox' : 'radio';
                const name = this.options.selectable === 'single' ? 'dt-radio-selection' : '';
                return `<input type="${inputType}" class="form-check-input" name="${name}" data-row-id="${row.id || ''}">`;
            };
            visibleCols.unshift({ key, title, render, width: 40, frozen: 'left' });
        }
        return visibleCols;
    }

    /**
     * [修改] 恢复选中行的样式类，确保显式添加/移除类
     * @private
     */
    _restoreSelectionClasses() {
        if (!this.options.selectable || !this.rowIdMap) return;
        const isSingle = this.options.selectable === 'single';
        const selectedId = isSingle ? this.selectedRowId : null;
        this.data.forEach(row => {
            const rowId = this.rowIdMap.get(row);
            if (!rowId) return;
            const isSelected = isSingle ? (selectedId === rowId) : this.selectedRows.has(rowId);
            const rows = this.container.querySelectorAll(`tr[data-row-id="${rowId}"]`);
            rows.forEach(r => {
                if (isSelected) {
                    r.classList.add('table-active-custom');
                } else {
                    r.classList.remove('table-active-custom');
                }
            });
        });
    }

    /**
     * 核心渲染方法，构建表格的 DOM 结构
     * @param {HTMLElement} container - 渲染表格的目标容器
     */
    render(container) {
        if (!container) return;
        this.container = container;
        this.scrollSyncAttached = false;

        // [新增] 在首次加载时执行列宽自适应
        if (this.isInitialLoad) {
            // 需要先让容器有宽度
            requestAnimationFrame(() => {
                const wrapper = this.container.querySelector('.datatable-main-wrapper');
                if(wrapper) {
                    this._autoFitColumns(wrapper.clientWidth);
                    this.isInitialLoad = false; // 只执行一次
                    this.render(container); // 重新渲染以应用新宽度
                }
            });
        }

        const visibleColumns = this._getVisibleColumns();
        const leftFrozenCols = visibleColumns.filter(c => c.frozen === 'left');
        const rightFrozenCols = visibleColumns.filter(c => c.frozen === 'right');
        const scrollableCols = visibleColumns.filter(c => !c.frozen);

        // [新增] 预计算一致的行ID映射，确保跨窗格rowId一致
        this.rowIdMap = new Map();
        this.data.forEach(row => {
            if (!this.rowIdMap.has(row)) {
                const rowId = String(row.id || Math.random().toString(36).substr(2, 9));
                this.rowIdMap.set(row, rowId);
            }
        });

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

        // [关键修复] 使用标记确保容器监听器只附加一次
        if (!this.listenersAttached) {
            this._attachContainerEventListeners();
            this.listenersAttached = true;
        }

        // 确保在DOM更新后同步行高，并恢复选中状态
        requestAnimationFrame(() => {
            this._syncRowHeights();
            this._restoreSelectionClasses();
            if (this.options.selectable === 'multiple') {
                this._updateSelectAllState();
            }
        });
        this._attachScrollSync();
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
                    <tbody>${this._createBody(columns, type)}</tbody>
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

        // 当没有任何工具时，不渲染工具栏
        if (!actionsHtml && !filtersHtml && !configBtn) {
            return '';
        }

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
     * @param {Array} columns - 列定义
     * @param {string} paneType - 窗格类型（'left', 'scroll', 'right'）
     */
    _createBody(columns, paneType) {
        if (!this.data || this.data.length === 0) {
            // [修改] 无数据时，滚动区域显示提示，锁定区域添加空行以保持边框和高度一致
            if (paneType === 'scroll') {
                return `<tr><td colspan="${columns.length || 1}" class="text-center p-4">没有数据</td></tr>`;
            } else {
                return `<tr><td colspan="${columns.length || 1}" class="text-center p-4" style="visibility: hidden;">没有数据</td></tr>`;
            }
        }
        const fragment = document.createDocumentFragment();
        this.data.forEach(row => {
            const rowId = this.rowIdMap.get(row);
            const tr = document.createElement('tr');
            tr.dataset.rowId = rowId;
            const rowClass = typeof this.options.getRowClass === 'function' ? this.options.getRowClass(row) : '';
            if(rowClass) tr.className = rowClass;

            columns.forEach(col => {
                const td = document.createElement('td');
                let cellHtml;
                if (col.key === '__selection' && this.options.selectable) {
                    const inputType = this.options.selectable === 'multiple' ? 'checkbox' : 'radio';
                    const name = this.options.selectable === 'single' ? 'dt-radio-selection' : '';
                    let checkedAttr = '';
                    if (this.options.selectable === 'multiple') {
                        checkedAttr = this.selectedRows.has(rowId) ? 'checked' : '';
                    } else if (this.options.selectable === 'single') {
                        checkedAttr = (this.selectedRowId === rowId) ? 'checked' : '';
                    }
                    cellHtml = `<input type="${inputType}" class="form-check-input" name="${name}" data-row-id="${rowId}" ${checkedAttr}>`;
                } else {
                    const cellValue = row[col.key] !== undefined && row[col.key] !== null ? row[col.key] : '-';
                    cellHtml = typeof col.render === 'function' ? col.render(cellValue, row) : String(cellValue);
                }
                td.innerHTML = cellHtml;
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
        const syncScroll = (e) => {
            if (isSyncing) return;
            isSyncing = true;
            const source = e.currentTarget;
            scrollables.forEach(target => {
                if (target !== source && target.scrollTop !== source.scrollTop) {
                    target.scrollTop = source.scrollTop;
                }
            });
            requestAnimationFrame(() => { isSyncing = false; });
        };

        scrollables.forEach(el => el.addEventListener('scroll', syncScroll));
        this.scrollSyncAttached = true;
    }

    /**
     * [修改] 绑定多表格之间的行高亮和选择同步事件，避免选中操作触发 queryChange
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
                // [新增] 停止事件冒泡，避免触发上层 queryChange（如排序或分页）
                e.stopPropagation();
                const row = e.target.closest('tr');
                if (!row || !row.dataset.rowId || e.target.closest('button')) return;

                const allRelatedRows = this.container.querySelectorAll(`tr[data-row-id="${row.dataset.rowId}"]`);
                const rowId = row.dataset.rowId;
                const checkboxOrRadio = row.querySelector('input[type="checkbox"], input[type="radio"]');

                if (this.options.selectable === 'single') {
                    // 清除所有旧的选中状态
                    this.container.querySelectorAll('tr.table-active-custom').forEach(r => r.classList.remove('table-active-custom'));

                    // 高亮所有相关的行（左、中、右）
                    allRelatedRows.forEach(r => r.classList.add('table-active-custom'));

                    // 根据行ID在整个组件内查找并选中对应的单选按钮，并更新状态
                    const radioForThisRow = this.container.querySelector(`input[data-row-id="${rowId}"][name="dt-radio-selection"]`);
                    if (radioForThisRow) {
                        radioForThisRow.checked = true;
                        this.selectedRowId = rowId;
                        // [新增] 手动触发 change 事件，确保同步（但不触发 queryChange，因为 name 不匹配过滤器）
                        radioForThisRow.dispatchEvent(new Event('change', { bubbles: true }));
                    }
                } else if (this.options.selectable === 'multiple') {
                    // 如果点击的不是 checkbox，则反转其状态
                    if (checkboxOrRadio && e.target !== checkboxOrRadio) {
                        const newChecked = !checkboxOrRadio.checked;
                        checkboxOrRadio.checked = newChecked;
                        // 手动更新状态和样式（因为程序化设置 checked 不触发 change 事件）
                        if (newChecked) {
                            this.selectedRows.add(rowId);
                        } else {
                            this.selectedRows.delete(rowId);
                        }
                        allRelatedRows.forEach(r => r.classList.toggle('table-active-custom', newChecked));
                        this._updateSelectAllState();
                    }
                }
            });
            // [新增] 绑定选择框 change 事件处理联动
            table.addEventListener('change', e => {
                const input = e.target;
                if (input.type !== 'checkbox' && input.type !== 'radio') return;
                if (!input.dataset.rowId && input.dataset.select !== 'all') return;
                const rowId = input.dataset.rowId;
                if (input.dataset.select === 'all') {
                    // 全选/全不选处理
                    const checked = input.checked;
                    this.data.forEach(row => {
                        const rid = this.rowIdMap.get(row);
                        if (checked) {
                            this.selectedRows.add(rid);
                        } else {
                            this.selectedRows.delete(rid);
                        }
                    });
                    // 更新所有行 checkbox 和样式
                    this.data.forEach(row => {
                        const rid = this.rowIdMap.get(row);
                        const cb = this.container.querySelector(`input[type="checkbox"][data-row-id="${rid}"]`);
                        if (cb) cb.checked = checked;
                        this.container.querySelectorAll(`tr[data-row-id="${rid}"]`).forEach(r => r.classList.toggle('table-active-custom', checked));
                    });
                } else if (input.type === 'radio') {
                    // 单选处理
                    if (input.checked) {
                        this.selectedRowId = rowId;
                        this.container.querySelectorAll('tr.table-active-custom').forEach(r => r.classList.remove('table-active-custom'));
                        this.container.querySelectorAll(`tr[data-row-id="${rowId}"]`).forEach(r => r.classList.add('table-active-custom'));
                    }
                } else {
                    // 行 checkbox 处理
                    if (input.checked) {
                        this.selectedRows.add(rowId);
                    } else {
                        this.selectedRows.delete(rowId);
                    }
                    this.container.querySelectorAll(`tr[data-row-id="${rowId}"]`).forEach(r => r.classList.toggle('table-active-custom', input.checked));
                    this._updateSelectAllState();
                }
            });
        });
    }

    _updateSelectAllState() {
        if (this.options.selectable !== 'multiple') return;
        const selectAllCheckbox = this.container.querySelector('input[data-select="all"]');
        if (!selectAllCheckbox) return;

        const rowCheckboxes = Array.from(this.container.querySelectorAll('#dt-table-left tbody input[data-row-id]'));
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
                        this.isInitialLoad = false; // 用户手动调整后，不再触发自动调整
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
     * [重写] 同步所有表格的行高和表头高，确保对齐。
     * 增加了对 `uniformRowHeight` 选项的支持，可以统一所有数据行的高度。
     * @private
     */
    _syncRowHeights() {
        const tables = Array.from(this.container.querySelectorAll('.datatable-main-wrapper table'));

        // 如果只有一个表格（没有冻结列）并且未启用统一行高，则无需执行同步。
        if (tables.length < 2 && !this.options.uniformRowHeight) {
            return;
        }

        // 如果找不到任何表格，也直接返回。
        if (tables.length === 0) {
            return;
        }

        // --- 步骤 1: 同步表头行高 ---
        // 无论是否统一行高，只要存在冻结列（即多个table），表头行高就必须同步以保证对齐。
        if (tables.length > 1) {
            const headerRowCount = Math.max(...tables.map(t => t.tHead?.rows.length || 0));
            for (let i = 0; i < headerRowCount; i++) {
                const rowElements = tables.map(t => t.tHead?.rows[i]).filter(Boolean);
                if (rowElements.length < 2) continue;

                let maxHeight = 0;
                rowElements.forEach(r => { r.style.height = ''; });
                rowElements.forEach(r => { if (r.offsetHeight > maxHeight) maxHeight = r.offsetHeight; });
                rowElements.forEach(r => { r.style.height = `${maxHeight}px`; });
            }
        }

        // --- 步骤 2: 根据选项处理数据行（tbody） ---
        if (this.options.uniformRowHeight) {
            // [新功能] 模式：统一所有数据行的高度。
            const allBodyRows = tables.map(t => Array.from(t.tBodies[0]?.rows || [])).flat();
            if (allBodyRows.length > 0) {
                let maxOverallHeight = 0;
                // a. 先重置所有行的高度，以测量其内容的自然高度。
                allBodyRows.forEach(r => { r.style.height = ''; });
                // b. 遍历所有数据行，找到一个全局的最大高度。
                allBodyRows.forEach(r => { if (r.offsetHeight > maxOverallHeight) maxOverallHeight = r.offsetHeight; });
                // c. 将这个全局最大高度应用到每一行。
                allBodyRows.forEach(r => { r.style.height = `${maxOverallHeight}px`; });
            }
        } else {
            // [原功能] 模式：逐行同步数据行高度（仅在有冻结列时需要）。
            if (tables.length > 1) {
                const bodyRowCount = Math.max(...tables.map(t => t.tBodies[0]?.rows.length || 0));
                for (let i = 0; i < bodyRowCount; i++) {
                    const rowElements = tables.map(t => t.tBodies[0]?.rows[i]).filter(Boolean);
                    if (rowElements.length < 2) continue;

                    let maxHeight = 0;
                    rowElements.forEach(r => { r.style.height = ''; });
                    rowElements.forEach(r => { if (r.offsetHeight > maxHeight) maxHeight = r.offsetHeight; });
                    rowElements.forEach(r => { r.style.height = `${maxHeight}px`; });
                }
            }
        }
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
        if (overlay) overlay.classList.toggle('d-flex', isLoading);
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
        renderList(this.columns.filter(c => c.key !== '__selection')); // 不显示选择列

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
            this.isInitialLoad = false; // 用户手动保存后，不再触发自动调整
            shouldRerender = true;
            configModal.hide();
        });

        modalElement.querySelector('#reset-column-config').addEventListener('click', async () => {
            const confirmed = await Modal.confirm('恢复默认设置', '您确定要将列的显示、排序、宽度和锁定状态恢复到初始默认设置吗？此操作不可撤销。');
            if (confirmed) {
                if (this.options.storageKey) localStorage.removeItem(this.options.storageKey);
                this.columns = this.originalColumns.map(c => ({...c}));
                this.isInitialLoad = true; // 恢复默认后，下次加载时应重新自适应
                shouldRerender = true;
                configModal.hide();
            }
        });

        modalElement.addEventListener('hidden.bs.modal', () => {
            this.isConfigModalOpen = false;
            if (shouldRerender) {
                this.render(this.container);
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
