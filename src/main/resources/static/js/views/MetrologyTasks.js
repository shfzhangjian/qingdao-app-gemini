/**
 * 源码路径: js/views/MetrologyTasks.js
 * 功能说明: 计量任务页面的视图逻辑 (已适配真实数据)
 * 版本变动:
 * v4.1.0 - 2025-11-18: [新增] 设备名称和企业编号字段升级为可补全下拉框。
 * @version 4.0.0 - 2025-10-15
 */
import DataTable from '../components/Optimized_DataTable.js';
import QueryForm from '../components/QueryForm.js';
import Modal from '../components/Modal.js';
import { getMetrologyTasks, exportMetrologyTasks, updateMetrologyTasks, getMetrologyTaskOptions } from '../services/api.js';

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

    _formatDate(dateString) {
        if (!dateString) return '-';
        const date = new Date(dateString);
        if (isNaN(date.getTime())) {
            return '-';
        }
        return date.toLocaleDateString();
    }


    _renderQueryForm(container) {
        // [修改] 将 'text' 改为 'autocomplete' 并提供 dataSource
        const formFields = [
            { type: 'daterange', label: '确认时间范围', name: 'dateRange', containerClass: 'col-12 col-md-4' },
            {
                type: 'autocomplete',
                label: '设备名称',
                name: 'deviceName',
                containerClass: 'col-12 col-md-4',
                dataSource: () => getMetrologyTaskOptions('deviceName') // 绑定后端 API
            },
            {
                type: 'autocomplete',
                label: '企业编号',
                name: 'enterpriseId',
                containerClass: 'col-12 col-md-4',
                dataSource: () => getMetrologyTaskOptions('enterpriseId') // 绑定后端 API
            },
        ];

        this.queryForm = new QueryForm({ fields: formFields });
        container.innerHTML = `<div class="p-3 rounded mb-3" style="background-color: var(--bg-primary);"><div class="row g-3 align-items-center">${this.queryForm._createFieldsHtml()}</div></div>`;
        this.queryForm.container = container; // Hack: bind container manually before initializing inputs
        this.queryForm._initializeDatePickers();
        this.queryForm._initializeAutocompletes(); // 手动调用初始化 (虽然 render 里已调用，但因为我们分两步，这里确保安全)
    }

    _renderDataTable(container) {
        const columns = [
            { key: 'dinit', title: '生成任务时间', visible: true, width: 120, sortable: true, render: (val) => this._formatDate(val) },
            { key: 'date', title: '确认时间', visible: true, width: 120, sortable: true, render: (val) => this._formatDate(val) },
            { key: 'pointCheckStatus', title: '点检状态', visible: true, width: 100 },
            { key: 'enterpriseId', title: '企业编号', visible: true, width: 120, sortable: true },
            { key: 'deviceName', title: '设备名称', visible: true, width: 180 },
            { key: 'model', title: '规格型号', visible: true, width: 120 },
            { key: 'factoryId', title: '出厂编号', visible: true, width: 120, sortable: true },
            { key: 'slc', title: '量程范围', visible: true, width: 90 },
            { key: 'location', title: '安装位置/使用人', visible: true, width: 150 },
            { key: 'accuracy', title: '准确度等级', visible: true, width: 120 },
            { key: 'status', title: '设备状态', visible: true, width: 90 },
            { key: 'seq', title: '所属设备', visible: true, width: 90 },
            { key: 'abc', title: 'ABC分类', visible: true, width: 90 },
            { key: 'erpId', title: 'ERP编号', visible: true, width: 90 },
            { key: 'sproduct', title: '制造单位', visible: true, width: 90 },
            { key: 'susedept', title: '使用部门', visible: true, width: 90 },
            { key: 'suser', title: '责任人', visible: true, width: 90 },
            { key: 'scheckuser', title: '检定员', visible: true, width: 90 },
        ];

        const actions = [
            { name: 'search', text: '查询', class: 'btn-primary' },
            { name: 'submit', text: '提交', class: 'btn-outline-secondary' },
            { name: 'markAbnormal', text: '异常标记', class: 'btn-outline-warning' },
            { name: 'export', text: '导出', class: 'btn-outline-success' },
        ];

        const filters = [
            { type: 'pills', label: '任务状态', name: 'taskStatus', options: [{label: '待检', value: 'unchecked', checked: true}, {label: '已检', value: 'checked'}, {label: '全部', value: 'all'}] },
            { type: 'pills', label: 'ABC分类', name: 'abcCategory', options: [{label: '全部', value: 'all', checked: true}, {label: 'A', value: 'A'}, {label: 'B', 'value': 'B'}, {label: 'C', value: 'C'}] }
        ];

        this.dataTable = new DataTable({
            columns, actions, filters, data:[],
            options: {
                uniformRowHeight: true,
                configurable: true,
                storageKey: 'metrologyTasksTable',
                selectable: 'multiple',
            }
        });

        this.dataTable.render(container);
    }

    // ... (其余方法保持不变: _loadData, _showAbnormalWorkOrderModal, _attachEventListeners) ...

    async _loadData() {
        this.dataTable.toggleLoading(true);

        try {
            const formParams = this.queryForm.getValues();
            const tableState = this.dataTable.state;

            const params = {
                ...formParams,
                ...tableState.filters,
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

    _showAbnormalWorkOrderModal(selectedIds) {
        const bodyHtml = `
            <p>您已选择 ${selectedIds.length} 个任务进行异常标记。</p>
            <form>
                <div class="mb-3">
                    <label for="abnormal-description" class="form-label">异常描述</label>
                    <textarea class="form-control" id="abnormal-description" rows="3" required></textarea>
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

        modal.modalElement.querySelector('#confirm-work-order').addEventListener('click', async () => {
            const description = modal.modalElement.querySelector('#abnormal-description').value;
            if (!description.trim()) {
                Modal.alert("请输入异常描述。");
                return;
            }

            modal.hide();
            this.dataTable.toggleLoading(true);
            try {
                const payload = {
                    ids: selectedIds,
                    pointCheckStatus: "已检",
                    checkResult: "异常",
                    abnormalDesc: description
                };
                const result = await updateMetrologyTasks(payload);
                Modal.alert(result.message || '操作成功');
                this._loadData();
            } catch (error) {
                Modal.alert(`异常标记失败: ${error.message}`);
            } finally {
                this.dataTable.toggleLoading(false);
            }
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

            const selectedCheckboxes = tableContainer.querySelectorAll('tbody input[type="checkbox"]:checked');
            const selectedRowIds = Array.from(selectedCheckboxes).map(cb => cb.dataset.rowId);

            if (action === 'search') {
                this.dataTable.state.pageNum = 1;
                this._loadData();
            } else if (action === 'submit') {
                if (selectedRowIds.length === 0) {
                    Modal.alert('请至少选择一个任务进行提交。');
                    return;
                }
                const confirmed = await Modal.confirm('确认提交', `您确定要提交选中的 ${selectedRowIds.length} 个任务吗？`);
                if (!confirmed) return;

                this.dataTable.toggleLoading(true);
                try {
                    const payload = {
                        ids: selectedRowIds,
                        pointCheckStatus: "已检",
                        checkResult: "正常"
                    };
                    const result = await updateMetrologyTasks(payload);
                    Modal.alert(result.message || '提交成功');
                    this._loadData();
                } catch (error) {
                    Modal.alert(`提交失败: ${error.message}`);
                } finally {
                    this.dataTable.toggleLoading(false);
                }

            } else if (action === 'markAbnormal') {
                if (selectedRowIds.length === 0) {
                    Modal.alert('请至少选择一个任务进行异常标记。');
                    return;
                }
                this._showAbnormalWorkOrderModal(selectedRowIds);

            } else if (action === 'export') {
                button.disabled = true;
                button.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 正在导出...';
                try {
                    const params = {
                        ...this.queryForm.getValues(),
                        ...this.dataTable.state.filters,
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