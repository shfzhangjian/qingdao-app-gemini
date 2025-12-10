/**
 * @file /js/views/NewSelfInspectionLedger.js
 * @description 新自检自控台账管理视图 (重构版)。
 * v2.8.0 - [Fix] 修复生成任务时已选设备显示“未命名”的问题 (改用 sbname)。
 * v2.8.1 - [Feat] 生成任务弹窗增加多列过滤 (机型、设备、主数据、PM编码)。
 * v2.8.2 - [Fix] 修复车速为空时 toFixed 报错的问题。
 * v2.8.3 - [Refactor] 调整表格列顺序，将序号、车速、车速更新时间放在前面。
 * v2.9.0 - [UI] 生成任务弹窗顶部过滤适配黑白主题，view=content 模式下隐藏操作按钮。
 */
import DataTable from '../components/Optimized_DataTable.js';
import Modal from '../components/Modal.js';
import {
    getLedgerList,
    saveLedger,
    deleteLedger,
    getAutocompleteOptions,
    getStandardFiles,
    uploadStandardFile,
    deleteStandardFile,
    getFilePreviewUrl,
    generateSiTasks,
    getTaskGenerationDeviceList,
    exportLedger,
    importLedger
} from '../services/selfInspectionApi.js';

export default class NewSelfInspectionLedger {
    constructor() {
        this.container = null;
        this.dataTable = null;
        // 缓存生成任务列表的数据表格实例
        this.genTaskTable = null;
        // [New] 缓存已选设备信息，使用 spmcode 作为 Key
        this.selectedItemsMap = new Map();
    }


    render(container) {
        this.container = container;

        // 检查是否为 content-only 模式
        const isContentOnly = document.body.classList.contains('content-only-mode');
        // 如果是 content-only 模式，隐藏生成任务、导入、导出按钮
        const actionButtonsVisibility = isContentOnly ? 'd-none' : '';

        container.innerHTML = `
            <div class="d-flex flex-column h-100">
                <div class="p-3 rounded mb-3" style="background-color: var(--bg-primary);">
                    <!-- [修改] 改为每行 3 列 (col-md-4) -->
                    <div class="row g-3 align-items-center">
                        <div class="col-md-4 d-flex align-items-center"><label class="form-label mb-0 me-2 flex-shrink-0 text-secondary" style="width: 80px; text-align: right;">车间:</label><input type="text" class="form-control form-control-sm" id="search-sdept" list="list-sdept"><datalist id="list-sdept"></datalist></div>
                        <div class="col-md-4 d-flex align-items-center"><label class="form-label mb-0 me-2 flex-shrink-0 text-secondary" style="width: 80px; text-align: right;">所属机型:</label><input type="text" class="form-control form-control-sm" id="search-sjx" list="list-sjx"><datalist id="list-sjx"></datalist></div>
                        <div class="col-md-4 d-flex align-items-center"><label class="form-label mb-0 me-2 flex-shrink-0 text-secondary" style="width: 110px; text-align: right;">主数据名称:</label><input type="text" class="form-control form-control-sm" id="search-sbname" list="list-sbname"><datalist id="list-sbname"></datalist></div>
                        
                        <div class="col-md-4 d-flex align-items-center"><label class="form-label mb-0 me-2 flex-shrink-0 text-secondary" style="width: 80px; text-align: right;">名称:</label><input type="text" class="form-control form-control-sm" id="search-sname"></div>
                        <div class="col-md-4 d-flex align-items-center"><label class="form-label mb-0 me-2 flex-shrink-0 text-secondary" style="width: 80px; text-align: right;">PM编码:</label><input type="text" class="form-control form-control-sm" id="search-spmcode"></div>
                        <div class="col-md-4 d-flex align-items-center"><label class="form-label mb-0 me-2 flex-shrink-0 text-secondary" style="width: 110px; text-align: right;">资产编码:</label><input type="text" class="form-control form-control-sm" id="search-szcno"></div>
                        
                        <div class="col-12 d-flex justify-content-between align-items-center mt-2 pt-2 border-top border-secondary">
                            <div class="d-flex gap-2">
                                <button class="btn btn-sm btn-outline-primary  ${actionButtonsVisibility}" id="btn-add"><i class="bi bi-plus-lg"></i> 增加</button>
                                <button class="btn btn-sm btn-outline-secondary  ${actionButtonsVisibility}" id="btn-edit"><i class="bi bi-pencil-square"></i> 编辑</button>
                                <button class="btn btn-sm btn-outline-danger  ${actionButtonsVisibility}" id="btn-delete"><i class="bi bi-trash"></i> 删除</button>
                                <button class="btn btn-sm btn-outline-success ${actionButtonsVisibility}" id="btn-import"><i class="bi bi-file-earmark-arrow-up"></i> 导入</button>
                                <button class="btn btn-sm btn-outline-success ${actionButtonsVisibility}" id="btn-export"><i class="bi bi-file-earmark-arrow-down"></i> 导出</button>
                                <div class="vr mx-1 text-secondary"></div>
                                <button class="btn btn-sm btn-info text-white" id="btn-files"><i class="bi bi-file-earmark-pdf"></i> 标准附件管理(PDF)</button>
                            </div>
                            <div class="d-flex gap-2">
                                <button class="btn btn-sm btn-warning text-dark ${actionButtonsVisibility}" id="btn-generate"><i class="bi bi-lightning-charge-fill"></i> 生成任务</button>
                                <button class="btn btn-sm btn-primary" id="btn-search" style="min-width: 80px;"><i class="bi bi-search"></i> 查询</button>
                                <button class="btn btn-sm btn-secondary" id="btn-clear" style="min-width: 80px;"><i class="bi bi-eraser"></i> 清空</button>
                            </div>
                        </div>
                    </div>
                </div>
                <div id="table-container" class="flex-grow-1 p-2 rounded" style="background-color: var(--bg-secondary); border: 1px solid var(--border-color);"></div>
            </div>
        `;

        this._initTable();
        this._initAutocompletes();
        this._attachEventListeners();
    }

