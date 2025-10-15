/**
 * 源码路径: js/views/MetrologyTasks.js
 * 功能说明: 计量任务页面的视图逻辑。
 * - 功能完整，包含查询、分页、筛选、异常标记和Excel导出。
 * 版本变动:
 * v3.3.0 - 2025-10-15: [UI重构] 将查询表单调整为响应式单行五列布局。
 */
import DataTable from '../components/Optimized_DataTable.js';
import QueryForm from '../components/QueryForm.js';
import Modal from '../components/Modal.js';
import { getMetrologyTasks, exportMetrologyTasks } from '../services/api.js';

export default class MetrologyTasks {
    constructor() {
        this.dataTable = null;
        this.queryForm = null;
        this.container = null;
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

    /**
     * [核心修改] 将所有筛选条件统一到此查询表单中
     */
    _renderQueryForm(container) {
        const formFields = [
            { type: 'daterange', label: '时间范围', name: 'dateRange', containerClass: 'col-12 col-lg' },
            { type: 'text', label: '设备名称', name: 'deviceName', containerClass: 'col-12 col-lg' },
            { type: 'text', label: '企业编号', name: 'enterpriseId', containerClass: 'col-12 col-lg' },
            { type: 'pills', label: '任务状态', name: 'taskStatus', options: [{label: '未检', value: 'unchecked', checked: true}, {label: '已检', value: 'checked'}, {label: '异常', value: 'abnormal'}, {label: '全部', value: 'all'}], containerClass: 'col-12 col-lg' },
            { type: 'pills', label: 'ABC分类', name: 'abcCategory', options: [{label: '全部', value: 'all', checked: true}, {label: 'A', value: 'a'}, {label: 'B', 'value': 'b'}, {label: 'C', value: 'c'}], containerClass: 'col-12 col-lg' }
        ];

        this.queryForm = new QueryForm({ fields: formFields });
        // 使用 Bootstrap grid system 来实现灵活布局
        container.innerHTML = `<div class="p-3 rounded mb-3" style="background-color: var(--bg-primary);"><div class="row g-3 align-items-center">${this.queryForm._createFieldsHtml()}</div></div>`;
        this.queryForm.container = container;
        this.queryForm._initializeDatePickers();
    }

    _renderDataTable(container) {
        const columns = [
            // --- 核心显示字段 ---
            { key: 'date', title: '任务时间', visible: true, width: 120, sortable: true },
            { key: 'pointCheckStatus', title: '点检状态', visible: true, width: 100 },
            { key: 'enterpriseId', title: '企业编号', visible: true, width: 120, sortable: true },
            { key: 'deviceName', title: '设备名称', visible: true, width: 180 },
            { key: 'model', title: '规格型号', visible: true, width: 120 },
            { key: 'factoryId', title: '出厂编号', visible: true, width: 120, sortable: true },
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

        // [核心修改] 移除表格内部的筛选器
        const filters = [];

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
        this.dataTable.toggleLoading(true);

        try {
            // [核心修改] 所有查询参数现在都来自 queryForm
            const formParams = this.queryForm.getValues();
            const tableState = this.dataTable.state;

            const params = {
                ...formParams,
                pageNum: tableState.pageNum,
                pageSize: tableState.pageSize,
                sortBy: tableState.sortBy,
                sortOrder: tableState.sortOrder,
            };

            const pageResult = await getMetrologyTasks(params);
            this.dataTable.updateView(pageResult);
        } catch (error) {
            console.error("加载计量任务失败:", error);
            Modal.alert(`加载数据失败: ${error.message}`);
        } finally {
            this.dataTable.toggleLoading(false);
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

    /**
     * [核心修改] 移除对表格内部筛选器 'change' 事件的监听
     */
    _attachEventListeners() {
        const tableContainer = this.container.querySelector('#data-table-container');
        if (!tableContainer) return;

        tableContainer.addEventListener('click', async (e) => {
            const button = e.target.closest('button[data-action]');
            if (!button) return;

            const action = button.dataset.action;

            if (action === 'search') {
                this.dataTable.state.pageNum = 1;
                this._loadData();
            } else if (action === 'markAbnormal') {
                const selectedTrs = tableContainer.querySelectorAll('tbody tr.table-active-custom');
                const selectedRowIds = [...new Set(Array.from(selectedTrs).map(tr => tr.dataset.rowId))];

                if (selectedRowIds.length !== 1) {
                    Modal.alert('请选择一行以标记异常');
                    return;
                }

                const selectedRowId = selectedRowIds[0];
                const rowData = this.dataTable.data.find(item => String(item.id) === selectedRowId);

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
                        sortBy: this.dataTable.state.sortBy,
                        sortOrder: this.dataTable.state.sortOrder,
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

        tableContainer.addEventListener('queryChange', () => {
            this._loadData();
        });
    }
}

