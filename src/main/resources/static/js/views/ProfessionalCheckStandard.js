/**
 * @file /js/views/ProfessionalCheckStandard.js
 * @description “专业点检”的保养标准视图，增加分页功能。
 * @version 2.0.0 - 2025-10-17
 */
import DataTable from '../components/Optimized_DataTable.js';
import Modal from '../components/Modal.js';

export default class ProfessionalCheckStandard {
    constructor() {
        this.container = null;
        this.dataTable = null;
        this.mockData = this._getMockData();
    }

    _getMockData() {
        // ... (数据已扩充)
        const data = [
            {
                id: 1,
                sbookno: 'DJ-制丝-切丝-2023',
                sclassnm: '制丝设备',
                smachintetype: '切丝机组',
                stitle: '切丝机组日常点检标准',
                sregnm: '机电一体化',
                sversion: 'A版',
                dregt: '2023-01-01'
            },
            {
                id: 2,
                sbookno: 'DJ-制丝-烘丝-2023',
                sclassnm: '制丝设备',
                smachintetype: '烘丝机组',
                stitle: '烘丝机组日常点检标准',
                sregnm: '机电一体化',
                sversion: 'A版',
                dregt: '2023-01-01'
            },
            {
                id: 3,
                sbookno: 'DJ-动力-锅炉-2023',
                sclassnm: '动力设备',
                smachintetype: '锅炉',
                stitle: '锅炉日常点检标准',
                sregnm: '机电一体化',
                sversion: 'A版',
                dregt: '2023-01-01'
            },
        ];
        for (let i = 4; i <= 50; i++) {
            data.push({...data[i % 3], id: i, sbookno: `DJ-XXX-点检-2023-${i}`});
        }
        return data;
    }

    render(container) {
        this.container = container;
        container.innerHTML = `
            <div class="d-flex flex-column h-100">
                <div class="p-3 rounded mb-3" style="background-color: var(--bg-primary);">
                    <div class="row g-3 align-items-center">
                        <div class="col-auto"><label class="form-label">标准分类:</label></div>
                        <div class="col-auto"><input type="text" class="form-control form-control-sm" placeholder="标准分类..."></div>
                        <div class="col-auto"><label class="form-label">机型/工段:</label></div>
                        <div class="col-auto"><input type="text" class="form-control form-control-sm" placeholder="机型/工段..."></div>
                        <div class="col-auto"><label class="form-label">标准标题:</label></div>
                        <div class="col-auto"><input type="text" class="form-control form-control-sm" placeholder="标准标题..."></div>
                         <div class="col-auto">
                            <button class="btn btn-sm btn-primary"><i class="bi bi-search"></i> 查询</button>
                        </div>
                    </div>
                </div>
                 <div class="d-flex justify-content-start align-items-center gap-2 mb-3">
                    <button class="btn btn-sm btn-outline-secondary"><i class="bi bi-plus-lg me-1"></i>新增</button>
                    <button class="btn btn-sm btn-outline-secondary"><i class="bi bi-pencil-square me-1"></i>修改</button>
                    <button class="btn btn-sm btn-outline-danger"><i class="bi bi-trash me-1"></i>删除</button>
                </div>
                <div id="data-table-container" style="flex-grow: 1; display: flex; flex-direction: column; min-height: 0;"></div>
            </div>
        `;
        this._renderDataTable(container.querySelector('#data-table-container'));
    }

    _renderDataTable(tableContainer) {
        const allData = this.mockData;
        const pageNum = this.dataTable ? this.dataTable.state.pageNum : 1;
        const pageSize = 15;
        const total = allData.length;
        const pages = Math.ceil(total / pageSize);
        const pagedData = allData.slice((pageNum - 1) * pageSize, pageNum * pageSize);

        const columns = [
            {key: 'sbookno', title: '标准编码', width: 220, sortable: true},
            {key: 'sclassnm', title: '标准分类', width: 120, sortable: true},
            {key: 'smachintetype', title: '机型/工段', width: 150, sortable: true},
            {key: 'stitle', title: '标准标题', width: 250},
            {key: 'sregnm', title: '责任专业', width: 120},
            {key: 'sversion', title: '版本', width: 80},
            {key: 'dregt', title: '创建日期', width: 120, sortable: true},
        ];

        const tableConfig = {
            columns,
            data: pagedData,
            options: {selectable: 'multiple', storageKey: 'profCheckStdTable'}
        };

        if (this.dataTable) {
            this.dataTable.updateView({list: pagedData, pageNum, pages, total});
        } else {
            this.dataTable = new DataTable(tableConfig);
            this.dataTable.paginationInfo = {pageNum, pages, total};
            this.dataTable.render(tableContainer);
            tableContainer.addEventListener('queryChange', (e) => {
                if (e.detail.pageNum) {
                    this.dataTable.state.pageNum = e.detail.pageNum;
                    this._renderDataTable(tableContainer);
                }
            });
        }
    }
}

