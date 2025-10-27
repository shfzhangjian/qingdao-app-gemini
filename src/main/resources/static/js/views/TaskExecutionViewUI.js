/**
 * @file /js/views/TaskExecutionView.js
 * @description 任务执行看板的具体视图，用于展示和操作保养任务卡片。
 * @version 1.8.0 - 2025-10-21 - [FEAT] 优化评分逻辑、卡片信息和历史报表样式
 */
import Modal from '../components/Modal.js';
import DatePicker from '../components/DatePicker.js';

export default class TaskExecutionView {
    constructor() {
        this.container = null;
        this.activeFilter = 'pending'; // 'pending', 'scoring', 'history'
        this.pendingSubView = 'pending'; // 'pending', 'completed'
        this.scoringSubView = 'unscored'; // 'unscored', 'scored'
        this.activeMachine = 'PRG 20#高速卷接机组/ZJ112';
        this.tasks = this._getMockTasks();
        this.machines = this._getMockMachines();

        this.crosstabStartDate = this._getStartOfWeek(new Date());
    }

    _getMockTasks() {
        const baseTasks = [
            { id: 'task-1', project: '703喷胶部位', content: '用干净湿抹布清洁喷胶头，清洗接胶盒、清洗喷胶洗盒并加注适量丙二醇', standard: '无积胶', score: 5, executor: '李明' },
            { id: 'task-2', project: '水箱和水循环系统', content: '检查水箱冷却液位，清洁水箱过滤网', standard: '液位正常', score: 3, executor: '李明' },
            { id: 'task-3', project: '检查空压气源', content: '检查压力是否正常，有无漏气现象', standard: '压力正常', score: 2, executor: '张三' },
            { id: 'task-4', project: '烟包输送带', content: '清洁输送带表面，检查张紧度', standard: '表面干净，张紧适度', score: 4, executor: '陈七' },
            { id: 'task-5', project: '操作面板清洁', content: '用软布擦拭操作面板及周边区域', standard: '无灰尘、无油污', score: 2, executor: '张三' },
            { id: 'task-6', project: '安全门检查', content: '检查所有安全门传感器是否灵敏，有无松动', standard: '开关灵敏，固定牢固', score: 4, executor: '王五' },
            { id: 'task-7', project: '主电机检查', content: '听主电机运行时有无异响，检查地脚螺丝', standard: '运行平稳，无松动', score: 5, executor: '赵六' },
            { id: 'task-8', project: '传送带张紧度', content: '检查各主要传送带张紧是否适度', standard: '无松弛或过紧现象', score: 3, executor: '王五' },
            { id: 'task-9', project: '润滑油位检查', content: '检查主减速箱油位视窗', standard: '油位在上下标线之间', score: 4, executor: '赵六' },
            { id: 'task-10', project: '滤芯清洁', content: '清洁空气过滤器滤芯，吹扫灰尘', standard: '滤芯洁净，无堵塞', score: 3, executor: '李明' },
            { id: 'task-11', project: '废料收集箱', content: '清理所有废料、废丝收集箱', standard: '箱内清洁，无溢出', score: 2, executor: '张三' },
            { id: 'task-12', project: '光电传感器', content: '用软布和清洁剂擦拭所有光电传感器探头', standard: '探头洁净，无附着物', score: 3, executor: '王五' },
        ];

        const tasks = baseTasks.map(t => ({
            ...t,
            currentScore: t.score, // 默认当前分等于基础分
            responsible: '操作工',
            status: 'pending',
            isAbnormal: false,
            abnormalReason: null,
            completeDate: null,
            checkedScore: null,
            checker: null
        }));

        // 手动设置一些任务状态用于演示
        tasks[2].status = 'completed'; tasks[2].completeDate = this._formatDate(new Date());
        tasks[3].status = 'completed'; tasks[3].completeDate = this._formatDate(new Date()); tasks[3].checkedScore = 3; tasks[3].checker = '王工'; // 扣分项
        tasks[4].status = 'completed'; tasks[4].completeDate = this._formatDate(new Date(), -1);
        tasks[5].status = 'completed'; tasks[5].completeDate = this._formatDate(new Date(), -1); tasks[5].checkedScore = 4; tasks[5].checker = '王工';
        tasks[6].status = 'completed'; tasks[6].completeDate = this._formatDate(new Date(), -1); tasks[6].isAbnormal = true; tasks[6].abnormalReason = "传感器轻微松动";

        const machine1Tasks = JSON.parse(JSON.stringify(tasks));
        const machine2Tasks = JSON.parse(JSON.stringify(tasks.slice(0,5)));
        machine2Tasks[0].status = 'completed'; machine2Tasks[0].completeDate = this._formatDate(new Date());
        machine2Tasks[1].status = 'completed'; machine2Tasks[1].completeDate = this._formatDate(new Date()); machine2Tasks[1].checkedScore = 3; machine2Tasks[1].checker = '刘工';


        return {
            'PRG 20#高速卷接机组/ZJ112': machine1Tasks,
            'GDX2 11#包装机(看板机)': machine2Tasks
        };
    }


