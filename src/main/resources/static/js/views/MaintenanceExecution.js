/**
 * @file /js/views/MaintenanceExecution.js
 * @description 保养任务执行视图（原型）
 * @version 1.0.0 - 2025-10-17
 */
import DataTable from '../components/Optimized_DataTable.js';
import Modal from '../components/Modal.js';

export default class MaintenanceExecution {
    constructor() {
        this.container = null;
        this.dataTable = null;
        this.mockData = this._getMockData();
    }

    _getMockData() {
        const data = [
            { id: 1, taskNo: 'T20251017-001', planNo: 'P20251016-002', device: 'PROTOS70', content: '检查PROTOS70卷烟机各部位', standard: '无松动', status: '待接收', executor: '张三', planDate: '2025-10-18' },
            { id: 2, taskNo: 'T20251017-002', planNo: 'P20251016-002', device: 'PROTOS70', content: '703切丝废丝检查', standard: '无积丝', status: '执行中', executor: '李四', planDate: '2025-10-18' },
            { id: 3, taskNo: 'T20251017-003', planNo: 'P20251016-002', device: 'PROTOS70', content: '设备到位检查', standard: '灵活、无卡阻', status: '已完成', executor: '王五', planDate: '2025-10-17' },
        ];
        for (let i = 4; i <= 40; i++) {
            const statusOptions = ['待接收', '执行中', '已完成'];
            data.push({ ...data[i % 3], id: i, taskNo: `T20251017-${String(i).padStart(3,'0')}`, status: statusOptions[i % 3] });
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
                        <label class="form-label mb-0">执行状态:</label>
                        <select class="form-select form-select-sm" style="width: 120px;">
                            <option value="all">全部</option>
                            <option value="pending">待接收</option>
                            <option value="doing">执行中</option>
                            <option value="done">已完成</option>
                        </select>
                        <button class="btn btn-sm btn-primary"><i class="bi bi-search me-1"></i>查询</button>
                    </div>
                     <div class="d-flex align-items-center gap-2">
                        <button class="btn btn-sm btn-info text-white"><i class="bi bi-check-circle me-1"></i>接收任务</button>
                        <button class="btn btn-sm btn-warning text-white"><i class="bi bi-play-circle me-1"></i>开始执行</button>
                        <button class="btn btn-sm btn-success"><i class="bi bi-check2-square me-1"></i>完成任务</button>
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
            { key: 'planNo', title: '计划单号', width: 150, sortable: true },
            { key: 'device', title: '设备', width: 120 },
            { key: 'content', title: '保养内容', width: 250 },
            { key: 'standard', title: '保养标准', width: 200 },
            { key: 'status', title: '状态', width: 100, render: (val) => {
                    if (val === '待接收') return `<span class="badge bg-info">${val}</span>`;
                    if (val === '执行中') return `<span class="badge bg-warning">${val}</span>`;
                    if (val === '已完成') return `<span class="badge bg-success">${val}</span>`;
                    return val;
                }},
            { key: 'executor', title: '执行人', width: 100 },
            { key: 'planDate', title: '计划日期', width: 120, sortable: true },
        ];

        this.dataTable = new DataTable({
            columns,
            data: this.mockData,
            options: {
                selectable: 'multiple',
                storageKey: 'maintenanceExecTable',
            }
        });
        this.dataTable.render(tableContainer);
    }
}
