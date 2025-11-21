/**
 * @file /js/views/SelfInspectionLedger.js
 * @description 自检自控台账管理视图。
 * 包含：左侧设备树、右侧查询与数据列表、以及新增/编辑模态框。
 * v1.1.0 - 优化布局：将维护按钮（增删改）移动到查询栏同一行左侧，查询按钮保持右侧。
 */
import DataTable from '../components/Optimized_DataTable.js';
import Splitter from '../utils/Splitter.js';
import SimpleTree from '../utils/SimpleTree.js';
import Modal from '../components/Modal.js';
import DatePicker from '../components/DatePicker.js';
import { getLedgerTree, getLedgerList, saveLedger, deleteLedger, getAutocompleteOptions } from '../services/selfInspectionApi.js';

export default class SelfInspectionLedger {
    constructor() {
        this.container = null;
        this.dataTable = null;
        this.tree = null;
        this.activeTreeNodeId = null;
        this.searchParams = {};
    }

    render(container) {
        this.container = container;
        container.innerHTML = `
            <div class="d-flex flex-column h-100">
                <!-- 顶部查询区域 (仿照上图样式，紧凑布局) -->
                <div class="p-3 rounded mb-3" style="background-color: var(--bg-primary);">
                    <div class="row g-2 align-items-center">
                        <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0">车间:</label>
                            <input type="text" class="form-control form-control-sm" id="search-workshop" list="list-workshop">
                            <datalist id="list-workshop"></datalist>
                        </div>
                        <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0">所属机型:</label>
                            <input type="text" class="form-control form-control-sm" id="search-model" list="list-model">
                            <datalist id="list-model"></datalist>
                        </div>
                        <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0">所属设备主数据名称:</label>
                            <input type="text" class="form-control form-control-sm" id="search-mainDevice" list="list-mainDevice">
                            <datalist id="list-mainDevice"></datalist>
                        </div>
                        <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0">订单号:</label>
                            <input type="text" class="form-control form-control-sm" id="search-orderNo">
                        </div>
                        
                        <!-- 第二行 -->
                        <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0">名称:</label>
                            <input type="text" class="form-control form-control-sm" id="search-name">
                        </div>
                        <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0">装置编号类别:</label>
                            <input type="text" class="form-control form-control-sm" id="search-codeType">
                        </div>
                        <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0">PM设备编码:</label>
                            <input type="text" class="form-control form-control-sm" id="search-pmCode">
                        </div>
                        <div class="col-md-3 d-flex align-items-center">
                            <label class="form-label mb-0 me-2 flex-shrink-0">资产编码:</label>
                            <input type="text" class="form-control form-control-sm" id="search-assetCode">
                        </div>

                        <!-- 按钮工具栏行：左侧维护按钮，右侧查询按钮 -->
                        <div class="col-12 d-flex justify-content-between align-items-center mt-2">
                            <!-- 左侧：维护按钮 -->
                            <div class="d-flex gap-2">
                                <button class="btn btn-sm btn-outline-primary" id="btn-add"><i class="bi bi-plus-lg"></i> 增加</button>
                                <button class="btn btn-sm btn-outline-secondary" id="btn-edit"><i class="bi bi-pencil-square"></i> 编辑</button>
                                <button class="btn btn-sm btn-outline-danger" id="btn-delete"><i class="bi bi-trash"></i> 删除</button>
                            </div>
                            <!-- 右侧：查询按钮 -->
                            <div class="d-flex gap-2">
                                <button class="btn btn-sm btn-primary" id="btn-search"><i class="bi bi-search"></i> 查询</button>
                                <button class="btn btn-sm btn-secondary" id="btn-clear"><i class="bi bi-eraser"></i> 清空</button>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 主体内容区：左树右表 -->
                <div class="splitter-container flex-grow-1" style="min-height: 0; border: 1px solid var(--border-color); border-radius: 4px;">
                    <div id="left-panel" style="width: 260px; overflow-y: auto; background-color: var(--bg-secondary); border-right: 1px solid var(--border-color);">
                        <div class="p-2 fw-bold border-bottom"><i class="bi bi-diagram-3"></i> 设备结构树</div>
                        <div id="tree-container"></div>
                    </div>
                    <div id="right-panel" class="d-flex flex-column p-2" style="background-color: var(--bg-secondary);">
                        <!-- 表格容器 (原有工具栏已移至顶部) -->
                        <div id="table-container" class="flex-grow-1"></div>
                    </div>
                </div>
            </div>
        `;

        // 初始化 Splitter
        const leftPanel = container.querySelector('#left-panel');
        const rightPanel = container.querySelector('#right-panel');
        new Splitter(container.querySelector('.splitter-container'), leftPanel, rightPanel);

        // 初始化组件
        this._initTree();
        this._initTable();
        this._initAutocompletes();
        this._attachEventListeners();
    }

