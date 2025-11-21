/**
 * @file /js/views/NewSelfInspectionLedger.js
 * @description 新自检自控台账管理视图。
 * v1.4.0 - [Fix] 修复“查看标准详情”弹窗的主题适配问题（移除硬编码的黑色样式，使用 CSS 变量）。
 */
import DataTable from '../components/Optimized_DataTable.js';
import Modal from '../components/Modal.js';
import DatePicker from '../components/DatePicker.js';
import { getLedgerList, saveLedger, deleteLedger, getAutocompleteOptions, getStandardDetails } from '../services/selfInspectionApi.js';

export default class NewSelfInspectionLedger {
    constructor() {
        this.container = null;
        this.dataTable = null;
        this.searchParams = {};
    }

    render(container) {
        this.container = container;
        container.innerHTML = `
            <div class="d-flex flex-column h-100">
                <!-- 顶部查询区域 -->
                <div class="p-3 rounded mb-3" style="background-color: var(--bg-primary);">
                    <div class="row g-2 align-items-center">
                        <!-- 第一行查询条件 -->
                        <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary">车间:</label>
                            <input type="text" class="form-control form-control-sm" id="search-workshop" list="list-workshop">
                            <datalist id="list-workshop"></datalist>
                        </div>
                        <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary">所属机型:</label>
                            <input type="text" class="form-control form-control-sm" id="search-model" list="list-model">
                            <datalist id="list-model"></datalist>
                        </div>
                        <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary">所属设备主数据名称:</label>
                            <input type="text" class="form-control form-control-sm" id="search-mainDevice" list="list-mainDevice">
                            <datalist id="list-mainDevice"></datalist>
                        </div>
                         <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary">资产编码:</label>
                            <input type="text" class="form-control form-control-sm" id="search-assetCode">
                        </div>

                        <!-- 第二行查询条件 -->
                        <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary">名称:</label>
                            <input type="text" class="form-control form-control-sm" id="search-name">
                        </div>
                        <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary">装置编号类别:</label>
                            <input type="text" class="form-control form-control-sm" id="search-codeType">
                        </div>
                        <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary">PM设备编码:</label>
                            <input type="text" class="form-control form-control-sm" id="search-pmCode">
                        </div>
                         <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0 text-secondary">订单号:</label>
                            <input type="text" class="form-control form-control-sm" id="search-orderNo">
                        </div>

                        <!-- 第三行：操作按钮行 -->
                        <div class="col-12 d-flex justify-content-between align-items-center mt-3">
                            <!-- 左侧：维护按钮组 -->
                            <div class="d-flex gap-2">
                                <button class="btn btn-sm btn-outline-primary" id="btn-add"><i class="bi bi-plus-lg"></i> 增加</button>
                                <button class="btn btn-sm btn-outline-secondary" id="btn-edit"><i class="bi bi-pencil-square"></i> 编辑</button>
                                <button class="btn btn-sm btn-outline-danger" id="btn-delete"><i class="bi bi-trash"></i> 删除</button>
                                <div class="vr mx-1 text-secondary"></div>
                                <button class="btn btn-sm btn-secondary" id="btn-import"><i class="bi bi-upload"></i> 点检标准导入</button>
                                <!-- 查看点检标准详情按钮 -->
                                <button class="btn btn-sm btn-info text-white" id="btn-view-standard"><i class="bi bi-list-check"></i> 查看点检标准详情</button>
                            </div>
                            <!-- 右侧：查询按钮组 -->
                            <div class="d-flex gap-2">
                                <button class="btn btn-sm btn-primary" id="btn-search" style="min-width: 80px;"><i class="bi bi-search"></i> 查询</button>
                                <button class="btn btn-sm btn-secondary" id="btn-clear" style="min-width: 80px;"><i class="bi bi-eraser"></i> 清空</button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 表格容器 -->
                <div id="table-container" class="flex-grow-1 p-2 rounded" style="background-color: var(--bg-secondary); border: 1px solid var(--border-color);"></div>
            </div>
        `;

        this._initTable();
        this._initAutocompletes();
        this._attachEventListeners();
    }

