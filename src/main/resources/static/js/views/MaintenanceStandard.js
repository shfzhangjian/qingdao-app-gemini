/**
 * @file /js/views/MaintenanceStandard.js
 * @description 通用保养标准视图，具有可调整布局和查询功能。
 * @version 2.1.0 - 2025-10-17 - 修复了视图切换时表格消失的Bug，并优化了工具栏布局。
 */
import DataTable from '../components/Optimized_DataTable.js';
import Splitter from '../utils/Splitter.js';

export default class MaintenanceStandard {
    constructor() {
        this.container = null;
        this.leftList = null;
        this.dataTableContainer = null;
        this.dataTable = null;
        this.activeItemId = null;
        this.viewType = ''; // 'routine', 'monthly', 'rotational'
        this.mockData = this._getMockData();
    }

    _getMockData() {
        const data = {
            routine: {
                list: [ { id: 'FH-2', title: 'FH-2 滤嘴发射器保养标准' }, { id: 'PROTOS70', title: 'PROTOS70卷烟机保养标准' }, { id: '卷烟机', title: '卷烟机' }, { id: 'ZT12', title: 'ZT12滤嘴存储机' }, { id: 'FOCKE', title: 'FOCKE装封箱机' }, { id: '易利达', title: '易利达' }, { id: 'ZB48', title: 'ZB48软盒包装机' }, { id: 'ZB47(GDX)', title: 'ZB47(GDX2)' }, { id: 'GDX2', title: 'GDX2硬盒包装机' }, { id: 'GDX1', title: 'GDX1硬盒包装机' }, { id: 'PROTOS-M5', title: 'PROTOS-M5卷烟机' }, ],
                details: { 'FH-2': [ { idtno: 1, stitle: '设备到位', sdetail: '松开刹车手动盘车，检查各部位...', sstandard: '灵活、无卡阻', iworkhour: '5分钟', scheckdutys: '包装机电工', ibasevalue: '3.00', snotes: '' } ], 'PROTOS70': [ { idtno: 2, stitle: '设备检查', sdetail: '检查PROTOS70卷烟机各部位', sstandard: '无松动', iworkhour: '2分钟', scheckdutys: '小组各工位', ibasevalue: '5.00', snotes: '' } ], '卷烟机': [ { idtno: 3, stitle: '703切丝废丝检查', sdetail: '清除切丝盘的废丝', sstandard: '无积丝', iworkhour: '2分钟', scheckdutys: '小组各工位', ibasevalue: '6.00', snotes: '' } ] }
            },
            monthly: {
                list: [ { id: 'FH-2', title: 'FH-2 滤嘴发射器保养标准' }, { id: '卷烟机', title: '卷烟机' }, { id: '德国-哈尼亚', title: '德国-哈尼亚' }, { id: 'PROTOS70', title: 'PROTOS70卷烟机保养标准' }, { id: '上海-中华', title: '上海-中华' }, { id: '易利达', title: '易利达' }, { id: 'ZL116', title: 'ZL116' }, { id: 'ZT12', title: 'ZT12' }, { id: 'ZB47(GDX2)', title: 'ZB47(GDX2)' }, { id: 'PROTOS-M5', title: 'PROTOS-M5' }, { id: 'GD16', title: 'GD16' }, { id: 'GDX1', title: 'GDX1' }, { id: 'GDX2', title: 'GDX2' }, { id: 'FOCKE', title: 'FOCKE' }, ],
                details: { 'FH-2': [ { idtno: 1, stitle: '清洁发射器内的碎滤嘴', sdetail: '关闭发射器上的压缩空气，用手将...', sstandard: '无碎滤嘴、清洁', iworkhour: '', sdutys: '维修电工', ibasevalue: '2.00', snotes: '' } ] }
            },
            rotational: {
                list: [ { id: 'FH-2', title: 'FH-2 滤嘴发射器保养标准' }, { id: 'PROTOS70', title: 'PROTOS70卷烟机保养标准' }, { id: 'ZT12', title: 'ZT12' }, { id: 'ZL116', title: 'ZL116' }, { id: 'FOCKE', title: 'FOCKE' }, { id: 'ZB48', title: 'ZB48' }, { id: 'ZB47(GDX2)', title: 'ZB47(GDX2)' }, { id: 'GDX2', title: 'GDX2' }, { id: 'GDX1', title: 'GDX1' }, { id: 'GD16', title: 'GD16' }, { id: 'PROTOS-M5', title: 'PROTOS-M5' }, ],
                details: { 'FH-2': [ { idtno: 1, stitle: '清洁发射器内的碎滤嘴', sdetail: '关闭发射器上的压缩空气，用手将...', sstandard: '无碎滤嘴、清洁', iworkhour: '20分钟', scheckdutys: '维修电工', ibasevalue: '2.00', snotes: '' } ] }
            }
        };
        Object.keys(data).forEach(type => { Object.keys(data[type].details).forEach(key => { const firstItem = data[type].details[key][0]; for (let i = 2; i <= 50; i++) { const newItem = {...firstItem, idtno: (firstItem.idtno || 0) + i -1 }; newItem.sdetail = `${newItem.sdetail} (示例行 ${i})`; newItem.ibasevalue = (parseFloat(newItem.ibasevalue) + (i * 0.1)).toFixed(2); data[type].details[key].push(newItem); } }); });
        return data;
    }

