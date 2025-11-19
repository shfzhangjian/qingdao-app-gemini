/**
 * 源码路径: js/views/PointCheck.js
 * 功能说明: 点检统计页面的最终版视图逻辑。
 * - 采用了全新的单行、左对齐、胶囊(Pills)风格Tab的UI布局。
 * - 彻底修复了列表视图下反复出现的“无限加载”Bug。
 * 版本变动:
 * v4.4.0 - 2025-11-18: [UI优化] 为统计表格中的数字链接添加主题适配样式（暗色白字，亮色蓝字）。
 * v4.3.0 - 2025-11-17: [修复] 增加下钻时对上半年/下半年的判断，自动更新列表视图的日期范围。
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

        // [修改] 默认日期范围改为读取当前年
        const currentYear = new Date().getFullYear();
        this.currentState = {
            viewMode: 'statistics',
            category: 'all',
            dateRange: `${currentYear}-01-01 至 ${currentYear}-12-31`, // 默认当年
            department: '',
            planStatus: 'all',
            resultStatus: 'all'
        };
    }

    render(container) {
        this.currentState.viewMode = 'statistics';
        this.dataTable = null;

        this.container = container;

        // [新增] 注入局部样式以处理链接颜色主题适配
        const style = document.createElement('style');
        style.textContent = `
            /* 默认（暗色主题）下，下钻链接为白色 */
            .drill-down { 
                color: #fff !important; 
                text-decoration: underline; 
                cursor: pointer;
            }
            .drill-down:hover {
                color: var(--accent-color) !important;
            }
            /* 亮色主题下，下钻链接为蓝色 */
            body.theme-light .drill-down { 
                color: #0d6efd !important; 
            }
            body.theme-light .drill-down:hover {
                color: #0a58ca !important;
            }
        `;
        container.appendChild(style);

        // 主布局容器
        const layoutDiv = document.createElement('div');
        layoutDiv.className = 'p-3 rounded';
        layoutDiv.style.cssText = 'background-color: var(--bg-secondary); display: flex; flex-direction: column; height: 100%;';
        layoutDiv.innerHTML = `
            <div id="point-check-header"></div>
            <div id="point-check-content" style="flex-grow: 1; display: flex; flex-direction: column; min-height: 0;"></div>
        `;
        container.appendChild(layoutDiv);

        this.headerContainer = layoutDiv.querySelector('#point-check-header');
        this.contentContainer = layoutDiv.querySelector('#point-check-content');

        this._renderHeader();
        this._updateView();
        this._attachEventListeners();
    }

    _renderHeader() {
        // ... (省略部分代码)
        // [修改] 确保 flatpickr 实例能正确销毁和重建
        if (this.datePicker && this.datePicker.instance) {
            this.datePicker.instance.destroy();
            this.datePicker = null;
        }

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

        // [修改] 重新初始化 DatePicker
        const dateInput = this.headerContainer.querySelector('#point-check-daterange');
        if (dateInput) {
            this.datePicker = new DatePicker(dateInput, {
                mode: 'range',
                defaultDate: this.currentState.dateRange.split(' 至 '),
                onChange: (selectedDates, dateStr) => {
                    if (selectedDates.length === 2) {
                        this.currentState.dateRange = dateStr;
                    }
                }
            });
        }
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

                // [RACE CONDITION FIX]
                // 检查：如果在等待数据时，用户已经切换回了列表视图，则不要渲染统计数据
                if (this.currentState.viewMode === 'statistics') {
                    this._renderStatisticsView(statsData);
                }

            } else { // viewMode === 'list'
                if (!this.dataTable) this._renderListViewSkeleton();

                // [RACE CONDITION FIX]
                // 必须在 await 之前，用一个局部变量保存当前的 dataTable 实例
                const currentDataTableInstance = this.dataTable;

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

                // [RACE CONDITION FIX]
                // 检查：
                // 1. this.currentState.viewMode 是否仍然是 'list'
                // 2. this.dataTable (当前实例) 是否等于我们发起请求时的实例 (currentDataTableInstance)
                // 3. 实例是否不为 null
                // 如果用户在 await 期间点击了 "统计" 标签，this.dataTable 会变为 null，导致此检查失败，从而安全地跳过 updateView。
                if (this.currentState.viewMode === 'list' && this.dataTable && this.dataTable === currentDataTableInstance) {
                    this.dataTable.updateView(pageResult);
                } else {
                    console.warn("PointCheck: Data loaded, but view has changed. Skipping updateView.");
                }
            }
        } catch (error) {
            // [RACE CONDITION FIX]
            // 只有在当前视图仍然是请求失败的视图时，才显示错误
            if (this.currentState.viewMode === viewMode) {
                console.error("加载点检数据失败:", error);
                this.contentContainer.innerHTML = `<div class="alert alert-danger">数据加载失败: ${error.message}</div>`;
            } else {
                console.warn("A background data load failed, but view has changed. Ignoring error.", error);
            }
        } finally {
            // [RACE CONDITION FIX]
            // 只有在 dataTable 实例仍然存在时才隐藏加载动画
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
                
                <!-- [修改] 为上半年的所有链接添加 data-half="h1" -->
                <td><a href="#" class="drill-down" data-half="h1" data-department="${row.dept}" data-plan-status="all">[${row.yingJianShu1}]</a></td>
                <td><a href="#" class="drill-down" data-half="h1" data-department="${row.dept}" data-plan-status="已检">[${row.yiJianShu1}]</a></td>
                <td><a href="#" class="drill-down" data-half="h1" data-department="${row.dept}" data-plan-status="未检">[${row.weiJianShu1}]</a></td>
                <td>${row.zhiXingLv1}</td>
                <td><a href="#" class="drill-down" data-half="h1" data-department="${row.dept}" data-result-status="正常">[${row.zhengChangShu1}]</a></td>
                <td><a href="#" class="drill-down" data-half="h1" data-department="${row.dept}" data-result-status="异常">[${row.yiChangShu1}]</a></td>

                <!-- [修改] 为下半年的所有链接添加 data-half="h2" -->
                <td><a href="#" class="drill-down" data-half="h2" data-department="${row.dept}" data-plan-status="all">[${row.yingJianShu2}]</a></td>
                <td><a href="#" class="drill-down" data-half="h2" data-department="${row.dept}" data-plan-status="已检">[${row.yiJianShu2}]</a></td>
                <td><a href="#" class="drill-down" data-half="h2" data-department="${row.dept}" data-plan-status="未检">[${row.weiJianShu2}]</a></td>
                <td>${row.zhiXingLv2}</td>
                <td><a href="#" class="drill-down" data-half="h2" data-department="${row.dept}" data-result-status="正常">[${row.zhengChangShu2}]</a></td>
                <td><a href="#" class="drill-down" data-half="h2" data-department="${row.dept}" data-result-status="异常">[${row.yiChangShu2}]</a></td>
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
                configurable: false, // [修改] 列表视图的配置列按钮在Header中，所以这里禁用组件自带的
                storageKey: 'pointCheckListTable',
                selectable: 'none'
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
                // [修改] 切换视图时，重置日期为全年
                const year = this.currentState.dateRange.substring(0, 4);
                this.currentState.dateRange = `${year}-01-01 至 ${year}-12-31`;

                this._renderHeader();
                this._updateView();
            } else if (button.id === 'query-btn') {
                if (this.dataTable) this.dataTable.state.pageNum = 1;
                // [修改] 查询时，从新渲染的 datePicker 实例获取值
                if (this.datePicker) {
                    this.currentState.dateRange = this.datePicker.instance.input.value;
                }
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
                e.stopPropagation(); // 阻止 app.js 的全局路由

                // [修改] 核心逻辑：根据 data-half 更新日期范围
                const half = drillDownLink.dataset.half;
                const year = this.currentState.dateRange.substring(0, 4); // 提取当前年份

                if (half === 'h1') {
                    // 点击了上半年
                    this.currentState.dateRange = `${year}-01-01 至 ${year}-06-30`;
                } else if (half === 'h2') {
                    // 点击了下半年
                    this.currentState.dateRange = `${year}-07-01 至 ${year}-12-31`;
                }
                // 如果没有 data-half 属性（例如点击部门名称），则日期范围保持不变（全年）

                this.currentState.department = drillDownLink.dataset.department || '';
                this.currentState.planStatus = drillDownLink.dataset.planStatus || 'all';
                this.currentState.resultStatus = drillDownLink.dataset.resultStatus || 'all';
                this.currentState.viewMode = 'list';
                this.dataTable = null;

                this._renderHeader(); // [关键] 使用更新后的 currentState 重新渲染头部
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