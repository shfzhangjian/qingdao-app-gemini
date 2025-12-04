/**
 * @file /js/services/selfInspectionApi.js
 * @description 自检自控模块的 API 服务 (完整版)。
 * 包含台账管理、附件管理、任务管理和统计分析的所有接口。
 */

import { apiFetch } from './api.js';

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
 * 归档操作
 * @param {Object} params - 归档参数 { device, taskType, dateRange }
 */
export async function archiveSiData(params) {
    return apiFetch('/tmis/api/si/archive', {
        method: 'POST',
        body: JSON.stringify(params)
    });
}