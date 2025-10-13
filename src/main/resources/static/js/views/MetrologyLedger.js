/**
 * 源码路径: js/views/MetrologyLedger.js
 * 功能说明: 计量台账页面的视图逻辑。
 * 负责组装QueryForm和DataTable组件来构建完整的页面。
 * 版本变动:
 * v1.0.0 - 2025-10-13: 初始版本，使用组件构建UI。
 * v1.1.0 - 2025-10-13: 适配v2.2.0的DataTable组件，将工具栏配置移入DataTable。
 */
import DataTable from '../components/DataTable.js';
import QueryForm from '../components/QueryForm.js';
import Modal from '../components/Modal.js';

function generateMetrologyLedgerData() {
    const items = [
        { expired: false, sysId: 'SYS001', seq: 1, enterpriseId: '01200002', erpId: 'JL009219', deviceName: '电子台秤', model: 'TCS-B', factoryId: '1339744', range: '0-1000kg', location: '车间A-01', accuracy: 'III级', nextDate: '2026-08-22', status: '正常' },
        { expired: true, sysId: 'SYS002', seq: 2, enterpriseId: '01200003', erpId: 'JL009220', deviceName: '电子台秤', model: 'TCS-B', factoryId: '234422', range: '0.4kg-60kg', location: '仓库B-05', accuracy: 'III级', nextDate: '2025-01-10', status: '正常' },
        { expired: false, sysId: 'SYS003', seq: 3, enterpriseId: '01200004', erpId: 'JL009221', deviceName: '电子台秤', model: 'TCS-B', factoryId: '223421', range: '0-1000kg', location: '车间A-02', accuracy: 'III级', nextDate: '2026-08-20', status: '正常' },
        { expired: false, sysId: 'SYS004', seq: 4, enterpriseId: '01200005', erpId: 'JL009222', deviceName: '压力计', model: 'YK-100', factoryId: '5582910', range: '0-1.6MPa', location: '管道1', accuracy: '1.6级', nextDate: '2026-08-19', status: '维修中' },
        { expired: false, sysId: 'SYS005', seq: 5, enterpriseId: '01200006', erpId: 'JL009223', deviceName: '温度计', model: 'WSS-411', factoryId: '9821345', range: '-40-600℃', location: '锅炉3', accuracy: '1.5级', nextDate: '2026-08-18', status: '已报废' },
    ];
    return items.map(item => ({...item, linkage: '', controlId: '-', gbAccuracy: '-', uncertainty: '-', resolution: '-', techParams: '-', parentDevice: '-', abc: 'A', controlType: '-', manufacturer: '-', mfgDate: '-', startDate: '-', owner: '-', funcUnit: '-', assetCode: '-', periodUnit: '-', confirmDate: '-', interval: '-', verificationType: '-', verificationUnit: '-', mandatory: '-', energyMgmt: '-', qcInstrument: '-', verifier: '-', validator: '-', notes: '-', deptId: '-', deptName: '-', createDate: '-', firstVerification: '-', reviewer: '-' }));
}

export default class MetrologyLedger {
    constructor() {
        this.data = generateMetrologyLedgerData();
        this.dataTable = null;
        this.queryForm = null;
    }

    render(container, footerContainer) {
        // The main container is now a flex container to support the table placeholder
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
        const formFields = [
            { type: 'text', label: '计量设备名称', name: 'deviceName', containerClass: 'col-md-4', labelWidth: '120px' },
            { type: 'text', label: '企业编号', name: 'enterpriseId', containerClass: 'col-md-4', labelWidth: '120px' },
            { type: 'text', label: '出厂编号', name: 'factoryId', containerClass: 'col-md-4', labelWidth: '120px' },
            { type: 'text', label: '使用部门', name: 'department', containerClass: 'col-md-4', labelWidth: '120px' },
            { type: 'text', label: '安装位置/使用人', name: 'locationUser', containerClass: 'col-md-4', labelWidth: '120px' },
            { type: 'text', label: '所属设备', name: 'parentDevice', containerClass: 'col-md-4', labelWidth: '120px', defaultValue: '46#ZB48' }
        ];

        this.queryForm = new QueryForm({ fields: formFields });
        container.innerHTML = `<div class="p-3 rounded mb-3" style="background-color: var(--bg-dark-primary);"><div class="d-flex flex-wrap align-items-center row-gap-3">${this.queryForm._createFieldsHtml()}</div></div>`;
    }

