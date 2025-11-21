/**
 * @file /js/services/selfInspectionApi.js
 * @description 自检自控台账的模拟 API 服务。
 * v1.2.0 - [新增] 模拟 'hasStandard' 字段，新增 getStandardDetails 接口模拟标准详情数据。
 */

// 模拟数据仓库
let mockDataStore = [];

const mockTreeData = [
    {
        id: 'f_qingdao', label: '青岛卷烟厂', icon: 'bi-building', children: [
            {
                id: 'w_juanbao', label: '卷包车间', icon: 'bi-house-gear', children: [
                    {
                        id: 'm_pack', label: '包装机组', icon: 'bi-boxes', children: [
                            { id: 'e_gdgy20', label: 'GDGY 20#高速包装机组', icon: 'bi-hdd-rack' },
                            { id: 'e_gdgy21', label: 'GDGY 21#高速包装机组', icon: 'bi-hdd-rack' },
                            { id: 'e_gdgy22', label: 'GDGY 22#高速包装机组', icon: 'bi-hdd-rack' },
                        ]
                    },
                    {
                        id: 'm_roll', label: '卷接机组', icon: 'bi-vinyl', children: [
                            { id: 'e_zj112', label: 'ZJ112 卷接机', icon: 'bi-hdd-rack' }
                        ]
                    }
                ]
            }
        ]
    }
];


/**
 * [新增] 获取标准详情列表 (模拟)
 * @param {number|string} ledgerId 台账ID
 */
export async function getStandardDetails(ledgerId) {
    return new Promise(resolve => {
        setTimeout(() => {
            // 模拟一些标准详情数据，参考图片 image_2bad65.jpg
            const details = [
                { device: '空头检测装置', item: '是否使用/删除功能', standard: '功能打开; 能够准确剔除缺陷烟支', executor: '包机电气维修工', cycle: '1', nextDate: '2024-04-08' },
                { device: '空头检测装置', item: '检测信号波形/检测相位', standard: '检测信号波形清晰, 波峰波谷明显; 检测点准确对准信号波峰...', executor: '包机电气维修工', cycle: '1', nextDate: '2024-04-08' },
                { device: '漏气检测装置', item: '是否使用/删除功能', standard: '功能打开; 能够准确剔除缺陷烟支', executor: '包机电气维修工', cycle: '1', nextDate: '2024-04-08' },
                { device: '漏气检测装置', item: '检测信号波形/检测相位', standard: '检测信号波形清晰, 波峰波谷明显; 检测点准确对准信号波峰...', executor: '包机电气维修工', cycle: '1', nextDate: '2024-04-08' },
                { device: '漏气检测装置', item: '检测气压', standard: '检测气压指示光带幅度大于50%', executor: '包机电气维修工', cycle: '1', nextDate: '2024-04-08' },
                { device: 'OTIS外观检测', item: '是否使用/删除功能', standard: '功能打开; 能够准确剔除缺陷烟支', executor: '包机电气维修工', cycle: '1', nextDate: '2024-04-08' },
                { device: '重量控制装置', item: '是否使用/删除功能', standard: '功能打开; 能够准确剔除缺陷烟支', executor: '包机电气维修工', cycle: '1', nextDate: '2024-04-08' },
                { device: '重量控制装置', item: '滑板温度 (适用于中速机)', standard: '65°C ± 0.1°C', executor: '包机电气维修工', cycle: '1', nextDate: '2024-04-08' },
                { device: '水松纸拼接头剔除', item: '删除功能', standard: '能够准确剔除缺陷烟支', executor: '包机电气维修工', cycle: '1', nextDate: '2024-04-08' },
                { device: '烟支污脏ORIS检测', item: '检测波形', standard: '检测波形正常', executor: '包机电气维修工', cycle: '1', nextDate: '2024-04-08' }
            ];
            resolve(details);
        }, 500);
    });
}

