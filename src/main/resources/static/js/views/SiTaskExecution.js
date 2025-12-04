/**
 * @file /js/views/SiTaskExecution.js
 * @description 点检任务执行详情页。
 */
import DataTable from '../components/Optimized_DataTable.js';
import Modal from '../components/Modal.js';
import { getSiTaskDetails, saveSiTaskDetails, getStandardFiles, getFilePreviewUrl } from '../services/selfInspectionApi.js';

export default class SiTaskExecution {
    /**
     * @param {SiTasks} parentView - 父级视图实例
     * @param {Object} taskInfo - 任务主表信息
     * @param {String} role - 当前角色 ('inspector' | 'operator')
     */
    constructor(parentView, taskInfo, role) {
        this.parentView = parentView;
        this.taskInfo = taskInfo;
        this.container = null;
        this.details = [];
        this.currentRole = role || 'inspector';
    }

    render(container) {
        this.container = container;
        const roleTitle = this.currentRole === 'inspector' ? '检查人' : '确认人 (操作工)';
        const roleBadgeClass = this.currentRole === 'inspector' ? 'bg-info' : 'bg-warning text-dark';

        container.innerHTML = `
            <div class="d-flex flex-column h-100">
                <!-- 顶部头部 -->
                <div class="d-flex justify-content-between align-items-center p-3 mb-2 rounded" style="background-color: var(--bg-primary);">
                    <div class="d-flex align-items-center gap-3">
                        <h5 class="mb-0 text-white"><i class="bi bi-check2-square"></i> 执行点检</h5>
                        <span class="badge bg-secondary">${this.taskInfo.sjx || '-'}</span>
                        <span class="badge bg-primary">${this.taskInfo.sfname || '-'}</span>
                        <span class="badge bg-info text-dark">${this.taskInfo.taskType || '-'}</span>
                        <span class="badge ${roleBadgeClass}">当前身份: ${roleTitle}</span>
                    </div>
                    
                    <div class="d-flex align-items-center gap-3">
                        <button class="btn btn-sm btn-outline-light" id="btn-view-standard"><i class="bi bi-file-text"></i> 点检标准预览</button>
                        <button class="btn btn-sm btn-success ${this.currentRole === 'operator' ? '' : 'd-none'}" id="btn-batch-confirm"><i class="bi bi-check-all"></i> 批量确认</button>
                    </div>
                </div>

                <!-- 主表信息卡片 -->
                <div class="card mb-2 border-secondary bg-transparent text-white">
                    <div class="card-body py-2">
                        <div class="row g-2 small">
                            <div class="col-md-2"><span class="text-muted">所属机型:</span> ${this.taskInfo.sjx || '-'}</div>
                            <div class="col-md-2"><span class="text-muted">所属设备:</span> ${this.taskInfo.sfname || '-'}</div>
                            <div class="col-md-2"><span class="text-muted">班组:</span> ${this.taskInfo.shiftType || '-'}</div> 
                            <div class="col-md-2"><span class="text-muted">班别:</span> ${this.taskInfo.shiftType || '-'}</div>
                            <div class="col-md-2"><span class="text-muted">班次:</span> ${this.taskInfo.shift || '-'}</div>
                            <div class="col-md-2"><span class="text-muted">生产状态:</span> ${this.taskInfo.prodStatus || '-'}</div>
                            
                            <div class="col-md-2"><span class="text-muted">任务时间:</span> ${this.taskInfo.taskTime || '-'}</div>
                            <div class="col-md-2"><span class="text-muted">点检状态:</span> <span class="${this.taskInfo.checkStatus==='已检'?'text-success':'text-warning'}">${this.taskInfo.checkStatus || '-'}</span></div>
                            <div class="col-md-2"><span class="text-muted">点检时间:</span> ${this.taskInfo.checkTime || '-'}</div>
                            <div class="col-md-2"><span class="text-muted">确认状态:</span> <span class="${this.taskInfo.confirmStatus==='已确认'?'text-success':'text-warning'}">${this.taskInfo.confirmStatus || '-'}</span></div>
                            <div class="col-md-2"><span class="text-muted">确认时间:</span> ${this.taskInfo.confirmTime || '-'}</div>
                            <div class="col-md-2"><span class="text-muted">任务类型:</span> ${this.taskInfo.taskType || '-'}</div>
                        </div>
                    </div>
                </div>

                <!-- 明细列表区域 -->
                <div class="flex-grow-1 border rounded mb-3 d-flex flex-column" style="background-color: var(--bg-secondary); overflow: hidden;">
                    <div class="table-responsive flex-grow-1" style="overflow-y: auto;">
                        <table class="table table-dark table-hover table-bordered table-sm align-middle mb-0 sticky-header" id="exec-table">
                            <thead>
                                <tr class="table-light text-dark">
                                    <th style="width: 50px;" class="text-center">序号</th>
                                    <th style="width: 30%;">检查项目名</th>
                                    <th style="width: 300px;" class="text-center">检查结果</th>
                                    <th>检查说明</th>
                                    <th style="width: 80px;" class="text-center">是否确认</th>
                                </tr>
                            </thead>
                            <tbody id="exec-tbody">
                                <tr><td colspan="5" class="text-center py-4"><div class="spinner-border text-light" role="status"></div></td></tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- 底部操作栏 -->
                <div class="d-flex justify-content-center gap-3 pb-2">
                    <button class="btn btn-primary px-5" id="btn-submit">提交</button>
                    <button class="btn btn-secondary px-5" id="btn-back">返回</button>
                </div>
            </div>

            <style>
                /* 固定表头样式 */
                .sticky-header thead th {
                    position: sticky;
                    top: 0;
                    z-index: 10;
                    background-color: #e9ecef;
                    color: #212529;
                }

                /* 检查结果单选按钮组样式 */
                .radio-group-container {
                    display: flex;
                    justify-content: center;
                    gap: 15px;
                }
                
                /* 自定义 Radio Label 样式，确保在黑色主题下可见 */
                .custom-radio-label {
                    cursor: pointer;
                    display: flex;
                    align-items: center;
                    gap: 4px;
                    color: var(--text-primary); /* 适配主题文字颜色 */
                    font-weight: 500;
                    font-size: 0.9rem;
                }
                
                /* Radio Input 微调 */
                .custom-radio-input {
                    width: 1.1em;
                    height: 1.1em;
                    cursor: pointer;
                    margin-top: 0; /* 对齐修正 */
                }
                
                /* 禁用状态下的 Radio 样式优化 */
                .custom-radio-input:disabled {
                    opacity: 0.6;
                }
                .custom-radio-input:disabled ~ .custom-radio-label {
                    opacity: 0.8;
                    cursor: default;
                    color: var(--text-primary); /* 保持文字颜色，避免看不清 */
                }
                
                /* 选中状态的高亮 (可选) */
                .custom-radio-input:checked ~ .custom-radio-label {
                    font-weight: bold;
                    text-decoration: underline;
                    text-decoration-color: var(--accent-color);
                    text-underline-offset: 3px;
                }
                
                /* 输入框自适应主题 */
                .input-remark {
                    border-color: var(--border-color);
                    color: var(--text-primary); 
                    background-color: var(--bg-primary);
                }
                .input-remark:focus {
                    border-color: var(--accent-color);
                    box-shadow: 0 0 0 0.2rem rgba(0,123,255,.25);
                    color: var(--text-primary);
                    background-color: var(--bg-primary);
                }
                /* 只读状态 */
                .input-remark[readonly] {
                    background-color: rgba(0,0,0,0.1);
                    border-color: transparent;
                    color: var(--text-secondary);
                    cursor: default;
                }
                /* 确认人视角下的 Badge */
                .result-badge {
                    padding: 0.3rem 0.8rem;
                    border-radius: 4px;
                    font-weight: normal;
                    color: white !important;
                    display: inline-block;
                    min-width: 60px;
                }
                .result-badge.normal { background-color: #198754; }
                .result-badge.abnormal { background-color: #dc3545; }
                .result-badge.unused { background-color: #6c757d; }
                .result-badge.pending { background-color: #343a40; border: 1px solid #6c757d; color: #adb5bd !important; }
            </style>
        `;

        this._attachEventListeners();
        this._loadDetails();
    }

