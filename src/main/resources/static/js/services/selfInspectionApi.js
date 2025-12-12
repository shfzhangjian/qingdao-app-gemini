/**
 * @file /js/services/selfInspectionApi.js
 * @description 自检自控模块的 API 服务 (完整版)。
 * 包含台账管理、附件管理、任务管理和统计分析的所有接口。
 */

import { apiFetch,postUpload } from './api.js';

const API_BASE = '/api/si';
// ==================================================================================
// --- 1. 自检自控台账 (Ledger) API ---
// ==================================================================================

export async function getLedgerList(params) {
    const queryParams = new URLSearchParams();
    for (const key in params) {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
            queryParams.append(key, params[key]);
        }
    }
    return apiFetch(`/tmis/api/si/ledger/list?${queryParams.toString()}`);
}

export async function saveLedger(data) {
    return apiFetch('/tmis/api/si/ledger/save', {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

export async function deleteLedger(ids) {
    if (!ids || ids.length === 0) return;
    const id = ids[0];
    return apiFetch(`/tmis/api/si/ledger/delete/${id}`, {
        method: 'DELETE'
    });
}

export async function getAutocompleteOptions(field) {
    return apiFetch(`/tmis/api/si/ledger/options?field=${field}`);
}

// ==================================================================================
// --- 2. 标准附件 (Standard File) API ---
// ==================================================================================

// 获取所有标准附件 (独立表)
export async function getStandardFiles() {
    return apiFetch(`/tmis/api/si/file/list`);
}

// 上传附件 (仅 PDF)
export async function uploadStandardFile(file) {
    const formData = new FormData();
    formData.append('file', file);

    const token = localStorage.getItem('jwt_token');
    const headers = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const response = await fetch(`/tmis/api/si/file/upload`, {
        method: 'POST',
        headers: headers,
        body: formData
    });

    if (!response.ok) {
        const errorText = await response.text();
        throw new Error(errorText);
    }
    return response.json();
}

export async function deleteStandardFile(id) {
    return apiFetch(`/tmis/api/si/file/delete/${id}`, {
        method: 'DELETE'
    });
}

export function getFilePreviewUrl(id) {
    return `/tmis/api/si/file/preview/${id}`;
}

// ==================================================================================
// --- 3. 点检任务 (Task) API ---
// ==================================================================================

// --- 2. 任务生成 ---
export async function generateSiTasks(data) {
    return apiFetch('/tmis/api/si/task/generate', {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

/**
 * 获取点检任务列表
 * @param {Object} params - 查询参数
 */
export async function getSiTaskList(params) {
    const queryParams = new URLSearchParams();
    for (const key in params) {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
            queryParams.append(key, params[key]);
        }
    }
    return apiFetch(`/tmis/api/si/task/list?${queryParams.toString()}`);
}

/**
 * 获取任务执行详情
 * @param {string|number} taskId - 任务ID
 */
export async function getSiTaskDetails(taskId) {
    return apiFetch(`/tmis/api/si/task/details/${taskId}`);
}

// --- [新增] 获取设备生成列表 (分组) ---
export async function getTaskGenerationDeviceList(params) {
    const queryParams = new URLSearchParams();
    for (const key in params) { if (params[key]) queryParams.append(key, params[key]); }
    // 调用新接口
    return apiFetch(`/tmis/api/si/ledger/group-list?${queryParams.toString()}`);
}

/**
 * 保存任务详情 (提交)
 * @param {string|number} taskId - 任务ID
 * @param {Array} details - 明细列表
 * @param {string} [role='inspector'] - 当前操作角色 (inspector/operator)
 */
export async function saveSiTaskDetails(taskId, details, role = 'inspector') {
    return apiFetch(`/tmis/api/si/task/submit/${taskId}`, {
        method: 'POST',
        headers: { 'X-Role': role },
        body: JSON.stringify(details)
    });
}



// ==================================================================================
// --- 4. 点检统计 (Stats) API ---
// ==================================================================================

/**
 * 获取点检统计列表
 * @param {Object} params - 查询参数
 */
export async function getSiStatsList(params) {
    const queryParams = new URLSearchParams();
    for (const key in params) {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
            queryParams.append(key, params[key]);
        }
    }
    return apiFetch(`/tmis/api/si/stats/list?${queryParams.toString()}`);
}

/**
 * [新增] 导出点检统计
 * @param {Object} params - 查询参数及列定义
 */
export async function exportSiStats(params) {
    const blob = await apiFetch('api/si/stats/export', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(params)
    });

    // 触发下载
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `点检统计_${new Date().toISOString().split('T')[0]}.xlsx`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
}


/**
 * 归档操作
 * @param {Object} params - 归档参数 { device, taskType, dateRange }
 */
export async function archiveSiData(params) {
    return apiFetch('/tmis/api/si/archive', {
        method: 'POST',
        body: JSON.stringify(params)
    });
}



/**
 * [新增] 导出台账 Excel
 */
export async function exportLedger(params) {
    const queryParams = new URLSearchParams();
    for (const key in params) {
        if (params[key]) queryParams.append(key, params[key]);
    }
    // 使用原生 fetch 处理 Blob 流，因为 apiFetch 默认处理 JSON
    const token = localStorage.getItem('jwt_token');
    const headers = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;

    const response = await fetch(`/tmis/api/si/ledger/export?${queryParams.toString()}`, {
        method: 'GET',
        headers: headers
    });

    if (!response.ok) {
        throw new Error("导出失败");
    }
    return response.blob();
}

export async function getArchiveReportData(req) {
    return apiFetch('/tmis/api/si/report/preview', {
        method: 'POST',
        body: JSON.stringify(req)
    });
}

export async function exportArchiveReport(req) {
    const blob = await apiFetch('/tmis/api/si/report/export', {
        method: 'POST',
        body: JSON.stringify(req)
    });

    // 触发下载
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `检查记录表_${req.deviceName}.xlsx`;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
}

/**
 * [新增] 导入台账 Excel
 */
export function importLedger(formData) {
    return postUpload(`${API_BASE}/ledger/import`, formData);
}