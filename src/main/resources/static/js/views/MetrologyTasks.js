/**
 * 源码路径: js/views/MetrologyTasks.js
 * 功能说明: 计量任务页面的视图逻辑。
 * - 功能完整，包含查询、分页、筛选、异常标记和Excel导出。
 * 版本变动:
 * v2.1.0 - 2025-10-14: 全面重构，对齐台账页面的所有功能。
 */
import DataTable from '../components/DataTable.js';
import QueryForm from '../components/QueryForm.js';
import Modal from '../components/Modal.js';
import { getMetrologyTasks, exportMetrologyTasks } from '../services/api.js';

export default class MetrologyTasks {
    constructor() {
        this.dataTable = null;
        this.queryForm = null;
        this.container = null;
        this.currentPage = 1;
        this.pageSize = 10;

        this.currentFilters = {
            taskStatus: 'unchecked',
            abcCategory: 'all' // 默认显示所有分类
        };
    }

    render(container) {
        this.container = container;
        container.innerHTML = `
            <div class="p-3 rounded" style="background-color: var(--bg-secondary); display: flex; flex-direction: column; height: 100%;">
                <div id="query-form-container"></div>
                <div id="data-table-container" style="flex-grow: 1; display: flex; flex-direction: column; min-height: 0;"></div>
            </div>
        `;

        this._renderQueryForm(container.querySelector('#query-form-container'));
        this._renderDataTable(container.querySelector('#data-table-container'));

        this._loadData();
        this._attachEventListeners();
    }

    _renderQueryForm(container) {
        this.queryForm = new QueryForm({
            fields: [
                { type: 'text', label: '设备名称', name: 'deviceName', style: 'width: 180px;' },
                { type: 'text', label: '企业编号', name: 'enterpriseId', style: 'width: 180px;' },
                { type: 'daterange', label: '时间范围', name: 'dateRange', style: 'width: 240px;' },
            ]
        });
        container.innerHTML = `<div class="d-flex flex-wrap align-items-center gap-3 mb-3 p-3 rounded" style="background-color: var(--bg-primary);">${this.queryForm._createFieldsHtml()}</div>`;
        this.queryForm.container = container;
        this.queryForm._initializeDatePickers();
    }

    _renderDataTable(container) {
        const columns = [
            // --- 核心显示字段 ---
            { key: 'date', title: '任务时间', visible: true, width: 120 },
            { key: 'pointCheckStatus', title: '点检状态', visible: true, width: 100 },
            { key: 'enterpriseId', title: '企业编号', visible: true, width: 120 },
            { key: 'deviceName', title: '设备名称', visible: true, width: 180 },
            { key: 'model', title: '规格型号', visible: true, width: 120 },
            { key: 'factoryId', title: '出厂编号', visible: true, width: 120 },
            { key: 'location', title: '安装位置/使用人', visible: true, width: 150 },
            { key: 'accuracy', title: '准确度等级', visible: true, width: 120 },
            { key: 'status', title: '设备状态', visible: true, width: 90 },
            { key: 'abc', title: 'ABC分类', visible: true, width: 90 },

            // --- 默认隐藏字段 ---
            { key: 'erpId', title: 'ERP编号', visible: false },
            { key: 'range', title: '量程范围', visible: false },
        ];

        const actions = [
            { name: 'search', text: '查询', class: 'btn-primary' },
            { name: 'submit', text: '提交', class: 'btn-outline-secondary' },
            { name: 'markAbnormal', text: '异常标记', class: 'btn-outline-warning' },
            { name: 'export', text: '导出', class: 'btn-outline-success' },
        ];

        const filters = [
            { type: 'pills', label: '任务状态', name: 'taskStatus', options: [{label: '未检', value: 'unchecked', checked: true}, {label: '已检', value: 'checked'}, {label: '异常', value: 'abnormal'}, {label: '全部', value: 'all'}] },
            { type: 'pills', label: 'ABC分类', name: 'abcCategory', options: [{label: '全部', value: 'all', checked: true}, {label: 'A', value: 'a'}, {label: 'B', value: 'b'}, {label: 'C', value: 'c'}] }
        ];

        this.dataTable = new DataTable({
            columns, actions, filters, data:[],
            options: {
                configurable: true,
                storageKey: 'metrologyTasksTable',
                selectable: 'multiple',
                getRowClass: (row) => row.isAbnormal ? 'table-row-abnormal' : ''
            }
        });

        this.dataTable.render(container);
    }