    _initTable() {
        const columns = [
            { key: '_index', title: '序号', width: 60, frozen: 'left' },
            {
                key: 'hasStandard',
                title: '是否过期', // 前端标题叫“是否过期”，实际上对应后端“是否上传标准”逻辑，或者这里复用字段名
                width: 90,
                render: (val) => val
                    ? '<i class="bi bi-check-circle-fill text-success" title="已上传"></i>'
                    : '<i class="bi bi-x-circle-fill text-secondary" title="未上传"></i>'
            },
            {
                key: 'isLinked', // 前端虚拟字段，如果后端没有返回，需确保不报错
                title: '台账挂接',
                width: 90,
                render: (val) => val
                    ? '<i class="bi bi-link-45deg text-primary" title="已挂接"></i>'
                    : '<i class="bi bi-unlink text-secondary" title="未挂接"></i>'
            },
            // 系统编号通常可以是 ID 或者其他唯一标识，这里暂且展示 indocno 或其他
            { key: 'sname', title: '企业编号', width: 120 }, // 注意：这里字段映射可能需要根据实际调整，暂用 sname 占位
            { key: 'sfname', title: '设备名称', width: 180 },
            { key: 'sxh', title: '规格型号', width: 150 },
            { key: 'scj', title: '出厂编号', width: 150 }, // 注意：字段需确认
            { key: 'sazwz', title: '安装位置/使用人', width: 180 },
            { key: 'slevel', title: '准确度等级', width: 100 }, // 注意：字段需确认
            { key: 'dtime', title: '下次确认日期', width: 120, render: (val) => val ? val.substring(0, 10) : '-' },
            { key: 'sbname', title: '所属设备', width: 200 },
            { key: 'sdept', title: '使用部门', width: 100 },
            { key: 'sabc', title: 'ABC分类', width: 80 }, // 注意：字段需确认
            { key: 'iqj', title: '强检标识', width: 80 }, // 注意：字段需确认
            { key: 'izj', title: '质检仪器', width: 80 }, // 注意：字段需确认
            { key: 'syl', title: '量程范围', width: 120 }, // 这里暂时用 syl (测量原理) 代替，具体需确认
            { key: 'scj', title: '制造单位', width: 150 }, // 这里暂时用 scj (厂家)
            { key: 'dfactory', title: '出厂时间', width: 120 }, // 注意：字段需确认
            { key: 'suser', title: '责任人', width: 100 }, // 注意：字段需确认
            { key: 'sverifier', title: '检定员', width: 100 }, // 注意：字段需确认
            { key: 'sdefine1', title: '确认方式', width: 100 }, // 注意：字段需确认
            { key: 'scertificate', title: '证书编号', width: 120 }, // 注意：字段需确认
            { key: 'sbuytype', title: '购置形式', width: 100 }, // 注意：字段需确认
            { key: 'sconfirmbasis', title: '确认依据', width: 120 }, // 注意：字段需确认
            { key: 'snote', title: '备注', width: 150 },
            // [新增] 车速显示列
            {
                key: 'lastAvgSpeed',
                title: '车速',
                width: 100,
                render: (val) => {
                    // [修复] 增加类型检查和转换，防止 toFixed 报错
                    if (val !== null && val !== undefined && !isNaN(Number(val))) {
                        return `<strong>${Number(val).toFixed(1)}</strong>`;
                    }
                    return '<span class="text-muted">-</span>';
                }
            },
            {
                key: 'lastSpeedTime',
                title: '车速更新时间',
                width: 140,
                render: (val) => val ? val.substring(5, 16) : '-' // 只显示 MM-dd HH:mm
            }
        ];

        this.dataTable = new DataTable({
            columns,
            data: [],
            options: {
                selectable: 'single',
                pagination: true,
                uniformRowHeight: true,
                storageKey: 'zjzkToolTable_v5' // Update storage key
            }
        });
        this.dataTable.render(this.container.querySelector('#table-container'));
        this._loadData();
    }

    async _initAutocompletes() {
        const fillDatalist = async (field, listId) => {
            const list = this.container.querySelector(`#${listId}`);
            if(!list) return;
            try {
                const options = await getAutocompleteOptions(field);
                list.innerHTML = options.map(opt => `<option value="${opt}">`).join('');
            } catch(e){}
        };
        fillDatalist('sdept', 'list-sdept');
        fillDatalist('sjx', 'list-sjx');
        fillDatalist('sbname', 'list-sbname');
    }

