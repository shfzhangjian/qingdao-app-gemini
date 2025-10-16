/**
 * 源码路径: js/views/PointCheck.js
 * 功能说明: 点检统计页面的最终版视图逻辑。
 * - 采用了全新的单行、左对齐、胶囊(Pills)风格Tab的UI布局。
 * - 彻底修复了列表视图下反复出现的“无限加载”Bug。
 * 版本变动:
 * v4.2.0 - 2025-10-15: [修复] 重构了交叉高亮逻辑，使用事件委托模式解决了下钻返回后事件丢失的问题。
 */
import DataTable from '../components/Optimized_DataTable.js';
import DatePicker from '../components/DatePicker.js';
import Modal from '../components/Modal.js';
import { getPointCheckStatistics, getPointCheckList, exportPointCheck } from '../services/api.js';

export default class PointCheck {
    constructor() {
        this.container = null;
        this.headerContainer = null;
        this.contentContainer = null;
        this.dataTable = null;
        this.datePicker = null;

        this.currentState = {
            viewMode: 'statistics',
            category: 'all',
            dateRange: '2025-01-01 to 2025-12-31',
            department: '',
            planStatus: 'all',
            resultStatus: 'all'
        };
    }

    render(container) {
        this.currentState.viewMode = 'statistics';
        this.dataTable = null;

        this.container = container;
        container.innerHTML = `
            <div class="p-3 rounded" style="background-color: var(--bg-secondary); display: flex; flex-direction: column; height: 100%;">
                <div id="point-check-header"></div>
                <div id="point-check-content" style="flex-grow: 1; display: flex; flex-direction: column; min-height: 0;"></div>
            </div>`;

        this.headerContainer = container.querySelector('#point-check-header');
        this.contentContainer = container.querySelector('#point-check-content');

        this._renderHeader();
        this._updateView();
        this._attachEventListeners();
    }

    _renderHeader() {
        const { viewMode, category, department, planStatus, resultStatus } = this.currentState;
        const isListMode = viewMode === 'list';
        const listFiltersVisibility = isListMode ? '' : 'd-none';

        this.headerContainer.innerHTML = `
            <div class="d-flex align-items-center justify-content-between flex-wrap row-gap-3 p-3 rounded mb-3" style="background-color: var(--bg-primary);">
                <div class="d-flex align-items-center gap-3 flex-wrap">
                    <ul class="nav nav-pills">
                        <li class="nav-item"><button class="nav-link ${viewMode === 'statistics' ? 'active' : ''} py-1" data-view="statistics">统计</button></li>
                        <li class="nav-item"><button class="nav-link ${isListMode ? 'active' : ''} py-1" data-view="list">列表</button></li>
                    </ul>
                    <input type="text" id="point-check-daterange" class="form-control form-control-sm" style="width: 240px;" placeholder="时间范围">
                    <select id="category-filter" class="form-select form-select-sm" style="width: 100px;">
                        <option value="all" ${category === 'all' ? 'selected' : ''}>全部类型</option>
                        <option value="A" ${category === 'A' ? 'selected' : ''}>A类</option>
                        <option value="B" ${category === 'B' ? 'selected' : ''}>B类</option>
                        <option value="C" ${category === 'C' ? 'selected' : ''}>C类</option>
                    </select>
                    <div id="list-specific-filters" class="d-flex align-items-center gap-3 ${listFiltersVisibility}">
                        <input type="text" id="department-filter" class="form-control form-control-sm" style="width: 150px;" value="${department}" placeholder="部门">
                        <select id="plan-status-filter" class="form-select form-select-sm" style="width: 120px;">
                            <option value="all" ${planStatus === 'all' ? 'selected' : ''}>全部计划</option>
                            <option value="已检" ${planStatus === '已检' ? 'selected' : ''}>已检</option>
                            <option value="未检" ${planStatus === '未检' ? 'selected' : ''}>未检</option>
                        </select>
                        <select id="result-status-filter" class="form-select form-select-sm" style="width: 120px;">
                            <option value="all" ${resultStatus === 'all' ? 'selected' : ''}>全部结果</option>
                            <option value="正常" ${resultStatus === '正常' ? 'selected' : ''}>正常</option>
                            <option value="异常" ${resultStatus === '异常' ? 'selected' : ''}>异常</option>
                            <option value="未检" ${resultStatus === '未检' ? 'selected' : ''}>未检</option>
                        </select>
                    </div>
                </div>
                <div class="d-flex align-items-center gap-2">
                    <button class="btn btn-sm btn-primary" id="query-btn"><i class="bi bi-search"></i> 查询</button>
                    <button class="btn btn-sm btn-outline-success" id="export-btn"><i class="bi bi-download"></i> 导出</button>
                    <button class="btn btn-sm btn-outline-secondary" id="configure-columns-btn" ${!isListMode ? 'disabled' : ''}><i class="bi bi-gear"></i> 配置列</button>
                </div>
            </div>
        `;

        if (this.datePicker) this.datePicker.destroy();
        const dateInput = this.headerContainer.querySelector('#point-check-daterange');
        this.datePicker = new DatePicker(dateInput, {
            mode: 'range',
            defaultDate: this.currentState.dateRange.split(' to '),
            onChange: (selectedDates, dateStr) => { if (selectedDates.length === 2) this.currentState.dateRange = dateStr; }
        });
    }

