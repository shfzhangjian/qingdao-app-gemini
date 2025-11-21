/**
 * @file /js/views/SiTaskExecution.js
 * @description 点检任务执行详情视图。
 * v1.9.0 - [Style Fix]
 * 1. 注入局部 CSS 重新定义 table-hover 样式。
 * 2. 修复暗色主题下 hover 背景色过暗导致看不清的问题（改为变亮）。
 * 3. 强制所有单元格文字颜色跟随主题变量。
 */
import Modal from '../components/Modal.js';
import { getSiTaskDetails, saveSiTaskDetails } from '../services/selfInspectionApi.js';

export default class SiTaskExecution {
    constructor(parentView, taskInfo) {
        this.parentView = parentView;
        this.taskInfo = taskInfo;
        this.container = null;
        this.details = [];
        this.currentRole = 'inspector';
    }

    render(container) {
        this.container = container;

        // [核心修复] 注入自定义样式，覆盖 Bootstrap 的默认 hover 行为
        // 1. 默认(暗色)下：hover 背景变亮 (rgba(255,255,255,0.05))
        // 2. 亮色(theme-light)下：hover 背景变暗 (rgba(0,0,0,0.05))
        const customStyles = `
            <style>
                /* 强制表格内容颜色跟随主题变量 */
                #exec-table tbody td, 
                #exec-table tbody input.form-control-sm {
                    color: var(--text-primary) !important;
                    border-color: var(--border-color) !important;
                }

                /* --- 暗色主题 (默认) Hover 样式 --- */
                #exec-table.table-hover tbody tr:hover > * {
                    --bs-table-accent-bg: rgba(255, 255, 255, 0.08) !important; /* 背景稍微变亮 */
                    color: #ffffff !important; /* 悬停时文字高亮 */
                    background-color: var(--bs-table-accent-bg); /* 兼容性处理 */
                }

                /* --- 亮色主题 Hover 样式 --- */
                body.theme-light #exec-table.table-hover tbody tr:hover > * {
                    --bs-table-accent-bg: rgba(0, 0, 0, 0.05) !important; /* 背景稍微变暗 */
                    color: #000000 !important;
                }

                /* 修复输入框在 hover 时的背景 */
                #exec-table tbody tr:hover input.form-control-sm {
                    background-color: transparent !important; 
                }
            </style>
        `;

        container.innerHTML = `
            ${customStyles}
            <div class="d-flex flex-column h-100">
                <!-- 顶部栏 -->
                <div class="d-flex justify-content-between align-items-center p-3 border-bottom" style="background-color: var(--bg-primary); color: var(--text-primary);">
                    <div class="d-flex align-items-center gap-3">
                        <h5 class="mb-0" style="color: inherit;"><i class="bi bi-clipboard-check me-2"></i>执行点检</h5>
                        <span class="badge bg-secondary">${this.taskInfo.model || '机型未知'}</span>
                        <span class="badge bg-primary">${this.taskInfo.device}</span>
                        <span class="badge bg-info text-dark">${this.taskInfo.taskType}</span>
                    </div>
                    
                    <!-- 角色模拟 -->
                    <div class="d-flex align-items-center gap-2 rounded p-1 px-3 border" style="border-color: var(--border-color) !important;">
                        <span class="small text-secondary"><i class="bi bi-person-bounding-box"></i> 模拟角色:</span>
                        <div class="btn-group btn-group-sm" role="group">
                            <input type="radio" class="btn-check" name="role-switch" id="role-inspector" value="inspector" checked>
                            <label class="btn btn-outline-info" for="role-inspector">检查人</label>
                            <input type="radio" class="btn-check" name="role-switch" id="role-operator" value="operator">
                            <label class="btn btn-outline-warning" for="role-operator">操作工</label>
                        </div>
                    </div>

                    <div class="d-flex gap-2">
                         <button class="btn btn-outline-secondary btn-sm"><i class="bi bi-file-earmark-pdf"></i> 点检标准预览</button>
                         <button class="btn btn-success btn-sm" id="btn-batch-confirm" disabled><i class="bi bi-check-all"></i> 批量确认</button>
                    </div>
                </div>

                <!-- 表格区域 -->
                <div class="flex-grow-1 overflow-auto p-3" style="background-color: var(--bg-secondary);">
                    <table class="table table-hover table-bordered align-middle text-center table-sm mb-0" id="exec-table">
                        <thead class="table-light">
                            <tr>
                                <th style="width: 50px;">序号</th>
                                <th>检查项目名</th>
                                <th style="width: 260px;">检查结果</th>
                                <th style="width: 80px;">生产状态</th>
                                <th style="width: 60px;">班别</th>
                                <th style="width: 60px;">班次</th>
                                <th>检查说明</th>
                                <th style="width: 80px;">是否确认</th>
                            </tr>
                        </thead>
                        <tbody id="exec-tbody">
                            <tr><td colspan="8" class="text-center py-4" style="color: var(--text-secondary);"><div class="spinner-border spinner-border-sm"></div> 加载中...</td></tr>
                        </tbody>
                    </table>
                </div>

                <!-- 底部按钮栏 -->
                <div class="p-3 border-top d-flex justify-content-center gap-3" style="background-color: var(--bg-primary);">
                    <button class="btn btn-primary px-5" id="btn-submit">提交</button>
                    <button class="btn btn-secondary px-5" id="btn-back">返回</button>
                </div>
            </div>
        `;

        this._attachRoleSwitcher();
        this._loadDetails();
        this._attachGlobalListeners();
    }

