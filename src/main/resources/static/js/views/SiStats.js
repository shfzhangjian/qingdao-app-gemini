/**
 * @file /js/views/SiStats.js
 * @description 点检统计界面视图。
 * v2.7.0 - [Feature] 实现真实的归档报表预览与导出功能 (适配主题)。
 */
import DataTable from '../components/Optimized_DataTable.js';
import Modal from '../components/Modal.js';
import DatePicker from '../components/DatePicker.js';
import { getSiStatsList, getAutocompleteOptions, exportSiStats, getArchiveReportData, exportArchiveReport, getTaskGenerationDeviceList } from '../services/selfInspectionApi.js';

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
        this.selectedArchiveDevice = null; // 归档选中的设备对象 {spmcode, name}
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
                                <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary small">任务日期:</label>
                                <input type="text" class="form-control form-control-sm" id="qs-taskTimeRange">
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
                            ${this._renderButtonGroup('检查结果', 'qs-checkStatus', [{v:'', l:'全部', c:true}, {v:'正常', l:'正常'}, {v:'异常', l:'异常'}])}
                        </div>

                        <!-- 右侧：操作按钮组 -->
                        <div class="d-flex gap-2">
                             <button class="btn btn-sm btn-primary px-3" id="btn-query"><i class="bi bi-search"></i> 查询</button>
                             <button class="btn btn-sm btn-success px-3" id="btn-export"><i class="bi bi-file-earmark-excel"></i> 导出</button>
                             <button class="btn btn-sm btn-warning px-3 text-dark" id="btn-archive"><i class="bi bi-archive"></i> 归档/报表</button>
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
        const checkTimeInput = this.container.querySelector('#qs-taskTimeRange');
        const end = new Date();
        const start = new Date();
        start.setMonth(start.getMonth() - 1);
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
            { key: '_index', title: '序号', width: 60, frozen: 'left' }, // [新增] 虚拟序号列
            { key: 'checkTime', title: '任务日期', width: 120, render: (val) => val ? val.substring(0, 10) : '-' },
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
            { key: 'actualCheckTime', title: '实际检查时间', width: 140, render: formatDate },
            { key: 'checker', title: '检查人', width: 80 },
            { key: 'confirmStatus', title: '确认状态', width: 80 },
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
                storageKey: 'siStatsList_v3' // Update key to force reload columns
            }
        });
        this.dataTable.render(this.container.querySelector('#table-wrapper'));
    }

    // [新增] 提取获取查询参数的逻辑
    _getQueryParams() {
        const getRadioVal = (name) => {
            const el = this.container.querySelector(`input[name="${name}"]:checked`);
            return el ? el.value : '';
        };

        const timeRangeStr = this.container.querySelector('#qs-taskTimeRange').value;
        let startDate = '', endDate = '';
        if (timeRangeStr && timeRangeStr.includes(' 至 ')) {
            const parts = timeRangeStr.split(' 至 ');
            startDate = parts[0];
            endDate = parts[1];
        } else {
            startDate = timeRangeStr;
            endDate = timeRangeStr;
        }

        return {
            prodStatus: getRadioVal('qs-prodStatus'),
            shiftType: getRadioVal('qs-shiftType'),
            shift: getRadioVal('qs-shift'),
            checkStatus: getRadioVal('qs-checkStatus'),
            device: this.container.querySelector('#qs-device').value,
            startDate: startDate,
            endDate: endDate
        };
    }

    async _loadData() {
        this.dataTable.toggleLoading(true);

        const params = {
            ...this._getQueryParams(),
            pageNum: this.dataTable.state.pageNum,
            pageSize: this.dataTable.state.pageSize
        };

        try {
            const result = await getSiStatsList(params);

            // [新增] 计算虚拟序号
            if (result && result.list) {
                const start = (result.pageNum - 1) * result.pageSize;
                result.list.forEach((item, index) => {
                    item._index = start + index + 1;
                    // 顺便处理一下日期格式，虽然 column render 也处理了，但在这里处理数据源更稳妥
                    if (item.checkTime && item.checkTime.length > 10) {
                        item.checkTime = item.checkTime.substring(0, 10);
                    }
                });
            }

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

        // [Fix] 修改监听事件为 'queryChange'，以适配 Optimized_DataTable.js
        this.container.querySelector('#table-wrapper').addEventListener('queryChange', (e) => {
            if (e.detail.pageNum) {
                this.dataTable.state.pageNum = e.detail.pageNum;
                this._loadData();
            }
        });

        // 导出
        this.container.querySelector('#btn-export').addEventListener('click', async () => {
            const btn = this.container.querySelector('#btn-export');
            const originalText = btn.innerHTML;
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 导出中...';

            try {
                const params = {
                    ...this._getQueryParams(),
                    // 排除序号列，只导出实际数据列
                    columns: this.dataTable.columns
                        .filter(c => c.visible && c.key !== '_index')
                        .map(c => ({ key: c.key, title: c.title }))
                };
                await exportSiStats(params);
            } catch (e) {
                console.error(e);
                Modal.alert("导出失败: " + e.message);
            } finally {
                btn.disabled = false;
                btn.innerHTML = originalText;
            }
        });

        // 归档
        this.container.querySelector('#btn-archive').addEventListener('click', () => this._openArchiveModal());
    }

    // --- 归档/报表生成模态框 ---
    _openArchiveModal() {
        const bodyHtml = `
            <div class="row g-3">
                <div class="col-md-12">
                    <label for="arc-device" class="form-label">选择机台 (必须选择)</label>
                    <input type="text" class="form-control" id="arc-device" list="list-arc-device" placeholder="输入机台名称或编号...">
                    <datalist id="list-arc-device"></datalist>
                    <input type="hidden" id="arc-spmcode">
                </div>
                <div class="col-md-6">
                    <label for="arc-taskType" class="form-label">任务类型</label>
                    <select class="form-select" id="arc-taskType">
                        <option value="三班电气" selected>三班电气</option>
                        <option value="白班电气">白班电气</option>
                        <option value="年检">年检</option>
                    </select>
                </div>
                <div class="col-md-6">
                    <label for="arc-dateRange" class="form-label">报表月份 / 范围</label>
                    <input type="text" class="form-control" id="arc-dateRange">
                </div>
            </div>
            
            <hr>
            
            <div id="report-preview-container" class="border rounded p-2 d-none" style="max-height: 500px; overflow: auto; background-color: var(--bg-secondary); border-color: var(--border-color) !important;">
                <!-- 报表预览区 -->
            </div>
        `;

        const footerHtml = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" id="btn-preview-report">预览</button>
            <button type="button" class="btn btn-success d-none" id="btn-export-excel"><i class="bi bi-file-earmark-excel"></i> 导出 Excel</button>
        `;

        const modal = new Modal({ title: "自检自控归档报表", body: bodyHtml, footer: footerHtml, size: 'xl' });

        // 自动补全处理
        const deviceInput = modal.modalElement.querySelector('#arc-device');
        const spmcodeInput = modal.modalElement.querySelector('#arc-spmcode');
        const datalist = modal.modalElement.querySelector('#list-arc-device');

        // 加载设备列表 (包含 PM 编码)
        getTaskGenerationDeviceList({ pageSize: 500 }).then(res => {
            if(res && res.list) {
                const devices = res.list;
                datalist.innerHTML = devices.map(d => `<option value="${d.sfname} (${d.spmcode})">`).join('');

                deviceInput.addEventListener('input', () => {
                    const val = deviceInput.value;
                    const match = val.match(/\((.*?)\)$/);
                    if (match) {
                        spmcodeInput.value = match[1];
                    } else {
                        const found = devices.find(d => d.sfname === val || d.spmcode === val);
                        if (found) spmcodeInput.value = found.spmcode;
                        else spmcodeInput.value = "";
                    }
                });
            }
        });

        // 日期选择器
        const dateInput = modal.modalElement.querySelector('#arc-dateRange');
        const taskTypeSelect = modal.modalElement.querySelector('#arc-taskType');

        const now = new Date();
        const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
        const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0);

        const fp = new DatePicker(dateInput, {
            mode: 'range',
            defaultDate: [startOfMonth, endOfMonth]
        });

        // 按钮事件
        const previewBtn = modal.modalElement.querySelector('#btn-preview-report');
        const exportBtn = modal.modalElement.querySelector('#btn-export-excel');
        const previewContainer = modal.modalElement.querySelector('#report-preview-container');

        const getRequestData = () => {
            return {
                spmcode: spmcodeInput.value,
                deviceName: deviceInput.value.split(' (')[0],
                taskType: taskTypeSelect.value,
                dateRange: dateInput.value
            };
        };

        previewBtn.addEventListener('click', async () => {
            const req = getRequestData();
            if (!req.spmcode) {
                Modal.alert("请选择有效的机台（必须包含PM编号）！");
                return;
            }

            previewBtn.disabled = true;
            previewBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 加载中...';

            try {
                const data = await getArchiveReportData(req);
                this._renderReportPreview(previewContainer, data);
                previewContainer.classList.remove('d-none');
                exportBtn.classList.remove('d-none');
            } catch (e) {
                Modal.alert("生成报表失败: " + e.message);
                previewContainer.classList.add('d-none');
                exportBtn.classList.add('d-none');
            } finally {
                previewBtn.disabled = false;
                previewBtn.innerHTML = '预览';
            }
        });

        exportBtn.addEventListener('click', async () => {
            const req = getRequestData();
            exportBtn.disabled = true;
            exportBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 导出中...';
            try {
                await exportArchiveReport(req);
            } catch (e) {
                Modal.alert("导出失败: " + e.message);
            } finally {
                exportBtn.disabled = false;
                exportBtn.innerHTML = '<i class="bi bi-file-earmark-excel"></i> 导出 Excel';
            }
        });

        modal.show();
    }

    /**
     * 渲染报表 HTML 预览 (支持深浅色主题)
     */
    _renderReportPreview(container, data) {
        if (!data || !data.rows || data.rows.length === 0) {
            container.innerHTML = '<div class="text-center p-4 text-muted">该时间段内无数据</div>';
            return;
        }

        // 构建日期列头 (1..31)
        let dayHeaders = '';
        for (let i = 1; i <= 31; i++) {
            dayHeaders += `<th class="text-center p-0 align-middle" style="min-width: 25px; font-size: 0.8rem;">${i}</th>`;
        }

        // 构建数据行
        const rowsHtml = data.rows.map(row => {
            let cells = '';
            for (let i = 1; i <= 31; i++) {
                const res = (row.dailyResults && row.dailyResults[i]) || '';
                // 样式处理
                let styleClass = '';
                if (res === '√') styleClass = 'text-success fw-bold';
                else if (res === '×') styleClass = 'text-danger fw-bold';

                cells += `<td class="text-center p-0 align-middle ${styleClass}" style="font-size: 0.8rem; height: 30px;">${res}</td>`;
            }
            return `
                <tr>
                    <td class="text-center align-middle">${row.seq}</td>
                    <td class="text-nowrap align-middle" style="font-size: 0.85rem;" title="${row.itemName}">${row.itemName}</td>
                    ${cells}
                </tr>
            `;
        }).join('');

        // 构建签字行
        const buildSignRow = (label, signMap) => {
            let cells = '';
            for (let i = 1; i <= 31; i++) {
                const name = (signMap && signMap[i]) || '';
                // 竖排显示模拟
                cells += `<td class="text-center p-1 align-bottom" style="font-size: 0.75rem; height: 100px;">
                    <div style="writing-mode: vertical-lr; margin: 0 auto; letter-spacing: 2px; text-orientation: upright;">${name}</div>
                </td>`;
            }
            return `<tr><td colspan="2" class="text-center fw-bold align-middle">${label}</td>${cells}</tr>`;
        };

        const checkerRow = buildSignRow("检查人签字", data.checkerSigns);
        const operatorRow = buildSignRow("操作工确认签字", data.operatorSigns);

        // 使用CSS变量来控制颜色，确保深色/浅色主题下的可见性
        const tableStyle = `
            width: 100%; 
            border-collapse: collapse; 
            color: var(--text-primary);
            border-color: var(--border-color);
        `;

        const html = `
            <div class="text-center mb-3" style="color: var(--text-primary);">
                <h5 class="fw-bold mb-2">${data.title}</h5>
                <div class="d-flex justify-content-between px-2 small border-bottom pb-2" style="border-color: var(--border-color) !important;">
                    <span><strong>日期:</strong> ${data.yearMonth}</span>
                    <span><strong>机台:</strong> ${data.machineName}</span>
                    <span><strong>编号:</strong> JL-QD SG 190</span>
                </div>
            </div>
            
            <div class="table-responsive">
                <table class="table table-bordered table-sm mb-0" style="${tableStyle}">
                    <thead style="background-color: var(--bg-primary);">
                        <tr>
                            <th class="text-center align-middle" rowspan="2" style="width: 40px;">序号</th>
                            <th class="text-center align-middle" rowspan="2" style="min-width: 150px;">检测装置</th>
                            <th class="text-center align-middle" colspan="31">检查结果 (第一行填写日期，并对应进行记录)</th>
                        </tr>
                        <tr>
                            ${dayHeaders}
                        </tr>
                    </thead>
                    <tbody>
                        ${rowsHtml}
                        ${checkerRow}
                        ${operatorRow}
                    </tbody>
                </table>
            </div>
            
            <div class="mt-3 small px-2 border-top pt-2" style="color: var(--text-secondary); border-color: var(--border-color) !important;">
                <div class="d-flex mb-1">
                    <span class="fw-bold me-2">备注1:</span> 
                    <span>停 / 休</span>
                </div>
                <div class="d-flex">
                    <span class="fw-bold me-2">备注2:</span> 
                    <span>当班电工对包机自检自控项目进行检查，检测项目正常打“√”，异常打“×”，并注明相应处理措施，不能进行项目打“/”杠掉。检查后签字确认。保存期1年。</span>
                </div>
            </div>
        `;

        container.innerHTML = html;
    }
}