    // --- 初始化树 ---
    async _initTree() {
        const treeContainer = this.container.querySelector('#tree-container');
        treeContainer.innerHTML = '<div class="text-center p-3"><div class="spinner-border spinner-border-sm text-secondary"></div></div>';
        try {
            const treeData = await getLedgerTree();
            treeContainer.innerHTML = '';
            this.tree = new SimpleTree(treeContainer, treeData, (nodeId) => {
                this.activeTreeNodeId = nodeId;
                // 点击树节点触发查询，重置页码
                if (this.dataTable) {
                    this.dataTable.state.pageNum = 1;
                    this._loadData();
                }
            });
        } catch (e) {
            treeContainer.innerHTML = '<div class="text-danger p-2">加载树失败</div>';
        }
    }

    // --- 初始化表格 ---
    _initTable() {
        const columns = [
            { key: 'auditStatus', title: '审批状态', width: 80, render: val => `<span class="badge bg-success">${val}</span>` },
            { key: 'id', title: '序号', width: 60 },
            { key: 'name', title: '名称', width: 200 },
            { key: 'workshop', title: '车间', width: 120 },
            { key: 'model', title: '所属机型', width: 120 },
            { key: 'device', title: '所属设备', width: 120 },
            { key: 'mainDevice', title: '所属设备主数据名称', width: 220 },
            { key: 'factory', title: '厂家', width: 150 },
            { key: 'spec', title: '规格型号', width: 150 },
            { key: 'location', title: '安装位置', width: 180 },
            { key: 'firstUseDate', title: '初次使用时间', width: 120 },
        ];

        this.dataTable = new DataTable({
            columns,
            data: [],
            options: {
                selectable: 'single', // 单选用于编辑
                pagination: true,
                uniformRowHeight: true
            }
        });
        this.dataTable.render(this.container.querySelector('#table-container'));
        this._loadData();
    }

    // --- 初始化搜索框的自动补全 ---
    async _initAutocompletes() {
        const fillDatalist = async (field, listId) => {
            const list = this.container.querySelector(`#${listId}`);
            if(!list) return;
            try {
                const options = await getAutocompleteOptions(field);
                list.innerHTML = options.map(opt => `<option value="${opt}">`).join('');
            } catch(e) {}
        };

        fillDatalist('workshop', 'list-workshop');
        fillDatalist('model', 'list-model');
        fillDatalist('mainDevice', 'list-mainDevice');
    }

    // --- 加载数据 ---
    async _loadData() {
        if (!this.dataTable) return;
        this.dataTable.toggleLoading(true);

        // 收集查询参数
        const params = {
            treeNodeId: this.activeTreeNodeId,
            workshop: this.container.querySelector('#search-workshop').value,
            model: this.container.querySelector('#search-model').value,
            mainDevice: this.container.querySelector('#search-mainDevice').value,
            name: this.container.querySelector('#search-name').value,
            pageNum: this.dataTable.state.pageNum,
            pageSize: this.dataTable.state.pageSize
        };

        try {
            const result = await getLedgerList(params);
            this.dataTable.updateView(result);
        } catch (e) {
            console.error(e);
            Modal.alert("加载数据失败");
        } finally {
            this.dataTable.toggleLoading(false);
        }
    }