    async _loadDetails() {
        try {
            console.log(`[SiTaskExecution] Requesting details for Task ID: ${this.taskInfo.indocno}`);
            this.details = await getSiTaskDetails(this.taskInfo.indocno);

            // [关键调试] 打印数据结构，确认字段名
            console.log("[SiTaskExecution] Received details from backend:", this.details);

            this._renderTableRows();
        } catch (e) {
            console.error("[SiTaskExecution] Failed to load details:", e);
            Modal.alert("加载明细失败: " + e.message);
        }
    }

    _renderTableRows() {
        const tbody = this.container.querySelector('#exec-tbody');
        if (!this.details || this.details.length === 0) {
            tbody.innerHTML = '<tr><td colspan="5" class="text-center py-3 text-muted">无检查明细</td></tr>';
            return;
        }

        const isInspector = this.currentRole === 'inspector';

        tbody.innerHTML = this.details.map((row, index) => {
            // ----------------------------------------------------------------
            // 1. 字段映射修正 (FIXED)
            // ----------------------------------------------------------------
            // 之前使用 row.result，现在改为兼容 row.checkResult 和 row.result
            // 并且对 remarks 也做同样处理 (row.checkRemark / row.remarks)
            const currentResult = row.checkResult || row.result;
            const currentRemarks = row.checkRemark || row.remarks || '';

            const isConfirmed = row.isConfirmed === 1;

            console.log(`[Row ${index+1}] ID:${row.indocno}, Field checkResult:${row.checkResult}, Final:${currentResult}`);

            // 2. 控制可编辑性
            const resultDisabled = isInspector ? '' : 'disabled';
            const remarkReadonly = isInspector ? '' : 'readonly';
            const confirmDisabled = isInspector ? 'disabled' : '';

            const checkboxChecked = isConfirmed ? 'checked' : '';

            let resultHtml = '';

            // 3. 渲染结果列
            if (isInspector) {
                // 检查人：显示 Radio 组
                // [关键] 使用修正后的 currentResult 进行比对
                const checkedNormal = currentResult === '正常' ? 'checked' : '';
                const checkedAbnormal = currentResult === '异常' ? 'checked' : '';
                const checkedUnused = currentResult === '不用' ? 'checked' : '';

                const name = `result-radio-${row.indocno}`;

                resultHtml = `
                    <div class="radio-group-container" id="result-group-${row.indocno}">
                        <div class="form-check form-check-inline m-0 radio-item normal">
                            <input class="form-check-input custom-radio-input input-result" type="radio" name="${name}" id="${name}-1" value="正常" ${checkedNormal} ${resultDisabled}>
                            <label class="form-check-label custom-radio-label" for="${name}-1">正常</label>
                        </div>
                        <div class="form-check form-check-inline m-0 radio-item abnormal">
                            <input class="form-check-input custom-radio-input input-result" type="radio" name="${name}" id="${name}-2" value="异常" ${checkedAbnormal} ${resultDisabled}>
                            <label class="form-check-label custom-radio-label text-danger" for="${name}-2">异常</label>
                        </div>
                        <div class="form-check form-check-inline m-0 radio-item unused">
                            <input class="form-check-input custom-radio-input input-result" type="radio" name="${name}" id="${name}-3" value="不用" ${checkedUnused} ${resultDisabled}>
                            <label class="form-check-label custom-radio-label text-secondary" for="${name}-3">不用</label>
                        </div>
                    </div>
                `;
            } else {
                // 确认人：只显示已选结果 (Badge)
                let badgeClass = 'pending';
                let resultText = currentResult || '未检';

                if (currentResult === '正常') badgeClass = 'normal';
                else if (currentResult === '异常') badgeClass = 'abnormal';
                else if (currentResult === '不用') badgeClass = 'unused';

                // 隐藏的 input 用于提交时保持原值
                resultHtml = `
                    <div class="d-flex justify-content-center align-items-center">
                        <span class="result-badge ${badgeClass}">
                             ${resultText}
                        </span>
                        <input type="hidden" class="input-result-hidden" value="${currentResult || ''}">
                    </div>
                `;
            }

            return `
                <tr data-id="${row.indocno}">
                    <td class="text-center">${index + 1}</td>
                    <td>
                        <div class="d-flex align-items-center">
                            ${row.itemName || '-'}
                        </div>
                    </td>
                    <td>${resultHtml}</td>
                    <td>
                        <input type="text" class="form-control form-control-sm input-remark" 
                               value="${currentRemarks}" ${remarkReadonly} placeholder="${isInspector?'填写说明...':''}">
                    </td>
                    <td class="text-center">
                        <div class="form-check d-flex justify-content-center mb-0">
                            <input class="form-check-input input-confirm" type="checkbox" ${checkboxChecked} ${confirmDisabled}>
                        </div>
                    </td>
                </tr>
            `;
        }).join('');
    }

