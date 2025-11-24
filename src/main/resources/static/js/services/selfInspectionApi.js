/**
 * @file /js/services/selfInspectionApi.js
 * @description 自检自控模块的 API 服务。
 * v2.0.0 - [Refactor] 移除 Mock 数据，全面接入后端 RESTful 接口。
 */

import { apiFetch } from './api.js';

// ==================================================================================
// --- 1. 自检自控台账 (Ledger) API ---
// ==================================================================================

/**
 * 获取左侧设备树
 * @returns {Promise<Array>} 树形结构数据
 */
export async function getLedgerTree() {
    return apiFetch('/tmis/api/si/ledger/tree');
}

/**
 * 分页查询台账列表
 * @param {Object} params - 查询参数 { pageNum, pageSize, workshop, model, ... }
 * @returns {Promise<Object>} 分页结果 { list, total, pageNum, pageSize, pages }
 */
export async function getLedgerList(params) {
    // 过滤掉空值参数
    const queryParams = new URLSearchParams();
    for (const key in params) {
        if (params[key] !== null && params[key] !== undefined && params[key] !== '') {
            queryParams.append(key, params[key]);
        }
    }
    return apiFetch(`/tmis/api/si/ledger/list?${queryParams.toString()}`);
}

/**
 * 保存台账（新增或修改）
 * @param {Object} data - 台账实体数据
 */
export async function saveLedger(data) {
    return apiFetch('/tmis/api/si/ledger/save', {
        method: 'POST',
        body: JSON.stringify(data)
    });
}

/**
 * 删除台账
 * @param {Array} ids - 要删除的ID数组 (目前后端仅支持单删，取第一个)
 */
export async function deleteLedger(ids) {
    if (!ids || ids.length === 0) return;
    // 暂时只处理单个删除，如果需要批量，需后端支持
    const id = ids[0];
    return apiFetch(`/tmis/api/si/ledger/delete/${id}`, {
        method: 'DELETE'
    });
}

/**
 * 获取下拉补全选项
 * @param {string} field - 字段名 (e.g., 'workshop', 'device')
 */
export async function getAutocompleteOptions(field) {
    return apiFetch(`/tmis/api/si/ledger/options?field=${field}`);
}

/**
 * 获取点检标准详情
 * @param {number|string} id - 台账ID
 */
export async function getStandardDetails(id) {
    // 注意：需确保后端有对应的 /api/si/ledger/standard/{id} 接口
    // 如果后端暂未实现，这里可能会 404
    return apiFetch(`/tmis/api/si/ledger/standard/${id}`);
}


// [修改] 导入标准 (支持批量)
// ledgerIds: 可以是单个ID(String/Number) 或 ID数组(Array)
export async function importStandard(ledgerIds, file) {
    const formData = new FormData();

    // 转换为逗号分隔的字符串
    let idsStr = ledgerIds;
    if (Array.isArray(ledgerIds)) {
        idsStr = ledgerIds.join(',');
    }

    formData.append('ids', idsStr);
    formData.append('file', file);

    const token = localStorage.getItem('jwt_token');
    const headers = {};
    if (token) headers['Authorization'] = `Bearer ${token}`;

    // 注意：URL 变更，去掉路径中的 ID
    const response = await fetch(`/tmis/api/si/ledger/import-standard`, {
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

// [新增] 下载标准附件
export async function downloadStandardAttachment(ledgerId, fileName) {
    const blob = await apiFetch(`/tmis/api/si/ledger/download-standard/${ledgerId}`, {
        method: 'POST'
    });

    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = fileName; // 使用前端传入的名称，或者依赖后端 header
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
}



// ==================================================================================
// --- 2. 点检任务 (Task) API ---
// ==================================================================================

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

/**
 * 保存任务详情 (提交)
 * @param {string|number} taskId - 任务ID
 * @param {Array} details - 明细列表
 * @param {string} [role='inspector'] - 当前操作角色 (inspector/operator)，用于后端鉴权或逻辑判断
 */
export async function saveSiTaskDetails(taskId, details, role = 'inspector') {
    return apiFetch(`/tmis/api/si/task/submit/${taskId}`, {
        method: 'POST',
        headers: {
            'X-Role': role // 通过 Header 传递角色信息
        },
        body: JSON.stringify(details)
    });
}


// ==================================================================================
// --- 3. 点检统计 (Stats) API ---
// ==================================================================================

/**
 * 获取点检统计列表
 * @param {Object} params - 查询参数
 */
export async function getSiStatsList(params) {
    // 假设后端统计接口路径为 /api/si/stats/list
    // 如果后端复用任务列表逻辑，也可以指向 /api/si/task/list
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