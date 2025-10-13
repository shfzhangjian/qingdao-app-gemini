/**
 * 源码路径: js/views/MetrologyTasks.js
 * 功能说明: 计量任务页面的视图逻辑。
 * 负责组装QueryForm和DataTable组件来构建完整的页面。
 * 版本变动:
 * v1.8.0 - 2025-10-13: 更新查询条件，并使用 getRowClass 选项为异常行添加背景色。
 */
import DataTable from '../components/DataTable.js';
import QueryForm from '../components/QueryForm.js';
import Modal from '../components/Modal.js';

function generateMetrologyTasksData() {
    const tasks = [
        { id: 1, date: '2025-8-23', enterpriseId: '01200002', erpId: 'JL009219', deviceName: '电子台秤', model: 'TCS-B', factoryId: '1339744', range: '0-1000kg', location: '车间A-01', accuracy: 'III级', status: '正常', pointCheckStatus: '未点检', isAbnormal: false },
        { id: 2, date: '2025-8-22', enterpriseId: '01200003', erpId: 'JL009220', deviceName: '电子台秤', model: 'TCS-B', factoryId: '234422', range: '0.4kg-60kg', location: '仓库B-05', accuracy: 'III级', status: '正常', pointCheckStatus: '已点检', isAbnormal: false },
        { id: 3, date: '2025-8-21', enterpriseId: '01200004', erpId: 'JL009221', deviceName: '电子台秤', model: 'TCS-B', factoryId: '223421', range: '0-1000kg', location: '车间A-02', accuracy: 'III级', status: '正常', pointCheckStatus: '未点检', isAbnormal: false },
        { id: 4, date: '2025-8-20', enterpriseId: '01200005', erpId: 'JL009222', deviceName: '压力计', model: 'YK-100', factoryId: '5582910', range: '0-1.6MPa', location: '管道1', accuracy: '1.6级', status: '维修中', pointCheckStatus: '未点检', isAbnormal: true },
        { id: 5, date: '2025-8-19', enterpriseId: '01200006', erpId: 'JL009223', deviceName: '温度计', model: 'WSS-411', factoryId: '9821345', range: '-40-600℃', location: '锅炉3', accuracy: '1.5级', status: '已报废', pointCheckStatus: '已点检', isAbnormal: true }
    ];
    // Add placeholder data for all new columns
    return tasks.map(item => ({
        ...item,
        gbAccuracy: '-', uncertainty: '-', resolution: '-', techParams: '-', abc: 'A', classStandard: '-', manufacturer: '某某制造', mfgDate: '2022-01-01', startDate: '2022-03-01', owner: '张三', parentDevice: '46#ZB48', funcUnit: '-', assetCode: '-', interval: '12', confirmDate: '2025-08-20', verificationType: '外校', verificationUnit: '市计量院', mandatory: '是', energyClass: '-', energyToolType: '-', qcInstrument: '否', department: '生产部', verifier: '李四', description: '-'
    }));
}


export default class MetrologyTasks {
    constructor() {
        this.data = generateMetrologyTasksData();
        this.dataTable = null;
        this.queryForm = null;
    }

    render(container) {
        container.innerHTML = `
            <div class="p-3 rounded" style="background-color: var(--bg-dark-secondary); display: flex; flex-direction: column; height: 100%;">
                <div id="query-form-container"></div>
                <div id="data-table-container" style="flex-grow: 1; display: flex; flex-direction: column; min-height: 0;"></div>
            </div>
        `;

        this._renderQueryForm(container.querySelector('#query-form-container'));
        this._renderDataTable(container.querySelector('#data-table-container'));

        this._attachEventListeners();
    }

    _renderQueryForm(container) {
        this.queryForm = new QueryForm({
            fields: [
                { type: 'text', label: '计量设备名称', name: 'deviceName', style: 'width: 150px;' },
                { type: 'text', label: '企业编号', name: 'enterpriseId', style: 'width: 150px;' },
                { type: 'daterange', label: '时间范围', name: 'dateRange', style: 'width: 200px;' },
                { type: 'pills', label: '任务状态', name: 'taskStatus', options: [{label: '未检', value: 'unchecked', checked: true}, {label: '已检', value: 'checked'}, {label: '异常', value: 'abnormal'}] },
                { type: 'pills', label: 'ABC分类', name: 'abcCategory', options: [{label: 'A', value: 'a'}, {label: 'B', value: 'b', checked: true}, {label: 'C', value: 'c'}] }
            ]
        });

        container.innerHTML = `<div class="d-flex flex-wrap align-items-center gap-3 mb-3 p-3 rounded" style="background-color: var(--bg-dark-primary);">${this.queryForm._createFieldsHtml()}</div>`;
        this.queryForm.container = container; // Re-assign container after innerHTML rewrite
        this.queryForm._initializeDatePickers();
    }

