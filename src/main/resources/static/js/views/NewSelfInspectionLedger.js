/**
 * @file /js/views/NewSelfInspectionLedger.js
 * @description 新自检自控台账管理视图 (重构版)。
 * v2.5.0 - [Fix] 修正生成任务时设备列表的数据源，严格调用分组接口 getTaskGenerationDeviceList。
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
    getTaskGenerationDeviceList // [修复] 必须导入此接口
} from '../services/selfInspectionApi.js';

export default class NewSelfInspectionLedger {
    constructor() {
        this.container = null;
        this.dataTable = null;
        // 缓存生成任务列表的数据表格实例
        this.genTaskTable = null;
        // [New] 缓存已选设备信息 {id: {name, ...}}，用于跨页显示名称
        this.selectedItemsMap = new Map();
    }

    render(container) {
        this.container = container;
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
                                <button class="btn btn-sm btn-outline-primary" id="btn-add"><i class="bi bi-plus-lg"></i> 增加</button>
                                <button class="btn btn-sm btn-outline-secondary" id="btn-edit"><i class="bi bi-pencil-square"></i> 编辑</button>
                                <button class="btn btn-sm btn-outline-danger" id="btn-delete"><i class="bi bi-trash"></i> 删除</button>
                                <div class="vr mx-1 text-secondary"></div>
                                <button class="btn btn-sm btn-info text-white" id="btn-files"><i class="bi bi-file-earmark-pdf"></i> 标准附件管理(PDF)</button>
                            </div>
                            <div class="d-flex gap-2">
                                <button class="btn btn-sm btn-warning text-dark" id="btn-generate"><i class="bi bi-lightning-charge-fill"></i> 生成任务</button>
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
            { key: 'sstepstate', title: '审批状态', width: 80, render: val => `<span class="badge bg-success">${val || '草稿'}</span>` },
            // 虚拟序号列
            { key: '_index', title: '序号', width: 60 },
            { key: 'sdept', title: '车间', width: 100 },
            { key: 'sname', title: '名称', width: 180 },
            { key: 'sjx', title: '所属机型', width: 120 },
            { key: 'sfname', title: '所属设备', width: 120 },
            { key: 'sbname', title: '主设备名称', width: 200 },
            { key: 'spmcode', title: 'PM编码', width: 120 },
            { key: 'sazwz', title: '安装位置', width: 150 },
        ];

        this.dataTable = new DataTable({
            columns,
            data: [],
            options: {
                selectable: 'single',
                pagination: true,
                uniformRowHeight: true,
                storageKey: 'zjzkToolTable_v2'
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
                    item.id = item.indocno;
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

        this.container.querySelector('#btn-files').addEventListener('click', () => this._openFileManagementModal());
        this.container.querySelector('#btn-generate').addEventListener('click', () => this._openGenerateTaskModal());
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
                const files = await getStandardFiles(); // 获取所有
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
                            const url = getFilePreviewUrl(btn.dataset.id);
                            window.open(url, '_blank');
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
                    // 上传时不传 ID
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

    // --- [修改] 生成任务模态框 (使用新接口) ---
    _openGenerateTaskModal() {
        this.selectedItemsMap.clear(); // 每次打开清空已选

        const now = new Date();
        const todayStr = `${now.getFullYear()}-${String(now.getMonth()+1).padStart(2,'0')}-${String(now.getDate()).padStart(2,'0')} ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}:${String(now.getSeconds()).padStart(2,'0')}`;

        // 使用 Flex 布局确保弹窗内容不溢出，内部滚动
        const bodyHtml = `
            <div class="container-fluid h-100 d-flex flex-column" style="height: 70vh; overflow: hidden;">
                <!-- 1. 任务参数表单 -->
                <div class="card mb-3" style="flex-shrink: 0; background-color: var(--bg-secondary); border-color: var(--border-color); color: var(--text-primary);">
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
                
                <!-- 2. 已选设备展示栏 (固定高度，使用变量颜色) -->
                <div class="card mb-2" style="flex-shrink: 0; background-color: var(--bg-secondary); border-color: var(--border-color);">
                    <div class="card-body py-2 d-flex align-items-center justify-content-between">
                        <!-- [修复] 字体颜色使用 var(--text-primary) 和 var(--accent-color) -->
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

                <!-- 3. 设备选择列表 (自动填充剩余高度，内部滚动) -->
                <div class="card flex-grow-1" id="device-selection-card" style="display: flex; flex-direction: column; overflow: hidden; background-color: var(--bg-secondary); border-color: var(--border-color);">
                    <div class="card-header fw-bold py-2 d-flex justify-content-between align-items-center flex-shrink-0" style="background-color: var(--bg-primary); border-bottom: 1px solid var(--border-color); color: var(--text-primary);">
                        <span>选择设备 (支持多选)</span>
                        <div class="input-group input-group-sm" style="width: 200px;">
                            <input type="text" class="form-control" id="gen-filter-input" placeholder="过滤主数据名称...">
                            <button class="btn btn-outline-secondary" type="button" id="gen-filter-btn"><i class="bi bi-search"></i></button>
                        </div>
                    </div>
                    <!-- [关键] card-body 设置为 flex-grow: 1 和 overflow: hidden -->
                    <div class="card-body p-0 position-relative" id="gen-table-container" style="height:300px;flex-grow: 1; overflow: hidden; display: flex; flex-direction: column;">
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

        // [修改] 列定义：移除 ID，按要求添加字段 (只显示分组字段)
        const columns = [
            { key: 'sjx', title: '所属机型', width: 150 },
            { key: 'sfname', title: '所属设备', width: 180 },
            { key: 'sbname', title: '主数据名称', width: 200 },
            { key: 'spmcode', title: 'PM编码', width: 200 },
            // { key: 'sazwz', title: '安装位置', width: 120 }, // 列表是分组后的，位置可能有多个，暂不显示
        ];

        this.genTaskTable = new DataTable({
            columns,
            data: [],
            options: {
                selectable: 'multiple',
                pagination: true,
                uniformRowHeight: true,
                storageKey: 'genTaskSelectTable_v2'
            }
        });

        // 延时渲染并计算高度
        setTimeout(() => {
            const tableContainer = modal.modalElement.querySelector('#gen-table-container');
            const deviceCard = modal.modalElement.querySelector('#device-selection-card');

            if (tableContainer && deviceCard) {
                console.log(`[Layout Debug] Device Card Height: ${deviceCard.clientHeight}px`);
                console.log(`[Layout Debug] Table Container Height: ${tableContainer.clientHeight}px`);
            }

            this.genTaskTable.render(tableContainer);
            this._loadGenTableData(); // [修改] 调用分组加载

            // 绑定选择事件
            tableContainer.addEventListener('click', () => this._syncSelectionMap());
            tableContainer.addEventListener('change', () => this._syncSelectionMap());
        }, 100);

        modal.modalElement.querySelector('#gen-filter-btn').addEventListener('click', () => {
            const val = modal.modalElement.querySelector('#gen-filter-input').value;
            this._loadGenTableData({ sbname: val });
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

            // [修改] 提交联合主键列表: {spmcode, sname}
            payload.selectedDevices = allSelectedItems.map(item => ({
                spmcode: item.spmcode,
                sname: item.sname
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
                const id = row.id; // 此处 id 为复合键 spmcode##sname
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
            // [Fix] 样式适配
            listContainer.innerHTML = items.map(item => `
                <span class="badge bg-info text-dark me-1 mb-1" style="font-weight: normal; border: 1px solid var(--border-color);">
                    ${item.sname || '未命名'}
                    <i class="bi bi-x ms-1" style="cursor: pointer;" onclick="document.getElementById('remove-item-${item.id}').click()"></i>
                    <button id="remove-item-${item.id}" class="d-none" data-id="${item.id}"></button>
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
            this.genTaskTable.render(this.genTaskTable.container);
        }
        this._updateSelectedView();
    }

    // [修改] 使用 getTaskGenerationDeviceList 接口加载分组数据
    async _loadGenTableData(filterParams = {}) {
        if (!this.genTaskTable) return;
        this.genTaskTable.toggleLoading(true);
        try {
            const params = { pageNum: this.genTaskTable.state.pageNum || 1, pageSize: 100, ...filterParams };

            // 调用分组专用接口
            const result = await getTaskGenerationDeviceList(params);

            if (result && result.list) {
                result.list.forEach(item => {
                    // [关键] 构建前端唯一 ID: spmcode##sname
                    item.id = `${item.spmcode}##${item.sname}`;
                });
            }
            this.genTaskTable.updateView(result);
            this._syncSelectionMap();

        } catch (e) { console.error(e); }
        finally { this.genTaskTable.toggleLoading(false); }
    }
}