/**
 * @file /js/views/SiTasks.js
 * @description 点检任务管理主视图 (适配 ZJZK_TASK)。
 * v2.1.0 - [Fix] 修正查询参数名以匹配后端数据库字段，解决筛选无效问题。
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
                    
                    <!-- 第一行：文本输入查询条件 (带下拉补全) -->
                    <div class="p-3 mb-3 rounded border border-secondary" style="background-color: rgba(255,255,255,0.03);">
                        <div class="row g-3">
                            <div class="col-md-3 d-flex align-items-center">
                                <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary small" style="width: 70px; text-align: right;">检查设备:</label>
                                <input type="text" class="form-control form-control-sm" id="q-sfname" list="list-device" placeholder="请输入设备名称">
                                <datalist id="list-device"></datalist>
                            </div>
                            <div class="col-md-3 d-flex align-items-center">
                                <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary small" style="width: 70px; text-align: right;">任务时间:</label>
                                <input type="text" class="form-control form-control-sm" id="q-taskTime" placeholder="选择日期">
                            </div>
                            <div class="col-md-3 d-flex align-items-center">
                                <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary small" style="width: 70px; text-align: right;">检查人:</label>
                                <input type="text" class="form-control form-control-sm" id="q-checker" list="list-checker" placeholder="姓名/工号">
                                <datalist id="list-checker"></datalist>
                            </div>
                            <div class="col-md-3 d-flex align-items-center">
                                <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary small" style="width: 70px; text-align: right;">确认人:</label>
                                <input type="text" class="form-control form-control-sm" id="q-confirmer" list="list-confirmer" placeholder="姓名/工号">
                                <datalist id="list-confirmer"></datalist>
                            </div>
                        </div>
                    </div>

                    <!-- 第二行：单选筛选组 + 查询按钮 -->
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3">
                        <div class="d-flex flex-wrap align-items-center gap-3 row-gap-2">
                            ${this._renderButtonGroup('生产状态', 'prodStatus', [{v:'', l:'全部', c:true}, {v:'生产', l:'生产'}, {v:'停产', l:'停产'}])}
                            ${this._renderButtonGroup('班别', 'shiftType', [{v:'', l:'全部', c:true}, {v:'甲班', l:'甲班'}, {v:'乙班', l:'乙班'}, {v:'丙班', l:'丙班'}, {v:'白班', l:'白班'}])}
                            ${this._renderButtonGroup('班次', 'shift', [{v:'', l:'全部', c:true}, {v:'早班', l:'早班'}, {v:'中班', l:'中班'}, {v:'晚班', l:'晚班'}])}
                            ${this._renderButtonGroup('点检状态', 'checkStatus', [{v:'', l:'全部', c:true}, {v:'待检', l:'待检'}, {v:'已检', l:'已检'}])}
                            ${this._renderButtonGroup('确认状态', 'confirmStatus', [{v:'', l:'全部', c:true}, {v:'待确认', l:'待确认'}, {v:'已确认', l:'已确认'}])}
                        </div>
                        <div><button class="btn btn-sm btn-primary px-4" id="btn-query"><i class="bi bi-search"></i> 查询</button></div>
                    </div>
                </div>

                <!-- 表格区域 -->
                <div class="flex-grow-1 p-0 rounded d-flex flex-column" id="task-table-container" style="background-color: var(--bg-secondary); border: 1px solid var(--border-color); overflow: hidden;">
                    <div id="table-wrapper" class="flex-grow-1" style="overflow: hidden; display: flex; flex-direction: column;"></div>
                </div>
            </div>
            
            <!-- 任务执行子视图容器 -->
            <div id="si-tasks-exec-view" class="d-none h-100" style="background-color: var(--bg-secondary);"></div>
        `;

        this.listContainer = container.querySelector('#si-tasks-list-view');
        this.executionContainer = container.querySelector('#si-tasks-exec-view');

        this._initDatePickers();
        this._initAutocompletes(); // 初始化下拉补全
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
        const checkTimeInput = this.container.querySelector('#q-taskTime');
        // 默认选中今天
        const today = new Date().toISOString().split('T')[0];
        checkTimeInput.value = today;
        new DatePicker(checkTimeInput, { mode: 'single', defaultDate: today });
    }

    async _initAutocompletes() {
        const fillDatalist = async (field, listId) => {
            const list = this.container.querySelector(`#${listId}`);
            if(!list) return;
            try {
                // 调用后端通用接口获取选项
                // 对应后端: /api/si/ledger/options?field=...
                const options = await getAutocompleteOptions(field);
                list.innerHTML = options.map(opt => `<option value="${opt}">`).join('');
            } catch(e){
                console.warn(`加载 ${field} 下拉选项失败`, e);
            }
        };

        // 并行加载所有下拉数据
        fillDatalist('device', 'list-device');    // 对应后端 field=device (映射为 SFNAME)
        fillDatalist('checker', 'list-checker');  // 对应后端 field=checker
        fillDatalist('confirmer', 'list-confirmer'); // 对应后端 field=confirmer
    }

    _initTable() {
        const columns = [
            { key: '_index', title: '序号', width: 60, frozen: 'left' },
            { key: 'sjx', title: '所属机型', width: 120 },
            { key: 'sfname', title: '所属设备', width: 150 },
            { key: 'prodStatus', title: '生产状态', width: 80 },
            { key: 'shiftType', title: '班别', width: 60 },
            { key: 'shift', title: '班次', width: 60 },
            { key: 'checkStatus', title: '点检状态', width: 80, render: val => val === '待检' ? `<span class="text-warning">${val}</span>` : `<span class="text-success">${val}</span>` },
            { key: 'confirmStatus', title: '确认状态', width: 80, render: val => val === '待确认' ? `<span class="text-warning">${val}</span>` : `<span class="text-success">${val}</span>` },
            { key: 'taskTime', title: '任务时间', width: 140 },
            { key: 'taskType', title: '任务类型', width: 100 },
            { key: 'isOverdue', title: '是否超期', width: 80 },
            { key: 'checker', title: '检查人', width: 80 },
            { key: 'confirmer', title: '确认人', width: 80 },
            {
                key: 'operation', title: '操作', width: 160, frozen: 'right',
                render: (val, row) => `
                    <div class="d-flex gap-2 justify-content-center">
                        <button class="btn btn-sm py-0 btn-outline-info btn-execute" data-id="${row.indocno}" data-role="inspector">执行点检</button>
                        <button class="btn btn-sm py-0 btn-outline-warning btn-execute" data-id="${row.indocno}" data-role="operator">执行确认</button>
                    </div>
                `
            }
        ];
        this.dataTable = new DataTable({ columns, data: [], options: { selectable: 'none', pagination: true, uniformRowHeight: true, storageKey: 'siTasksList_v10' } });
        this.dataTable.render(this.container.querySelector('#table-wrapper'));
    }

    async _loadData() {
        this.dataTable.toggleLoading(true);
        const getRadioVal = (name) => { const el = this.container.querySelector(`input[name="${name}"]:checked`); return el ? el.value : ''; };

        // [Fix] 修改参数名以匹配 ZjzkTaskMapper.xml 中的参数
        const params = {
            sfname: this.container.querySelector('#q-sfname').value, // 改为 sfname
            taskTime: this.container.querySelector('#q-taskTime').value, // 改为 taskTime
            checker: this.container.querySelector('#q-checker').value,
            confirmer: this.container.querySelector('#q-confirmer').value,
            prodStatus: getRadioVal('prodStatus'),
            shiftType: getRadioVal('shiftType'),
            shift: getRadioVal('shift'),
            checkStatus: getRadioVal('checkStatus'),
            confirmStatus: getRadioVal('confirmStatus'),
            pageNum: this.dataTable.state.pageNum,
            pageSize: this.dataTable.state.pageSize
        };

        try {
            const result = await getSiTaskList(params);

            if (result && result.list) {
                const start = (result.pageNum - 1) * result.pageSize;
                result.list.forEach((item, index) => {
                    item._index = start + index + 1;
                    item.id = item.indocno;
                    if (item.taskTime && item.taskTime.length > 10) {
                        item.taskTime = item.taskTime.substring(0, 10);
                    }
                });
            }

            this.dataTable.updateView(result);
        } catch(e) {
            console.error(e);
            Modal.alert("加载任务失败: " + e.message);
        } finally {
            this.dataTable.toggleLoading(false);
        }
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
                const role = btn.dataset.role;
                const rowData = this.dataTable.data.find(d => d.indocno === id);
                if(rowData) this._openExecutionView(rowData, role);
            }
        }, true);
    }

    _openExecutionView(taskInfo, role) {
        this.listContainer.classList.add('d-none');
        this.executionContainer.classList.remove('d-none');
        const executionView = new SiTaskExecution(this, taskInfo, role);
        executionView.render(this.executionContainer);
    }

    showList() {
        this.executionContainer.innerHTML = '';
        this.executionContainer.classList.add('d-none');
        this.listContainer.classList.remove('d-none');
        this._loadData();
    }
}