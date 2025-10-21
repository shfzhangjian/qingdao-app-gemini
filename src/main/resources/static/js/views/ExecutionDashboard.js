/**
 * @file /js/views/ExecutionDashboard.js
 * @description “执行看板”的主视图，显示九宫格导航菜单。
 * @version 1.1.0 - 2025-10-21
 */

export default class ExecutionDashboard {
    constructor() {
        this.container = null;
    }

    render(container) {
        this.container = container;
        // --- [修改] ---
        // 1. 将 <img> 替换为 <i> 字体图标
        // 2. 移除图片文件夹的依赖
        // 3. 链接指向 menu.js 中新定义的子路由
        this.container.innerHTML = `
            <div class="execution-dashboard">
                <div class="dashboard-grid">
                    <a href="#!/execution_board/tasks/routine_maintenance" class="grid-item">
                        <i class="bi bi-tools"></i>
                        <span>精点例保</span>
                    </a>
                    <a href="#!/execution_board/tasks/daily_maintenance" class="grid-item">
                        <i class="bi bi-calendar-check"></i>
                        <span>精益日保</span>
                    </a>
                    <a href="#!/execution_board/tasks/rotational_maintenance" class="grid-item">
                        <i class="bi bi-arrow-repeat"></i>
                        <span>精准轮保</span>
                    </a>
                    <a href="#!/execution_board/tasks/monthly_maintenance" class="grid-item">
                        <i class="bi bi-calendar-month"></i>
                        <span>精深月保</span>
                    </a>
                    <a href="#!/execution_board/tasks/professional_check" class="grid-item">
                        <i class="bi bi-clipboard2-check"></i>
                        <span>专业点检</span>
                    </a>
                    <a href="#!/execution_board/tasks/mechanical_lubrication" class="grid-item">
                        <i class="bi bi-water"></i>
                        <span>机械润滑</span>
                    </a>
                    <a href="#" class="grid-item disabled">
                        <i class="bi bi-book"></i>
                        <span>SOP手册</span>
                    </a>
                    <a href="#" class="grid-item disabled">
                        <i class="bi bi-person-workspace"></i>
                        <span>我的包机</span>
                    </a>
                </div>
            </div>
        `;
        // --- [修改结束] ---
    }
}

