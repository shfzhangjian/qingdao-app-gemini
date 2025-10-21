/**
 * @file /js/views/MaintenanceCheck.js
 * @description 保养任务检查与结果视图（原型）
 * @version 1.0.0 - 2025-10-17
 */
import DataTable from '../components/Optimized_DataTable.js';
import Modal from '../components/Modal.js';

export default class MaintenanceCheck {
    constructor() {
        this.container = null;
        this.dataTable = null;
        this.mockData = this._getMockData();
    }

    _getMockData() {
        const data = [
            { id: 1, taskNo: 'T20251017-003', device: 'PROTOS70', content: '设备到位检查', executor: '王五', completeTime: '2025-10-17 10:30', result: '正常', score: 100, checker: '管理员', checkTime: '2025-10-17 11:00' },
            { id: 2, taskNo: 'T20251016-005', device: 'FOCKE', content: '光电检查', executor: '赵六', completeTime: '2025-10-16 14:00', result: '异常', score: 80, checker: '管理员', checkTime: '2025-10-16 15:00' },
            { id: 3, taskNo: 'T20251015-010', device: 'GDX2', content: '气路检查', executor: '孙七', completeTime: '2025-10-15 09:00', result: '待检查', score: null, checker: '', checkTime: '' },
        ];
        for (let i = 4; i <= 50; i++) {
            const statusOptions = ['正常', '异常', '待检查'];
            data.push({ ...data[i % 3], id: i, taskNo: `T20251017-${String(i).padStart(3,'0')}`, result: statusOptions[i % 3] });
        }
        return data;
    }

    render(container) {
        this.container = container;
        container.innerHTML = `
            <div class="d-flex flex-column h-100">
                 <div class="p-3 rounded mb-3 d-flex justify-content-between align-items-center" style="background-color: var(--bg-primary);">
                    <div class="d-flex align-items-center gap-3">
                        <label class="form-label mb-0">设备:</label>
                        <input type="text" class="form-control form-control-sm" style="width: 150px;">
                         <label class="form-label mb-0">检查结果:</label>
                        <select class="form-select form-select-sm" style="width: 120px;">
                            <option value="all">全部</option>
                            <option value="normal">正常</option>
                            <option value="abnormal">异常</option>
                            <option value="pending">待检查</option>
                        </select>
                        <button class="btn btn-sm btn-primary"><i class="bi bi-search me-1"></i>查询</button>
                    </div>
                     <div class="d-flex align-items-center gap-2">
                        <button class="btn btn-sm btn-outline-primary"><i class="bi bi-card-checklist me-1"></i>任务检查</button>
                    </div>
                </div>
                <div id="data-table-container" class="flex-grow-1" style="min-height: 0;"></div>
            </div>
        `;
        this._renderDataTable(container.querySelector('#data-table-container'));
    }

    _renderDataTable(tableContainer) {
        const columns = [
            { key: 'taskNo', title: '任务单号', width: 150, sortable: true },
            { key: 'device', title: '设备', width: 120 },
            { key: 'content', title: '保养内容', width: 250 },
            { key: 'executor', title: '执行人', width: 100 },
            { key: 'completeTime', title: '完成时间', width: 150, sortable: true },
            { key: 'result', title: '检查结果', width: 100, render: (val) => {
                    if (val === '待检查') return `<span class="badge bg-secondary">${val}</span>`;
                    if (val === '异常') return `<span class="badge bg-danger">${val}</span>`;
                    if (val === '正常') return `<span class="badge bg-success">${val}</span>`;
                    return val;
                }},
            { key: 'score', title: '得分', width: 80 },
            { key: 'checker', title: '检查人', width: 100 },
            { key: 'checkTime', title: '检查时间', width: 150, sortable: true },
        ];

        this.dataTable = new DataTable({
            columns,
            data: this.mockData,
            options: {
                selectable: 'multiple',
                storageKey: 'maintenanceCheckTable',
            }
        });
        this.dataTable.render(tableContainer);
    }
}