    async _loadData() {
        if (!this.dataTable) return;
        this.dataTable.toggleLoading(true);
        const params = {
            sdept: this.container.querySelector('#search-sdept').value,
            sjx: this.container.querySelector('#search-sjx').value,
            sbname: this.container.querySelector('#search-sbname').value,
            sname: this.container.querySelector('#search-sname').value,
            spmcode: this.container.querySelector('#search-spmcode').value,
            szcno: this.container.querySelector('#search-szcno').value,
            pageNum: this.dataTable.state.pageNum,
            pageSize: this.dataTable.state.pageSize
        };
        try {
            const result = await getLedgerList(params);

            if (result && result.list) {
                const start = (result.pageNum - 1) * result.pageSize;
                result.list.forEach((item, index) => {
                    item._index = start + index + 1;
                    // 确保 id 字段存在，用于表格操作
                    if (!item.id) item.id = item.indocno;
                });
            }

            this.dataTable.updateView(result);
        } catch (e) { console.error(e); Modal.alert("加载数据失败: " + e.message); }
        finally { this.dataTable.toggleLoading(false); }
    }

    _attachEventListeners() {
        this.container.querySelector('#btn-search').addEventListener('click', () => {
            this.dataTable.state.pageNum = 1;
            this._loadData();
        });
        this.container.querySelector('#btn-clear').addEventListener('click', () => {
            this.container.querySelectorAll('input').forEach(inp => inp.value = '');
            this.dataTable.state.pageNum = 1;
            this._loadData();
        });

        this.container.querySelector('#table-container').addEventListener('queryChange', (e) => {
            this._loadData();
        });

        this.container.querySelector('#btn-add').addEventListener('click', () => this._openEditModal(null));
        this.container.querySelector('#btn-edit').addEventListener('click', () => {
            const selectedId = this.dataTable.selectedRowId;
            if (!selectedId) { Modal.alert("请选择一条记录进行编辑"); return; }
            const rowData = this.dataTable.data.find(d => String(d.indocno) === String(selectedId));
            if (!rowData) {
                Modal.alert("无法获取选中行数据，请刷新重试");
                return;
            }
            this._openEditModal(rowData);
        });
        this.container.querySelector('#btn-delete').addEventListener('click', async () => {
            const selectedId = this.dataTable.selectedRowId;
            if (!selectedId) { Modal.alert("请选择要删除的记录"); return; }
            if (await Modal.confirm("确认删除选中记录吗？")) {
                await deleteLedger([selectedId]);
                this._loadData();
                Modal.alert("删除成功");
            }
        });

        // 绑定导入导出按钮事件
        const btnImport = this.container.querySelector('#btn-import');
        if(btnImport) btnImport.addEventListener('click', () => this._showImportModal());

        const btnExport = this.container.querySelector('#btn-export');
        if(btnExport) btnExport.addEventListener('click', () => this._handleExport());

        this.container.querySelector('#btn-files').addEventListener('click', () => this._openFileManagementModal());

        const btnGenerate = this.container.querySelector('#btn-generate');
        if(btnGenerate) btnGenerate.addEventListener('click', () => this._openGenerateTaskModal());
    }

