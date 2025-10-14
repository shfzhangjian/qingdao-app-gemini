/**
 * @file /js/services/api.js
 * @description API服务模块，用于与后端接口交互。
 * @version 1.4.0 - 2025-10-14: 补全了计量任务模块的API函数。
 */

/**
 * 封装的fetch函数，用于处理通用逻辑（如添加认证头、处理JSON等）
 * @param {string} url - 请求的URL
 * @param {object} options - fetch函数的配置选项
 * @returns {Promise<any>} - 解析后的JSON数据或Blob对象
 */
async function apiFetch(url, options = {}) {
    // 未来可以在这里统一添加认证token等
    // const headers = {
    //     'Content-Type': 'application/json',
    //     'Authorization': `Bearer ${localStorage.getItem('token') || ''}`,
    //     ...options.headers,
    // };

    const response = await fetch(url, { ...options });

    if (!response.ok) {
        const errorBody = await response.text();
        throw new Error(`网络请求失败: ${response.status} ${response.statusText} - ${errorBody}`);
    }

    // 如果是下载文件，响应头中会有 'attachment'
    const disposition = response.headers.get('Content-Disposition');
    if (disposition && disposition.indexOf('attachment') !== -1) {
        return response.blob(); // 返回Blob对象用于下载
    }

    return response.json(); // 否则返回JSON
}


// --- 计量台账 ---
export async function getMetrologyLedger(params) {
    const url = `/api/metrology/ledger/list?${new URLSearchParams(params)}`;
    return apiFetch(url);
}

export async function exportMetrologyLedger(params) {
    const blob = await apiFetch('/api/metrology/ledger/export', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(params)
    });

    // 触发下载
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `计量台账_${new Date().toLocaleString()}.xlsx`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
}

// --- 计量任务 ---
export async function getMetrologyTasks(params) {
    const url = `/api/metrology/task/list?${new URLSearchParams(params)}`;
    return apiFetch(url);
}

/**
 * 【核心修复】新增缺失的 exportMetrologyTasks 函数
 */
export async function exportMetrologyTasks(params) {
    const blob = await apiFetch('/api/metrology/task/export', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(params)
    });

    // 触发下载
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `计量任务_${new Date().toLocaleString()}.xlsx`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
}


// --- 点检统计 ---
export async function getPointCheckStatistics(params) {
    const url = `/api/metrology/point-check/statistics?${new URLSearchParams(params)}`;
    return apiFetch(url);
}

export async function getPointCheckList(params) {
    const url = `/api/metrology/point-check/list?${new URLSearchParams(params)}`;
    return apiFetch(url);
}

export async function exportPointCheck(params) {
    const blob = await apiFetch('/api/metrology/point-check/export', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(params)
    });

    const filename = params.viewMode === 'list' ? '点检列表' : '点检统计';
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `${filename}_${new Date().toLocaleString()}.xlsx`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
}

