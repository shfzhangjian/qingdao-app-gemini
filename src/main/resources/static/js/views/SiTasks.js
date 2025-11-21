/**
 * @file /js/views/SiTasks.js
 * @description 点检任务管理主视图。
 * v2.1.0 - [UI] 布局深度优化：
 * 1. 输入框区域增加边框和背景包裹。
 * 2. 查询按钮移至快捷筛选行（第二行）的最右侧。
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
                <!-- 顶部查询面板 -->
                <div class="p-3 rounded mb-3" style="background-color: var(--bg-primary);">
                    
                    <!-- 第一行：详细筛选输入框 (被“框起来”的区域) -->
                    <div class="p-3 mb-3 rounded border border-secondary" style="background-color: rgba(255,255,255,0.03);">
                        <div class="row g-3">
                            <div class="col-md-3 d-flex align-items-center">
                                <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary small">检查设备:</label>
                                <input type="text" class="form-control form-control-sm" id="q-device" list="list-device">
                                <datalist id="list-device"></datalist>
                            </div>
                            <div class="col-md-3 d-flex align-items-center">
                                <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary small">检查时间:</label>
                                <input type="text" class="form-control form-control-sm" id="q-checkTime">
                            </div>
                            <div class="col-md-3 d-flex align-items-center">
                                <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary small">检查人:</label>
                                <input type="text" class="form-control form-control-sm" id="q-checker" list="list-checker">
                                <datalist id="list-checker"></datalist>
                            </div>
                            <div class="col-md-3 d-flex align-items-center">
                                <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary small">确认人:</label>
                                <input type="text" class="form-control form-control-sm" id="q-confirmer" list="list-confirmer">
                                <datalist id="list-confirmer"></datalist>
                            </div>
                        </div>
                    </div>

                    <!-- 第二行：快捷筛选行 + 查询按钮 (右侧) -->
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3">
                        <!-- 左侧：所有的快捷筛选按钮组 -->
                        <div class="d-flex flex-wrap align-items-center gap-3">
                            ${this._renderButtonGroup('生产状态', 'prodStatus', [{v:'', l:'全部', c:true}, {v:'生产', l:'生产'}, {v:'停产', l:'停产'}])}
                            ${this._renderButtonGroup('班别', 'shiftType', [{v:'', l:'全部', c:true}, {v:'甲班', l:'甲班'}, {v:'乙班', l:'乙班'}, {v:'丙班', l:'丙班'}, {v:'白班', l:'白班'}])}
                            ${this._renderButtonGroup('班次', 'shift', [{v:'', l:'全部', c:true}, {v:'早班', l:'早班'}, {v:'中班', l:'中班'}, {v:'晚班', l:'晚班'}])}
                            ${this._renderButtonGroup('点检状态', 'checkStatus', [{v:'', l:'全部', c:true}, {v:'待检', l:'待检'}, {v:'已检', l:'已检'}])}
                            ${this._renderButtonGroup('确认状态', 'confirmStatus', [{v:'', l:'全部', c:true}, {v:'待确认', l:'待确认'}, {v:'已确认', l:'已确认'}])}
                        </div>

                        <!-- 右侧：查询按钮 -->
                        <div>
                             <button class="btn btn-sm btn-primary px-4" id="btn-query"><i class="bi bi-search"></i> 查询</button>
                        </div>
                    </div>
                </div>

                <!-- 列表容器 -->
                <div class="flex-grow-1 p-0 rounded d-flex flex-column" id="task-table-container" style="background-color: var(--bg-secondary); border: 1px solid var(--border-color); overflow: hidden;">
                    <div id="table-wrapper" class="flex-grow-1" style="overflow: hidden; display: flex; flex-direction: column;"></div>
                </div>
            </div>
            
            <!-- 执行视图容器 (默认隐藏) -->
            <div id="si-tasks-exec-view" class="d-none h-100" style="background-color: var(--bg-secondary);"></div>
        `;

        this.listContainer = container.querySelector('#si-tasks-list-view');
        this.executionContainer = container.querySelector('#si-tasks-exec-view');

        this._initDatePickers();
        this._initAutocompletes();
        this._initTable();
        this._attachEventListeners();
        this._loadData();
    }

    _renderButtonGroup(label, name, options) {
        const btns = options.map(opt => `
            <input type="radio" class="btn-check" name="${name}" id="${name}-${opt.v || 'all'}" value="${opt.v}" ${opt.c ? 'checked' : ''}>
            <label class="btn btn-sm btn-outline-secondary" for="${name}-${opt.v || 'all'}">${opt.l}</label>
        `).join('');
        return `
            <div class="d-flex align-items-center gap-2">
                <label class="form-label mb-0 text-secondary small text-nowrap">${label}:</label>
                <div class="btn-group btn-group-sm" role="group">${btns}</div>
            </div>
        `;
    }

    _initDatePickers() {
        const checkTimeInput = this.container.querySelector('#q-checkTime');
        const today = new Date().toISOString().split('T')[0];
        checkTimeInput.value = today;
        new DatePicker(checkTimeInput, { mode: 'single' });
    }

    async _initAutocompletes() {
        const setup = async (field, id) => {
            const list = this.container.querySelector(`#${id}`);
            if(list) {
                try {
                    const options = await getAutocompleteOptions(field);
                    list.innerHTML = options.map(o => `<option value="${o}">`).join('');
                } catch(e) {}
            }
        };
        setup('device', 'list-device');
        setup('checker', 'list-checker');
        setup('confirmer', 'list-confirmer');
    }

    _initTable() {
        const columns = [
            { key: 'id', title: '序号', width: 60 },
            { key: 'model', title: '所属机型', width: 100 },
            { key: 'device', title: '所属设备', width: 120 },
            { key: 'prodStatus', title: '生产状态', width: 80 },
            { key: 'shiftType', title: '班别', width: 60 },
            { key: 'shift', title: '班次', width: 60 },
            {
                key: 'checkStatus', title: '点检状态', width: 80,
                render: val => val === '待检' ? `<span class="text-warning">${val}</span>` : `<span class="text-success">${val}</span>`
            },
            {
                key: 'confirmStatus', title: '确认状态', width: 80,
                render: val => val === '待确认' ? `<span class="text-warning">${val}</span>` : `<span class="text-success">${val}</span>`
            },
            { key: 'taskTime', title: '任务时间', width: 100 },
            { key: 'taskType', title: '任务类型', width: 100 },
            { key: 'isOverdue', title: '是否超期', width: 80 },
            { key: 'checker', title: '检查人', width: 80 },
            { key: 'confirmer', title: '确认人', width: 80 },
            {
                key: 'operation', title: '操作', width: 100,
                render: (val, row) => `<button class="btn btn-sm btn-link text-info p-0 text-decoration-none btn-execute" data-id="${row.id}">执行点检</button>`
            }
        ];

        this.dataTable = new DataTable({
            columns,
            data: [],
            options: {
                selectable: 'none',
                pagination: true,
                uniformRowHeight: true,
                storageKey: 'siTasksList_v4'
            }
        });
        this.dataTable.render(this.container.querySelector('#table-wrapper'));
    }

    async _loadData() {
        this.dataTable.toggleLoading(true);
        const getRadioVal = (name) => {
            const el = this.container.querySelector(`input[name="${name}"]:checked`);
            return el ? el.value : '';
        };

        const params = {
            prodStatus: getRadioVal('prodStatus'),
            shiftType: getRadioVal('shiftType'),
            shift: getRadioVal('shift'),
            checkStatus: getRadioVal('checkStatus'),
            confirmStatus: getRadioVal('confirmStatus'),
            device: this.container.querySelector('#q-device').value,
            checkTime: this.container.querySelector('#q-checkTime').value,
            checker: this.container.querySelector('#q-checker').value,
            confirmer: this.container.querySelector('#q-confirmer').value,
            pageNum: this.dataTable.state.pageNum,
            pageSize: this.dataTable.state.pageSize
        };

        try {
            const result = await getSiTaskList(params);
            this.dataTable.updateView(result);
        } catch(e) {
            console.error(e);
            Modal.alert("加载任务失败");
        } finally {
            this.dataTable.toggleLoading(false);
        }
    }

    _attachEventListeners() {
        // 查询按钮
        this.container.querySelector('#btn-query').addEventListener('click', () => {
            this.dataTable.state.pageNum = 1;
            this._loadData();
        });

        // 监听所有Radio Group的变化，触发自动查询
        this.container.querySelectorAll('input[type="radio"]').forEach(radio => {
            radio.addEventListener('change', () => this._loadData());
        });

        // 列表操作委托 (使用 Capture 模式)
        const tableWrapper = this.container.querySelector('#table-wrapper');
        tableWrapper.addEventListener('click', (e) => {
            const btn = e.target.closest('.btn-execute');
            if (btn) {
                e.preventDefault();
                const id = parseInt(btn.dataset.id);
                const rowData = this.dataTable.data.find(d => d.id === id);
                if(rowData) {
                    this._openExecutionView(rowData);
                }
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