    _showImportModal() {
        const bodyHtml = `
            <div class="mb-3">
                <label class="form-label">选择Excel文件 (.xlsx)</label>
                <input class="form-control" type="file" id="import-file" accept=".xlsx" style="background-color: var(--bg-primary); color: var(--text-primary); border-color: var(--border-color);">
                <div class="form-text" style="color: var(--text-primary); opacity: 0.7;">
                    提示：<b>PM编码</b>与<b>资产编码</b>为必填项。<br>
                    系统将联合校验(车间、名称、机型、设备、主数据、PM、位置、厂家、规格、原理、资产、订单)共12个字段进行防重。
                </div>
            </div>
            
            <div id="import-result-area" class="d-none">
                <div class="alert alert-danger d-flex align-items-center justify-content-between" role="alert">
                    <div>
                        <i class="bi bi-exclamation-triangle-fill me-2"></i>
                        <span id="import-error-msg">校验未通过</span>
                    </div>
                    <button class="btn btn-sm btn-light text-danger fw-bold" id="btn-download-error-report">
                        <i class="bi bi-download"></i> 下载修正文件
                    </button>
                </div>
                
                <div class="border rounded p-2" style="max-height: 200px; overflow-y: auto; background-color: var(--bg-secondary); border-color: var(--border-color) !important;">
                    <h6 class="text-danger border-bottom pb-1 mb-2" style="border-color: var(--border-color) !important;">错误详情 (点击展开)</h6>
                    <ul class="list-group list-group-flush small" id="error-list">
                    </ul>
                </div>
            </div>
            
            <div id="import-success-area" class="d-none">
                <div class="alert alert-success text-center">
                    <i class="bi bi-check-circle-fill me-2"></i> <span id="import-success-msg"></span>
                </div>
            </div>
        `;

        const modal = new Modal({
            title: "台账导入",
            body: bodyHtml,
            footer: `
                <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
                <button type="button" class="btn btn-primary" id="btn-start-import">开始导入</button>
            `
        });
        modal.show();

        const fileInput = modal.modalElement.querySelector('#import-file');
        const startBtn = modal.modalElement.querySelector('#btn-start-import');
        const resultArea = modal.modalElement.querySelector('#import-result-area');
        const successArea = modal.modalElement.querySelector('#import-success-area');
        const errorList = modal.modalElement.querySelector('#error-list');
        const downloadBtn = modal.modalElement.querySelector('#btn-download-error-report');

        let currentErrorFileId = null;

        downloadBtn.addEventListener('click', () => {
            if(currentErrorFileId) {
                window.location.href = `/tmis/api/si/ledger/import/error-report/${currentErrorFileId}`;
            }
        });

        startBtn.addEventListener('click', async () => {
            if (fileInput.files.length === 0) {
                Modal.alert("请先选择文件！");
                return;
            }

            startBtn.disabled = true;
            startBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 正在校验...';
            resultArea.classList.add('d-none');
            successArea.classList.add('d-none');

            const formData = new FormData();
            formData.append("file", fileInput.files[0]);

            try {
                const res = await importLedger(formData);

                startBtn.disabled = false;
                startBtn.innerHTML = '开始导入';

                if (res.success) {
                    successArea.classList.remove('d-none');
                    modal.modalElement.querySelector('#import-success-msg').textContent = res.message;
                    setTimeout(() => {
                        modal.hide();
                        this._loadData();
                    }, 2000);
                } else {
                    resultArea.classList.remove('d-none');
                    modal.modalElement.querySelector('#import-error-msg').textContent = res.message;
                    currentErrorFileId = res.errorFileId;

                    if (res.errorDetails && res.errorDetails.length > 0) {
                        errorList.innerHTML = res.errorDetails.map(err => `
                            <li class="list-group-item list-group-item-danger d-flex justify-content-between align-items-start py-1" 
                                style="background-color: rgba(220, 53, 69, 0.1); color: var(--text-primary); border-color: rgba(220, 53, 69, 0.2);">
                                <div class="ms-2 me-auto">
                                    <div class="fw-bold text-danger">第 ${err.row} 行</div>
                                    <span style="opacity: 0.8;">${err.msg}</span>
                                </div>
                            </li>
                        `).join('');
                    } else {
                        errorList.innerHTML = '<li class="list-group-item" style="background-color: transparent; color: var(--text-primary);">校验未通过，请下载报告查看详情。</li>';
                    }
                }

            } catch (e) {
                console.error(e);
                startBtn.disabled = false;
                startBtn.innerHTML = '开始导入';
                Modal.alert("系统错误: " + (e.message || "请求失败"));
            }
        });
    }

    async _handleExport() {
        const params = {
            sdept: this.container.querySelector('#search-sdept').value,
            sjx: this.container.querySelector('#search-sjx').value,
            sbname: this.container.querySelector('#search-sbname').value,
            sname: this.container.querySelector('#search-sname').value,
            spmcode: this.container.querySelector('#search-spmcode').value,
            szcno: this.container.querySelector('#search-szcno').value
        };

        const btn = this.container.querySelector('#btn-export');
        const originalText = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 导出中...';

        try {
            const blob = await exportLedger(params);

            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = `自检自控台账_${new Date().toISOString().split('T')[0]}.xlsx`;
            document.body.appendChild(a);
            a.click();
            a.remove();
            window.URL.revokeObjectURL(url);
        } catch (e) {
            console.error(e);
            Modal.alert("导出失败: " + e.message);
        } finally {
            btn.disabled = false;
            btn.innerHTML = originalText;
        }
    }