    _getMockMachines() {
        return ['PRG 20#高速卷接机组/ZJ112', 'GDX2 11#包装机(看板机)'];
    }

    render(container, route) {
        this.container = container;
        this.maintenanceType = this._getTypeFromRoute(route);
        const isContentOnly = document.body.classList.contains('content-only-mode');
        const backButtonHtml = isContentOnly ? '' : `<button class="btn btn-sm btn-outline-secondary me-3" id="back-to-dashboard"><i class="bi bi-arrow-left"></i></button>`;

        this.container.innerHTML = `
            <div class="task-execution-view">
                <div class="task-sidebar">
                    <div class="sidebar-header">${this.maintenanceType}</div>
                    <ul class="nav flex-column">
                        <li class="nav-item"><a class="nav-link active" href="#" data-filter="pending"><i class="bi bi-list-task"></i> 待办任务</a></li>
                        <li class="nav-item"><a class="nav-link" href="#" data-filter="scoring"><i class="bi bi-star-half"></i> 任务评分</a></li>
                        <li class="nav-item"><a class="nav-link" href="#" data-filter="history"><i class="bi bi-table"></i> 历史查询</a></li>
                    </ul>
                </div>
                <div class="task-main-content">
                    <div class="content-header">
                        <div class="d-flex align-items-center">
                            ${backButtonHtml}
                            <nav aria-label="breadcrumb">
                                <ol class="breadcrumb mb-0">
                                    <li class="breadcrumb-item"><a href="#!/execution_board/dashboard_main">执行看板</a></li>
                                    <li class="breadcrumb-item active" aria-current="page">${this.maintenanceType}</li>
                                </ol>
                            </nav>
                        </div>
                        <div class="d-flex align-items-center gap-2" id="header-actions"></div>
                    </div>
                    <div class="content-body" id="task-cards-container"></div>
                </div>
            </div>
        `;
        this._renderContent();
        this._attachEventListeners();
    }

    _renderHeaderActions() {
        const container = this.container.querySelector('#header-actions');
        let buttonsHtml = `<button class="btn btn-light" type="button" id="machine-selector">${this.activeMachine} <i class="bi bi-chevron-down ms-2 small"></i></button>`;
        if (this.activeFilter === 'pending' && this.pendingSubView === 'pending') {
            buttonsHtml += `<button class="btn btn-primary" id="complete-all-btn">全部完成</button>`;
        } else if (this.activeFilter === 'scoring' && this.scoringSubView === 'unscored') {
            buttonsHtml += `<button class="btn btn-success" id="score-all-btn">全部评分</button>`;
        }
        container.innerHTML = buttonsHtml;
    }

    _getTypeFromRoute(route){
        if(!route || !route.id) return "未知任务";
        const typeMap = {"routine_maintenance": "精点例保", "daily_maintenance": "精益日保", "rotational_maintenance": "精准轮保", "monthly_maintenance": "精深月保", "professional_check": "专业点检", "mechanical_lubrication": "机械润滑"};
        return typeMap[route.id] || "未知任务";
    }

    _renderContent() {
        this._renderHeaderActions();
        const container = this.container.querySelector('#task-cards-container');

        switch(this.activeFilter) {
            case 'pending': this._renderPendingView(container); break;
            case 'scoring': this._renderScoringView(container); break;
            case 'history': this._renderHistoryCrosstab(container); break;
            default: container.innerHTML = '';
        }
    }

