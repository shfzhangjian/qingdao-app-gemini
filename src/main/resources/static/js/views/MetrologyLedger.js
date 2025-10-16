/**
 * 源码路径: js/views/MetrologyLedger.js
 * 功能说明: 计量台账页面视图逻辑 (已适配真实后端数据)
 *
 * --- 主要变化 ---
 * 1.  **移除模拟数据**: _renderDataTable 方法不再生成任何模拟数据。
 * 2.  **适配真实列**: 列定义（columns）已根据 V_JL_EQUIP 视图和字段字典进行精确配置。
 * 3.  **日期格式化**: 为日期字段添加了 render 函数，以提供更友好的显示。
 * 4.  **主键适配**: 确保表格行的 id 绑定到从后端获取的 `indocno` 主键。
 *
 * @version 4.1.0 - 2025-10-15 (修复日期显示问题)
 */
import DataTable from '../components/Optimized_DataTable.js';
import QueryForm from '../components/QueryForm.js';
import Modal from '../components/Modal.js';
import { getMetrologyLedger, exportMetrologyLedger } from '../services/api.js';

/**
 * [新增] 一个健壮的日期格式化函数，用于防止 "Invalid Date" 的显示
 * @param {string} dateString - 从后端接收的日期字符串或时间戳
 * @returns {string} 格式化后的 'YYYY-MM-DD' 字符串或 '-'
 */
const formatDate = (dateString) => {
    if (!dateString) {
        return '-';
    }
    const date = new Date(dateString);
    // 检查日期对象是否有效
    if (isNaN(date.getTime())) {
        return '-'; // 如果解析失败，返回一个占位符
    }

    // 手动格式化为 YYYY-MM-DD 以避免不同环境下的显示差异
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');

    return `${year}-${month}-${day}`;
};


export default class MetrologyLedger {
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
        // --- [核心修改] 根据数据库字典和DTO重新定义列 ---
        const columns = [
            // --- 默认显示列 ---
            { key: 'expired', title: '是否过期', visible: true, width: 90, render: (val) => val ? '<i class="bi bi-exclamation-triangle-fill text-danger"></i>' : '<i class="bi bi-shield-check-fill text-success"></i>' },
            { key: 'isLinked', title: '台账挂接', visible: true, width: 90, render: (val) => val ? '<i class="bi bi-check-circle-fill text-success"></i>' : '<i class="bi bi-circle text-secondary"></i>' },
            { key: 'sysId', title: '系统编号', visible: true, width: 120, sortable: true },
            { key: 'enterpriseId', title: '企业编号', visible: true, width: 120, sortable: true },
            { key: 'deviceName', title: '设备名称', visible: true, width: 180 },
            { key: 'model', title: '规格型号', visible: true, width: 120 },
            { key: 'factoryId', title: '出厂编号', visible: true, width: 120 },
            { key: 'location', title: '安装位置/使用人', visible: true, width: 150 },
            { key: 'accuracy', title: '准确度等级', visible: true, width: 120 },
            { key: 'status', title: '设备状态', visible: true, width: 90 },
            { key: 'nextDate', title: '下次确认日期', visible: true, width: 120, sortable: true, render: formatDate },
            { key: 'parentDevice', title: '所属设备', visible: true, width: 120 },
            { key: 'department', title: '使用部门', visible: true, width: 120 },
            { key: 'abc', title: 'ABC分类', visible: true, width: 90 },

            // --- 默认隐藏列 (根据字典配置) ---
            { key: 'iqj', title: '强检标识', visible: false },
            { key: 'izj', title: '质检仪器', visible: false },
            { key: 'slc', title: '量程范围', visible: false },
            { key: 'sproduct', title: '制造单位', visible: false },
            { key: 'dfactory', title: '出厂时间', visible: false, render: formatDate },
            { key: 'suser', title: '责任人', visible: false },
            { key: 'sverifier', title: '检定员', visible: false },
            { key: 'sdefine1', title: '确认方式', visible: false },
            { key: 'scertificate', title: '证书编号', visible: false },
            { key: 'sbuytype', title: '购置形式', visible: false },
            { key: 'dcheck', title: '本次确认日期', visible: false, render: formatDate },
            { key: 'sconfirmbasis', title: '确认依据', visible: false },
            { key: 'snotes', title: '备注', visible: false },
        ];

        const actions = [
            { name: 'search', text: '查询', class: 'btn-primary' },
            { name: 'export', text: '导出', class: 'btn-outline-success' },
        ];

        const filters = [
            { type: 'pills', label: '设备状态', name: 'deviceStatus', options: [{label: '全部', value: 'all', checked: true}, {label: '在用', value: 'normal'}, {label: '维修', value: 'repair'}, {label: '报废', value: 'scrapped'}] },
            { type: 'pills', label: 'ABC分类', name: 'abcCategory', options: [{label: '全部', value: 'all', checked: true}, {label: 'A', value: 'A'}, {label: 'B', 'value': 'B'}, {label: 'C', value: 'C'}] }
        ];

        this.dataTable = new DataTable({
            columns, actions, filters, data: [],
            options: {
                configurable: true,
                uniformRowHeight: true,
                storageKey: 'metrologyLedgerTable_v2', // Use a new key for the new config
                selectable: 'single',
                defaultSortBy: 'sysId',
                defaultSortOrder: 'desc'
            }
        });
        this.dataTable.render(container);
    }

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

            // [重要] 后端 DTO 的 id 字段是 indocno，确保行ID正确绑定
            const pageResult = await getMetrologyLedger(params);
            pageResult.list.forEach(item => {
                item.id = item.indocno; // 将 indocno 赋值给 id，以便表格组件正确识别
            });

            this.dataTable.updateView(pageResult);
        } catch (error) {
            console.error("加载台账数据失败:", error);
            Modal.alert(`加载数据失败: ${error.message}`);
        } finally {
            this.dataTable.toggleLoading(false);
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
                this.dataTable.state.pageNum = 1;
                this._loadData();
            } else if (action === 'export') {
                button.disabled = true;
                button.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 正在导出...';
                try {
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

        tableContainer.addEventListener('queryChange', () => {
            this._loadData();
        });
    }
}