    async _loadDetails() {
        try {
            this.details = await getSiTaskDetails(this.taskInfo.id);
            this._renderTableRows();
        } catch (e) {
            const tbody = this.container.querySelector('#exec-tbody');
            if(tbody) tbody.innerHTML = '<tr><td colspan="8" class="text-danger">加载失败</td></tr>';
        }
    }

    _renderTableRows() {
        const tbody = this.container.querySelector('#exec-tbody');
        if (!tbody) return;

        const isInspector = this.currentRole === 'inspector';
        const isOperator = this.currentRole === 'operator';

        const batchConfirmBtn = this.container.querySelector('#btn-batch-confirm');
        if (batchConfirmBtn) batchConfirmBtn.disabled = !isOperator;

        if (this.details.length === 0) {
            tbody.innerHTML = '<tr><td colspan="8" style="color: var(--text-secondary);">无检查项</td></tr>';
            return;
        }

        tbody.innerHTML = this.details.map((item, index) => {
            const resultDisabled = !isInspector ? 'disabled' : '';
            const confirmDisabled = !isOperator ? 'disabled' : '';

            const resultHtml = `
                <div class="d-flex justify-content-center gap-2">
                    <div class="form-check form-check-inline m-0 d-flex align-items-center">
                        <input class="form-check-input bg-success border-success" type="radio" name="res-${item.id}" id="res-${item.id}-ok" value="正常" ${item.result==='正常'?'checked':''} ${resultDisabled}>
                        <label class="form-check-label ms-1 small" style="color: inherit;" for="res-${item.id}-ok">正常</label>
                    </div>
                    <div class="form-check form-check-inline m-0 d-flex align-items-center">
                        <input class="form-check-input bg-danger border-danger" type="radio" name="res-${item.id}" id="res-${item.id}-ng" value="异常" ${item.result==='异常'?'checked':''} ${resultDisabled}>
                        <label class="form-check-label ms-1 small" style="color: inherit;" for="res-${item.id}-ng">异常</label>
                    </div>
                    <div class="form-check form-check-inline m-0 d-flex align-items-center">
                        <input class="form-check-input bg-secondary border-secondary" type="radio" name="res-${item.id}" id="res-${item.id}-na" value="不用" ${item.result==='不用'?'checked':''} ${resultDisabled}>
                        <label class="form-check-label ms-1 small" style="color: inherit;" for="res-${item.id}-na">不用</label>
                    </div>
                </div>
            `;

            return `
                <tr data-id="${item.id}">
                    <td>${index + 1}</td>
                    <td class="text-start">${item.itemName}</td>
                    <td>${resultHtml}</td>
                    <td>${item.prodStatus}</td>
                    <td>${item.shiftType}</td>
                    <td>${item.shift}</td>
                    <td>
                        <input type="text" class="form-control form-control-sm input-remarks" value="${item.remarks || ''}" style="background-color: transparent; color: inherit; border-color: var(--border-color);">
                    </td>
                    <td>
                        <div class="form-check d-flex justify-content-center">
                            <input class="form-check-input input-confirm" type="checkbox" ${item.isConfirmed ? 'checked' : ''} ${confirmDisabled}>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    }

    _attachRoleSwitcher() {
        this.container.querySelectorAll('input[name="role-switch"]').forEach(r => {
            r.addEventListener('change', (e) => {
                this.currentRole = e.target.value;
                this._saveLocalState();
                this._renderTableRows();
            });
        });
    }

    _attachGlobalListeners() {
        this.container.querySelector('#btn-back').addEventListener('click', () => {
            this.parentView.showList();
        });

        this.container.querySelector('#btn-submit').addEventListener('click', async () => {
            this._saveLocalState();

            const now = new Date().toLocaleString('zh-CN', { hour12: false });

            if (this.currentRole === 'inspector') {
                this.details.forEach(d => {
                    if (d.result) d.checkTime = now;
                });
            } else if (this.currentRole === 'operator') {
                this.details.forEach(d => {
                    if (d.isConfirmed) d.confirmTime = now;
                });
            }

            const btn = this.container.querySelector('#btn-submit');
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 提交中...';

            try {
                await saveSiTaskDetails(this.taskInfo.id, this.details);
                Modal.alert("提交成功！");
                this.parentView.showList();
            } catch(e) {
                Modal.alert("提交失败: " + e.message);
                btn.disabled = false;
                btn.innerHTML = '提交';
            }
        });

        this.container.querySelector('#btn-batch-confirm').addEventListener('click', () => {
            if (this.currentRole !== 'operator') return;
            const confirmBoxes = this.container.querySelectorAll('.input-confirm:not(:disabled)');
            if (confirmBoxes.length === 0) return;

            let allChecked = true;
            confirmBoxes.forEach(cb => { if(!cb.checked) allChecked = false; });
            const newState = !allChecked;

            confirmBoxes.forEach(cb => {
                cb.checked = newState;
                cb.dispatchEvent(new Event('change', {bubbles: true}));
            });
        });

        this.container.querySelector('#exec-tbody').addEventListener('change', (e) => {
            const target = e.target;
            const row = target.closest('tr');
            if (!row) return;
            const id = parseInt(row.dataset.id);
            const item = this.details.find(d => d.id === id);
            if (!item) return;

            if (target.type === 'radio' && target.name.startsWith('res-')) {
                item.result = target.value;
            } else if (target.classList.contains('input-remarks')) {
                item.remarks = target.value;
            } else if (target.classList.contains('input-confirm')) {
                item.isConfirmed = target.checked;
            }
        });
    }

    _saveLocalState() {
        const rows = this.container.querySelectorAll('#exec-tbody tr');
        rows.forEach(row => {
            const id = parseInt(row.dataset.id);
            const item = this.details.find(d => d.id === id);
            if (item) {
                const checkedRes = row.querySelector(`input[name="res-${id}"]:checked`);
                if (checkedRes && !checkedRes.disabled) item.result = checkedRes.value;

                const remarkInput = row.querySelector('.input-remarks');
                if (remarkInput) item.remarks = remarkInput.value;

                const confirmInput = row.querySelector('.input-confirm');
                if (confirmInput && !confirmInput.disabled) item.isConfirmed = confirmInput.checked;
            }
        });
    }
}