    _attachEventListeners() {
        // 批量确认 (仅操作工)
        const batchBtn = this.container.querySelector('#btn-batch-confirm');
        if (batchBtn) {
            batchBtn.addEventListener('click', () => {
                console.log("[UI] Batch confirm clicked");
                const checkboxes = this.container.querySelectorAll('.input-confirm:not(:disabled)');
                let allChecked = true;
                checkboxes.forEach(cb => { if(!cb.checked) allChecked = false; });

                // 如果未全选，则全选；如果已全选，则全不选
                const targetState = !allChecked;
                checkboxes.forEach(cb => cb.checked = targetState);
            });
        }

        // 返回
        this.container.querySelector('#btn-back').addEventListener('click', () => {
            this.parentView.showList();
        });

        // 提交
        this.container.querySelector('#btn-submit').addEventListener('click', async () => {
            console.log("[Action] Submit button clicked");
            const rows = this.container.querySelectorAll('#exec-tbody tr');
            const updates = [];
            let hasError = false;

            // 提交前校验与数据收集
            for (let tr of rows) {
                const id = parseInt(tr.dataset.id);
                const remarkInput = tr.querySelector('.input-remark');
                const confirmInput = tr.querySelector('.input-confirm');

                let resultValue = null;

                if (this.currentRole === 'inspector') {
                    // 检查人模式：查找选中的 Radio
                    const checkedRadio = tr.querySelector(`input.input-result:checked`);
                    if (checkedRadio) {
                        resultValue = checkedRadio.value;
                    }
                } else {
                    // 确认人模式：查找隐藏域的值
                    const hiddenInput = tr.querySelector('.input-result-hidden');
                    if (hiddenInput) {
                        resultValue = hiddenInput.value;
                    }
                }

                const isConfirmed = confirmInput.checked;
                const remarkValue = remarkInput.value;

                // 校验逻辑
                if (this.currentRole === 'inspector') {
                    if (!resultValue) {
                        Modal.alert(`第 ${tr.rowIndex} 行未选择检查结果！`);
                        tr.scrollIntoView({behavior: "smooth", block: "center"});
                        hasError = true;
                        break;
                    }
                } else if (this.currentRole === 'operator') {
                    if (!isConfirmed) {
                        Modal.alert(`第 ${tr.rowIndex} 行未勾选确认！请全部确认后再提交。`);
                        tr.scrollIntoView({behavior: "smooth", block: "center"});
                        hasError = true;
                        break;
                    }
                }

                updates.push({
                    indocno: id,
                    checkResult: resultValue,
                    checkRemark: remarkValue,
                    isConfirmed: isConfirmed ? 1 : 0
                });
            }

            if (hasError) return;

            console.log(`[Action] Submitting ${updates.length} updates. Sample payload:`, updates[0]);

            const btn = this.container.querySelector('#btn-submit');
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 提交中...';

            try {
                // 传递当前角色，后端根据角色决定更新哪些字段 (checkTime/checker OR confirmTime/confirmer)
                await saveSiTaskDetails(this.taskInfo.indocno, updates, this.currentRole);
                Modal.alert("提交成功");
                this.parentView.showList();
            } catch (e) {
                console.error("[Action] Submit failed:", e);
                Modal.alert("提交失败: " + e.message);
                btn.disabled = false;
                btn.innerHTML = '提交';
            }
        });

        this.container.querySelector('#btn-view-standard').addEventListener('click', () => this._showStandardPreviewModal());
    }

