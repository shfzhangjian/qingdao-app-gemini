/**
 * 源码路径: js/views/PointCheck.js
 * 功能说明: 点检统计页面的视图逻辑。
 * - 实现了统计视图和列表视图的切换。
 * - 实现了A/B/C设备分类的数据切换。
 * - 集成了日期范围选择器。
 * 版本变动:
 * v2.3.0 - 2025-10-13: 调整工具栏布局，并将“配置列”功能外置到主工具栏。
 */
import DataTable from '../components/DataTable.js';
import DatePicker from '../components/DatePicker.js';

// --- 模拟数据生成 ---
function generateStatisticsData(category) {
    const departments = ['卷材一厂', '制丝车间', '卷包车间', '能源动力处(动力车间)', '行政管理处', '安全保卫处', '工艺质量处', '生产供应处'];
    const multiplier = category === 'A' ? 1.5 : (category === 'B' ? 1 : 0.5);

    return departments.map(dept => {
        const yingJian = Math.floor(Math.random() * 2000 * multiplier);
        const isFull = Math.random() > 0.3;
        const yiJian = isFull ? yingJian : Math.floor(yingJian * Math.random());
        const zhiXingLv = yingJian > 0 ? Math.round((yiJian / yingJian) * 100) + '%' : '0%';
        return {
            dept,
            yingJian1: `[${yingJian}]`,
            yiJian1: `[${yiJian}]`,
            zhiXingLv1: zhiXingLv,
            zhengChang1: `[${yiJian}]`,
            yingJian2: '[0]',
            yiJian2: '[0]',
            zhiXingLv2: '0%',
            zhengChang2: '[0]',
        };
    });
}

function generateListData(category) {
    const data = [];
    const statsData = generateStatisticsData(category);
    let idCounter = 1;
    statsData.forEach(stat => {
        const count = parseInt(stat.yingJian1.replace(/\[|\]/g, ''), 10);
        for (let i = 0; i < 5; i++) {
            if (i >= count) break;
            data.push({
                id: idCounter++,
                department: stat.dept,
                deviceName: `${stat.dept}的设备-${i + 1}`,
                deviceType: `型号-${category}${i}`,
                checkDate: `2025-06-${Math.floor(Math.random() * 30) + 1}`,
                status: Math.random() > 0.2 ? '正常' : '异常',
                checker: '张三'
            });
        }
    });
    return data;
}


export default class PointCheck {
    constructor() {
        this.currentView = 'statistics';
        this.activeCategory = 'A';
        this.container = null;
        this.dataTable = null; // Store the instance of DataTable
    }

    render(container) {
        this.container = container;
        container.innerHTML = `
            <div class="p-3 rounded" style="background-color: var(--bg-dark-secondary); display: flex; flex-direction: column; height: 100%;">
                <div id="point-check-header"></div>
                <div id="point-check-content" style="flex-grow: 1; display: flex; flex-direction: column; min-height: 0;"></div>
            </div>`;

        this._renderHeader();
        this._renderContent();
        this._attachHeaderEventListeners();
    }

    _renderHeader() {
        const headerContainer = this.container.querySelector('#point-check-header');
        const isListMode = this.currentView === 'list';

        headerContainer.innerHTML = `
            <div class="d-flex justify-content-between align-items-center mb-3">
                <div class="d-flex align-items-center gap-3">
                    <span>时间范围:</span>
                    <input type="text" id="point-check-daterange" class="form-control" style="width: 250px;">
                    <div id="category-pills" class="btn-group" role="group">
                        <button type="button" class="btn btn-sm btn-outline-secondary ${this.activeCategory === 'A' ? 'active' : ''}" data-category="A">A类</button>
                        <button type="button" class="btn btn-sm btn-outline-secondary ${this.activeCategory === 'B' ? 'active' : ''}" data-category="B">B类</button>
                        <button type="button" class="btn btn-sm btn-outline-secondary ${this.activeCategory === 'C' ? 'active' : ''}" data-category="C">C类</button>
                    </div>
                </div>
                <div class="ms-auto d-flex gap-2">
                    <div id="view-switcher" class="btn-group btn-group-sm" role="group">
                         <button type="button" class="btn btn-sm btn-outline-secondary ${this.currentView === 'statistics' ? 'active' : ''}" data-view="statistics">统计</button>
                         <button type="button" class="btn btn-sm btn-outline-secondary ${isListMode ? 'active' : ''}" data-view="list">列表</button>
                    </div>
                    <button class="btn btn-sm btn-primary" id="query-btn">查询</button>
                    <button class="btn btn-sm btn-outline-success">导出</button>
                    <button class="btn btn-sm btn-outline-info" id="configure-columns-btn" ${!isListMode ? 'disabled' : ''}>
                        <i class="bi bi-gear"></i> 配置列
                    </button>
                </div>
            </div>
        `;

        new DatePicker(headerContainer.querySelector('#point-check-daterange'), { defaultDate: ["2025-06-01", "2025-06-30"] });
    }