    _updateView() {
        if (this.currentState.viewMode === 'list') {
            this._renderListViewSkeleton();
        }
        this._loadData();
    }

    async _loadData() {
        const { viewMode } = this.currentState;

        if (viewMode === 'list' && this.dataTable) {
            this.dataTable.toggleLoading(true);
        } else if (viewMode === 'statistics') {
            this.contentContainer.innerHTML = `<div class="d-flex justify-content-center align-items-center h-100"><div class="spinner-border" role="status"></div></div>`;
        }

        try {
            const baseParams = { category: this.currentState.category, dateRange: this.currentState.dateRange };

            if (viewMode === 'statistics') {
                const statsData = await getPointCheckStatistics(baseParams);
                this._renderStatisticsView(statsData);
            } else {
                if (!this.dataTable) this._renderListViewSkeleton();

                const tableState = this.dataTable.state;
                const params = {
                    ...baseParams,
                    department: this.currentState.department,
                    planStatus: this.currentState.planStatus,
                    resultStatus: this.currentState.resultStatus,
                    pageNum: tableState.pageNum,
                    pageSize: tableState.pageSize,
                    sortBy: tableState.sortBy,
                    sortOrder: tableState.sortOrder
                };
                const pageResult = await getPointCheckList(params);
                this.dataTable.updateView(pageResult);
            }
        } catch (error) {
            console.error("加载点检数据失败:", error);
            this.contentContainer.innerHTML = `<div class="alert alert-danger">数据加载失败: ${error.message}</div>`;
        } finally {
            if (viewMode === 'list' && this.dataTable) {
                this.dataTable.toggleLoading(false);
            }
        }
    }

    _renderStatisticsView(data) {
        if (!data || data.length === 0) {
            this.contentContainer.innerHTML = '<div class="alert alert-info">没有可供统计的数据。</div>';
            return;
        }
        const tableRows = data.map(row => `
            <tr>
                <td class="text-start ps-3">${row.dept}</td>
                <td><a href="#" class="drill-down" data-department="${row.dept}" data-plan-status="all">[${row.yingJianShu1}]</a></td>
                <td><a href="#" class="drill-down" data-department="${row.dept}" data-plan-status="已检">[${row.yiJianShu1}]</a></td>
                <td><a href="#" class="drill-down" data-department="${row.dept}" data-plan-status="未检">[${row.weiJianShu1}]</a></td>
                <td>${row.zhiXingLv1}</td>
                <td><a href="#" class="drill-down" data-department="${row.dept}" data-result-status="正常">[${row.zhengChangShu1}]</a></td>
                <td><a href="#" class="drill-down" data-department="${row.dept}" data-result-status="异常">[${row.yiChangShu1}]</a></td>
                <td><a href="#" class="drill-down" data-department="${row.dept}" data-plan-status="all">[${row.yingJianShu2}]</a></td>
                <td><a href="#" class="drill-down" data-department="${row.dept}" data-plan-status="已检">[${row.yiJianShu2}]</a></td>
                <td><a href="#" class="drill-down" data-department="${row.dept}" data-plan-status="未检">[${row.weiJianShu2}]</a></td>
                <td>${row.zhiXingLv2}</td>
                <td><a href="#" class="drill-down" data-department="${row.dept}" data-result-status="正常">[${row.zhengChangShu2}]</a></td>
                <td><a href="#" class="drill-down" data-department="${row.dept}" data-result-status="异常">[${row.yiChangShu2}]</a></td>
            </tr>
        `).join('');

        this.contentContainer.innerHTML = `
            <div class="table-responsive">
                <table class="table table-bordered table-sm text-center stats-table-hover" id="stats-table">
                    <thead>
                        <tr><th rowspan="2" class="align-middle">部门名称</th><th colspan="6">上半年点检情况</th><th colspan="6">下半年点检情况</th></tr>
                        <tr><th>应检数量</th><th>已检数量</th><th>未检数量</th><th>执行率</th><th>正常数量</th><th>异常数量</th><th>应检数量</th><th>已检数量</th><th>未检数量</th><th>执行率</th><th>正常数量</th><th>异常数量</th></tr>
                    </thead>
                    <tbody>${tableRows}</tbody>
                </table>
            </div>
        `;
    }