    async _showStandardPreviewModal() {
        // ... (保持原有的标准预览逻辑不变)
        const bodyHtml = `
            <div class="table-responsive" style="max-height: 400px; overflow-y: auto;">
                <table class="table table-hover table-bordered table-sm align-middle mb-0 text-center">
                    <thead class="table-light">
                        <tr>
                            <th>文件名</th>
                            <th style="width: 150px;">上传时间</th>
                            <th style="width: 100px;">上传人</th>
                            <th style="width: 80px;">操作</th>
                        </tr>
                    </thead>
                    <tbody id="standard-file-list-tbody">
                        <tr><td colspan="4" class="text-center p-3"><div class="spinner-border spinner-border-sm"></div> 加载中...</td></tr>
                    </tbody>
                </table>
            </div>
        `;

        const modal = new Modal({
            title: "点检标准预览",
            body: bodyHtml,
            footer: '<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>',
            size: 'lg'
        });

        modal.show();

        try {
            const files = await getStandardFiles();
            const tbody = modal.modalElement.querySelector('#standard-file-list-tbody');

            if (files.length === 0) {
                tbody.innerHTML = '<tr><td colspan="4" class="text-center p-3 text-muted">暂无标准文件</td></tr>';
            } else {
                tbody.innerHTML = files.map(f => `
                    <tr>
                        <td class="text-start"><i class="bi bi-file-earmark-pdf text-danger me-2"></i>${f.fileName}</td>
                        <td>${f.uploadTime || '-'}</td>
                        <td>${f.uploader || '-'}</td>
                        <td>
                            <button class="btn btn-xs btn-outline-primary btn-preview-pdf" data-id="${f.id}">预览</button>
                        </td>
                    </tr>
                `).join('');

                tbody.querySelectorAll('.btn-preview-pdf').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const fileId = btn.dataset.id;
                        this._openPdfPreview(fileId);
                    });
                });
            }
        } catch (e) {
            const tbody = modal.modalElement.querySelector('#standard-file-list-tbody');
            if(tbody) tbody.innerHTML = '<tr><td colspan="4" class="text-center text-danger p-3">加载失败</td></tr>';
        }
    }

    _openPdfPreview(fileId) {
        const url = getFilePreviewUrl(fileId);
        const bodyHtml = `
            <div class="ratio ratio-4x3" style="height: 70vh;">
                <iframe src="${url}" allowfullscreen></iframe>
            </div>
        `;
        new Modal({
            title: "标准文件预览",
            body: bodyHtml,
            footer: '<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>',
            size: 'xl'
        }).show();
    }
}