    _renderDataTable(container) {
        const columns = [
            { key: 'expired', title: '是否过期', visible: true, render: (val, row) => row.expired ? `<span class="badge bg-danger">是</span>` : `<span class="badge bg-success">否</span>` },
            { key: 'linkage', title: '台账挂接', visible: true, render: () => `<button class="btn btn-sm btn-outline-primary py-0">挂接</button>` },
            { key: 'sysId', title: '系统编号', visible: true },
            { key: 'seq', title: '序号', visible: true },
            { key: 'enterpriseId', title: '企业编号', visible: true },
            { key: 'erpId', title: 'ERP编号', visible: true },
            { key: 'deviceName', title: '设备名称', visible: true },
            { key: 'model', title: '规格型号', visible: true },
            { key: 'controlId', title: '中控编号', visible: true },
            { key: 'location', title: '安装位置/使用人', visible: true },
            { key: 'factoryId', title: '出厂编号', visible: true },
            { key: 'range', title: '量程范围', visible: true },
            { key: 'accuracy', title: '准确度等级/最大允许误差', visible: true },
            { key: 'gbAccuracy', title: 'GB17167要求准确度', visible: false },
            { key: 'uncertainty', title: '不确定度', visible: false },
            { key: 'resolution', title: '分辨力/分度值', visible: false },
            { key: 'techParams', title: '技术参数', visible: false },
            { key: 'parentDevice', title: '所属设备', visible: true },
            { key: 'abc', title: 'ABC分类', visible: true },
            { key: 'controlType', title: '管控类型', visible: false },
            { key: 'manufacturer', title: '制造单位', visible: false },
            { key: 'mfgDate', title: '出厂日期', visible: false },
            { key: 'startDate', title: '启用日期', visible: false },
            { key: 'owner', title: '责任人', visible: false },
            { key: 'funcUnit', title: '设备或系统功能单元', visible: false },
            { key: 'assetCode', title: '固定资产编码', visible: false },
            { key: 'periodUnit', title: '周期单位', visible: false },
            { key: 'confirmDate', title: '确认日期', visible: false },
            { key: 'nextDate', title: '下次确认日期', visible: true },
            { key: 'interval', title: '确认间隔', visible: false },
            { key: 'verificationType', title: '检定类型', visible: false },
            { key: 'verificationUnit', title: '检定单位', visible: false },
            { key: 'mandatory', title: '强检标识', visible: false },
            { key: 'energyMgmt', title: '能源管理分项', visible: false },
            { key: 'qcInstrument', title: '质检仪器', visible: false },
            { key: 'verifier', title: '检定员', visible: false },
            { key: 'validator', title: '验证人', visible: false },
            { key: 'notes', title: '备注', visible: false },
            { key: 'status', title: '设备状态', visible: true },
            { key: 'deptId', title: '使用部门ID', visible: false },
            { key: 'deptName', title: '使用部门', visible: false },
            { key: 'createDate', title: '创建日期', visible: false },
            { key: 'firstVerification', title: '首次检定', visible: false },
            { key: 'reviewer', title: '复核人', visible: false },
        ];

        const actions = [
            { name: 'search', text: '查询', class: 'btn-primary' },
            { name: 'export', text: '导出', class: 'btn-outline-success' },
        ];

        const filters = [
            { type: 'pills', label: '设备状态', name: 'deviceStatus', options: [{label: '全部', value: 'all', checked: true}, {label: '正常', value: 'normal'}, {label: '维修中', value: 'repair'}, {label: '已报废', value: 'scrapped'}] },
            { type: 'pills', label: 'ABC分类', name: 'abcCategory', options: [{label: 'A', value: 'a', checked: true}, {label: 'B', value: 'b'}, {label: 'C', value: 'c'}] }
        ];

        this.dataTable = new DataTable({
            columns: columns,
            data: this.data,
            actions: actions,
            filters: filters,
            options: {
                configurable: true,
                storageKey: 'metrologyLedgerTable'
            }
        });

        this.dataTable.render(container);
    }

    _renderFooter(container) {
        container.innerHTML = `
            <nav class="d-flex justify-content-end">
                <ul class="pagination pagination-sm mb-0">
                    <li class="page-item disabled"><a class="page-link" href="#">&laquo;</a></li>
                    <li class="page-item"><a class="page-link" href="#">1</a></li>
                    <li class="page-item active"><a class="page-link" href="#">2</a></li>
                    <li class="page-item"><a class="page-link" href="#">3</a></li>
                    <li class="page-item"><a class="page-link" href="#">...</a></li>
                    <li class="page-item"><a class="page-link" href="#">10</a></li>
                    <li class="page-item"><a class="page-link" href="#">&raquo;</a></li>
                </ul>
            </nav>
        `;
    }

    _attachEventListeners() {
        const tableContainer = document.getElementById('data-table-container');
        if (!tableContainer) return;

        // Listen for clicks on the toolbar buttons
        tableContainer.addEventListener('click', (e) => {
            const button = e.target.closest('button[data-action]');
            if (!button) return;

            const action = button.dataset.action;
            if (action === 'search') {
                const queryValues = this.queryForm.getValues();
                const status = tableContainer.querySelector('input[name="deviceStatus"]:checked').id.split('-').pop();
                const category = tableContainer.querySelector('input[name="abcCategory"]:checked').id.split('-').pop();

                console.log('Searching with:', { ...queryValues, status, category });
                Modal.alert('执行查询（模拟）');
                // this.dataTable.updateData( ... new data from API ... );
            }
        });

        // Listen for clicks on table rows for selection
        const tableBody = tableContainer.querySelector('tbody');
        if (tableBody) {
            tableBody.addEventListener('click', (e) => {
                const row = e.target.closest('tr');
                if (!row || !row.parentElement) return; // Ensure row and its parent exist

                // Do not trigger selection if a button inside the row was clicked
                if (e.target.closest('button')) {
                    return;
                }

                const currentlyActive = tableBody.querySelector('.table-active-custom');
                if (currentlyActive) {
                    currentlyActive.classList.remove('table-active-custom');
                }
                row.classList.add('table-active-custom');
            });
        }
    }
}