    _renderListViewSkeleton() {
        if (this.dataTable) return;
        const columns = [
            { key: 'department', title: '部门', visible: true, sortable: true },
            { key: 'deviceName', title: '设备名称', visible: true, sortable: true },
            { key: 'deviceType', title: '类型', visible: true, width: 80 },
            { key: 'checkDate', title: '日期', visible: true, sortable: true },
            { key: 'planStatus', title: '计划状态', visible: true },
            { key: 'resultStatus', title: '检查结果', visible: true, render: (val) => val === '异常' ? `<span class="text-danger">${val}</span>` : val },
        ];
        this.dataTable = new DataTable({
            columns, data: [], actions: [],
            options: {
                uniformRowHeight: true,
                configurable: false,
                storageKey: 'pointCheckListTable',
                selectable: 'multiple'
            }
        });
        this.dataTable.render(this.contentContainer);
    }

    _attachEventListeners() {
        // --- 头部按钮事件 ---
        this.headerContainer.addEventListener('click', async (e) => {
            const button = e.target.closest('button');
            if (!button) return;

            if (button.dataset.view) {
                this.currentState.viewMode = button.dataset.view;
                this.dataTable = null;
                this._renderHeader();
                this._updateView();
            } else if (button.id === 'query-btn') {
                if (this.dataTable) this.dataTable.state.pageNum = 1;
                this.currentState.category = this.headerContainer.querySelector('#category-filter').value;
                const deptInput = this.headerContainer.querySelector('#department-filter');
                if (deptInput) this.currentState.department = deptInput.value;
                const planStatusSelect = this.headerContainer.querySelector('#plan-status-filter');
                if (planStatusSelect) this.currentState.planStatus = planStatusSelect.value;
                const resultStatusSelect = this.headerContainer.querySelector('#result-status-filter');
                if (resultStatusSelect) this.currentState.resultStatus = resultStatusSelect.value;
                this._loadData();
            } else if (button.id === 'configure-columns-btn' && this.dataTable) {
                this.dataTable._showColumnConfigModal();
            } else if (button.id === 'export-btn') {
                button.disabled = true;
                button.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 正在导出...';
                try {
                    const params = { ...this.currentState };
                    delete params.pageNum;
                    delete params.pageSize;

                    if (params.viewMode === 'list' && this.dataTable) {
                        params.columns = this.dataTable.columns.filter(c => c.visible).map(({ key, title }) => ({ key, title }));
                    }
                    await exportPointCheck(params);
                } catch (error) {
                    console.error("导出失败:", error);
                    Modal.alert(`导出失败: ${error.message}`);
                } finally {
                    button.disabled = false;
                    button.innerHTML = '<i class="bi bi-download"></i> 导出';
                }
            }
        });

        // --- 内容区事件（委托模式） ---
        this.contentContainer.addEventListener('click', (e) => {
            // 下钻链接
            const drillDownLink = e.target.closest('a.drill-down');
            if (drillDownLink) {
                e.preventDefault();
                this.currentState.department = drillDownLink.dataset.department || '';
                this.currentState.planStatus = drillDownLink.dataset.planStatus || 'all';
                this.currentState.resultStatus = drillDownLink.dataset.resultStatus || 'all';
                this.currentState.viewMode = 'list';
                this.dataTable = null;
                this._renderHeader();
                this._updateView();
            }
        });

        // [核心修复] 使用事件委托处理统计表格的交叉高亮
        this.contentContainer.addEventListener('mouseover', e => {
            if (this.currentState.viewMode !== 'statistics') return;
            const cell = e.target.closest('td');
            if (!cell || !cell.closest('#stats-table')) return;

            const table = cell.closest('table');
            const rowIndex = cell.parentElement.rowIndex;
            const colIndex = cell.cellIndex;

            // 表头有2行, tbody 的 rowIndex 从 2 开始
            if (rowIndex >= 2) {
                // 高亮行
                const rowCells = table.rows[rowIndex].cells;
                for (let i = 0; i < rowCells.length; i++) {
                    rowCells[i].classList.add('cell-highlight');
                }
                // 高亮列
                for (let i = 2; i < table.rows.length; i++) {
                    if (table.rows[i].cells.length > colIndex) {
                        table.rows[i].cells[colIndex].classList.add('cell-highlight');
                    }
                }
            }
        });

        this.contentContainer.addEventListener('mouseout', e => {
            if (this.currentState.viewMode !== 'statistics') return;
            const cell = e.target.closest('td');
            if (!cell || !cell.closest('#stats-table')) return;

            const highlightedCells = this.contentContainer.querySelectorAll('.cell-highlight');
            highlightedCells.forEach(c => c.classList.remove('cell-highlight'));
        });

        // DataTable组件的自定义事件
        this.contentContainer.addEventListener('queryChange', () => {
            if (this.currentState.viewMode === 'list') {
                this._loadData();
            }
        });
    }
}