// 初始化模拟列表数据
function initMockData() {
    if (mockDataStore.length > 0) return;

    const baseData = [
        {
            id: 1, auditStatus: '已归档', name: '条盒拉线检测', workshop: '卷包车间', model: '包装机组', device: 'ZB47 20#',
            mainDevice: 'GDGY 20#高速包装机组', factory: '南京家泉', spec: 'Ver5.3.2 211110', location: '透明纸输送通道',
            firstUseDate: '2020-01-01', principle: '光电检测', pmCode: 'JB000225', orderNo: '320000192', assetCode: '310001979',
            hasStandard: true // [新增] 是否上传标准
        },
        {
            id: 2, auditStatus: '已归档', name: '光子缺失检测', workshop: '卷包车间', model: '包装机组', device: 'ZB47 20#',
            mainDevice: 'GDGY 20#高速包装机组', factory: '设备自管', spec: 'JQYC10B', location: '条烟提升/输送通道',
            firstUseDate: '2021-01-01', principle: '视觉成像', pmCode: 'JB000226', orderNo: '320000193', assetCode: '310001980',
            hasStandard: true
        },
        {
            id: 3, auditStatus: '已归档', name: 'CT入口堆叠检测', workshop: '卷包车间', model: '包装机组', device: 'ZB47 20#',
            mainDevice: 'GDGY 20#高速包装机组', factory: 'KEYENCE', spec: 'FS2-60P, FU6F', location: 'CT入口通道',
            firstUseDate: '2018-01-01', principle: '激光测距', pmCode: 'JB000227', orderNo: '320000194', assetCode: '310001981',
            hasStandard: false
        },
        {
            id: 4, auditStatus: '已归档', name: '小包透明纸存在检测', workshop: '卷包车间', model: '包装机组', device: 'ZB47 20#',
            mainDevice: 'GDGY 20#高速包装机组', factory: 'KEYENCE', spec: 'V31P, FU6F', location: '透明纸输送通道',
            firstUseDate: '2018-01-01', principle: '光电开关', pmCode: 'JB000228', orderNo: '320000195', assetCode: '310001982',
            hasStandard: false
        },
        {
            id: 5, auditStatus: '已归档', name: '商标纸印刷检测', workshop: '卷包车间', model: '包装机组', device: 'ZB47 20#',
            mainDevice: 'GDGY 20#高速包装机组', factory: 'SICK', spec: 'WT150-P460', location: '商标纸输送通道',
            firstUseDate: '2018-01-01', principle: '色标传感器', pmCode: 'JB000229', orderNo: '320000196', assetCode: '310001983',
            hasStandard: true
        },
    ];

    // 生成更多数据
    for (let i = 0; i < 45; i++) {
        const template = baseData[i % baseData.length];
        mockDataStore.push({
            ...template,
            id: 100 + i,
            name: `${template.name} ${i + 1}`,
            spec: `${template.spec}-${i}`,
            pmCode: `${template.pmCode}${i}`,
            mainDevice: i % 2 === 0 ? 'GDGY 20#高速包装机组' : 'GDGY 21#高速包装机组',
            hasStandard: i % 3 !== 0 // 随机生成是否有标准
        });
    }
}
export async function getLedgerTree() {
    return new Promise(resolve => { setTimeout(() => resolve(mockTreeData), 200); });
}

export async function getLedgerList(params) {
    initMockData();
    return new Promise(resolve => {
        setTimeout(() => {
            let filtered = [...mockDataStore];
            if (params.treeNodeId && params.treeNodeId.startsWith('e_')) {
                const nodeLabel = findNodeLabel(mockTreeData, params.treeNodeId);
                if (nodeLabel) filtered = filtered.filter(item => item.mainDevice === nodeLabel);
            }
            if (params.workshop) filtered = filtered.filter(item => item.workshop.includes(params.workshop));
            if (params.model) filtered = filtered.filter(item => item.model.includes(params.model));
            if (params.mainDevice) filtered = filtered.filter(item => item.mainDevice.includes(params.mainDevice));
            if (params.name) filtered = filtered.filter(item => item.name.includes(params.name));
            if (params.pmCode) filtered = filtered.filter(item => item.pmCode.includes(params.pmCode));
            if (params.assetCode) filtered = filtered.filter(item => item.assetCode.includes(params.assetCode));
            // 分页
            const total = filtered.length;
            const pageNum = parseInt(params.pageNum) || 1;
            const pageSize = parseInt(params.pageSize) || 15;
            const start = (pageNum - 1) * pageSize;
            const list = filtered.slice(start, start + pageSize);
            resolve({ list, total, pageNum, pageSize, pages: Math.ceil(total / pageSize) });
        }, 300);
    });
}

function findNodeLabel(nodes, id) {
    for (const node of nodes) {
        if (node.id === id) return node.label;
        if (node.children) {
            const found = findNodeLabel(node.children, id);
            if (found) return found;
        }
    }
    return null;
}

