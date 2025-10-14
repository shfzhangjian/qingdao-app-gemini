/**
 * 源码路径: js/views/PointCheck.js
 * 功能说明: 点检统计页面的最终版视图逻辑。
 * - 采用了全新的单行、左对齐、胶囊(Pills)风格Tab的UI布局。
 * - 彻底修复了列表视图下反复出现的“无限加载”Bug。
 * 版本变动:
 * v3.5.0 - 2025-10-14: 完成最终UI布局重构和“无限加载”Bug的修复。
 */
import DataTable from '../components/DataTable.js';
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

        this.currentPage = 1;
        this.pageSize = 10;

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
                <!-- Left Section: Tabs and Filters -->
                <div class="d-flex align-items-center gap-3 flex-wrap">
                    <ul class="nav nav-pills">
                        <li class="nav-item">
                            <button class="nav-link ${viewMode === 'statistics' ? 'active' : ''} py-1" data-view="statistics">统计</button>
                        </li>
                        <li class="nav-item">
                            <button class="nav-link ${isListMode ? 'active' : ''} py-1" data-view="list">列表</button>
                        </li>
                    </ul>
                    
                    <input type="text" id="point-check-daterange" class="form-control form-control-sm" style="width: 240px;" placeholder="时间范围">
                    <select id="category-filter" class="form-select form-select-sm" style="width: 100px;">
                        <option value="all" ${category === 'all' ? 'selected' : ''}>全部类型</option>
                        <option value="A" ${category === 'A' ? 'selected' : ''}>A类</option>
                        <option value="B" ${category === 'B' ? 'selected' : ''}>B类</option>
                        <option value="C" ${category === 'C' ? 'selected' : ''}>C类</option>
                    </select>

                    <!-- List-only filters -->
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

                <!-- Right Section: Actions -->
                <div class="d-flex align-items-center gap-2">
                    <button class="btn btn-sm btn-primary" id="query-btn"><i class="bi bi-search"></i> 查询</button>
                    <button class="btn btn-sm btn-outline-success" id="export-btn"><i class="bi bi-download"></i> 导出</button>
                    <button class="btn btn-sm btn-outline-secondary" id="configure-columns-btn" ${!isListMode ? 'disabled' : ''}>
                        <i class="bi bi-gear"></i> 配置列
                    </button>
                </div>
            </div>
        `;

        if (this.datePicker) this.datePicker.destroy();
        const dateInput = this.headerContainer.querySelector('#point-check-daterange');
        this.datePicker = new DatePicker(dateInput, {
            mode: 'range',
            defaultDate: this.currentState.dateRange.split(' to '),
            onChange: (selectedDates, dateStr) => {
                if (selectedDates.length === 2) {
                    this.currentState.dateRange = dateStr;
                }
            }
        });
    }

    _updateView() {
        if (this.currentState.viewMode === 'list') {
            this._renderListViewSkeleton();
        }
        this._loadData();
    }

    async _loadData() {
        try {
            const params = {
                category: this.currentState.category,
                dateRange: this.currentState.dateRange,
                pageNum: this.currentPage,
                pageSize: this.pageSize
            };

            if (this.currentState.viewMode === 'list') {
                params.department = this.currentState.department;
                params.planStatus = this.currentState.planStatus;
                params.resultStatus = this.currentState.resultStatus;
            }

            if (this.currentState.viewMode === 'statistics') {
                this.contentContainer.innerHTML = `<div class="d-flex justify-content-center align-items-center h-100"><div class="spinner-border" role="status"></div></div>`;
                const statsData = await getPointCheckStatistics(params);
                this._renderStatisticsView(statsData);
            } else {
                if (!this.dataTable) {
                    this._renderListViewSkeleton();
                }

                const tbody = this.dataTable.container.querySelector('tbody');
                if (tbody) {
                    const visibleCols = this.dataTable._getVisibleColumns().length;
                    tbody.innerHTML = `<tr><td colspan="${visibleCols}" class="text-center p-4"><div class="spinner-border spinner-border-sm"></div> 正在加载...</td></tr>`;
                }
                const pageResult = await getPointCheckList(params);
                this.dataTable.updateView(pageResult);
            }
        } catch (error) {
            console.error("加载点检数据失败:", error);
            this.contentContainer.innerHTML = `<div class="alert alert-danger">数据加载失败: ${error.message}</div>`;
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
                <table class="table table-bordered table-sm text-center">
                  <thead>
                      <tr>
                          <th rowspan="2" class="align-middle">部门名称</th>
                          <th colspan="6">上半年点检情况</th>
                          <th colspan="6">下半年点检情况</th>
                      </tr>
                      <tr>
                          <th>应检数量</th>
                          <th>已检数量</th>
                          <th>未检数量</th>
                          <th>执行率</th>
                          <th>正常数量</th>
                          <th>异常数量</th>
                          <th>应检数量</th>
                          <th>已检数量</th>
                          <th>未检数量</th>
                          <th>执行率</th>
                          <th>正常数量</th>
                          <th>异常数量</th>
                      </tr>
                  </thead>
                   <tbody>${tableRows}</tbody>
                </table>
            </div>
        `;
    }

    _renderListViewSkeleton() {
        if (this.dataTable) return;
        const columns = [
            { key: 'department', title: '部门', visible: true },
            { key: 'deviceName', title: '设备名称', visible: true },
            { key: 'deviceType', title: '类型', visible: true },
            { key: 'checkDate', title: '日期', visible: true },
            { key: 'planStatus', title: '计划状态', visible: true },
            { key: 'resultStatus', title: '检查结果', visible: true, render: (val) => val === '异常' ? `<span class="text-danger">${val}</span>` : val },
        ];
        this.dataTable = new DataTable({
            columns, data: [],
            actions: [],
            options: { configurable: false, storageKey: 'pointCheckListNew' }
        });
        this.dataTable.render(this.contentContainer);
    }

    _attachEventListeners() {
        this.headerContainer.addEventListener('click', (e) => {
            const button = e.target.closest('button');
            if (!button) return;

            if (button.dataset.view) {
                this.currentPage = 1;
                this.currentState.viewMode = button.dataset.view;
                this.dataTable = null;
                this._renderHeader();
                this._updateView();
            } else if (button.id === 'query-btn') {
                this.currentPage = 1;
                this.currentState.category = this.headerContainer.querySelector('#category-filter').value;
                const deptInput = this.headerContainer.querySelector('#department-filter');
                const planStatusSelect = this.headerContainer.querySelector('#plan-status-filter');
                const resultStatusSelect = this.headerContainer.querySelector('#result-status-filter');

                this.currentState.department = deptInput ? deptInput.value : '';
                this.currentState.planStatus = planStatusSelect ? planStatusSelect.value : 'all';
                this.currentState.resultStatus = resultStatusSelect ? resultStatusSelect.value : 'all';
                this._loadData();
            } else if (button.id === 'configure-columns-btn' && this.dataTable) {
                this.dataTable._showColumnConfigModal();
            } else if (button.id === 'export-btn') {
                // ... Export logic
            }
        });

        this.contentContainer.addEventListener('click', (e) => {
            const drillDownLink = e.target.closest('a.drill-down');
            if (drillDownLink) {
                e.preventDefault();
                this.currentState.department = drillDownLink.dataset.department || '';
                this.currentState.planStatus = drillDownLink.dataset.planStatus || 'all';
                this.currentState.resultStatus = drillDownLink.dataset.resultStatus || 'all';

                this.currentState.viewMode = 'list';
                this.currentPage = 1;
                this.dataTable = null;
                this._renderHeader();
                this._updateView();
            }
        });

        this.contentContainer.addEventListener('pageChange', (e) => {
            if (this.currentState.viewMode === 'list') {
                this.currentPage = e.detail.pageNum;
                this._loadData();
            }
        });
    }
}