    _openEditModal(data) {
        const isEdit = !!data;
        const title = isEdit ? "编辑台账" : "新增台账";

        const formHtml = `
            <form id="ledger-form" class="container-fluid">
                <input type="hidden" name="indocno" value="${data?.indocno || ''}">
                <div class="row mb-3 align-items-center">
                    <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>车间</label></div>
                    <div class="col-md-4"><input type="text" class="form-control" name="sdept" list="dl-sdept" required value="${data?.sdept || ''}"><datalist id="dl-sdept"></datalist></div>
                    <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>名称</label></div>
                    <div class="col-md-4"><input type="text" class="form-control" name="sname" required value="${data?.sname || ''}"></div>
                </div>
                <div class="row mb-3 align-items-center">
                    <div class="col-md-2 text-end"><label class="form-label">所属机型</label></div>
                    <div class="col-md-4"><input type="text" class="form-control" name="sjx" list="dl-sjx" value="${data?.sjx || ''}"><datalist id="dl-sjx"></datalist></div>
                    <div class="col-md-2 text-end"><label class="form-label">所属设备</label></div>
                    <div class="col-md-4"><input type="text" class="form-control" name="sfname" value="${data?.sfname || ''}"></div>
                </div>
                <div class="row mb-3 align-items-center">
                    <div class="col-md-2 text-end"><label class="form-label">主数据名称</label></div>
                    <div class="col-md-4"><input type="text" class="form-control" name="sbname" list="dl-sbname" value="${data?.sbname || ''}"><datalist id="dl-sbname"></datalist></div>
                    <div class="col-md-2 text-end"><label class="form-label">厂家</label></div>
                    <div class="col-md-4"><input type="text" class="form-control" name="scj" value="${data?.scj || ''}"></div>
                </div>
                 <div class="row mb-3 align-items-center">
                     <div class="col-md-2 text-end"><label class="form-label">规格型号</label></div>
                     <div class="col-md-4"><input type="text" class="form-control" name="sxh" value="${data?.sxh || ''}"></div>
                     <div class="col-md-2 text-end"><label class="form-label">安装位置</label></div>
                     <div class="col-md-4"><input type="text" class="form-control" name="sazwz" value="${data?.sazwz || ''}"></div>
                </div>
                <div class="row mb-3 align-items-center">
                     <div class="col-md-2 text-end"><label class="form-label">测量原理</label></div>
                     <div class="col-md-4"><input type="text" class="form-control" name="syl" value="${data?.syl || ''}"></div>
                     <div class="col-md-2 text-end"><label class="form-label">PM设备编码</label></div>
                     <div class="col-md-4"><input type="text" class="form-control" name="spmcode" value="${data?.spmcode || ''}"></div>
                </div>
                <div class="row mb-3 align-items-center">
                     <div class="col-md-2 text-end"><label class="form-label">订单号</label></div>
                     <div class="col-md-4"><input type="text" class="form-control" name="sddno" value="${data?.sddno || ''}"></div>
                     <div class="col-md-2 text-end"><label class="form-label">资产编码</label></div>
                     <div class="col-md-4"><input type="text" class="form-control" name="szcno" value="${data?.szcno || ''}"></div>
                </div>
                 <div class="row mb-3 align-items-center">
                     <div class="col-md-2 text-end"><label class="form-label">初次使用时间</label></div>
                     <div class="col-md-4"><input type="text" class="form-control" name="dtime" value="${data?.dtime || ''}" placeholder="yyyy-MM-dd"></div>
                     <div class="col-md-2 text-end"><label class="form-label">使用寿命</label></div>
                     <div class="col-md-4"><input type="text" class="form-control" name="ssm" value="${data?.ssm || ''}"></div>
                </div>
            </form>
        `;
        const footerHtml = `<button class="btn btn-secondary" data-bs-dismiss="modal">取消</button><button class="btn btn-primary" id="btn-save-modal">提交</button>`;
        const modal = new Modal({ title: title, body: formHtml, footer: footerHtml, size: 'xl' });
        const fillModalDatalist = async (field, listId) => {
            try {
                const options = await getAutocompleteOptions(field);
                const list = modal.modalElement.querySelector(`#${listId}`);
                if(list) list.innerHTML = options.map(opt => `<option value="${opt}">`).join('');
            } catch(e){}
        };
        fillModalDatalist('sdept', 'dl-sdept');
        fillModalDatalist('sjx', 'dl-sjx');
        fillModalDatalist('sbname', 'dl-sbname');
        modal.modalElement.querySelector('#btn-save-modal').addEventListener('click', async () => {
            const form = modal.modalElement.querySelector('form');
            if (!form.checkValidity()) { form.reportValidity(); return; }
            const formData = new FormData(form);
            const payload = Object.fromEntries(formData.entries());
            try {
                await saveLedger(payload);
                modal.hide();
                this._loadData();
                Modal.alert("保存成功");
            } catch (e) { Modal.alert("保存失败: " + e.message); }
        });
        modal.show();
    }