export async function saveLedger(data) {
    return new Promise(resolve => {
        setTimeout(() => {
            if (data.id) {
                const index = mockDataStore.findIndex(item => item.id == data.id);
                if (index !== -1) mockDataStore[index] = { ...mockDataStore[index], ...data };
            } else {
                const newId = Math.max(...mockDataStore.map(i => i.id)) + 1;
                mockDataStore.unshift({ ...data, id: newId, auditStatus: '草稿', firstUseDate: data.firstUseDate || new Date().toISOString().split('T')[0], hasStandard: false });
            }
            resolve({ success: true });
        }, 300);
    });
}

export async function deleteLedger(ids) {
    return new Promise(resolve => {
        setTimeout(() => {
            mockDataStore = mockDataStore.filter(item => !ids.includes(String(item.id)));
            resolve({ success: true });
        }, 300);
    });
}


// ==================================================================================
// --- 点检任务 API ---
// ==================================================================================

let mockTaskList = [];
let mockTaskDetails = {};

// 初始化任务列表数据
function initTaskData() {
    if (mockTaskList.length > 0) return;
    const baseTasks = [
        { id: 1, model: '包装机组', device: 'ZB48 46#', prodStatus: '生产', shiftType: '甲班', shift: '中班', checkStatus: '待检', confirmStatus: '待确认', taskTime: '2025-07-01', taskType: '三班电气', isOverdue: '否', checker: '王某某', confirmer: '张三' },
        { id: 2, model: '包装机组', device: 'ZB48 46#', prodStatus: '生产', shiftType: '甲班', shift: '中班', checkStatus: '已检', confirmStatus: '待确认', taskTime: '2025-07-02', taskType: '三班电气', isOverdue: '否', checker: '李四', confirmer: '' },
        { id: 3, model: '包装机组', device: 'ZB48 46#', prodStatus: '生产', shiftType: '白班', shift: '-', checkStatus: '待检', confirmStatus: '待确认', taskTime: '2025-07-02', taskType: '白班电气', isOverdue: '否', checker: '', confirmer: '' },
        { id: 4, model: '包装机组', device: 'ZB48 46#', prodStatus: '生产', shiftType: '-', shift: '-', checkStatus: '待检', confirmStatus: '待确认', taskTime: '2025-07-02', taskType: '年检', isOverdue: '否', checker: '', confirmer: '' },
    ];
    mockTaskList = baseTasks;

    // 初始化任务详情数据 (Mock)
    const detailsTemplate = [
        { itemName: '条盒拉带存在检测', result: '正常', remarks: '' },
        { itemName: '条包空位检测', result: '正常', remarks: 'OK' },
        { itemName: '条盒透明纸存在检测', result: '异常', remarks: '感应不灵敏' },
        { itemName: '光子缺盒检测', result: '正常', remarks: '' },
        { itemName: '条盒商标纸存在检测', result: '正常', remarks: '' },
        { itemName: 'CT入口堆叠检测', result: '正常', remarks: '' },
        { itemName: '小包透明纸微包检测', result: '不用', remarks: '' },
        { itemName: '小包透明纸存在检测', result: '正常', remarks: '' },
        { itemName: '小包透明纸接头检测', result: '正常', remarks: '' },
        { itemName: 'CH拉带存在检测', result: '正常', remarks: '' }
    ];

    mockTaskList.forEach(t => {
        mockTaskDetails[t.id] = detailsTemplate.map((d, idx) => ({
            id: t.id * 100 + idx + 1,
            taskId: t.id,
            model: t.model,
            device: t.device,
            mainDevice: 'GDGY 46#高速包装机组',
            itemName: d.itemName,
            result: d.result,
            remarks: d.remarks,
            prodStatus: t.prodStatus,
            shiftType: t.shiftType,
            shift: t.shift,
            isConfirmed: false,
            checkTime: '',
            confirmTime: ''
        }));
    });
}