    _renderPendingView(container) {
        container.innerHTML = `
            <ul class="nav nav-pills nav-pills-slim mb-3">
                <li class="nav-item"><a class="nav-link ${this.pendingSubView === 'pending' ? 'active' : ''}" href="#" data-subview="pending">本班待办</a></li>
                <li class="nav-item"><a class="nav-link ${this.pendingSubView === 'completed' ? 'active' : ''}" href="#" data-subview="completed">本班已办</a></li>
            </ul>
            <div id="pending-tasks-list" class="scrollable-content"></div>
        `;
        const listContainer = container.querySelector('#pending-tasks-list');
        const tasks = this.tasks[this.activeMachine]?.filter(t => {
            const isToday = t.completeDate === this._formatDate(new Date());
            return this.pendingSubView === 'pending' ? t.status === 'pending' : (t.status === 'completed' && isToday);
        }) || [];

        listContainer.innerHTML = tasks.length > 0
            ? tasks.map(task => this._createPendingTaskCard(task)).join('')
            : `<p class="text-secondary mt-3">没有相关任务。</p>`;
    }

    _createPendingTaskCard(task) {
        const isCompleted = task.status === 'completed';
        return `
            <div class="task-card" data-task-id="${task.id}">
                <div class="card-header d-flex justify-content-between">
                    <span><strong>项目：</strong> ${task.project}</span>
                    ${task.isAbnormal ? `<span class="badge bg-danger" title="异常原因: ${task.abnormalReason || '无'}">异常</span>` : ''}
                </div>
                <div class="card-body">
                    <p class="card-text"><strong>内容：</strong> ${task.content}</p>
                    <p class="card-text"><strong>标准：</strong> ${task.standard}</p>
                </div>
                <div class="card-footer">
                    <span class="score-badge">分值：【${task.score}】</span>
                    ${isCompleted ? `
                        <span class="status-badge status-completed"><i class="bi bi-check-circle-fill"></i> ${task.executor} 已执行</span>
                    ` : `
                        <div class="action-buttons">
                            <button class="btn btn-sm btn-outline-danger abnormal-btn">异常</button>
                            <button class="btn btn-sm btn-primary complete-btn">完成</button>
                        </div>
                    `}
                </div>
            </div>`;
    }

    _renderScoringView(container) {
        container.innerHTML = `
            <div class="scoring-view">
                <ul class="nav nav-pills nav-pills-slim mb-3">
                    <li class="nav-item"><a class="nav-link ${this.scoringSubView === 'unscored' ? 'active' : ''}" href="#" data-subview="unscored">本班待评分</a></li>
                    <li class="nav-item"><a class="nav-link ${this.scoringSubView === 'scored' ? 'active' : ''}" href="#" data-subview="scored">本班已评分</a></li>
                </ul>
                <div id="scoring-tasks-container" class="scrollable-content"></div>
                <div id="scoring-footer"></div>
            </div>`;
        this._renderScoringTasks();
    }

    _renderScoringTasks() {
        const tasksContainer = this.container.querySelector('#scoring-tasks-container');
        const showScored = this.scoringSubView === 'scored';
        const tasks = this.tasks[this.activeMachine]?.filter(t => {
            const isToday = t.completeDate === this._formatDate(new Date());
            if (showScored) {
                return isToday && t.checkedScore !== null;
            }
            return isToday && t.status === 'completed' && t.checkedScore === null;
        }) || [];

        tasksContainer.innerHTML = tasks.length > 0
            ? tasks.map(task => this._createScoringTaskCard(task)).join('')
            : `<p class="text-secondary mt-3">没有相关任务。</p>`;

        this._updateScoringFooter(tasks);
    }

    _createScoringTaskCard(task) {
        const currentScore = this.scoringSubView === 'scored' ? task.checkedScore : task.currentScore;
        return `
            <div class="task-card scoring-card" data-task-id="${task.id}">
                <div class="card-header d-flex justify-content-between">
                    <span><strong>项目：</strong> ${task.project}</span>
                    ${task.isAbnormal ? `<span class="badge bg-danger" title="异常原因: ${task.abnormalReason || '无'}">异常</span>` : ''}
                </div>
                <div class="card-body">
                    <p class="card-text"><strong>内容：</strong> ${task.content}</p>
                    <p class="card-text"><strong>标准：</strong> ${task.standard}</p>
                    <div class="task-meta-info">
                        <span><i class="bi bi-person-fill"></i> ${task.executor}</span>
                        <span><i class="bi bi-clock-fill"></i> ${task.completeDate}</span>
                    </div>
                </div>
                <div class="card-footer">
                    <span class="score-badge">基础分：【${task.score}】</span>
                    <div class="scoring-controls">
                        <button class="btn btn-sm btn-outline-secondary score-adjust-btn" data-action="decrease">-</button>
                        <span class="score-value">${currentScore}</span>
                        <button class="btn btn-sm btn-outline-secondary score-adjust-btn" data-action="increase">+</button>
                    </div>
                </div>
            </div>
        `;
    }

