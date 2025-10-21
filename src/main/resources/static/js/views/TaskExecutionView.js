/**
 * @file /js/views/TaskExecutionView.js
 * @description 任务执行看板的具体视图，用于展示和操作保养任务卡片。
 * @version 1.0.0 - 2025-10-21
 */
import Modal from '../components/Modal.js';

export default class TaskExecutionView {
    constructor() {
        this.container = null;
        this.activeFilter = 'pending'; // 'pending', 'completed', 'history'
        this.activeMachine = 'PRG 20#高速卷接机组/ZJ112';
        this.tasks = this._getMockTasks();
        this.machines = this._getMockMachines();
    }

    _getMockTasks() {
        // 以日保为例的模拟数据
        return {
            'PRG 20#高速卷接机组/ZJ112': [
                { id: 'task-1', project: '703喷胶部位', content: '用干净湿抹布清洁喷胶头，清洗接胶盒、清洗喷胶洗盒并加注适量丙二醇', standard: '无积胶', score: 5, responsible: '包装挡车工', status: 'pending' },
                { id: 'task-2', project: '水箱和水循环系统', content: '检查水箱冷却液位，清洁水箱过滤网', standard: '液位正常，过滤网清洁', score: 3, responsible: '包装挡车工', status: 'pending' },
                { id: 'task-3', project: '检查空压气源', content: '检查压力是否正常，有无漏气现象', standard: '压力正常，无漏气', score: 2, responsible: '操作工', status: 'completed' },
            ],
            'GDX2 11#包装机(看板机)': [
                { id: 'task-4', project: '烟包输送带', content: '清洁输送带表面，检查张紧度', standard: '表面干净，张紧适度', score: 4, responsible: '操作工', status: 'pending' },
            ]
        };
    }

    _getMockMachines() {
        return [
            'GDX2 13+包装机(看板机)', 'PROTOS 11#卷接机组(看板机)', 'GDX2 11#包装机(看板机)', 'PRG 21#高速卷接机组/ZJ112(看板机)',
            'GDGY 21#高速包装机组(看板机)', 'GDX2 41#包装机组(细支)(看板机)', 'PROTOS 41#卷接机组(细支)(看板机)',
            'PROTOS 14#卷接机组(看板机)', 'GDX2 14#包装机组(看板机)', 'PRG 20#高速卷接机组/ZJ112', 'GDGY 20#高速包装机组(看板机)'
        ];
    }

    render(container, route) {
        this.container = container;
        this.maintenanceType = this._getTypeFromRoute(route);
        this.container.innerHTML = `
            <div class="task-execution-view">
                <div class="task-sidebar">
                    <div class="sidebar-header">${this.maintenanceType}</div>
                    <ul class="nav flex-column">
                        <li class="nav-item">
                            <a class="nav-link active" href="#" data-filter="pending"><i class="bi bi-list-task"></i> 待办任务</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="#" data-filter="completed"><i class="bi bi-check2-square"></i> 已办任务</a>
                        </li>
                        <li class="nav-item">
                            <a class="nav-link" href="#" data-filter="history"><i class="bi bi-calendar3"></i> 历史查询</a>
                        </li>
                    </ul>
                </div>
                <div class="task-main-content">
                    <div class="content-header">
                        <div class="d-flex align-items-center">
                            <button class="btn btn-sm btn-outline-secondary me-3" id="back-to-dashboard"><i class="bi bi-arrow-left"></i></button>
                            <nav aria-label="breadcrumb">
                                <ol class="breadcrumb mb-0">
                                    <li class="breadcrumb-item"><a href="#!/execution_board">执行看板</a></li>
                                    <li class="breadcrumb-item active" aria-current="page">${this.maintenanceType}</li>
                                </ol>
                            </nav>
                        </div>
                        <div class="d-flex align-items-center gap-2">
                             <!-- [修改] 将 dropdown 替换为 button，用于触发模态框 -->
                            <button class="btn btn-light" type="button" id="machine-selector">
                                ${this.activeMachine} <i class="bi bi-chevron-down ms-2 small"></i>
                            </button>
                            <button class="btn btn-primary" id="complete-all-btn">全部完成</button>
                        </div>
                    </div>
                    <div class="content-body" id="task-cards-container">
                        <!-- 任务卡片将在这里渲染 -->
                    </div>
                </div>
            </div>
        `;
        this._renderTaskCards();
        this._attachEventListeners();
    }

    _getTypeFromRoute(route){
        if(!route || !route.id) return "未知任务";
        const typeMap = {
            "routine_maintenance": "精点例保",
            "daily_maintenance": "精益日保",
            "rotational_maintenance": "精准轮保",
            "monthly_maintenance": "精深月保",
            "professional_check": "专业点检",
            "mechanical_lubrication": "机械润滑"
        };
        return typeMap[route.id] || "未知任务";
    }

    _renderTaskCards() {
        const container = this.container.querySelector('#task-cards-container');
        const tasksForMachine = this.tasks[this.activeMachine] || [];
        const filteredTasks = tasksForMachine.filter(t => this.activeFilter === 'history' || t.status === this.activeFilter);

        if (this.activeFilter === 'history') {
            container.innerHTML = `
                <div class="d-flex align-items-center gap-2 mb-3">
                    <label>选择日期:</label>
                    <input type="text" id="history-datepicker" class="form-control form-control-sm" style="width: 150px;">
                </div>
                <div id="history-list">
                    <p class="text-secondary">请选择日期以查询历史记录。</p>
                </div>`;
            new DatePicker(container.querySelector('#history-datepicker'), {
                mode: 'single',
                dateFormat: 'Y-m-d',
                defaultDate: new Date(),
                locale: 'zh',
                onChange: (selectedDates, dateStr) => this._renderHistoryList(dateStr)
            });
        } else if (filteredTasks.length > 0) {
            container.innerHTML = filteredTasks.map(task => this._createTaskCard(task)).join('');
        } else {
            container.innerHTML = `<p class="text-secondary mt-3">当前筛选条件下没有任务。</p>`;
        }
    }