export async function getSiTaskList(params) {
    initTaskData();
    return new Promise(resolve => {
        setTimeout(() => {
            let filtered = [...mockTaskList];
            if (params.device) filtered = filtered.filter(t => t.device.includes(params.device));
            if (params.prodStatus) filtered = filtered.filter(t => t.prodStatus === params.prodStatus);
            if (params.shiftType) filtered = filtered.filter(t => t.shiftType === params.shiftType);
            if (params.shift) filtered = filtered.filter(t => t.shift === params.shift);
            if (params.checkStatus) filtered = filtered.filter(t => t.checkStatus === params.checkStatus);
            if (params.checker) filtered = filtered.filter(t => t.checker.includes(params.checker)); // 新增
            if (params.confirmer) filtered = filtered.filter(t => t.confirmer.includes(params.confirmer)); // 新增

            resolve({
                list: filtered,
                total: filtered.length,
                pageNum: 1,
                pageSize: 20,
                pages: 1
            });
        }, 300);
    });
}

export async function getSiTaskDetails(taskId) {
    initTaskData();
    return new Promise(resolve => setTimeout(() => resolve(mockTaskDetails[taskId] || []), 300));
}

export async function saveSiTaskDetails(taskId, details) {
    return new Promise(resolve => {
        setTimeout(() => {
            mockTaskDetails[taskId] = details;
            resolve({ success: true });
        }, 500);
    });
}

/**
 * 获取下拉补全选项 (增强版，支持 Ledger 和 Task 字段)
 * @param {string} field 字段名
 */
export async function getAutocompleteOptions(field) {
    return new Promise(resolve => {
        // 1. 尝试从 Ledger 数据获取
        initMockData();
        let options = new Set(mockDataStore.map(item => item[field]).filter(Boolean));

        // 2. 尝试从 Task 数据获取 (如果 Ledger 中没有或为了合并)
        initTaskData();
        const taskOptions = mockTaskList.map(item => item[field]).filter(Boolean);
        taskOptions.forEach(opt => options.add(opt));

        resolve([...options]);
    });
}



// ==================================================================================
// --- [新增] 点检统计 (SiStats) API ---
// ==================================================================================

let mockStatsList = [];

// 初始化统计数据 (模拟包含检查结果等详情的扁平化数据)
function initStatsData() {
    if (mockStatsList.length > 0) return;

    // 模拟数据生成
    for (let i = 1; i <= 50; i++) {
        mockStatsList.push({
            id: i,
            checkTime: `2025-03-02 14:${Math.floor(Math.random()*60)}:${Math.floor(Math.random()*60)}`,
            device: 'ZB48 46#',
            itemName: i % 2 === 0 ? '条盒拉带存在检测' : '条包空位检测',
            result: i % 5 === 0 ? '异常' : '正常', // 少量异常
            remarks: i % 5 === 0 ? '感应器故障' : '',
            prodStatus: '生产',
            shift: '早班',
            shiftType: '乙班',
            taskType: i % 3 === 0 ? '年检' : '三班电气',
            actualCheckTime: `2025-03-02 14:${Math.floor(Math.random()*60)}:00`,
            checker: '王某某',
            confirmStatus: '已确认',
            confirmTime: `2025-03-02 15:00:00`,
            confirmer: '张三'
        });
    }
}

/**
 * 获取点检统计列表
 */
export async function getSiStatsList(params) {
    initStatsData();
    return new Promise(resolve => {
        setTimeout(() => {
            let filtered = [...mockStatsList];
            // 简单的过滤逻辑
            if (params.prodStatus) filtered = filtered.filter(t => t.prodStatus === params.prodStatus);
            if (params.shiftType) filtered = filtered.filter(t => t.shiftType === params.shiftType);
            if (params.shift) filtered = filtered.filter(t => t.shift === params.shift);
            if (params.device) filtered = filtered.filter(t => t.device.includes(params.device));
            if (params.checkStatus) {
                // 这里的 checkStatus 对应的是 result (正常/异常)
                if (params.checkStatus === '异常') filtered = filtered.filter(t => t.result === '异常');
            }

            // 分页
            const pageNum = params.pageNum || 1;
            const pageSize = params.pageSize || 20;
            const start = (pageNum - 1) * pageSize;
            const list = filtered.slice(start, start + pageSize);

            resolve({
                list: list,
                total: filtered.length,
                pageNum: pageNum,
                pageSize: pageSize,
                pages: Math.ceil(filtered.length / pageSize)
            });
        }, 300);
    });
}

/**
 * 归档操作
 */
export async function archiveSiData(params) {
    return new Promise(resolve => {
        setTimeout(() => {
            console.log("执行归档:", params);
            resolve({ success: true, message: `归档成功！设备: ${params.device}, 范围: ${params.dateRange}` });
        }, 800);
    });
}