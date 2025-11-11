import { apiFetch } from './api.js';

export async function getMachines() {
    return apiFetch('api/kb/machines');
}

export async function getTasks(query) {
    const params = new URLSearchParams(query);
    return apiFetch(`api/kb/tasks?${params.toString()}`);
}

export async function batchCompleteTasks(machine, taskIds) {
    return apiFetch(`api/kb/tasks/batch-complete?machine=${encodeURIComponent(machine)}`, {
        method: 'POST',
        body: JSON.stringify({ taskIds })
    });
}

export async function markTaskAsAbnormal(machine, taskId, reason) {
    return apiFetch(`api/kb/tasks/${taskId}/abnormal?machine=${encodeURIComponent(machine)}`, {
        method: 'POST',
        body: JSON.stringify({ abnormalReason: reason })
    });
}

export async function batchScoreTasks(machine, scores, checker) {
    return apiFetch(`api/kb/tasks/batch-score?machine=${encodeURIComponent(machine)}`, {
        method: 'POST',
        body: JSON.stringify({ scores, checker })
    });
}

export async function updateTaskScore(machine, taskId, score) {
    return apiFetch(`api/kb/tasks/${taskId}/score?machine=${encodeURIComponent(machine)}`, {
        method: 'PUT',
        body: JSON.stringify({ score })
    });
}