    render(container, route) {
        this.container = container;
        // [BUG FIX] 强制重置 dataTable 实例，确保每次渲染都创建一个新的表格
        this.dataTable = null;

        if (route.url.includes('routine_maintenance')) this.viewType = 'routine';
        else if (route.url.includes('monthly_maintenance')) this.viewType = 'monthly';
        else if (route.url.includes('rotational_maintenance')) this.viewType = 'rotational';

        container.innerHTML = `
            <div class="d-flex flex-column h-100">
                <div class="p-3 rounded mb-3 d-flex justify-content-between align-items-center" style="background-color: var(--bg-primary);">
                    <div class="d-flex align-items-center gap-3">
                        <label class="form-label mb-0">设备:</label>
                        <input type="text" class="form-control form-control-sm" style="width: 150px;">
                        <label class="form-label mb-0">保养标题:</label>
                        <input type="text" class="form-control form-control-sm" style="width: 150px;">
                        <label class="form-label mb-0">保养内容:</label>
                        <input type="text" class="form-control form-control-sm" style="width: 150px;">
                        <button class="btn btn-sm btn-primary"><i class="bi bi-search me-1"></i>查询</button>
                    </div>
                    <div class="d-flex align-items-center gap-2">
                        <button class="btn btn-sm btn-outline-secondary"><i class="bi bi-plus-lg me-1"></i>新增</button>
                        <button class="btn btn-sm btn-outline-secondary"><i class="bi bi-pencil-square me-1"></i>修改</button>
                        <button class="btn btn-sm btn-outline-danger"><i class="bi bi-trash me-1"></i>删除</button>
                        <button class="btn btn-sm btn-outline-success"><i class="bi bi-file-earmark-excel me-1"></i>导出</button>
                    </div>
                </div>
                <div class="splitter-container flex-grow-1" style="min-height: 0;">
                    <div id="left-panel" style="width: 250px; overflow-y: auto;"></div>
                    <div id="right-panel" class="d-flex flex-column"></div>
                </div>
            </div>
        `;

        const leftPanel = container.querySelector('#left-panel');
        const rightPanel = container.querySelector('#right-panel');
        new Splitter(container.querySelector('.splitter-container'), leftPanel, rightPanel);

        this.leftList = leftPanel;
        this.dataTableContainer = rightPanel;

        this._renderLeftList();
        this._attachEventListeners();
    }

    _renderLeftList() {
        const listData = this.mockData[this.viewType]?.list || [];
        this.leftList.innerHTML = `<div class="list-group list-group-flush">${listData.map(item => `<a href="#" class="list-group-item list-group-item-action" data-id="${item.id}">${item.title}</a>`).join('')}</div>`;
        if (listData.length > 0) {
            this.activeItemId = listData[0].id;
            const activeElement = this.leftList.querySelector(`[data-id="${this.activeItemId}"]`);
            if (activeElement) {
                activeElement.classList.add('active');
            }
            this._renderDataTable();
        } else {
            this.dataTableContainer.innerHTML = ''; // 清空右侧表格
        }
    }

