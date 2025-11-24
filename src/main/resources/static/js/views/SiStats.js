/**
 * @file /js/views/SiStats.js
 * @description 点检统计界面视图。
 * v2.2.0 - [Fix] 修复日期列显示为 ISO 格式的问题，统一格式化为 yyyy-MM-dd HH:mm。
 */
import DataTable from '../components/Optimized_DataTable.js';
import Modal from '../components/Modal.js';
import DatePicker from '../components/DatePicker.js';
import { getSiStatsList, archiveSiData, getAutocompleteOptions } from '../services/selfInspectionApi.js';

// [新增] 日期格式化工具函数
const formatDate = (val) => {
    if (!val) return '-';
    const date = new Date(val);
    if (isNaN(date.getTime())) return val;

    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${year}-${month}-${day} ${hours}:${minutes}`;
};

export default class SiStats {
    constructor() {
        this.container = null;
        this.dataTable = null;
        this.datePicker = null;
        this.archiveDatePicker = null; // 归档弹窗用的日期选择器
    }

    render(container) {
        this.container = container;
        container.innerHTML = `
            <div class="d-flex flex-column h-100">
                <!-- 顶部查询区域 -->
                <div class="p-3 rounded mb-3" style="background-color: var(--bg-primary);">
                    
                    <!-- 第一行：输入框条件 (被“框起来”的区域) -->
                    <div class="p-3 mb-3 rounded border border-secondary" style="background-color: rgba(255,255,255,0.03);">
                        <div class="row g-3">
                            <div class="col-md-4 d-flex align-items-center">
                                <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary small">检查设备:</label>
                                <input type="text" class="form-control form-control-sm" id="qs-device" list="list-qs-device">
                                <datalist id="list-qs-device"></datalist>
                            </div>
                            <div class="col-md-4 d-flex align-items-center">
                                <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary small">检查时间:</label>
                                <input type="text" class="form-control form-control-sm" id="qs-checkTime">
                            </div>
                        </div>
                    </div>

                    <!-- 第二行：快捷筛选按钮组 (左侧) + 操作按钮 (右侧) -->
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-3">
                        
                        <!-- 左侧：所有的快捷筛选按钮组 -->
                        <div class="d-flex flex-wrap align-items-center gap-3">
                            ${this._renderButtonGroup('生产状态', 'qs-prodStatus', [{v:'', l:'全部', c:true}, {v:'生产', l:'生产'}, {v:'停产', l:'停产'}])}
                            ${this._renderButtonGroup('班别', 'qs-shiftType', [{v:'', l:'全部', c:true}, {v:'甲班', l:'甲班'}, {v:'乙班', l:'乙班'}, {v:'丙班', l:'丙班'}, {v:'白班', l:'白班'}])}
                            ${this._renderButtonGroup('班次', 'qs-shift', [{v:'', l:'全部', c:true}, {v:'早班', l:'早班'}, {v:'中班', l:'中班'}, {v:'晚班', l:'晚班'}])}
                            ${this._renderButtonGroup('点检状态', 'qs-checkStatus', [{v:'', l:'全部', c:true}, {v:'正常', l:'正常'}, {v:'异常', l:'异常'}])}
                        </div>

                        <!-- 右侧：操作按钮组 -->
                        <div class="d-flex gap-2">
                             <button class="btn btn-sm btn-primary px-3" id="btn-query"><i class="bi bi-search"></i> 查询</button>
                             <button class="btn btn-sm btn-success px-3" id="btn-export"><i class="bi bi-file-earmark-excel"></i> 导出</button>
                             <button class="btn btn-sm btn-warning px-3 text-dark" id="btn-archive"><i class="bi bi-archive"></i> 归档</button>
                        </div>
                    </div>
                </div>

                <!-- 列表容器 (Flex 布局，表格撑满剩余空间，分页在底端) -->
                <div class="flex-grow-1 p-0 rounded d-flex flex-column" id="stats-table-container" style="background-color: var(--bg-secondary); border: 1px solid var(--border-color); overflow: hidden;">
                    <div id="table-wrapper" class="flex-grow-1" style="overflow: hidden; display: flex; flex-direction: column;"></div>
                </div>
            </div>
        `;

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
        const checkTimeInput = this.container.querySelector('#qs-checkTime');
        const end = new Date();
        const start = new Date();
        start.setMonth(start.getMonth() - 1);
        // const formatDate = d => d.toISOString().split('T')[0]; // Remove local var to avoid conflict
        const formatDateStr = d => d.getFullYear() + "-" + String(d.getMonth()+1).padStart(2,'0') + "-" + String(d.getDate()).padStart(2,'0');
        checkTimeInput.value = `${formatDateStr(start)} 至 ${formatDateStr(end)}`;
        this.datePicker = new DatePicker(checkTimeInput, { mode: 'range' });
    }

    async _initAutocompletes() {
        const list = this.container.querySelector('#list-qs-device');
        if(list) {
            try {
                const options = await getAutocompleteOptions('device');
                list.innerHTML = options.map(o => `<option value="${o}">`).join('');
            } catch(e) {}
        }
    }

    _initTable() {
        const columns = [
            // [修改] 添加 render: formatDate
            { key: 'checkTime', title: '检查时间', width: 140, render: formatDate },
            { key: 'device', title: '检查设备', width: 120 },
            { key: 'itemName', title: '检查项目名称', width: 150, render: val => `<div class="text-start">${val}</div>` },
            {
                key: 'result', title: '检查结果', width: 80,
                render: val => val === '异常' ? `<span class="text-danger"><i class="bi bi-circle-fill small me-1"></i>异常</span>` : (val === '正常' ? `<span class="text-success"><i class="bi bi-circle-fill small me-1"></i>正常</span>` : val)
            },
            { key: 'remarks', title: '检查说明', width: 120 },
            { key: 'prodStatus', title: '生产状态', width: 80 },
            { key: 'shift', title: '班次', width: 60 },
            { key: 'shiftType', title: '班别', width: 60 },
            { key: 'taskType', title: '任务类型', width: 100 },
            // [修改] 添加 render: formatDate
            { key: 'actualCheckTime', title: '实际检查时间', width: 140, render: formatDate },
            { key: 'checker', title: '检查人', width: 80 },
            { key: 'confirmStatus', title: '确认状态', width: 80 },
            // [修改] 添加 render: formatDate
            { key: 'confirmTime', title: '确认时间', width: 140, render: formatDate },
            { key: 'confirmer', title: '确认人', width: 80 },
        ];

        this.dataTable = new DataTable({
            columns,
            data: [],
            options: {
                selectable: 'none',
                pagination: true,
                uniformRowHeight: true,
                storageKey: 'siStatsList'
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
            prodStatus: getRadioVal('qs-prodStatus'),
            shiftType: getRadioVal('qs-shiftType'),
            shift: getRadioVal('qs-shift'),
            checkStatus: getRadioVal('qs-checkStatus'),
            device: this.container.querySelector('#qs-device').value,
            checkTime: this.container.querySelector('#qs-checkTime').value,
            pageNum: this.dataTable.state.pageNum,
            pageSize: this.dataTable.state.pageSize
        };

        try {
            const result = await getSiStatsList(params);
            this.dataTable.updateView(result);
        } catch(e) {
            console.error(e);
            Modal.alert("加载统计数据失败");
        } finally {
            this.dataTable.toggleLoading(false);
        }
    }

    _attachEventListeners() {
        this.container.querySelector('#btn-query').addEventListener('click', () => {
            this.dataTable.state.pageNum = 1;
            this._loadData();
        });

        this.container.querySelectorAll('input[type="radio"]').forEach(radio => {
            radio.addEventListener('change', () => this._loadData());
        });

        this.container.querySelector('#table-wrapper').addEventListener('pageChange', (e) => {
            this.dataTable.state.pageNum = e.detail.pageNum;
            this._loadData();
        });

        // 导出
        this.container.querySelector('#btn-export').addEventListener('click', () => {
            const btn = this.container.querySelector('#btn-export');
            const originalText = btn.innerHTML;
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 导出中...';
            setTimeout(() => {
                btn.disabled = false;
                btn.innerHTML = originalText;
                Modal.alert("点检统计数据导出成功！(模拟)");
            }, 1500);
        });

        // 归档
        this.container.querySelector('#btn-archive').addEventListener('click', () => this._openArchiveModal());
    }

    // --- 归档模态框 ---
    _openArchiveModal() {
        const bodyHtml = `
            <form id="archive-form">
                <div class="mb-3">
                    <label for="arc-device" class="form-label">选择设备</label>
                    <input type="text" class="form-control" id="arc-device" list="list-arc-device" placeholder="请输入设备名称...">
                    <datalist id="list-arc-device"></datalist>
                </div>
                <div class="mb-3">
                    <label for="arc-taskType" class="form-label">任务类型</label>
                    <select class="form-select" id="arc-taskType">
                        <option value="三班电气">三班电气 (默认1个月)</option>
                        <option value="白班电气">白班电气 (默认半年)</option>
                    </select>
                </div>
                <div class="mb-3">
                    <label for="arc-dateRange" class="form-label">归档日期</label>
                    <input type="text" class="form-control" id="arc-dateRange">
                </div>
            </form>
        `;

        const footerHtml = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" id="btn-confirm-archive">确定</button>
        `;

        const modal = new Modal({ title: "数据归档", body: bodyHtml, footer: footerHtml });

        const dateInput = modal.modalElement.querySelector('#arc-dateRange');
        const taskTypeSelect = modal.modalElement.querySelector('#arc-taskType');

        // 初始化日期选择器
        const setDateRange = (months) => {
            const end = new Date();
            const start = new Date();
            start.setMonth(start.getMonth() - months);
            const formatDateStr = d => d.getFullYear() + "-" + String(d.getMonth()+1).padStart(2,'0') + "-" + String(d.getDate()).padStart(2,'0');
            if (this.archiveDatePicker && this.archiveDatePicker.instance) {
                this.archiveDatePicker.instance.setDate([start, end]);
            } else {
                dateInput.value = `${formatDateStr(start)} 至 ${formatDateStr(end)}`;
            }
        };

        this.archiveDatePicker = new DatePicker(dateInput, { mode: 'range' });
        setDateRange(1); // 默认1个月

        // 初始化设备自动补全
        const initArchiveDeviceAutocomplete = async () => {
            const list = modal.modalElement.querySelector('#list-arc-device');
            if(list) {
                try {
                    const options = await getAutocompleteOptions('device');
                    list.innerHTML = options.map(o => `<option value="${o}">`).join('');
                } catch(e) {}
            }
        };
        initArchiveDeviceAutocomplete();

        // 联动日期
        taskTypeSelect.addEventListener('change', (e) => {
            if (e.target.value === '三班电气') {
                setDateRange(1);
            } else if (e.target.value === '白班电气') {
                setDateRange(6);
            }
        });

        // 确认归档
        modal.modalElement.querySelector('#btn-confirm-archive').addEventListener('click', async () => {
            const btn = modal.modalElement.querySelector('#btn-confirm-archive');
            const deviceVal = modal.modalElement.querySelector('#arc-device').value;

            if (!deviceVal) {
                Modal.alert("请选择设备！");
                return;
            }

            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 处理中...';

            const params = {
                device: deviceVal,
                taskType: taskTypeSelect.value,
                dateRange: dateInput.value
            };

            try {
                const result = await archiveSiData(params);
                modal.hide();
                Modal.alert(result.message);
            } catch (e) {
                Modal.alert("归档失败");
            } finally {
                btn.disabled = false;
            }
        });

        modal.show();
    }
}