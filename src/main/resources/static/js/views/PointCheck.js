/**
 * @file /js/views/PointCheck.js
 * @description 点检统计业务视图
 * @version 1.1.0 - 2025-10-13 - Gemini - 移除内置标题，适配面包屑
 */

/**
 * Mock data generation for PointCheck view
 */
function generateTableData() {
    const departments = ['卷材一厂', '制丝车间', '卷包车间', '能源动力处(动力车间)', '行政管理处', '安全保卫处', '工艺质量处', '生产供应处', '物流管理处', '设备管理处', '青岛技术工作站', '工艺研发室', '原料实验室', '产品开发一室', '金香料研究室', '产品开发二室', '产品开发三室', '试验和研究型'];
    let html = '';
    departments.forEach((dept) => {
        const yingJian = Math.floor(Math.random() * 4000);
        const isFull = Math.random() > 0.3;
        const yiJian = isFull ? yingJian : Math.floor(yingJian * Math.random());
        const zhiXingLv = yingJian > 0 ? Math.round((yiJian / yingJian) * 100) + '%' : '0%';
        html += `
            <tr>
                <td class="text-start ps-3">${dept}</td>
                <td>[${yingJian}]</td><td>[${yiJian}]</td><td>${zhiXingLv}</td><td>[${yiJian}]</td>
                <td class="text-danger">0</td><td class="text-danger">0</td><td class="text-danger">0%</td><td class="text-danger">0</td>
            </tr>
        `;
    });
    return html;
}

export default class PointCheck {
    render(container, footerContainer) {
        container.innerHTML = `
            <div class="p-3 rounded" style="background-color: var(--bg-dark-secondary);">
                <div class="d-flex align-items-center gap-3 mb-3">
                    <span>时间范围:</span>
                    <input type="text" class="form-control" style="width: 200px;" value="2025-06-01 — 2025-06-30">
                    <div class="btn-group" role="group">
                        <button type="button" class="btn btn-sm btn-outline-secondary active">A类设备点检总统计</button>
                        <button type="button" class="btn btn-sm btn-outline-secondary">B类设备点检总统计</button>
                        <button type="button" class="btn btn-sm btn-outline-secondary">C类设备点检总统计</button>
                    </div>
                    <div class="ms-auto d-flex gap-2">
                        <button class="btn btn-sm btn-outline-secondary">统计</button>
                        <button class="btn btn-sm btn-outline-secondary">列表</button>
                        <button class="btn btn-sm btn-primary">查询</button>
                        <button class="btn btn-sm btn-outline-success">导出</button>
                    </div>
                </div>
                <div class="table-responsive">
                    <table class="table table-bordered table-sm text-center">
                      <thead>
                          <tr>
                              <th rowspan="2" class="align-middle">部门名称</th>
                              <th colspan="4">第一次盘点合格数(1-6月)</th>
                              <th colspan="4">第二次盘点合格数(7-12月)</th>
                          </tr>
                          <tr>
                              <th>应检数量</th><th>已检数量</th><th>执行率</th><th>正常数量</th>
                              <th>应检数量</th><th>已检数量</th><th>执行率</th><th>正常数量</th>
                          </tr>
                      </thead>
                       <tbody>
                        ${generateTableData()}
                       </tbody>
                    </table>
                </div>
            </div>`;

        footerContainer.innerHTML = ''; // This page does not have a footer
    }
}

