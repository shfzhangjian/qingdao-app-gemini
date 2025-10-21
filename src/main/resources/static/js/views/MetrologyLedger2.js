/**
 * 源码路径: js/views/MetrologyLedger.js
 * 功能说明: 计量台账页面的视图逻辑。
 * - 从后端API获取并展示分页数据。
 * - 实现查询表单和表格筛选器的联合查询。
 * - 监听分页事件，实现前后端分页联动。
 * 版本变动:
 * v2.5.0 - 2025-10-14: 采用“先渲染框架，后加载数据”模式，优化了加载体验和错误处理。
 */
/**
 * 源码路径: js/views/MetrologyLedger.js
 * 功能说明: 计量台账页面的视图逻辑。
 * - 适配了独立的后端Controller。
 * 版本变动:
 * v2.7.0 - 2025-10-14: 更新API调用以匹配新的Controller结构。
 */
import DataTable from '../components/DataTable.js';
import QueryForm from '../components/QueryForm.js';
import Modal from '../components/Modal.js';
import { getMetrologyLedger, exportMetrologyLedger } from '../services/api.js';

export default class MetrologyLedger {
    constructor() {
        this.dataTable = null;
        this.queryForm = null;
        this.container = null;
        this.currentPage = 1;
        this.pageSize = 10;

        this.currentFilters = {
            deviceStatus: 'all',
            abcCategory: 'all'
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
        const formFields = [
            { type: 'text', label: '计量设备名称', name: 'deviceName', containerClass: 'col-md-4', labelWidth: '120px' },
            { type: 'text', label: '企业编号', name: 'enterpriseId', containerClass: 'col-md-4', labelWidth: '120px' },
            { type: 'text', label: '出厂编号', name: 'factoryId', containerClass: 'col-md-4', labelWidth: '120px' },
            { type: 'text', label: '使用部门', name: 'department', containerClass: 'col-md-4', labelWidth: '120px' },
            { type: 'text', label: '安装位置/使用人', name: 'locationUser', containerClass: 'col-md-4', labelWidth: '120px' },
            { type: 'text', label: '所属设备', name: 'parentDevice', containerClass: 'col-md-4', labelWidth: '120px', defaultValue: '' }
        ];

        this.queryForm = new QueryForm({ fields: formFields });
        container.innerHTML = `<div class="p-3 rounded mb-3" style="background-color: var(--bg-primary);"><div class="d-flex flex-wrap align-items-center row-gap-3">${this.queryForm._createFieldsHtml()}</div></div>`;
        this.queryForm.container = container;
    }

    _renderDataTable(container) {
        const columns = [
            { key: 'expired', title: '是否过期', visible: true, width: 90, render: (val) => val ? '<i class="bi bi-check-circle-fill text-danger"></i>' : '<i class="bi bi-circle text-secondary"></i>' },
            { key: 'isLinked', title: '台账挂接', visible: true, width: 90, render: (val) => val ? '<i class="bi bi-check-circle-fill text-success"></i>' : '<i class="bi bi-circle text-secondary"></i>' },
            { key: 'sysId', title: '系统编号', visible: true, width: 100 },
            { key: 'enterpriseId', title: '企业编号', visible: true, width: 120 },
            { key: 'deviceName', title: '设备名称', visible: true, width: 180 },
            { key: 'model', title: '规格型号', visible: true, width: 120 },
            { key: 'factoryId', title: '出厂编号', visible: true, width: 120 },
            { key: 'location', title: '安装位置/使用人', visible: true, width: 150 },
            { key: 'accuracy', title: '准确度等级', visible: true, width: 120 },
            { key: 'status', title: '设备状态', visible: true, width: 90 },
            { key: 'nextDate', title: '下次确认日期', visible: true, width: 120 },
            { key: 'parentDevice', title: '所属设备', visible: true, width: 120 },
            { key: 'department', title: '使用部门', visible: true, width: 120 },
            { key: 'abc', title: 'ABC分类', visible: true, width: 90 },
            { key: 'seq', title: '序号', visible: false },
            { key: 'erpId', title: 'ERP编号', visible: false },
            { key: 'range', title: '量程范围', visible: false },
            { key: 'pointCheckStatus', title: '点检状态', visible: false },
            { key: 'owner', title: '责任人', visible: false },
            { key: 'verifier', title: '检定员', visible: false },
            { key: 'gbAccuracy', title: 'GB17167要求准确度', visible: false },
            { key: 'uncertainty', title: '不确定度', visible: false },
            { key: 'resolution', title: '分辨力/分度值', visible: false },
            { key: 'techParams', title: '技术参数', visible: false },
            { key: 'classStandard', title: '分类标准', visible: false },
            { key: 'manufacturer', title: '制造单位', visible: false },
            { key: 'mfgDate', title: '出厂日期', visible: false },
            { key: 'startDate', title: '启用日期', visible: false },
            { key: 'funcUnit', title: '设备或系统功能单元', visible: false },
            { key: 'assetCode', title: '固定资产编码', visible: false },
            { key: 'interval', title: '确认间隔', visible: false },
            { key: 'confirmDate', title: '确认日期', visible: false },
            { key: 'verificationType', title: '检定类型', visible: false },
            { key: 'verificationUnit', title: '检定单位', visible: false },
            { key: 'mandatory', title: '强检标识', visible: false },
            { key: 'energyClass', title: '能源分类', visible: false },
            { key: 'energyToolType', title: '能源器具种类', visible: false },
            { key: 'qcInstrument', title: '质检仪器', visible: false },
            { key: 'description', title: '异常描述', visible: false },
        ];

        const actions = [
            { name: 'search', text: '查询', class: 'btn-primary' },
            { name: 'export', text: '导出', class: 'btn-outline-success' },
        ];

        const filters = [
            { type: 'pills', label: '设备状态', name: 'deviceStatus', options: [{label: '全部', value: 'all', checked: true}, {label: '正常', value: 'normal'}, {label: '维修中', value: 'repair'}, {label: '已报废', value: 'scrapped'}] },
            { type: 'pills', label: 'ABC分类', name: 'abcCategory', options: [{label: '全部', value: 'all', checked: true}, {label: 'A', value: 'a'}, {label: 'B', 'value': 'b'}, {label: 'C', value: 'c'}] }
        ];

        this.dataTable = new DataTable({
            columns, actions, filters, data: [],
            options: { configurable: true, storageKey: 'metrologyLedgerTable', selectable: 'single' }
        });
        this.dataTable.render(container);
    }

    async _loadData() {
        const tbody = this.dataTable.container.querySelector('tbody');
        if (!tbody) return;

        const visibleCols = this.dataTable._getVisibleColumns().length;
        tbody.innerHTML = `<tr><td colspan="${visibleCols}" class="text-center p-4"><div class="spinner-border spinner-border-sm" role="status"><span class="visually-hidden">Loading...</span></div> 正在加载...</td></tr>`;

        try {
            const params = {
                ...this.queryForm.getValues(),
                pageNum: this.currentPage,
                pageSize: this.pageSize,
                ...this.currentFilters
            };
            const pageResult = await getMetrologyLedger(params);
            this.dataTable.updateView(pageResult);
        } catch (error) {
            console.error("加载台账数据失败:", error);
            tbody.innerHTML = `<tr><td colspan="${visibleCols}" class="text-center p-4 text-danger">加载数据失败: ${error.message}</td></tr>`;
        }
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
            } else if (action === 'export') {
                button.disabled = true;
                button.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 正在导出...';
                try {
                    const params = {
                        ...this.queryForm.getValues(),
                        ...this.currentFilters,
                        columns: this.dataTable.columns.filter(c => c.visible).map(({ key, title }) => ({ key, title }))
                    };
                    await exportMetrologyLedger(params);
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

