/**
 * @file /js/services/api.js
 * @description API服务模块，用于与后端接口交互。
 * @version 2.0.0 - 2025-10-14 -  实现了与后端 MetrologyController 对接的真实API调用。
 *
 */

const API_BASE = '/api/metrology';

/**
 * 通用请求函数
 * @param {string} url - 请求的URL
 * @param {object} params - URL查询参数
 * @returns {Promise<any>} - 解析后的JSON数据
 */
async function fetchData(url, params = {}) {
    // 清理掉值为空的参数
    Object.keys(params).forEach(key => {
        if (params[key] === null || params[key] === undefined || params[key] === '') {
            delete params[key];
        }
    });

    const queryString = new URLSearchParams(params).toString();
    const fullUrl = `${url}${queryString ? `?${queryString}` : ''}`;

    console.log(`[API] Fetching: ${fullUrl}`);

    try {
        const response = await fetch(fullUrl);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return await response.json();
    } catch (error) {
        console.error(`[API] Error fetching from ${fullUrl}:`, error);
        throw error; // 将错误继续抛出，以便调用方可以处理
    }
}

/**
 * 获取计量台账数据
 * @param {object} params - 查询参数，包含分页和筛选条件
 * @returns {Promise<PageResult<Ledger>>}
 */
export function getMetrologyLedger(params) {
    return fetchData(`${API_BASE}/ledger`, params);
}

/**
 * 获取计量任务数据
 * @param {object} params - 查询参数，包含分页和筛选条件
 * @returns {Promise<PageResult<Task>>}
 */
export function getMetrologyTasks(params) {
    return fetchData(`${API_BASE}/tasks`, params);
}

/**
 * 获取点检统计数据
 * @param {object} params - 查询参数，主要是 category 和 dateRange
 * @returns {Promise<List<PointCheckStatistics>>}
 */
export function getPointCheckStatistics(params) {
    return fetchData(`${API_BASE}/point-check/statistics`, params);
}

/**
 * 获取点检列表数据
 * @param {object} params - 查询参数，包含分页、category 和 dateRange
 * @returns {Promise<PageResult<PointCheckListItem>>}
 */
export function getPointCheckList(params) {
    return fetchData(`${API_BASE}/point-check/list`, params);
}



/**
 * 导出计量台账为Excel文件
 * @param {object} params - 包含筛选条件和列配置的参数对象
 */
export async function exportMetrologyLedger(params) {
    try {
        const response = await fetch('/api/metrology/ledger/export', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(params),
        });

        if (!response.ok) {
            const errorText = await response.text();
            throw new Error(`导出失败: ${response.status} ${response.statusText}. ${errorText || ''}`);
        }

        const blob = await response.blob();

        // 从响应头获取文件名 (如果后端设置了)
        const disposition = response.headers.get('Content-Disposition');
        let filename = '计量台账.xlsx';
        if (disposition && disposition.indexOf('attachment') !== -1) {
            const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/;
            const matches = filenameRegex.exec(disposition);
            if (matches != null && matches[1]) {
                filename = matches[1].replace(/['"]/g, '');
            }
        }

        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.style.display = 'none';
        a.href = url;
        a.download = decodeURI(filename); // 解码文件名
        document.body.appendChild(a);
        a.click();

        window.URL.revokeObjectURL(url);
        document.body.removeChild(a);

    } catch (error) {
        console.error('导出Excel时发生错误:', error);
        throw error;
    }
}
