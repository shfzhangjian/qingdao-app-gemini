/**
 * @file /js/views/SiTasks.js
 * @description 点检任务管理主视图 (适配 ZJZK_TASK)。
 */
import DataTable from '../components/Optimized_DataTable.js';
import Modal from '../components/Modal.js';
import DatePicker from '../components/DatePicker.js';
import { getSiTaskList, getAutocompleteOptions } from '../services/selfInspectionApi.js';
import SiTaskExecution from './SiTaskExecution.js';

export default class SiTasks {
    constructor() {
        this.container = null;
        this.listContainer = null;
        this.executionContainer = null;
        this.dataTable = null;
    }

    render(container) {
        this.container = container;
        container.innerHTML = `
            <div id="si-tasks-list-view" class="d-flex flex-column h-100">
                <div class="p-3 rounded mb-3" style="background-color: var(--bg-primary);">
                    <div class="p-3 mb-3 rounded border border-secondary" style="background-color: rgba(255,255,255,0.03);">
                        <div class="row g-3">
                            <div class="col-md-3 d-flex align-items-center">
                                <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary small">所属设备:</label>
                                <input type="text" class="form-control form-control-sm" id="q-sfname">
                            </div>
                            <div class="col-md-3 d-flex align-items-center">
                                <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary small">检查时间:</label>
                                <input type="text" class="form-control form-control-sm" id="q-checkTime">
                            </div>
                            <!-- 其他筛选暂略 -->
                        </div>
                    </div>
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3">
                        <div class="d-flex flex-wrap align-items-center gap-3">
                            ${this._renderButtonGroup('状态', 'checkStatus', [{v:'', l:'全部', c:true}, {v:'待检', l:'待检'}, {v:'已检', l:'已检'}])}
                        </div>
                        <div><button class="btn btn-sm btn-primary px-4" id="btn-query"><i class="bi bi-search"></i> 查询</button></div>
                    </div>
                </div>
                <div class="flex-grow-1 p-0 rounded d-flex flex-column" id="task-table-container" style="background-color: var(--bg-secondary); border: 1px solid var(--border-color); overflow: hidden;">
                    <div id="table-wrapper" class="flex-grow-1" style="overflow: hidden; display: flex; flex-direction: column;"></div>
                </div>
            </div>
            <div id="si-tasks-exec-view" class="d-none h-100" style="background-color: var(--bg-secondary);"></div>
        `;

        this.listContainer = container.querySelector('#si-tasks-list-view');
        this.executionContainer = container.querySelector('#si-tasks-exec-view');

        this._initDatePickers();
        this._initTable();
        this._attachEventListeners();
        this._loadData();
    }

    _renderButtonGroup(label, name, options) {
        const btns = options.map(opt => `
            <input type="radio" class="btn-check" name="${name}" id="${name}-${opt.v || 'all'}" value="${opt.v}" ${opt.c ? 'checked' : ''}>
            <label class="btn btn-sm btn-outline-secondary" for="${name}-${opt.v || 'all'}">${opt.l}</label>
        `).join('');
        return `<div class="d-flex align-items-center gap-2"><label class="form-label mb-0 text-secondary small text-nowrap">${label}:</label><div class="btn-group btn-group-sm" role="group">${btns}</div></div>`;
    }

    _initDatePickers() {
        const checkTimeInput = this.container.querySelector('#q-checkTime');
        new DatePicker(checkTimeInput, { mode: 'single' });
    }

    _initTable() {
        const columns = [
            { key: 'taskNo', title: '任务编号', width: 140 },
            { key: 'sjx', title: '所属机型', width: 100 },
            { key: 'sfname', title: '所属设备', width: 120 },
            { key: 'taskTime', title: '任务时间', width: 140 },
            { key: 'taskType', title: '任务类型', width: 100 },
            { key: 'checkStatus', title: '点检状态', width: 80, render: val => val === '待检' ? `<span class="text-warning">${val}</span>` : `<span class="text-success">${val}</span>` },
            { key: 'checker', title: '检查人', width: 80 },
            {
                key: 'operation', title: '操作', width: 100,
                render: (val, row) => `<button class="btn btn-sm btn-link text-info p-0 text-decoration-none btn-execute" data-id="${row.indocno}">执行</button>`
            }
        ];
        this.dataTable = new DataTable({ columns, data: [], options: { selectable: 'none', pagination: true, uniformRowHeight: true, storageKey: 'siTasksList_v5' } });
        this.dataTable.render(this.container.querySelector('#table-wrapper'));
    }

    async _loadData() {
        this.dataTable.toggleLoading(true);
        const getRadioVal = (name) => { const el = this.container.querySelector(`input[name="${name}"]:checked`); return el ? el.value : ''; };
        const params = {
            sfname: this.container.querySelector('#q-sfname').value,
            taskTime: this.container.querySelector('#q-checkTime').value,
            checkStatus: getRadioVal('checkStatus'),
            pageNum: this.dataTable.state.pageNum,
            pageSize: this.dataTable.state.pageSize
        };
        try {
            const result = await getSiTaskList(params);
            this.dataTable.updateView(result);
        } catch(e) { console.error(e); Modal.alert("加载任务失败"); }
        finally { this.dataTable.toggleLoading(false); }
    }

    _attachEventListeners() {
        this.container.querySelector('#btn-query').addEventListener('click', () => { this.dataTable.state.pageNum = 1; this._loadData(); });
        this.container.querySelectorAll('input[type="radio"]').forEach(radio => radio.addEventListener('change', () => this._loadData()));
        const tableWrapper = this.container.querySelector('#table-wrapper');
        tableWrapper.addEventListener('click', (e) => {
            const btn = e.target.closest('.btn-execute');
            if (btn) {
                e.preventDefault();
                const id = parseInt(btn.dataset.id);
                const rowData = this.dataTable.data.find(d => d.indocno === id);
                if(rowData) this._openExecutionView(rowData);
            }
        }, true);
    }

    _openExecutionView(taskInfo) {
        this.listContainer.classList.add('d-none');
        this.executionContainer.classList.remove('d-none');
        const executionView = new SiTaskExecution(this, taskInfo);
        executionView.render(this.executionContainer);
    }

    showList() {
        this.executionContainer.innerHTML = '';
        this.executionContainer.classList.add('d-none');
        this.listContainer.classList.remove('d-none');
        this._loadData();
    }
}