    // --- 事件绑定 ---
    _attachEventListeners() {
        // 查询按钮
        this.container.querySelector('#btn-search').addEventListener('click', () => {
            this.dataTable.state.pageNum = 1;
            this._loadData();
        });

        // 清空按钮
        this.container.querySelector('#btn-clear').addEventListener('click', () => {
            this.container.querySelectorAll('input').forEach(inp => inp.value = '');
            this.activeTreeNodeId = null; // 也清除树选择
            // 清除树高亮
            this.container.querySelectorAll('.tree-node.active').forEach(el => el.classList.remove('active'));
            this.dataTable.state.pageNum = 1;
            this._loadData();
        });

        // 分页事件
        this.container.querySelector('#table-container').addEventListener('pageChange', (e) => {
            this.dataTable.state.pageNum = e.detail.pageNum;
            this._loadData();
        });

        // 增加按钮
        this.container.querySelector('#btn-add').addEventListener('click', () => {
            this._openEditModal(null);
        });

        // 编辑按钮
        this.container.querySelector('#btn-edit').addEventListener('click', () => {
            const selectedId = this.dataTable.selectedRowId;
            if (!selectedId) {
                Modal.alert("请选择一条记录进行编辑");
                return;
            }
            const rowData = this.dataTable.data.find(d => String(d.id) === String(selectedId));
            this._openEditModal(rowData);
        });

        // 删除按钮
        this.container.querySelector('#btn-delete').addEventListener('click', async () => {
            const selectedId = this.dataTable.selectedRowId;
            if (!selectedId) {
                Modal.alert("请选择要删除的记录");
                return;
            }
            const confirmed = await Modal.confirm("确认删除选中的记录吗？");
            if (confirmed) {
                await deleteLedger([selectedId]);
                this._loadData();
                Modal.alert("删除成功");
            }
        });
    }

