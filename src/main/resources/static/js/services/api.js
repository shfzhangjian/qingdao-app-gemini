/**
 * @file /js/services/api.js
 * @description API服务模块，用于与后端接口交互。
 * @version 1.6.0 - 2025-10-15: [优化] 针对401/403错误提供更明确的错误提示。
 */
import AuthManager from '../components/AuthManager.js';

/**
 * 封装的fetch函数，用于处理通用逻辑（如添加认证头、处理JSON等）
 * @param {string} url - 请求的URL
 * @param {object} options - fetch函数的配置选项
 * @returns {Promise<any>} - 解析后的JSON数据或Blob对象
 */
export async function apiFetch(url, options = {}, isRetry = false) {
    const token = localStorage.getItem('jwt_token');
    const headers = {
        'Content-Type': 'application/json',
        ...options.headers,
    };

    if (token) {
        headers['Authorization'] = `Bearer ${token}`;
    }

    const response = await fetch(url, { ...options, headers });

    if (!response.ok) {
        // [Key Change] Handle authentication errors
        if ((response.status === 401 || response.status === 403) && !isRetry) {
            try {
                // Request new credentials via the AuthManager
                const { token: newToken } = await AuthManager.requestCredentials();
                // If successful, retry the original request with the new token
                return apiFetch(url, options, true);
            } catch (authError) {
                // If user cancels login, throw a user-friendly error
                throw new Error("认证失败或会话已过期，请重新登录。");
            }
        }

        const errorBody = await response.text();
        throw new Error(`网络请求失败: ${response.status} ${response.statusText} - ${errorBody}`);
    }

    const disposition = response.headers.get('Content-Disposition');
    if (disposition && disposition.includes('attachment')) {
        return response.blob();
    }

    // Handle empty JSON response
    const text = await response.text();
    return text ? JSON.parse(text) : {};
}

/**
 * [新增] 更新计量任务状态
 */
export async function updateMetrologyTasks(payload) {
    return apiFetch('api/metrology/task/update', {
        method: 'POST',
        body: JSON.stringify(payload)
    });
}


// --- 计量台账 ---
export async function getMetrologyLedger(params) {
    const url = `api/metrology/ledger/list?${new URLSearchParams(params)}`;
    return apiFetch(url);
}

export async function exportMetrologyLedger(params) {
    const blob = await apiFetch('api/metrology/ledger/export', {
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
    const url = `api/metrology/task/list?${new URLSearchParams(params)}`;
    return apiFetch(url);
}

/**
 * 【核心修复】新增缺失的 exportMetrologyTasks 函数
 */
export async function exportMetrologyTasks(params) {
    const blob = await apiFetch('api/metrology/task/export', {
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
    const url = `api/metrology/point-check-stats/statistics?${new URLSearchParams(params)}`;
    return apiFetch(url);
}

export async function getPointCheckList(params) {
    const url = `api/metrology/point-check/list?${new URLSearchParams(params)}`;
    return apiFetch(url);
}

export async function exportPointCheck(params) {
    const blob = await apiFetch('api/metrology/point-check/export', {
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