    _updateScoringFooter(tasks) {
        const footerContainer = this.container.querySelector('#scoring-footer');
        if (!footerContainer) return;

        const totalScore = tasks.reduce((sum, task) => sum + (this.scoringSubView === 'scored' ? task.checkedScore : task.currentScore), 0);

        footerContainer.innerHTML = tasks.length > 0 ? `
            <div class="scoring-footer-content">
                <div class="scorer-info"><strong>评分人:</strong> 王工</div>
                <div class="total-score-info"><strong>总得分:</strong> <span class="text-primary fs-5">${totalScore}</span></div>
            </div>
        ` : '';
    }

    _renderHistoryCrosstab(container) {
        const weekDates = Array.from({ length: 7 }).map((_, i) => {
            const date = new Date(this.crosstabStartDate);
            date.setDate(date.getDate() + i);
            return date;
        });
        const tasksForMachine = this.tasks[this.activeMachine] || [];
        const groupedTasks = this._groupTasksForCrosstab(tasksForMachine);

        let seqCounter = 1;

        container.innerHTML = `
            <div class="history-crosstab-view">
                <div class="crosstab-header">
                    <button id="prev-week" class="btn btn-sm btn-outline-secondary"><i class="bi bi-chevron-left"></i> 上一周</button>
                    <div class="date-display">
                        <i class="bi bi-calendar-week"></i>
                        <span id="crosstab-date-display">${this._formatDate(weekDates[0])} 至 ${this._formatDate(weekDates[6])}</span>
                    </div>
                    <button id="next-week" class="btn btn-sm btn-outline-secondary">下一周 <i class="bi bi-chevron-right"></i></button>
                </div>
                <div class="table-responsive scrollable-content">
                    <table class="table table-bordered table-sm history-crosstab">
                        <thead>
                            <tr>
                                <th class="row-header crosstab-seq" style="width: 5%;">序号</th>
                                <th class="row-header" style="width: 15%;">保养项目</th>
                                <th class="row-header" style="width: 20%;">保养内容</th>
                                <th class="row-header" style="width: 20%;">保养标准</th>
                                ${weekDates.map(d => `<th>${this._formatDate(d).substring(5)}<br>${['日','一','二','三','四','五','六'][d.getDay()]}</th>`).join('')}
                            </tr>
                        </thead>
                        <tbody>
                            ${Object.keys(groupedTasks).map(project => this._createCrosstabRows(project, groupedTasks[project], weekDates, seqCounter++)).join('')}
                        </tbody>
                        <tfoot>
                            ${this._createCrosstabFooter('executor', '执行人', tasksForMachine, weekDates)}
                            ${this._createCrosstabFooter('checker', '检查人/分', tasksForMachine, weekDates)}
                        </tfoot>
                    </table>
                </div>
            </div>`;
        this._attachCrosstabListeners();
    }

    _groupTasksForCrosstab(tasks) {
        return tasks.reduce((acc, task) => {
            if (!acc[task.project]) acc[task.project] = {};
            if (!acc[task.project][task.content]) acc[task.project][task.content] = {};
            if (!acc[task.project][task.content][task.standard]) acc[task.project][task.content][task.standard] = [];
            acc[task.project][task.content][task.standard].push(task);
            return acc;
        }, {});
    }

    _createCrosstabRows(project, contents, weekDates, projectIndex) {
        let rowsHtml = '';
        const contentEntries = Object.entries(contents);
        const projectRowSpan = Object.values(contents).flatMap(Object.keys).length;

        contentEntries.forEach(([content, standards], contentIndex) => {
            const standardEntries = Object.entries(standards);
            const contentRowSpan = standardEntries.length;

            standardEntries.forEach(([standard, tasks], standardIndex) => {
                rowsHtml += `<tr>
                    ${contentIndex === 0 && standardIndex === 0 ? `<td rowspan="${projectRowSpan}" class="crosstab-seq">${projectIndex}</td>` : ''}
                    ${contentIndex === 0 && standardIndex === 0 ? `<td rowspan="${projectRowSpan}">${project}</td>` : ''}
                    ${standardIndex === 0 ? `<td rowspan="${contentRowSpan}">${content}</td>` : ''}
                    <td>${standard}</td>
                    ${weekDates.map(date => {
                    const dateStr = this._formatDate(date);
                    const dayTask = tasks.find(t => t.completeDate === dateStr);
                    if (dayTask && dayTask.status === 'completed') {
                        if (dayTask.isAbnormal) return `<td><i class="bi bi-exclamation-triangle-fill text-danger" title="异常: ${dayTask.abnormalReason || ''}"></i></td>`;
                        if (dayTask.checkedScore !== null && dayTask.checkedScore < dayTask.score) return `<td><i class="bi bi-arrow-down-circle-fill text-warning" title="扣分项"></i></td>`;
                        return '<td><i class="bi bi-check-lg text-success"></i></td>';
                    }
                    return '<td></td>';
                }).join('')}
                </tr>`;
            });
        });
        return rowsHtml;
    }