    _initTable() {
        const columns = [
            { key: 'auditStatus', title: '审批状态', width: 80, render: val => `<span class="badge bg-success">${val}</span>` },
            { key: 'id', title: '序号', width: 60 },
            {
                key: 'hasStandard',
                title: '是否上传标准',
                width: 100,
                render: (val) => val
                    ? '<i class="bi bi-check-circle-fill text-success" title="已上传"></i>'
                    : '<i class="bi bi-x-circle-fill text-secondary" title="未上传"></i>'
            },
            { key: 'workshop', title: '车间', width: 100 },
            { key: 'name', title: '名称', width: 180 },
            { key: 'model', title: '所属机型', width: 120 },
            { key: 'device', title: '所属设备', width: 120 },
            { key: 'mainDevice', title: '所属设备主数据名称', width: 200 },
            { key: 'factory', title: '厂家', width: 120 },
            { key: 'spec', title: '规格型号', width: 120 },
            { key: 'location', title: '安装位置', width: 150 },
            { key: 'principle', title: '测量原理', width: 120 },
            { key: 'pmCode', title: 'PM设备编码', width: 120 },
            { key: 'orderNo', title: '订单号', width: 120 },
            { key: 'assetCode', title: '资产编码', width: 120 },
        ];

        this.dataTable = new DataTable({
            columns,
            data: [],
            options: {
                selectable: 'single',
                pagination: true,
                uniformRowHeight: true,
                storageKey: 'newSiLedgerTable_v2'
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
        fillDatalist('workshop', 'list-workshop');
        fillDatalist('model', 'list-model');
        fillDatalist('mainDevice', 'list-mainDevice');
    }

    async _loadData() {
        if (!this.dataTable) return;
        this.dataTable.toggleLoading(true);
        const params = {
            workshop: this.container.querySelector('#search-workshop').value,
            model: this.container.querySelector('#search-model').value,
            mainDevice: this.container.querySelector('#search-mainDevice').value,
            name: this.container.querySelector('#search-name').value,
            pmCode: this.container.querySelector('#search-pmCode').value,
            assetCode: this.container.querySelector('#search-assetCode').value,
            pageNum: this.dataTable.state.pageNum,
            pageSize: this.dataTable.state.pageSize
        };
        try {
            const result = await getLedgerList(params);
            this.dataTable.updateView(result);
        } catch (e) { console.error(e); Modal.alert("加载数据失败"); }
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
        this.container.querySelector('#table-container').addEventListener('pageChange', (e) => {
            this.dataTable.state.pageNum = e.detail.pageNum;
            this._loadData();
        });
        this.container.querySelector('#btn-add').addEventListener('click', () => this._openEditModal(null));
        this.container.querySelector('#btn-edit').addEventListener('click', () => {
            const selectedId = this.dataTable.selectedRowId;
            if (!selectedId) { Modal.alert("请选择一条记录进行编辑"); return; }
            const rowData = this.dataTable.data.find(d => String(d.id) === String(selectedId));
            this._openEditModal(rowData);
        });
        this.container.querySelector('#btn-delete').addEventListener('click', async () => {
            const selectedId = this.dataTable.selectedRowId;
            if (!selectedId) { Modal.alert("请选择要删除的记录"); return; }
            const confirmed = await Modal.confirm("确认删除选中的记录吗？");
            if (confirmed) {
                await deleteLedger([selectedId]);
                this._loadData();
                Modal.alert("删除成功");
            }
        });
        this.container.querySelector('#btn-import').addEventListener('click', () => this._openImportModal());
        this.container.querySelector('#btn-view-standard').addEventListener('click', () => this._openStandardDetailModal());
    }

    // --- [修改] 查看点检标准详情模态框 ---
    async _openStandardDetailModal() {
        const selectedId = this.dataTable.selectedRowId;
        if (!selectedId) {
            Modal.alert("请先选择一条记录");
            return;
        }
        const rowData = this.dataTable.data.find(d => String(d.id) === String(selectedId));

        // 1. 准备顶部信息区 (使用 CSS 变量适配主题)
        const headerInfoHtml = `
            <div class="p-3 mb-3 rounded" style="background-color: var(--bg-primary); color: var(--text-primary); border: 1px solid var(--border-color);">
                <div class="row">
                    <div class="col-md-6 mb-1"><strong>设备名称:</strong> ${rowData.name || '-'}</div>
                    <div class="col-md-6 mb-1"><strong>资产编号:</strong> ${rowData.assetCode || '-'}</div>
                    <div class="col-md-6"><strong>所属设备:</strong> ${rowData.device || '-'}</div>
                    <div class="col-md-6"><strong>安装位置:</strong> ${rowData.location || '-'}</div>
                </div>
            </div>
        `;

        // 2. 获取详情数据
        let detailsData = [];
        try {
            detailsData = await getStandardDetails(selectedId);
        } catch (e) {
            Modal.alert("获取详情失败");
            return;
        }

        // 3. 构建表格 HTML
        const tableRows = detailsData.map((item, index) => `
            <tr>
                <td>${item.device}</td>
                <td>${item.item}</td>
                <td>${item.standard}</td>
                <td>${item.executor}</td>
            </tr>
        `).join('');

        const tableHtml = `
            <div class="table-responsive" style="max-height: 400px; overflow-y: auto;">
                <!-- [修改] 移除 table-dark, table-striped，使用 style="color: inherit" -->
                <table class="table table-hover table-bordered table-sm align-middle mb-0" style="color: inherit; background-color: transparent; border-color: var(--border-color);">
                    <thead class="table-light">
                        <tr>
                            <th>检测装置 (SJCZZ)</th>
                            <th>检测项目 (SJCXM)</th>
                            <th>检测标准 (SJCBZ)</th>
                            <th>执行人 (SEXUSER)</th>
                        </tr>
                    </thead>
                    <tbody>
                        ${detailsData.length > 0 ? tableRows : '<tr><td colspan="4" class="text-center py-3">无标准数据</td></tr>'}
                    </tbody>
                </table>
            </div>
        `;

        const bodyHtml = headerInfoHtml + tableHtml;

        const footerHtml = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">关闭</button>
            <button type="button" class="btn btn-success" id="btn-download-attachment">
                <i class="bi bi-download"></i> 下载标准附件
            </button>
        `;

        const modal = new Modal({
            title: "点检标准详情",
            body: bodyHtml,
            footer: footerHtml,
            size: 'xl'
        });

        // [关键修改] 移除之前所有手动设置 style.backgroundColor 的代码
        // 模态框会自动继承 style.css 中定义的 .modal-content 样式 (background-color: var(--bg-secondary))
        // 这确保了在白色主题下是白色，黑色主题下是黑色。

        // 绑定下载事件
        modal.modalElement.querySelector('#btn-download-attachment').addEventListener('click', () => {
            const btn = modal.modalElement.querySelector('#btn-download-attachment');
            btn.disabled = true;
            btn.innerHTML = '<span class="spinner-border spinner-border-sm"></span> 下载中...';
            setTimeout(() => {
                btn.disabled = false;
                btn.innerHTML = '<i class="bi bi-download"></i> 下载标准附件';
                Modal.alert(`附件 "标准详情_${rowData.name}.xlsx" 已开始下载。`);
            }, 1000);
        });

        modal.show();
    }

    // --- 导入模态框 ---
    _openImportModal() {
        const selectedId = this.dataTable.selectedRowId;
        if (!selectedId) {
            Modal.alert("请先选择一条要导入标准的台账记录！");
            return;
        }

        const bodyHtml = `
            <form id="import-form">
                <div class="mb-3">
                    <label for="import-file" class="form-label">选择点检标准文件 (仅限 Excel)</label>
                    <input class="form-control" type="file" id="import-file" accept=".xlsx, .xls">
                </div>
                <div class="progress d-none mb-3" id="upload-progress-container">
                    <div class="progress-bar progress-bar-striped progress-bar-animated" role="progressbar" style="width: 0%" id="upload-progress-bar">0%</div>
                </div>
                <div class="alert alert-info small">
                    <i class="bi bi-info-circle"></i> 请上传 .xlsx 或 .xls 格式的标准模板。
                </div>
            </form>
        `;

        const footerHtml = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal" id="btn-cancel-import">取消</button>
            <button type="button" class="btn btn-primary" id="btn-import-confirm">上传并导入</button>
        `;

        const modal = new Modal({ title: "点检标准导入", body: bodyHtml, footer: footerHtml });
        const progressBar = modal.modalElement.querySelector('#upload-progress-bar');
        const progressContainer = modal.modalElement.querySelector('#upload-progress-container');
        const confirmBtn = modal.modalElement.querySelector('#btn-import-confirm');
        const cancelBtn = modal.modalElement.querySelector('#btn-cancel-import');

        confirmBtn.addEventListener('click', () => {
            const fileInput = modal.modalElement.querySelector('#import-file');
            if (fileInput.files.length === 0) {
                Modal.alert("请先选择文件！");
                return;
            }
            const fileName = fileInput.files[0].name;
            if (!fileName.endsWith('.xlsx') && !fileName.endsWith('.xls')) {
                Modal.alert("文件格式错误，请上传 Excel 文件。");
                return;
            }

            confirmBtn.disabled = true;
            cancelBtn.disabled = true;
            fileInput.disabled = true;
            progressContainer.classList.remove('d-none');

            let progress = 0;
            const interval = setInterval(() => {
                progress += Math.floor(Math.random() * 10) + 5;
                if (progress > 100) progress = 100;
                progressBar.style.width = `${progress}%`;
                progressBar.textContent = `${progress}%`;
                if (progress === 100) {
                    clearInterval(interval);
                    setTimeout(() => {
                        modal.hide();
                        Modal.alert(`文件 "${fileName}" 导入成功！`);
                        this._loadData();
                    }, 500);
                }
            }, 200);
        });
        modal.show();
    }

    _openEditModal(data) {
        const isEdit = !!data;
        const title = isEdit ? "编辑台账" : "新增台账";
        const formHtml = `
            <form id="ledger-form" class="container-fluid">
                <input type="hidden" name="id" value="${data?.id || ''}">
                <div class="row mb-3 align-items-center">
                    <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>车间</label></div>
                    <div class="col-md-4"><input type="text" class="form-control" name="workshop" list="dl-workshop" required value="${data?.workshop || ''}"><datalist id="dl-workshop"></datalist></div>
                    <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>名称</label></div>
                    <div class="col-md-4"><input type="text" class="form-control" name="name" required value="${data?.name || ''}"></div>
                </div>
                <div class="row mb-3 align-items-center">
                    <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>所属机型</label></div>
                    <div class="col-md-4"><input type="text" class="form-control" name="model" list="dl-model" required value="${data?.model || ''}"><datalist id="dl-model"></datalist></div>
                    <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>所属设备</label></div>
                    <div class="col-md-4"><input type="text" class="form-control" name="device" required value="${data?.device || ''}"></div>
                </div>
                <div class="row mb-3 align-items-center">
                    <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>所属设备主数据名称</label></div>
                    <div class="col-md-4"><input type="text" class="form-control" name="mainDevice" list="dl-mainDevice" required value="${data?.mainDevice || ''}"><datalist id="dl-mainDevice"></datalist></div>
                    <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>厂家</label></div>
                    <div class="col-md-4"><input type="text" class="form-control" name="factory" required value="${data?.factory || ''}"></div>
                </div>
                <div class="row mb-3 align-items-center">
                     <div class="col-md-2 text-end"><label class="form-label">规格型号</label></div>
                     <div class="col-md-4"><input type="text" class="form-control" name="spec" value="${data?.spec || ''}"></div>
                     <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>安装位置</label></div>
                     <div class="col-md-4"><input type="text" class="form-control" name="location" required value="${data?.location || ''}"></div>
                </div>
                 <div class="row mb-3 align-items-center">
                     <div class="col-md-2 text-end"><label class="form-label">测量原理</label></div>
                     <div class="col-md-4"><input type="text" class="form-control" name="principle" value="${data?.principle || ''}"></div>
                     <div class="col-md-2 text-end"><label class="form-label">PM设备编码</label></div>
                     <div class="col-md-4"><input type="text" class="form-control" name="pmCode" value="${data?.pmCode || ''}"></div>
                </div>
                <div class="row mb-3 align-items-center">
                     <div class="col-md-2 text-end"><label class="form-label">订单号</label></div>
                     <div class="col-md-4"><input type="text" class="form-control" name="orderNo" value="${data?.orderNo || ''}"></div>
                     <div class="col-md-2 text-end"><label class="form-label">资产编码</label></div>
                     <div class="col-md-4"><input type="text" class="form-control" name="assetCode" value="${data?.assetCode || ''}"></div>
                </div>
            </form>
        `;
        const footerHtml = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" id="btn-save-modal">提交</button>
        `;
        const modal = new Modal({ title: title, body: formHtml, footer: footerHtml, size: 'xl' });
        const fillModalDatalist = async (field, listId) => {
            try {
                const options = await getAutocompleteOptions(field);
                const list = modal.modalElement.querySelector(`#${listId}`);
                if(list) list.innerHTML = options.map(opt => `<option value="${opt}">`).join('');
            } catch(e){}
        };
        fillModalDatalist('workshop', 'dl-workshop');
        fillModalDatalist('model', 'dl-model');
        fillModalDatalist('mainDevice', 'dl-mainDevice');
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
}