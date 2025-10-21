/**
 * @file /js/views/MaintenancePlan.js
 * @description 保养计划生成视图（原型）
 * @version 1.0.0 - 2025-10-17
 */
import DataTable from '../components/Optimized_DataTable.js';
import Modal from '../components/Modal.js';

export default class MaintenancePlan {
    constructor() {
        this.container = null;
        this.dataTable = null;
        this.mockData = this._getMockData();
    }

    _getMockData() {
        const data = [
            { id: 1, planNo: 'P20251017-001', standardName: 'FH-2 滤嘴发射器保养标准', device: 'FH-2', cycle: '例保', year: '2025', status: '待生成', createdBy: '系统', createdAt: '2025-10-17' },
            { id: 2, planNo: 'P20251017-002', standardName: 'PROTOS70卷烟机保养标准', device: 'PROTOS70', cycle: '例保', year: '2025', status: '已生成', createdBy: '王工', createdAt: '2025-10-16' },
        ];
        for (let i = 3; i <= 30; i++) {
            data.push({ ...data[i % 2], id: i, planNo: `P20251017-${String(i).padStart(3,'0')}` });
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
                        <label class="form-label mb-0">年度:</label>
                        <input type="number" class="form-control form-control-sm" style="width: 100px;" value="2025">
                        <button class="btn btn-sm btn-primary"><i class="bi bi-search me-1"></i>查询</button>
                    </div>
                     <div class="d-flex align-items-center gap-2">
                        <button class="btn btn-sm btn-success"><i class="bi bi-gear-wide-connected me-1"></i>生成计划</button>
                        <button class="btn btn-sm btn-danger"><i class="bi bi-trash me-1"></i>删除计划</button>
                    </div>
                </div>
                <div id="data-table-container" class="flex-grow-1" style="min-height: 0;"></div>
            </div>
        `;
        this._renderDataTable(container.querySelector('#data-table-container'));
    }

    _renderDataTable(tableContainer) {
        const columns = [
            { key: 'planNo', title: '计划单号', width: 150, sortable: true },
            { key: 'standardName', title: '保养标准', width: 250, sortable: true },
            { key: 'device', title: '设备', width: 120 },
            { key: 'cycle', title: '保养周期', width: 100 },
            { key: 'year', title: '年度', width: 80 },
            { key: 'status', title: '状态', width: 100, render: (val) => val === '待生成' ? `<span class="badge bg-warning">${val}</span>` : `<span class="badge bg-success">${val}</span>` },
            { key: 'createdBy', title: '创建人', width: 100 },
            { key: 'createdAt', title: '创建日期', width: 120, sortable: true },
        ];

        this.dataTable = new DataTable({
            columns,
            data: this.mockData,
            options: {
                selectable: 'multiple',
                storageKey: 'maintenancePlanTable',
            }
        });
        this.dataTable.render(tableContainer);
    }
}