    async _loadData() {
        const tbody = this.dataTable.container.querySelector('tbody');
        if (!tbody) return;

        const visibleCols = this.dataTable._getVisibleColumns().length;
        tbody.innerHTML = `<tr><td colspan="${visibleCols}" class="text-center p-4"><div class="spinner-border spinner-border-sm"></div> 正在加载...</td></tr>`;

        try {
            const params = {
                ...this.queryForm.getValues(),
                ...this.currentFilters,
                pageNum: this.currentPage,
                pageSize: this.pageSize,
            };
            const pageResult = await getMetrologyTasks(params);
            this.dataTable.updateView(pageResult);
        } catch (error) {
            console.error("加载计量任务失败:", error);
            tbody.innerHTML = `<tr><td colspan="${visibleCols}" class="text-center p-4 text-danger">加载数据失败: ${error.message}</td></tr>`;
        }
    }

    _showAbnormalWorkOrderModal(rowData) {
        const bodyHtml = `
            <form>
                <div class="mb-3">
                    <label class="form-label">企业编号</label>
                    <input type="text" class="form-control" value="${rowData.enterpriseId}" readonly>
                </div>
                <div class="mb-3">
                    <label class="form-label">计量设备名称</label>
                    <input type="text" class="form-control" value="${rowData.deviceName}" readonly>
                </div>
                <div class="mb-3">
                    <label for="abnormal-description" class="form-label">异常描述</label>
                    <textarea class="form-control" id="abnormal-description" rows="3"></textarea>
                </div>
            </form>
        `;

        const footerHtml = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" id="confirm-work-order">确认生成异常工单</button>
        `;

        const modal = new Modal({
            title: '生成异常工单',
            body: bodyHtml,
            footer: footerHtml
        });

        modal.modalElement.querySelector('#confirm-work-order').addEventListener('click', () => {
            const description = modal.modalElement.querySelector('#abnormal-description').value;
            if (!description.trim()) {
                Modal.alert("请输入异常描述。");
                return;
            }
            console.log('生成异常工单 (模拟):', { device: rowData, description });
            Modal.alert('异常工单已生成 (模拟)');
            modal.hide();
        });

        modal.show();
    }

    _attachEventListeners() {
        const tableContainer = this.container.querySelector('#data-table-container');
        if (!tableContainer) return;

        tableContainer.addEventListener('click', async (e) => {
            const button = e.target.closest('button[data-action]');
            if (!button) return;

            const action = button.dataset.action;

            if (action === 'search') {
                this.currentPage = 1;
                this._loadData();
            } else if (action === 'markAbnormal') {
                const selectedRows = tableContainer.querySelectorAll('tbody tr.table-active-custom');
                if (selectedRows.length !== 1) {
                    Modal.alert(selectedRows.length > 1 ? '异常情况不支持批量提交' : '请选择一行以标记异常');
                    return;
                }
                const selectedRowId = selectedRows[0].dataset.rowId;
                const rowData = this.dataTable.data.find(item => item.id == selectedRowId);
                if (rowData) {
                    this._showAbnormalWorkOrderModal(rowData);
                } else {
                    Modal.alert('无法找到所选行的数据。');
                }
            } else if (action === 'export') {
                button.disabled = true;
                button.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 正在导出...';
                try {
                    const params = {
                        ...this.queryForm.getValues(),
                        ...this.currentFilters,
                        columns: this.dataTable.columns
                            .filter(col => col.visible)
                            .map(({ key, title }) => ({ key, title }))
                    };
                    await exportMetrologyTasks(params);
                } catch (error) {
                    console.error("导出失败:", error);
                    Modal.alert(`导出失败: ${error.message}`);
                } finally {
                    button.disabled = false;
                    button.innerHTML = '导出';
                }
            }
        });

        tableContainer.addEventListener('change', (e) => {
            const radio = e.target.closest('input[type="radio"]');
            if (radio && this.currentFilters.hasOwnProperty(radio.name)) {
                this.currentFilters[radio.name] = radio.value;
                this.currentPage = 1;
                this._loadData();
            }
        });

        tableContainer.addEventListener('pageChange', (e) => {
            this.currentPage = e.detail.pageNum;
            this._loadData();
        });
    }
}

