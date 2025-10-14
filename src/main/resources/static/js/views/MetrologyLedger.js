/**
 * 源码路径: js/views/MetrologyLedger_Updated.js
 * 功能说明: 这是 MetrologyLedger.js 的更新版本，展示了如何使用 Optimized_DataTable.js 组件。
 *
 * --- 主要变化 ---
 * 1.  **组件引用**: 导入了 Optimized_DataTable.js。
 * 2.  **状态简化**: 移除了视图层中对 currentPage 和 currentFilters 的管理，这些状态已移入新表格组件内部。
 * 3.  **事件统一**: 废弃了对 'pageChange' 和筛选器 'change' 的单独监听，改为统一监听表格派发的 'queryChange' 事件。
 * 4.  **开启排序**: 在列定义中，为 'sysId', 'enterpriseId', 'nextDate' 添加了 `sortable: true` 属性。
 * 5.  **加载逻辑**: `_loadData` 方法现在接收所有查询参数，并使用新表格的 `toggleLoading` 方法来显示加载状态。
 *
 * @version 3.2.0 - 2025-10-14 (修复了 'pageNum=undefined' 的bug)
 */
// 变化1: 导入优化后的组件
import DataTable from '../components/Optimized_DataTable.js';
import QueryForm from '../components/QueryForm.js';
import Modal from '../components/Modal.js';
import { getMetrologyLedger, exportMetrologyLedger } from '../services/api.js';

export default class MetrologyLedgerUpdated {
    constructor() {
        this.dataTable = null;
        this.queryForm = null;
        this.container = null;
        // 变化2: 移除 currentPage, pageSize, currentFilters，这些状态由 DataTable 内部管理
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

        // 初始加载
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
            // --- 默认显示列 ---
            { key: 'expired', title: '是否过期', visible: true, width: 90, render: (val) => val ? '<i class="bi bi-check-circle-fill text-danger"></i>' : '<i class="bi bi-circle text-secondary"></i>' },
            { key: 'isLinked', title: '台账挂接', visible: true, width: 90, render: (val) => val ? '<i class="bi bi-check-circle-fill text-success"></i>' : '<i class="bi bi-circle text-secondary"></i>' },
            { key: 'sysId', title: '系统编号', visible: true, width: 100, sortable: true },
            { key: 'enterpriseId', title: '企业编号', visible: true, width: 120, sortable: true },
            { key: 'deviceName', title: '设备名称', visible: true, width: 180 },
            { key: 'model', title: '规格型号', visible: true, width: 120 },
            { key: 'factoryId', title: '出厂编号', visible: true, width: 120 },
            { key: 'location', title: '安装位置/使用人', visible: true, width: 150 },
            { key: 'accuracy', title: '准确度等级', visible: true, width: 120 },
            { key: 'status', title: '设备状态', visible: true, width: 90 },
            { key: 'nextDate', title: '下次确认日期', visible: true, width: 120, sortable: true },
            { key: 'parentDevice', title: '所属设备', visible: true, width: 120 },
            { key: 'department', title: '使用部门', visible: true, width: 120 },
            { key: 'abc', title: 'ABC分类', visible: true, width: 90 },

            // --- 默认隐藏列 (已补全) ---
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

        this.dataTable = new DataTable({ // 使用优化后的 DataTable
            columns, actions, filters, data: [],
            options: {
                configurable: true,
                storageKey: 'metrologyLedgerTable',
                selectable: 'single',
                // 可以设置默认排序
                defaultSortBy: 'sysId',
                defaultSortOrder: 'desc'
            }
        });
        this.dataTable.render(container);
    }

    /**
     * [已修复] _loadData 方法不再接收参数，而是直接从组件状态中获取所有查询条件，逻辑更健壮。
     */
    async _loadData() {
        this.dataTable.toggleLoading(true); // 显示加载动画

        try {
            const formParams = this.queryForm.getValues();
            const tableState = this.dataTable.state; // 直接从 DataTable 实例获取当前状态

            // 合并来自查询表单和表格内部状态（分页、排序、筛选）的参数
            const params = {
                ...formParams,
                ...tableState.filters,
                pageNum: tableState.pageNum,
                pageSize: tableState.pageSize,
                sortBy: tableState.sortBy,
                sortOrder: tableState.sortOrder,
            };

            // 清理无效参数
            if (!params.sortBy) {
                delete params.sortBy;
                delete params.sortOrder;
            }

            const pageResult = await getMetrologyLedger(params);
            this.dataTable.updateView(pageResult);
        } catch (error) {
            console.error("加载台账数据失败:", error);
            Modal.alert(`加载数据失败: ${error.message}`);
        } finally {
            this.dataTable.toggleLoading(false); // 隐藏加载动画
        }
    }

    /**
     * [已修复] 简化事件监听逻辑，统一处理
     */
    _attachEventListeners() {
        const tableContainer = this.container.querySelector('#data-table-container');
        if (!tableContainer) return;

        // 监听工具栏的 "查询" 和 "导出" 按钮
        tableContainer.addEventListener('click', async (e) => {
            const button = e.target.closest('button[data-action]');
            if (!button) return;

            const action = button.dataset.action;
            if (action === 'search') {
                this.dataTable.state.pageNum = 1; // 点击查询按钮，重置到第一页
                this._loadData();
            } else if (action === 'export') {
                button.disabled = true;
                button.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 正在导出...';
                try {
                    // 导出时也直接从组件状态获取最新参数
                    const params = {
                        ...this.queryForm.getValues(),
                        ...this.dataTable.state.filters,
                        sortBy: this.dataTable.state.sortBy,
                        sortOrder: this.dataTable.state.sortOrder,
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

        // [已修复] 统一监听表格内部状态变化（分页、排序、筛选），触发数据重新加载
        tableContainer.addEventListener('queryChange', () => {
            this._loadData();
        });
    }
}