    // --- 打开编辑/新增模态框 ---
    _openEditModal(data) {
        const isEdit = !!data;
        const title = isEdit ? "编辑自检自控台账" : "新增自检自控台账";

        // 构造表单 HTML (Bootstrap Grid)
        // 注意：为了支持辅助输入，使用了 datalist
        const formHtml = `
            <form id="ledger-form" class="container-fluid">
                <input type="hidden" name="id" value="${data?.id || ''}">
                <!-- 第一行 -->
                <div class="row mb-3 align-items-center">
                    <div class="col-md-2 text-end"><label class="form-label">装置编号类别</label></div>
                    <div class="col-md-4">
                        <input type="text" class="form-control" name="codeType" value="${data?.codeType || '149'}">
                    </div>
                    <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>名称</label></div>
                    <div class="col-md-4">
                        <input type="text" class="form-control" name="name" required value="${data?.name || ''}">
                    </div>
                </div>

                <!-- 第二行 -->
                <div class="row mb-3 align-items-center">
                    <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>所属设备</label></div>
                    <div class="col-md-4">
                        <input type="text" class="form-control" name="device" list="dl-device" value="${data?.device || ''}">
                        <datalist id="dl-device"><option value="YP18D细支 13#"></option></datalist>
                    </div>
                    <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>所属设备主数据名称</label></div>
                    <div class="col-md-4">
                         <input type="text" class="form-control" name="mainDevice" list="dl-mainDevice" required value="${data?.mainDevice || ''}">
                         <datalist id="dl-mainDevice"></datalist>
                    </div>
                </div>

                <!-- 第三行 -->
                <div class="row mb-3 align-items-center">
                    <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>车间</label></div>
                    <div class="col-md-4">
                         <input type="text" class="form-control" name="workshop" list="dl-workshop" required value="${data?.workshop || ''}">
                         <datalist id="dl-workshop"></datalist>
                    </div>
                    <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>所属机型</label></div>
                    <div class="col-md-4">
                        <input type="text" class="form-control" name="model" list="dl-model" required value="${data?.model || ''}">
                        <datalist id="dl-model"></datalist>
                    </div>
                </div>

                <!-- 第四行 -->
                <div class="row mb-3 align-items-center">
                    <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>安装位置</label></div>
                    <div class="col-md-4">
                        <input type="text" class="form-control" name="location" value="${data?.location || ''}">
                    </div>
                    <div class="col-md-2 text-end"><label class="form-label">使用寿命</label></div>
                    <div class="col-md-4">
                        <input type="text" class="form-control" name="lifeSpan" value="${data?.lifeSpan || '5年'}">
                    </div>
                </div>

                <!-- 第五行 -->
                <div class="row mb-3 align-items-center">
                     <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>厂家</label></div>
                     <div class="col-md-4">
                        <input type="text" class="form-control" name="factory" list="dl-factory" required value="${data?.factory || ''}">
                        <datalist id="dl-factory"><option value="BASLER巴斯勒"></option></datalist>
                     </div>
                     <div class="col-md-2 text-end"><label class="form-label">测量原理</label></div>
                     <div class="col-md-4">
                        <input type="text" class="form-control" name="principle" value="${data?.principle || '视觉成像检测原理'}">
                     </div>
                </div>

                <!-- 第六行 -->
                <div class="row mb-3 align-items-center">
                     <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>规格型号</label></div>
                     <div class="col-md-4">
                        <input type="text" class="form-control" name="spec" required value="${data?.spec || ''}">
                     </div>
                     <div class="col-md-2 text-end"><label class="form-label"><span class="text-danger">*</span>初次使用时间</label></div>
                     <div class="col-md-4">
                        <input type="text" class="form-control" name="firstUseDate" id="modal-date-input" placeholder="请选择日期" value="${data?.firstUseDate || ''}">
                     </div>
                </div>

                 <!-- 第七行 -->
                <div class="row mb-3 align-items-center">
                     <div class="col-md-2 text-end"><label class="form-label">PM设备编码</label></div>
                     <div class="col-md-4">
                        <input type="text" class="form-control" name="pmCode" value="${data?.pmCode || ''}">
                     </div>
                     <div class="col-md-2 text-end"><label class="form-label">订单号</label></div>
                     <div class="col-md-4">
                        <input type="text" class="form-control" name="orderNo" value="${data?.orderNo || ''}">
                     </div>
                </div>
                
                <!-- 第八行 -->
                <div class="row mb-3 align-items-center">
                     <div class="col-md-2 text-end"><label class="form-label">是否为固定资产</label></div>
                     <div class="col-md-4">
                        <select class="form-select" name="isFixedAsset">
                            <option value="是" ${data?.isFixedAsset === '是' ? 'selected' : ''}>是</option>
                            <option value="否" ${data?.isFixedAsset !== '是' ? 'selected' : ''}>否</option>
                        </select>
                     </div>
                     <div class="col-md-2 text-end"><label class="form-label">资产编码</label></div>
                     <div class="col-md-4">
                        <input type="text" class="form-control" name="assetCode" value="${data?.assetCode || ''}">
                     </div>
                </div>
            </form>
        `;

        const footerHtml = `
            <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">取消</button>
            <button type="button" class="btn btn-primary" id="btn-save-modal">提交</button>
        `;

        const modal = new Modal({
            title: title,
            body: formHtml,
            footer: footerHtml,
            size: 'xl' // 使用超大模态框以容纳多列
        });

        // 初始化模态框内的日期选择器
        const dateInput = modal.modalElement.querySelector('#modal-date-input');
        new DatePicker(dateInput, { mode: 'single' });

        // 填充模态框内的 datalist (简单起见，复用外部查询的逻辑)
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


        // 绑定保存事件
        modal.modalElement.querySelector('#btn-save-modal').addEventListener('click', async () => {
            const form = modal.modalElement.querySelector('form');
            if (!form.checkValidity()) {
                form.reportValidity();
                return;
            }

            const formData = new FormData(form);
            const payload = Object.fromEntries(formData.entries());

            try {
                await saveLedger(payload);
                modal.hide();
                this._loadData(); // 刷新列表
                Modal.alert("保存成功");
            } catch (e) {
                Modal.alert("保存失败: " + e.message);
            }
        });

        modal.show();
    }
}