    _renderDataTable() {
        if (!this.activeItemId) {
            this.dataTableContainer.innerHTML = '';
            return;
        }

        let columns = [];
        const allData = this.mockData[this.viewType]?.details[this.activeItemId] || [];

        const pageNum = this.dataTable ? this.dataTable.state.pageNum : 1;
        const pageSize = 15;
        const total = allData.length;
        const pages = Math.ceil(total / pageSize);
        const pagedData = allData.slice((pageNum - 1) * pageSize, pageNum * pageSize);

        switch(this.viewType) {
            case 'routine': columns = [ { key: 'idtno', title: '项', width: 60, sortable: true }, { key: 'stitle', title: '保养部位(STITLE)', width: 200, sortable: true }, { key: 'sdetail', title: '保养内容(SDETAIL)', width: 300 }, { key: 'sstandard', title: '保养标准(SSTANDARD)', width: 200 }, { key: 'iworkhour', title: '用时(小时)(IWORKHOUR)', width: 120 }, { key: 'scheckdutys', title: '责任岗位(SCHECKDUTYS)', width: 150 }, { key: 'ibasevalue', title: '分值(IBASEVALUE)', width: 100 }, { key: 'snotes', title: '备注(SNOTES)', width: 150 }, ]; break;
            case 'monthly': columns = [ { key: 'idtno', title: '项', width: 60, sortable: true }, { key: 'stitle', title: '保养项目(STITLE)', width: 200, sortable: true }, { key: 'sdetail', title: '保养内容(SDETAIL)', width: 300 }, { key: 'sstandard', title: '保养标准(SSTANDARD)', width: 200 }, { key: 'iworkhour', title: '用时(分钟)(IWORKHOUR)', width: 120 }, { key: 'sdutys', title: '责任岗位(SDUTYS)', width: 150 }, { key: 'ibasevalue', title: '分值(IBASEVALUE)', width: 100 }, { key: 'snotes', title: '备注(SNOTES)', width: 150 }, ]; break;
            case 'rotational': columns = [ { key: 'idtno', title: '项', width: 60, sortable: true }, { key: 'stitle', title: '保养项目(STITLE)', width: 200, sortable: true }, { key: 'sdetail', title: '保养内容(SDETAIL)', width: 300 }, { key: 'sstandard', title: '保养标准(SSTANDARD)', width: 200 }, { key: 'iworkhour', title: '用时(分钟)(IWORKHOUR)', width: 120 }, { key: 'scheckdutys', title: '责任岗位(SCHECKDUTYS)', width: 150 }, { key: 'ibasevalue', title: '分值(IBASEVALUE)', width: 100 }, { key: 'snotes', title: '备注(SNOTES)', width: 150 }, ]; break;
        }

        const tableConfig = {
            columns,
            data: pagedData.map((d, i) => ({...d, id: `${this.activeItemId}-${(pageNum-1)*pageSize + i}`})),
            options: { selectable: 'multiple', storageKey: `maintenanceStdTable_${this.viewType}` }
        };

        if (this.dataTable) {
            this.dataTable.updateView({ list: tableConfig.data, pageNum, pages, total });
        } else {
            this.dataTable = new DataTable(tableConfig);
            this.dataTable.paginationInfo = { pageNum, pages, total };
            this.dataTable.render(this.dataTableContainer);

            this.dataTableContainer.addEventListener('queryChange', (e) => {
                if(e.detail.pageNum) {
                    this.dataTable.state.pageNum = e.detail.pageNum;
                    this._renderDataTable();
                }
            });
        }
    }

    _attachEventListeners() {
        this.leftList.addEventListener('click', e => {
            e.preventDefault();
            const link = e.target.closest('a.list-group-item');
            if (link && link.dataset.id !== this.activeItemId) {
                this.leftList.querySelector('.active')?.classList.remove('active');
                link.classList.add('active');
                this.activeItemId = link.dataset.id;
                // [BUG FIX] 重置 dataTable 实例以便在切换时重新渲染
                this.dataTable = null;
                this._renderDataTable();
            }
        });
    }
}