    async _openFileManagementModal() {

        const bodyHtml = `
            <div class="mb-3">
                <div class="d-flex justify-content-between align-items-center mb-2">
                    <h6 class="mb-0 text-info">自检自控标准文件库 (PDF)</h6>
                    <button class="btn btn-sm btn-outline-primary" id="btn-upload-pdf">
                        <i class="bi bi-cloud-upload"></i> 上传PDF标准
                    </button>
                </div>
                <input type="file" id="hidden-file-input" accept=".pdf" style="display:none">
                
                <div class="table-responsive border rounded" style="max-height: 300px; overflow-y: auto;">
                    <table class="table table-sm table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th>文件名</th>
                                <th style="width: 150px;">上传时间</th>
                                <th style="width: 100px;">上传人</th>
                                <th style="width: 120px;">操作</th>
                            </tr>
                        </thead>
                        <tbody id="file-list-tbody">
                            <tr><td colspan="4" class="text-center text-secondary p-3"><div class="spinner-border spinner-border-sm"></div> 加载中...</td></tr>
                        </tbody>
                    </table>
                </div>
            </div>
        `;

        const footerHtml = `<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>`;

        const modal = new Modal({
            title: "标准附件管理",
            body: bodyHtml,
            footer: footerHtml,
            size: 'lg'
        });

        const loadFiles = async () => {
            const tbody = modal.modalElement.querySelector('#file-list-tbody');
            try {
                const files = await getStandardFiles();
                if (files.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="4" class="text-center text-secondary p-3">暂无上传文件</td></tr>';
                } else {
                    tbody.innerHTML = files.map(f => `
                        <tr>
                            <td><i class="bi bi-file-pdf text-danger me-2"></i>${f.fileName}</td>
                            <td>${f.uploadTime || '-'}</td>
                            <td>${f.uploader || '-'}</td>
                            <td>
                                <button class="btn btn-xs btn-link text-primary btn-preview" data-id="${f.id}"><i class="bi bi-eye"></i> 预览</button>
                                <button class="btn btn-xs btn-link text-danger btn-delete-file" data-id="${f.id}"><i class="bi bi-trash"></i> 删除</button>
                            </td>
                        </tr>
                    `).join('');

                    tbody.querySelectorAll('.btn-preview').forEach(btn => {
                        btn.addEventListener('click', () => {
                            this._openPdfPreview(btn.dataset.id);
                        });
                    });

                    tbody.querySelectorAll('.btn-delete-file').forEach(btn => {
                        btn.addEventListener('click', async () => {
                            if (await Modal.confirm('确定删除该标准附件吗？')) {
                                try {
                                    await deleteStandardFile(btn.dataset.id);
                                    loadFiles();
                                } catch(e) { Modal.alert("删除失败: " + e.message); }
                            }
                        });
                    });
                }
            } catch (e) {
                tbody.innerHTML = '<tr><td colspan="4" class="text-center text-danger p-3">加载失败</td></tr>';
            }
        };

        const fileInput = modal.modalElement.querySelector('#hidden-file-input');
        modal.modalElement.querySelector('#btn-upload-pdf').addEventListener('click', () => fileInput.click());

        fileInput.addEventListener('change', async () => {
            if (fileInput.files.length > 0) {
                const file = fileInput.files[0];
                if (file.type !== 'application/pdf' && !file.name.toLowerCase().endsWith('.pdf')) {
                    Modal.alert("仅支持上传 PDF 文件！");
                    return;
                }

                const uploadBtn = modal.modalElement.querySelector('#btn-upload-pdf');
                uploadBtn.disabled = true;
                uploadBtn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 上传中...';

                try {
                    await uploadStandardFile(file);
                    fileInput.value = '';
                    await loadFiles();
                    Modal.alert("上传成功");
                } catch(e) {
                    Modal.alert("上传失败: " + e.message);
                } finally {
                    uploadBtn.disabled = false;
                    uploadBtn.innerHTML = '<i class="bi bi-cloud-upload"></i> 上传PDF标准';
                }
            }
        });

        modal.show();
        loadFiles();
    }

    _openPdfPreview(fileId) {
        const url = getFilePreviewUrl(fileId);
        const bodyHtml = `
            <div class="ratio ratio-4x3" style="height: 70vh;">
                <iframe src="${url}" allowfullscreen style="border:none;"></iframe>
            </div>
        `;
        new Modal({
            title: "标准文件预览",
            body: bodyHtml,
            footer: '<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>',
            size: 'xl'
        }).show();
    }

    // --- [修改] 生成任务模态框 (支持多列过滤 + 主题适配) ---
    _openGenerateTaskModal() {
        this.selectedItemsMap.clear();

        const now = new Date();
        const todayStr = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}:${String(now.getSeconds()).padStart(2,'0')}`;

        // [修改] 为过滤输入框和背景添加主题变量，确保黑白主题下显示正常
        const bodyHtml = `
            <div class="container-fluid h-100 d-flex flex-column" style="height: 75vh; overflow: hidden;">
                <!-- 1. 任务参数表单 -->
                <div class="card mb-3 flex-shrink-0" style="background-color: var(--bg-secondary); border-color: var(--border-color); color: var(--text-primary);">
                    <div class="card-header fw-bold py-2" style="background-color: var(--bg-primary); border-bottom: 1px solid var(--border-color);">任务参数配置</div>
                    <div class="card-body py-2">
                        <form id="gen-task-form" class="row g-2 align-items-center">
                            <div class="col-md-4">
                                <label class="form-label small mb-0">任务时间</label>
                                <input type="text" class="form-control form-control-sm" name="taskTime" value="${todayStr}">
                            </div>
                            <div class="col-md-4">
                                <label class="form-label small mb-0">任务类型</label>
                                <select class="form-select form-select-sm" name="taskType">
                                    <option value="三班电气" selected>三班电气</option>
                                    <option value="白班">白班</option>
                                    <option value="年检">年检</option>
                                </select>
                            </div>
                            <div class="col-md-4">
                                <label class="form-label small mb-0">生产状态</label>
                                <select class="form-select form-select-sm" name="prodStatus">
                                    <option value="生产" selected>生产</option>
                                    <option value="停产">停产</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small mb-0">班别</label>
                                <select class="form-select form-select-sm" name="shiftType">
                                    <option value="甲班" selected>甲班</option>
                                    <option value="乙班">乙班</option>
                                    <option value="丙班">丙班</option>
                                    <option value="白班">白班</option>
                                </select>
                            </div>
                            <div class="col-md-6">
                                <label class="form-label small mb-0">班次</label>
                                <select class="form-select form-select-sm" name="shift">
                                    <option value="早班" selected>早班</option>
                                    <option value="中班">中班</option>
                                    <option value="夜班">夜班</option>
                                </select>
                            </div>
                        </form>
                    </div>
                </div>
                