    _renderContent() {
        if (this.currentView === 'statistics') {
            this._renderStatisticsView();
        } else {
            this._renderListView();
        }
    }

    _renderStatisticsView() {
        const contentContainer = this.container.querySelector('#point-check-content');
        const statsData = generateStatisticsData(this.activeCategory);
        const tableRows = statsData.map(row => `
            <tr>
                <td class="text-start ps-3">${row.dept}</td>
                <td>${row.yingJian1}</td><td>${row.yiJian1}</td><td>${row.zhiXingLv1}</td><td>${row.zhengChang1}</td>
                <td>${row.yingJian2}</td><td>${row.yiJian2}</td><td>${row.zhiXingLv2}</td><td>${row.zhengChang2}</td>
            </tr>
        `).join('');

        contentContainer.innerHTML = `
            <div class="table-responsive">
                <table class="table table-bordered table-sm text-center">
                  <thead>
                      <tr>
                          <th rowspan="2" class="align-middle">部门名称</th>
                          <th colspan="4">第一次盘点合格数(1-6月)</th>
                          <th colspan="4">第二次盘点合格数(7-12月)</th>
                      </tr>
                      <tr>
                          <th>应检数量</th><th>已检数量</th><th>执行率</th><th>正常数量</th>
                          <th>应检数量</th><th>已检数量</th><th>执行率</th><th>正常数量</th>
                      </tr>
                  </thead>
                   <tbody>
                    ${tableRows}
                   </tbody>
                </table>
            </div>
        `;
    }

    _renderListView() {
        const contentContainer = this.container.querySelector('#point-check-content');
        contentContainer.innerHTML = '';

        const listData = generateListData(this.activeCategory);
        const columns = [
            { key: 'department', title: '部门', visible: true },
            { key: 'deviceName', title: '设备名称', visible: true },
            { key: 'deviceType', title: '设备型号', visible: true },
            { key: 'checkDate', title: '点检日期', visible: true },
            { key: 'status', title: '状态', visible: true, render: (val) => val === '异常' ? `<span class="text-danger">${val}</span>` : val },
            { key: 'checker', title: '点检人', visible: true },
        ];

        // 【核心修改】隐藏DataTable内部的工具栏
        this.dataTable = new DataTable({
            columns,
            data: listData,
            actions: [], // Empty actions
            pagination: true,
            options: {
                configurable: false, // Disable internal config button
                storageKey: `pointCheckListTable_${this.activeCategory}`
            }
        });

        this.dataTable.render(contentContainer);
    }

    _attachHeaderEventListeners() {
        const header = this.container.querySelector('#point-check-header');

        header.addEventListener('click', (e) => {
            const button = e.target.closest('button');
            if (!button) return;

            if (button.dataset.category) {
                this.activeCategory = button.dataset.category;
                this.render(this.container);
            } else if (button.dataset.view) {
                this.currentView = button.dataset.view;
                this.render(this.container);
            } else if (button.id === 'query-btn') {
                console.log(`Querying for category ${this.activeCategory} in view ${this.currentView}...`);
                this.render(this.container);
            } else if (button.id === 'configure-columns-btn') {
                // 【核心修改】从外部调用DataTable的配置方法
                if (this.currentView === 'list' && this.dataTable) {
                    this.dataTable._showColumnConfigModal();
                }
            }
        });

        header.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' && e.target.matches('input')) {
                e.preventDefault();
                header.querySelector('#query-btn').click();
            }

            if (['ArrowUp', 'ArrowDown', 'ArrowLeft', 'ArrowRight'].includes(e.key)) {
                e.preventDefault();
                const focusable = Array.from(header.querySelectorAll('input, button'));
                const currentIndex = focusable.indexOf(document.activeElement);

                if (currentIndex === -1) {
                    focusable[0]?.focus();
                    return;
                }

                let nextIndex;
                if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
                    nextIndex = (currentIndex + 1) % focusable.length;
                } else {
                    nextIndex = (currentIndex - 1 + focusable.length) % focusable.length;
                }
                focusable[nextIndex]?.focus();
            }
        });
    }
}