    _renderHistoryList(date){
        const listContainer = this.container.querySelector('#history-list');
        // 模拟历史数据
        const historyTasks = this._getMockTasks()[this.activeMachine].filter(t=> t.status === 'completed');
        if(historyTasks.length > 0){
            listContainer.innerHTML = `<ul class="list-group">${historyTasks.map(t=>`<li class="list-group-item">【${t.project}】${t.content} - <strong>已完成</strong></li>`).join('')}</ul>`;
        } else {
            listContainer.innerHTML = `<p class="text-secondary">该日期没有历史记录。</p>`;
        }
    }

    _createTaskCard(task) {
        const isCompleted = task.status === 'completed';
        return `
            <div class="task-card" data-task-id="${task.id}" data-status="${task.status}">
                <div class="card-header">
                    <i class="bi bi-tools me-2"></i>
                    <strong>保养项目：</strong> ${task.project}
                </div>
                <div class="card-body">
                    <p class="card-text"><strong>保养内容：</strong> ${task.content}</p>
                    <p class="card-text"><strong>保养标准：</strong> ${task.standard} <span class="score">分值：【${task.score}】</span> 责任岗位：【${task.responsible}】</p>
                </div>
                <div class="card-footer">
                     <span class="status-badge ${isCompleted ? 'status-completed' : 'status-pending'}">
                        <i class="bi ${isCompleted ? 'bi-check-circle-fill' : 'bi-hourglass-split'}"></i>
                        ${isCompleted ? '已执行' : '未执行'}
                    </span>
                    <button class="btn btn-sm ${isCompleted ? 'btn-secondary disabled' : 'btn-primary'} complete-btn" ${isCompleted ? 'disabled' : ''}>
                        ${isCompleted ? '已完成' : '完成'}
                    </button>
                </div>
            </div>
        `;
    }

    _attachEventListeners() {
        // 返回按钮
        this.container.querySelector('#back-to-dashboard').addEventListener('click', () => {
            window.history.back();
        });

        // 左侧导航筛选
        this.container.querySelectorAll('.task-sidebar .nav-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                this.container.querySelector('.task-sidebar .nav-link.active').classList.remove('active');
                e.currentTarget.classList.add('active');
                this.activeFilter = e.currentTarget.dataset.filter;
                this._renderTaskCards();
            });
        });

        // --- [修改] ---
        // 机台切换按钮现在打开模态框
        this.container.querySelector('#machine-selector').addEventListener('click', () => {
            this._showMachineSelectionModal();
        });

        // 卡片内完成按钮
        this.container.querySelector('#task-cards-container').addEventListener('click', (e)=>{
            const completeBtn = e.target.closest('.complete-btn:not(.disabled)');
            if(completeBtn){
                const card = completeBtn.closest('.task-card');
                const taskId = card.dataset.taskId;
                this._markTaskAsComplete(taskId);
            }
        });

        // 全部完成按钮
        this.container.querySelector('#complete-all-btn').addEventListener('click', async () => {
            const pendingTasks = this.container.querySelectorAll('.task-card[data-status="pending"]');
            if(pendingTasks.length === 0){
                Modal.alert("没有待办任务可以完成。");
                return;
            }
            const confirmed = await Modal.confirm('确认操作', `您确定要将当前设备下所有的 ${pendingTasks.length} 个待办任务全部标记为完成吗？`);
            if (confirmed) {
                pendingTasks.forEach(card => {
                    this._markTaskAsComplete(card.dataset.taskId, false);
                });
                this._renderTaskCards(); // 重新渲染以移动卡片
            }
        });
    }

    // --- [新增] 显示机台设备选择模态框 ---
    _showMachineSelectionModal() {
        const machineGridHtml = `
            <div class="machine-grid">
                ${this.machines.map(m => `
                    <div class="machine-item ${m === this.activeMachine ? 'active' : ''}" data-machine-name="${m}">
                        <i class="bi bi-display"></i>
                        <span>${m}</span>
                    </div>
                `).join('')}
            </div>
        `;

        const modal = new Modal({
            title: '设备列表',
            body: machineGridHtml,
            size: 'xl' // 使用一个较大的模态框
        });

        modal.modalElement.querySelector('.machine-grid').addEventListener('click', (e) => {
            const machineItem = e.target.closest('.machine-item');
            if (machineItem) {
                this.activeMachine = machineItem.dataset.machineName;
                this.container.querySelector('#machine-selector').innerHTML = `${this.activeMachine} <i class="bi bi-chevron-down ms-2 small"></i>`;
                modal.hide();
                this._renderTaskCards();
            }
        });

        modal.show();
    }

    _markTaskAsComplete(taskId, rerender = true) {
        const allTasks = Object.values(this.tasks).flat();
        const task = allTasks.find(t => t.id === taskId);
        if (task) {
            task.status = 'completed';
            if(rerender) {
                this._renderTaskCards();
            }
        }
    }
}