                <!-- 2. 已选设备展示栏 -->
                <div class="card mb-2 flex-shrink-0" style="background-color: var(--bg-secondary); border-color: var(--border-color);">
                    <div class="card-body py-2 d-flex align-items-center justify-content-between">
                        <span class="small" style="color: var(--text-primary);">已选设备: <strong id="selected-count" style="color: var(--accent-color);">0</strong> 个</span>
                        <button class="btn btn-xs btn-link text-decoration-none" type="button" data-bs-toggle="collapse" data-bs-target="#selected-items-panel">
                            查看详情 <i class="bi bi-chevron-down"></i>
                        </button>
                    </div>
                    <div class="collapse border-top" id="selected-items-panel" style="border-color: var(--border-color) !important;">
                        <div class="card-body py-2 small" id="selected-items-list" style="max-height: 100px; overflow-y: auto; color: var(--text-primary);">
                            <span class="text-muted">暂无选择</span>
                        </div>
                    </div>
                </div>

                <!-- 3. 设备选择列表 (带多列过滤) -->
                <div class="card flex-grow-1 d-flex flex-column overflow-hidden" id="device-selection-card" style="background-color: var(--bg-secondary); border-color: var(--border-color);">
                    <div class="card-header fw-bold py-2 d-flex justify-content-between align-items-center flex-shrink-0" style="background-color: var(--bg-primary); border-bottom: 1px solid var(--border-color); color: var(--text-primary);">
                        <span>选择设备 (支持多选)</span>
                    </div>
                    
                    <!-- [新增] 过滤工具栏：使用透明背景或根据主题变色，移除硬编码的 bg-light-subtle -->
                    <div class="p-2 border-bottom d-flex gap-2 align-items-center" style="background-color: var(--bg-primary); border-color: var(--border-color) !important;">
                        <input type="text" class="form-control form-control-sm" id="gen-filter-sjx" placeholder="机型" style="background-color: var(--bg-secondary); color: var(--text-primary); border-color: var(--border-color);">
                        <input type="text" class="form-control form-control-sm" id="gen-filter-sfname" placeholder="所属设备" style="background-color: var(--bg-secondary); color: var(--text-primary); border-color: var(--border-color);">
                        <input type="text" class="form-control form-control-sm" id="gen-filter-sbname" placeholder="主数据名称" style="background-color: var(--bg-secondary); color: var(--text-primary); border-color: var(--border-color);">
                        <input type="text" class="form-control form-control-sm" id="gen-filter-spmcode" placeholder="PM编码" style="background-color: var(--bg-secondary); color: var(--text-primary); border-color: var(--border-color);">
                        <button class="btn btn-sm btn-primary" type="button" id="gen-filter-btn"><i class="bi bi-search"></i></button>
                        <button class="btn btn-sm btn-secondary" type="button" id="gen-reset-btn"><i class="bi bi-arrow-counterclockwise"></i></button>
                    </div>

                    <div class="card-body p-0 position-relative flex-grow-1 overflow-hidden d-flex flex-column" id="gen-table-container">
                        <!-- DataTable 挂载点 -->
                    </div>
                </div>
            </div>
        `;

        const footerHtml = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-success" id="btn-confirm-gen"><i class="bi bi-lightning-fill"></i> 确认生成</button>
        `;

        const modal = new Modal({ title: "生成点检任务", body: bodyHtml, footer: footerHtml, size: 'xl' });

        const columns = [
            { key: 'sjx', title: '所属机型', width: 150 },
            { key: 'sfname', title: '所属设备', width: 180 },
            { key: 'sbname', title: '主数据名称', width: 200 },
            { key: 'spmcode', title: 'PM编码', width: 200 },
        ];

        this.genTaskTable = new DataTable({
            columns,
            data: [],
            options: {
                selectable: 'multiple',
                pagination: true,
                uniformRowHeight: true,
                storageKey: 'genTaskSelectTable_v3' // Update storage key
            }
        });

        setTimeout(() => {
            const tableContainer = modal.modalElement.querySelector('#gen-table-container');
            this.genTaskTable.render(tableContainer);
            this._loadGenTableData();

            tableContainer.addEventListener('click', () => this._syncSelectionMap());
            tableContainer.addEventListener('change', () => this._syncSelectionMap());
            tableContainer.addEventListener('pageChange', (e) => {
                this.genTaskTable.state.pageNum = e.detail.pageNum;
                this._loadGenTableData();
            });

        }, 100);

        // 绑定过滤事件
        const getFilterParams = () => ({
            sjx: modal.modalElement.querySelector('#gen-filter-sjx').value,
            sfname: modal.modalElement.querySelector('#gen-filter-sfname').value,
            sbname: modal.modalElement.querySelector('#gen-filter-sbname').value,
            spmcode: modal.modalElement.querySelector('#gen-filter-spmcode').value
        });

        modal.modalElement.querySelector('#gen-filter-btn').addEventListener('click', () => {
            this.genTaskTable.state.pageNum = 1;
            this._loadGenTableData(getFilterParams());
        });

        modal.modalElement.querySelector('#gen-reset-btn').addEventListener('click', () => {
            modal.modalElement.querySelectorAll('input[id^="gen-filter-"]').forEach(inp => inp.value = '');
            this.genTaskTable.state.pageNum = 1;
            this._loadGenTableData();
        });

