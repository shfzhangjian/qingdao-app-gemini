/**
 * @file /js/views/DailyMaintenanceStandard.js
 * @description “精益日保”的保养标准视图，具有树状导航和可调整布局。
 * @version 2.1.0 - 2025-10-17 - 优化查询区域布局
 */
import DataTable from '../components/Optimized_DataTable.js';
import Splitter from '../utils/Splitter.js';
import SimpleTree from '../utils/SimpleTree.js';

export default class DailyMaintenanceStandard {
    constructor() {
        this.container = null;
        this.dataTable = null;
        this.tree = null;
        this.activeNodeId = null;
        this.mockData = this._getMockData();
    }

    _getMockData() {
        const treeData = [
            { id: 'jb', label: '卷包车间', icon: 'bi-archive', children: [
                    { id: 'focke', label: 'FOCKE', icon: 'bi-gear' },
                    { id: 'gdx2', label: 'GDX2', icon: 'bi-gear' },
                    { id: 'gdx1', label: 'GDX1', icon: 'bi-gear' }
                ] },
            { id: 'jj', label: '卷接车间', icon: 'bi-archive', children: [
                    { id: 'protos70', label: 'PROTOS70', icon: 'bi-gear' }
                ] },
        ];
        const tableData = {
            'focke': [ { id:1, sbookno: 'YB-卷包-FOCKE-日保-2022', sclassnm: '包装机', smachintetype: 'FOCKE', stitle: 'FOCKE日常保养标准', sregnm: '韩工', sversion: 'A版', dregt: '2023-01-01' } ],
            'gdx2': [ { id:2, sbookno: 'YB-卷包-GDX2-日保-2022', sclassnm: '包装机', smachintetype: 'GDX2', stitle: 'GDX2日常保养标准', sregnm: '韩工', sversion: 'A版', dregt: '2023-01-01' } ],
            'gdx1': [ { id:3, sbookno: 'YB-卷包-GDX1-日保-2022', sclassnm: '包装机', smachintetype: 'GDX1', stitle: 'GDX1日常保养标准', sregnm: '韩工', sversion: 'A版', dregt: '2023-01-01' } ],
            'protos70': [ { id:4, sbookno: 'YB-卷接-PROTOS70-日保-2022', sclassnm: '卷接机', smachintetype: 'PROTOS70', stitle: 'PROTOS70日常保养标准', sregnm: '韩工', sversion: 'A版', dregt: '2023-01-01' } ],
        };
        Object.keys(tableData).forEach(key => {
            for (let i = 2; i <= 25; i++) {
                tableData[key].push({...tableData[key][0], id: `${key}-${i}`, sbookno: `${tableData[key][0].sbookno}-${i}`});
            }
        });
        return { treeData, tableData };
    }

    render(container) {
        this.container = container;
        container.innerHTML = `
            <div class="d-flex flex-column h-100">
                <div class="p-3 rounded mb-3 d-flex justify-content-between align-items-center" style="background-color: var(--bg-primary);">
                    <div class="d-flex align-items-center gap-3">
                        <label class="form-label mb-0">标准标题:</label>
                        <input type="text" class="form-control form-control-sm" style="width: 200px;">
                        <button class="btn btn-sm btn-primary"><i class="bi bi-search me-1"></i>查询</button>
                    </div>
                    <div class="d-flex align-items-center gap-2">
                        <button class="btn btn-sm btn-outline-secondary"><i class="bi bi-plus-lg me-1"></i>新增</button>
                        <button class="btn btn-sm btn-outline-secondary"><i class="bi bi-pencil-square me-1"></i>修改</button>
                        <button class="btn btn-sm btn-outline-danger"><i class="bi bi-trash me-1"></i>删除</button>
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

        this.tree = new SimpleTree(leftPanel, this.mockData.treeData, (nodeId) => {
            this.activeNodeId = nodeId;
            if(this.dataTable) {
                this.dataTable.state.pageNum = 1;
            }
            this._renderDataTable(rightPanel);
        });

        // 默认选中第一个叶子节点
        this.activeNodeId = this.mockData.treeData[0].children[0].id;
        this._renderDataTable(rightPanel);
    }

    _renderDataTable(tableContainer) {
        const allData = this.mockData.tableData[this.activeNodeId] || [];
        const pageNum = this.dataTable ? this.dataTable.state.pageNum : 1;
        const pageSize = 10;
        const total = allData.length;
        const pages = Math.ceil(total / pageSize);
        const pagedData = allData.slice((pageNum - 1) * pageSize, pageNum * pageSize);

        const columns = [
            { key: 'sbookno', title: '标准编码', width: 220, sortable: true },
            { key: 'sclassnm', title: '标准分类', width: 120, sortable: true },
            { key: 'smachintetype', title: '机型/工段', width: 150, sortable: true },
            { key: 'stitle', title: '标准标题', width: 250 },
            { key: 'sregnm', title: '责任人', width: 100 },
            { key: 'sversion', title: '版本', width: 80 },
            { key: 'dregt', title: '创建时间', width: 120, sortable: true },
        ];

        const tableConfig = {
            columns,
            data: pagedData,
            options: { selectable: 'multiple', storageKey: 'dailyMaintenanceStdTable' }
        };

        if (this.dataTable && this.dataTable.container === tableContainer) {
            this.dataTable.updateView({ list: tableConfig.data, pageNum, pages, total });
        } else {
            this.dataTable = new DataTable(tableConfig);
            this.dataTable.paginationInfo = { pageNum, pages, total };
            this.dataTable.render(tableContainer);
            tableContainer.addEventListener('queryChange', (e) => {
                if(e.detail.pageNum) {
                    this.dataTable.state.pageNum = e.detail.pageNum;
                    this._renderDataTable(tableContainer);
                }
            });
        }
    }
}