    _renderDataTable(container) {
        const columns = [
            { key: 'date', title: '生成任务时间', visible: true },
            { key: 'pointCheckStatus', title: '点检状态', visible: true },
            { key: 'enterpriseId', title: '企业编号', visible: true },
            { key: 'erpId', title: 'ERP编号', visible: true },
            { key: 'deviceName', title: '设备名称', visible: true },
            { key: 'model', title: '规格型号', visible: true },
            { key: 'factoryId', title: '出厂编号', visible: true },
            { key: 'range', title: '量程范围', visible: true },
            { key: 'location', title: '安装位置/使用人', visible: true },
            { key: 'accuracy', title: '准确度等级/最大允许误差', visible: true },
            { key: 'gbAccuracy', title: 'GB17167要求准确度', visible: false },
            { key: 'uncertainty', title: '不确定度', visible: false },
            { key: 'resolution', title: '分辨力/分度值', visible: false },
            { key: 'techParams', title: '技术参数', visible: false },
            { key: 'abc', title: 'ABC分类', visible: false },
            { key: 'classStandard', title: '分类标准', visible: false },
            { key: 'manufacturer', title: '制造单位', visible: false },
            { key: 'mfgDate', title: '出厂日期', visible: false },
            { key: 'startDate', title: '启用日期', visible: false },
            { key: 'status', title: '设备状态', visible: true },
            { key: 'owner', title: '责任人', visible: false },
            { key: 'parentDevice', title: '所属设备', visible: false },
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
            { key: 'department', title: '使用部门', visible: false },
            { key: 'verifier', title: '检定员', visible: false },
            { key: 'description', title: '异常描述', visible: false },
        ];

        const actions = [
            { name: 'search', text: '查询', class: 'btn-primary' },
            { name: 'submit', text: '提交', class: 'btn-outline-secondary' },
            { name: 'markAbnormal', text: '异常标记', class: 'btn-outline-warning' },
            { name: 'export', text: '导出', class: 'btn-outline-success' },
        ];

        this.dataTable = new DataTable({
            columns,
            data: this.data,
            actions,
            options: {
                configurable: true,
                storageKey: 'metrologyTasksTable',
                selectable: 'multiple',
                getRowClass: (row) => row.isAbnormal ? 'table-row-abnormal' : ''
            }
        });

        this.dataTable.render(container);
    }

    _showAbnormalWorkOrderModal(rowData) {
        const bodyHtml = `
            <form>
                <div class="mb-3">
                    <label class="form-label">企业编号</label>
                    <input type="text" class="form-control" value="${rowData.enterpriseId}" readonly>
                </div>
                <div class="mb-3">
                    <label class="form-label">ERP编号</label>
                    <input type="text" class="form-control" value="${rowData.erpId}" readonly>
                </div>
                <div class="mb-3">
                    <label class="form-label">计量设备名称</label>
                    <input type="text" class="form-control" value="${rowData.deviceName}" readonly>
                </div>
                <div class="mb-3">
                    <label class="form-label">规格型号</label>
                    <input type="text" class="form-control" value="${rowData.model}" readonly>
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

            console.log('--- 正在生成异常工单 (模拟) ---');
            console.log('设备信息:', {
                enterpriseId: rowData.enterpriseId,
                erpId: rowData.erpId,
                deviceName: rowData.deviceName,
                model: rowData.model
            });
            console.log('异常描述:', description);
            console.log('-----------------------------');

            Modal.alert('异常工单已生成 (模拟)');
            modal.hide();
        });

        modal.show();
    }


    _attachEventListeners() {
        const tableContainer = document.getElementById('data-table-container');
        if (!tableContainer) return;

        tableContainer.addEventListener('click', e => {
            const button = e.target.closest('button[data-action]');
            if (!button) return;

            const action = button.dataset.action;
            if (action === 'search') {
                const values = this.queryForm.getValues();
                console.log("Searching with:", values);
                Modal.alert("执行查询（模拟）");
            } else if (action === 'markAbnormal') {
                const selectedRows = tableContainer.querySelectorAll('tbody tr.table-active-custom');

                if (selectedRows.length !== 1) {
                    Modal.alert(selectedRows.length > 1 ? '异常情况不支持批量提交' : '请选择一行以标记异常');
                    return;
                }

                const selectedRowId = selectedRows[0].dataset.rowId;
                const rowData = this.data.find(item => item.id == selectedRowId);

                if (rowData) {
                    this._showAbnormalWorkOrderModal(rowData);
                } else {
                    Modal.alert('无法找到所选行的数据。');
                }
            }
        });
    }
}