    _createCrosstabFooter(key, label, tasks, weekDates) {
        const weeklyExecutor = tasks.find(t => t.executor)?.executor || '-';
        const weeklyChecker = tasks.find(t => t.checker)?.checker || '-';

        let contentHtml = '';
        if (key === 'executor') {
            contentHtml = weekDates.map(date => {
                const dateStr = this._formatDate(date);
                const hasTasksOnDate = tasks.some(t => t.completeDate === dateStr);
                return `<td>${hasTasksOnDate ? weeklyExecutor : '-'}</td>`;
            }).join('');
        } else if (key === 'checker') {
            contentHtml = weekDates.map(date => {
                const dateStr = this._formatDate(date);
                const tasksOnDate = tasks.filter(t => t.completeDate === dateStr && t.checkedScore !== null);
                const totalScore = tasksOnDate.reduce((sum, t) => sum + t.checkedScore, 0);
                return `<td>${tasksOnDate.length > 0 ? `${weeklyChecker}(${totalScore})` : '-'}</td>`;
            }).join('');
        }

        return `
            <tr>
                <td colspan="4" class="text-end fw-bold">${label}</td>
                ${contentHtml}
            </tr>
        `;
    }

    _attachEventListeners() {
        const backButton = this.container.querySelector('#back-to-dashboard');
        if (backButton) { backButton.addEventListener('click', () => { window.location.hash = '#!/execution_board/dashboard_main'; }); }

        this.container.querySelectorAll('.task-sidebar .nav-link').forEach(link => {
            link.addEventListener('click', (e) => {
                e.preventDefault();
                this.container.querySelector('.task-sidebar .nav-link.active').classList.remove('active');
                e.currentTarget.classList.add('active');
                this.activeFilter = e.currentTarget.dataset.filter;
                this._renderContent();
            });
        });

        this.container.addEventListener('click', async (e) => {
            const headerButton = e.target.closest('#header-actions button');
            if (headerButton) {
                if(headerButton.id === 'machine-selector') { this._showMachineSelectionModal(); }
                else if (headerButton.id === 'complete-all-btn') {
                    const pendingTasks = this.tasks[this.activeMachine]?.filter(t => t.status === 'pending') || [];
                    if(pendingTasks.length === 0){ Modal.alert("没有待办任务可以完成。"); return; }
                    const confirmed = await Modal.confirm('确认操作', `您确定要将当前设备下所有的 ${pendingTasks.length} 个待办任务全部标记为完成吗？`);
                    if (confirmed) {
                        pendingTasks.forEach(task => { this._markTaskAsComplete(task.id, false); });
                        this._renderContent();
                    }
                } else if (headerButton.id === 'score-all-btn') {
                    const tasksToScore = this.tasks[this.activeMachine]?.filter(t => t.status === 'completed' && t.checkedScore === null && t.completeDate === this._formatDate(new Date())) || [];
                    if (tasksToScore.length === 0) { Modal.alert("当前没有待评分的任务。"); return; }
                    const confirmed = await Modal.confirm('确认全部评分', `您确定要为 ${tasksToScore.length} 个未评分任务按当前显示的分值提交吗？`);
                    if(confirmed) {
                        tasksToScore.forEach(task => { task.checkedScore = task.currentScore; task.checker = "王工"; });
                        this.scoringSubView = 'scored';
                        this._renderContent();
                    }
                }
                return;
            }

            const card = e.target.closest('.task-card');
            if(card) {
                const taskId = card.dataset.taskId;
                if(e.target.matches('.complete-btn')) { this._markTaskAsComplete(taskId); }
                else if (e.target.matches('.abnormal-btn')) {
                    const { value: reason, isConfirmed } = await Modal.prompt('异常提报', '请输入异常情况描述：');
                    if (isConfirmed) { this._markTaskAsAbnormal(taskId, reason || "无描述"); }
                }
                else if (e.target.closest('.score-adjust-btn')) {
                    const task = this.tasks[this.activeMachine]?.find(t => t.id === taskId);
                    if (!task) return;
                    const action = e.target.closest('.score-adjust-btn').dataset.action;

                    let scoreHolder = this.scoringSubView === 'scored' ? 'checkedScore' : 'currentScore';
                    let currentVal = task[scoreHolder];

                    if(action === 'increase') currentVal = Math.min(task.score, currentVal + 1);
                    else if (action === 'decrease') currentVal = Math.max(0, currentVal - 1);

                    task[scoreHolder] = currentVal;

                    this._renderScoringTasks();
                }
                return;
            }

            const subviewLink = e.target.closest('.nav-pills-slim .nav-link');
            if(subviewLink) {
                e.preventDefault();
                if(this.activeFilter === 'pending') {
                    this.pendingSubView = subviewLink.dataset.subview;
                    this._renderPendingView(this.container.querySelector('#task-cards-container'));
                } else if (this.activeFilter === 'scoring') {
                    this.scoringSubView = subviewLink.dataset.subview;
                    this._renderScoringView(this.container.querySelector('#task-cards-container'));
                }
                this._renderHeaderActions();
            }
        });
    }

