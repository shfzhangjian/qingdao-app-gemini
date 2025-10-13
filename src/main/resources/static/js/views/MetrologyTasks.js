/**
 * 源码路径: js/views/MetrologyTasks.js
 * 功能说明: 计量任务页面的视图逻辑。
 * 负责组装QueryForm和DataTable组件来构建完整的页面。
 * 版本变动:
 * v1.0.0 - 2025-10-13: 初始版本，使用组件构建UI。
 * v1.1.0 - 2025-10-13: 适配v2.2.0的DataTable组件，将工具栏配置移入DataTable。
 */
import DataTable from '../components/DataTable.js';
import QueryForm from '../components/QueryForm.js';
import Modal from '../components/Modal.js';

function generateMetrologyTasksData() {
    const tasks = [
        { id: 1, date: '2025-8-23', enterpriseId: '01200002', erpId: 'JL009219', deviceName: '电子台秤', model: 'TCS-B', factoryId: '1339744', range: '0-1000kg' },
        { id: 2, date: '2025-8-22', enterpriseId: '01200003', erpId: 'JL009220', deviceName: '电子台秤', model: 'TCS-B', factoryId: '234422', range: '0.4kg-60kg' },
        { id: 3, date: '2025-8-21', enterpriseId: '01200004', erpId: 'JL009221', deviceName: '电子台秤', model: 'TCS-B', factoryId: '223421', range: '0-1000kg' },
        { id: 4, date: '2025-8-20', enterpriseId: '01200005', erpId: 'JL009222', deviceName: '压力计', model: 'YK-100', factoryId: '5582910', range: '0-1.6MPa' },
        { id: 5, date: '2025-8-19', enterpriseId: '01200006', erpId: 'JL009223', deviceName: '温度计', model: 'WSS-411', factoryId: '9821345', range: '-40-600℃' }
    ];
    return tasks;
}


export default class MetrologyTasks {
    constructor() {
        this.data = generateMetrologyTasksData();
        this.dataTable = null;
        this.queryForm = null;
    }

    render(container, footerContainer) {
        container.innerHTML = `
             <div id="breadcrumb-container" class="mb-3"></div>
            <div class="p-3 rounded" style="background-color: var(--bg-dark-secondary); display: flex; flex-direction: column; flex-grow: 1;">
                <div id="query-form-container"></div>
                <div id="data-table-container" style="flex-grow: 1; display: flex; flex-direction: column;"></div>
            </div>
        `;

        this._renderQueryForm(container.querySelector('#query-form-container'));
        this._renderDataTable(container.querySelector('#data-table-container'));
        this._renderFooter(footerContainer);

        this._attachEventListeners();
    }

    _renderQueryForm(container) {
        this.queryForm = new QueryForm({
            fields: [
                { type: 'text', label: '计量设备名称', name: 'deviceName', style: 'width: 150px;' },
                { type: 'text', label: '企业编号', name: 'enterpriseId', style: 'width: 150px;' },
                { type: 'text', label: '时间范围', name: 'dateRange', style: 'width: 200px;', defaultValue: '2025-06-01 — 2025-06-30' },
                { type: 'pills', label: '任务类型', name: 'taskType', options: [{label: '未检', value: 'unchecked', checked: true}, {label: '已检', value: 'checked'}] },
                { type: 'pills', label: 'ABC分类', name: 'abcCategory', options: [{label: 'A', value: 'a'}, {label: 'B', value: 'b', checked: true}, {label: 'C', value: 'c'}] }
            ]
        });

        // This view uses a different layout for the query form, so we render it directly
        // instead of calling the component's render method.
        container.innerHTML = `<div class="d-flex flex-wrap align-items-center gap-3 mb-3 p-3 rounded" style="background-color: var(--bg-dark-primary);">${this.queryForm._createFieldsHtml()}</div>`;

    }

    _renderDataTable(container) {
        const columns = [
            { key: 'selection', title: '<input type="checkbox" class="form-check-input" data-select="all">', visible: true, render: (val, row) => `<input type="checkbox" class="form-check-input" data-row-id="${row.id}">` },
            { key: 'date', title: '任务时间', visible: true },
            { key: 'enterpriseId', title: '企业编号', visible: true },
            { key: 'erpId', title: 'ERP编号', visible: true },
            { key: 'deviceName', title: '计量设备名称', visible: true },
            { key: 'model', title: '规格型号', visible: true },
            { key: 'factoryId', title: '出厂编号', visible: true },
            { key: 'range', title: '量程范围', visible: true },
        ];

        const actions = [
            { name: 'search', text: '查询', class: 'btn-primary' },
            { name: 'submit', text: '提交', class: 'btn-outline-secondary' },
            { name: 'export', text: '导出', class: 'btn-outline-success' },
        ];

        this.dataTable = new DataTable({
            columns,
            data: this.data,
            actions,
            options: {
                configurable: true,
                storageKey: 'metrologyTasksTable'
            }
        });

        this.dataTable.render(container);
    }

    _renderFooter(container) {
        container.innerHTML = `
            <nav class="d-flex justify-content-end">
                <ul class="pagination pagination-sm mb-0">
                    <li class="page-item disabled"><a class="page-link" href="#">&laquo;</a></li>
                    <li class="page-item active"><a class="page-link" href="#">1</a></li>
                    <li class="page-item"><a class="page-link" href="#">2</a></li>
                    <li class="page-item"><a class="page-link" href="#">3</a></li>
                    <li class="page-item"><a class="page-link" href="#">&raquo;</a></li>
                </ul>
            </nav>
        `;
    }

    _attachEventListeners() {
        const tableContainer = document.getElementById('data-table-container');
        if (!tableContainer) return;

        tableContainer.addEventListener('click', e => {
            const button = e.target.closest('button[data-action]');
            if (button) {
                const action = button.dataset.action;
                if (action === 'search') {
                    const values = this.queryForm.getValues();
                    console.log("Searching with:", values);
                    Modal.alert("执行查询（模拟）");
                }
                return;
            }

            const target = e.target;
            if (target.matches('input[type="checkbox"]')) {
                if (target.dataset.select === 'all') {
                    tableContainer.querySelectorAll('input[data-row-id]').forEach(cb => {
                        cb.checked = target.checked;
                        cb.closest('tr').classList.toggle('table-active-custom', target.checked);
                    });
                } else if (target.dataset.rowId) {
                    target.closest('tr').classList.toggle('table-active-custom', target.checked);
                }
            } else {
                const row = target.closest('tr');
                if (row && row.parentElement.tagName === 'TBODY') {
                    const checkbox = row.querySelector('input[type="checkbox"]');
                    if (checkbox) {
                        checkbox.checked = !checkbox.checked;
                        row.classList.toggle('table-active-custom', checkbox.checked);
                    }
                }
            }
        });
    }
}