        modal.modalElement.querySelector('#btn-confirm-gen').addEventListener('click', async () => {
            const allSelectedItems = Array.from(this.selectedItemsMap.values());

            if (allSelectedItems.length === 0) {
                Modal.alert("请至少选择一个设备/工装！");
                return;
            }

            const form = modal.modalElement.querySelector('#gen-task-form');
            const formData = new FormData(form);
            const payload = Object.fromEntries(formData.entries());
            payload.taskTime = payload.taskTime.replace(' ', 'T');

            // [修改] 提交 spmcode 和 sname (后端需要 sname 做校验或展示，但主要是 spmcode)
            // 注意：这里的 sname 其实取的是 sbname (见 _syncSelectionMap)
            payload.selectedDevices = allSelectedItems.map(item => ({
                spmcode: item.spmcode,
                sname: item.sbname || item.sfname // 优先取主数据名
            }));

            const btn = modal.modalElement.querySelector('#btn-confirm-gen');
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 处理中...';

            try {
                await generateSiTasks(payload);
                modal.hide();
                Modal.alert("任务生成成功！请前往“点检任务”页面查看。");
            } catch (e) {
                Modal.alert("生成失败: " + e.message);
            } finally {
                btn.disabled = false;
                btn.innerHTML = '<i class="bi bi-lightning-fill"></i> 确认生成';
            }
        });

        modal.show();
    }

    _syncSelectionMap() {
        if (!this.genTaskTable) return;
        const currentSelectionIds = this.genTaskTable.selectedRows;

        if (this.genTaskTable.data) {
            this.genTaskTable.data.forEach(row => {
                // [修改] 使用 spmcode 作为 ID
                const id = row.spmcode;
                if (currentSelectionIds.has(id)) {
                    if (!this.selectedItemsMap.has(id)) {
                        this.selectedItemsMap.set(id, row);
                    }
                } else {
                    if (this.selectedItemsMap.has(id)) {
                        this.selectedItemsMap.delete(id);
                    }
                }
            });
        }
        this._updateSelectedView();
    }

    _updateSelectedView() {
        const countSpan = document.getElementById('selected-count');
        const listContainer = document.getElementById('selected-items-list');
        if (!countSpan || !listContainer) return;

        const items = Array.from(this.selectedItemsMap.values());
        countSpan.textContent = items.length;

        if (items.length === 0) {
            listContainer.innerHTML = '<span class="text-muted">暂无选择</span>';
        } else {
            // [Fix] 显示 sbname 或 sfname，解决“未命名”问题
            listContainer.innerHTML = items.map(item => `
                <span class="badge bg-info text-dark me-1 mb-1" style="font-weight: normal; border: 1px solid var(--border-color);">
                    ${item.sbname || item.sfname || item.spmcode}
                    <i class="bi bi-x ms-1" style="cursor: pointer;" onclick="document.getElementById('remove-item-${item.spmcode}').click()"></i>
                    <button id="remove-item-${item.spmcode}" class="d-none" data-id="${item.spmcode}"></button>
                </span>
            `).join('');

            listContainer.querySelectorAll('button').forEach(btn => {
                btn.addEventListener('click', (e) => {
                    e.stopPropagation();
                    const idKey = btn.dataset.id;
                    this._removeItem(idKey);
                });
            });
        }
    }

    _removeItem(idKey) {
        this.selectedItemsMap.delete(idKey);
        if (this.genTaskTable && this.genTaskTable.selectedRows.has(idKey)) {
            this.genTaskTable.selectedRows.delete(idKey);
            // 刷新视图以更新 checkbox 状态
            this.genTaskTable.render(this.genTaskTable.container);
        }
        this._updateSelectedView();
    }

    async _loadGenTableData(filterParams = {}) {
        if (!this.genTaskTable) return;
        this.genTaskTable.toggleLoading(true);
        try {
            const params = { pageNum: this.genTaskTable.state.pageNum || 1, pageSize: 100, ...filterParams };

            const result = await getTaskGenerationDeviceList(params);

            if (result && result.list) {
                result.list.forEach(item => {
                    // [修改] 使用 SPMCODE 作为前端 ID，因为它在生成列表里是唯一的
                    item.id = item.spmcode;
                });
            }
            this.genTaskTable.updateView(result);

            // 恢复选中状态 (翻页后保持)
            if (this.selectedItemsMap.size > 0) {
                const currentRows = this.genTaskTable.data;
                currentRows.forEach(row => {
                    if (this.selectedItemsMap.has(row.spmcode)) {
                        this.genTaskTable.selectedRows.add(row.spmcode);
                    }
                });
                // 重新渲染以显示勾选
                // 注意：updateView 已经调用了 render，但此时 selectedRows 可能刚被更新
                // 我们需要一种机制让 DataTable 根据 selectedRows 渲染 checkbox
                // 目前 DataTable 的 render 逻辑依赖于 selectedRows Set，所以再次调用 render 是安全的
                // 但为了避免闪烁，最好是 updateView 内部处理。
                // 由于 Optimized_DataTable 的 updateView 只是简单的覆盖数据并 render，
                // 我们可以在数据赋值后手动同步一下 Set。
                // 这里的逻辑是：数据加载 -> 检查 selectedItemsMap -> 更新 selectedRows Set -> render

                // 简单的做法：再次调用 render，虽然有点浪费但最稳妥
                this.genTaskTable.render(this.genTaskTable.container);
            }

        } catch (e) { console.error(e); }
        finally { this.genTaskTable.toggleLoading(false); }
    }
}