    _attachCrosstabListeners() {
        const header = this.container.querySelector('.crosstab-header');
        if (!header) return;
        header.querySelector('#prev-week').addEventListener('click', () => {
            this.crosstabStartDate.setDate(this.crosstabStartDate.getDate() - 7);
            this._renderHistoryCrosstab(this.container.querySelector('#task-cards-container'));
        });
        header.querySelector('#next-week').addEventListener('click', () => {
            this.crosstabStartDate.setDate(this.crosstabStartDate.getDate() + 7);
            this._renderHistoryCrosstab(this.container.querySelector('#task-cards-container'));
        });
        header.querySelector('.date-display').addEventListener('click', () => {
            const dpInput = document.createElement('input');
            const dp = new DatePicker(dpInput, {
                defaultDate: this.crosstabStartDate,
                mode: 'single',
                onChange: (selectedDates) => {
                    if (selectedDates[0]) {
                        this.crosstabStartDate = this._getStartOfWeek(selectedDates[0]);
                        this._renderHistoryCrosstab(this.container.querySelector('#task-cards-container'));
                        dp.instance.destroy();
                    }
                }
            });
            dp.instance.open();
        });
    }

    _showMachineSelectionModal() {
        const machineGridHtml = `<div class="machine-grid">${this.machines.map(m => `<div class="machine-item ${m === this.activeMachine ? 'active' : ''}" data-machine-name="${m}"><i class="bi bi-display"></i><span>${m}</span></div>`).join('')}</div>`;
        const modal = new Modal({ title: '设备列表', body: machineGridHtml, size: 'xl' });
        modal.modalElement.querySelector('.machine-grid').addEventListener('click', (e) => {
            const machineItem = e.target.closest('.machine-item');
            if (machineItem) {
                this.activeMachine = machineItem.dataset.machineName;
                this._renderContent();
                modal.hide();
            }
        });
        modal.show();
    }

    _markTaskAsComplete(taskId, rerender = true) {
        const task = this.tasks[this.activeMachine]?.find(t => t.id === taskId);
        if (task) {
            task.status = 'completed';
            task.completeDate = this._formatDate(new Date());
            if (rerender) this._renderContent();
        }
    }

    _markTaskAsAbnormal(taskId, reason) {
        const task = this.tasks[this.activeMachine]?.find(t => t.id === taskId);
        if (task) {
            task.status = 'completed';
            task.isAbnormal = true;
            task.abnormalReason = reason;
            task.completeDate = this._formatDate(new Date());
            this._renderContent();
        }
    }

    _formatDate(date, daysOffset = 0) {
        if (!date) return '';
        const d = new Date(date);
        if (daysOffset) d.setDate(d.getDate() + daysOffset);
        if (isNaN(d.getTime())) return '';
        const year = d.getFullYear();
        const month = String(d.getMonth() + 1).padStart(2, '0');
        const day = String(d.getDate()).padStart(2, '0');
        return `${year}-${month}-${day}`;
    }

    _getStartOfWeek(date) {
        const d = new Date(date);
        const day = d.getDay();
        const diff = d.getDate() - day + (day === 0 ? -6 : 1); // adjust when day is sunday
        d.setDate(diff)
        return new Date(d.setHours(0, 0, 0, 0));
    }
}

