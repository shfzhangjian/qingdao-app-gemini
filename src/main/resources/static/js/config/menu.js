/**
 * @file /js/config/menu.js
 * @description 应用程序的导航菜单配置文件
 * @version 2.2.0 - 2025-10-17
 */

const menuConfig = [
    {
        id: "performance_opt",
        title: "设备性能优化",
        icon: "bi-graph-up-arrow",
        children: [
            { id: "fault_diagnosis", title: "故障诊断", url: "#!/performance_opt/fault_diagnosis", component: "./views/Placeholder.js" },
            {
                id: "metrology_mgmt",
                title: "计量管理",
                icon: "bi-rulers",
                children: [
                    { id: "tasks", title: "计量任务", url: "#!/performance_opt/metrology_mgmt/tasks", component: "./views/MetrologyTasks.js" },
                    { id: "ledger", title: "计量台账", url: "#!/performance_opt/metrology_mgmt/ledger", component: "./views/MetrologyLedger.js" },
                    { id: "point_check", title: "点检统计", url: "#!/performance_opt/metrology_mgmt/point_check", component: "./views/PointCheck.js" }
                ]
            },
            { id: "asset_mgmt", title: "备件管理", url: "#!/performance_opt/asset_mgmt", component: "./views/Placeholder.js" },
            { id: "knowledge_base", title: "知识库分析", url: "#!/performance_opt/knowledge_base", component: "./views/Placeholder.js" },
            { id: "spare_parts_mgmt", title: "备品备件管理", url: "#!/performance_opt/spare_parts_mgmt", component: "./views/Placeholder.js" },
        ]
    },
    {
        id: "running_opt",
        title: "设备运行优化",
        icon: "bi-activity",
        children: [
            { id: "run_monitor", title: "运行监控", url: "#!/running_opt/run_monitor", component: "./views/Placeholder.js" }
        ]
    },
    {
        id: "lifecycle_mgmt",
        title: "全生命周期管理",
        icon: "bi-arrow-repeat",
        children: [
            { id: "device_files", title: "设备档案", url: "#!/lifecycle_mgmt/device_files", component: "./views/Placeholder.js" }
        ]
    },
    {
        id: "maintenance_mgmt",
        title: "维保模块",
        icon: "bi-tools",
        children: [
            {
                id: "routine_maintenance",
                title: "精点例保",
                icon: "bi-clock",
                children: [
                    { id: "routine_std", title: "保养标准", url: "#!/maintenance_mgmt/routine_maintenance/routine_std", component: "./views/MaintenanceStandard.js" },
                    { id: "routine_plan", title: "生成计划", url: "#!/maintenance_mgmt/routine_maintenance/routine_plan", component: "./views/MaintenancePlan.js" },
                    { id: "routine_exec", title: "执行任务", url: "#!/maintenance_mgmt/routine_maintenance/routine_exec", component: "./views/MaintenanceExecution.js" },
                    { id: "routine_check", title: "任务检查", url: "#!/maintenance_mgmt/routine_maintenance/routine_check", component: "./views/MaintenanceCheck.js" },
                    { id: "routine_result", title: "执行结果", url: "#!/maintenance_mgmt/routine_maintenance/routine_result", component: "./views/Placeholder.js" },
                ]
            },
            {
                id: "daily_maintenance",
                title: "精益日保",
                icon: "bi-clock-history",
                children: [
                    { id: "daily_std", title: "保养标准", url: "#!/maintenance_mgmt/daily_maintenance/daily_std", component: "./views/DailyMaintenanceStandard.js" },
                    { id: "daily_plan", title: "生成计划", url: "#!/maintenance_mgmt/daily_maintenance/daily_plan", component: "./views/Placeholder.js" },
                    { id: "daily_exec", title: "执行任务", url: "#!/maintenance_mgmt/daily_maintenance/daily_exec", component: "./views/Placeholder.js" },
                    { id: "daily_check", title: "任务检查", url: "#!/maintenance_mgmt/daily_maintenance/daily_check", component: "./views/Placeholder.js" },
                    { id: "daily_result", title: "执行结果", url: "#!/maintenance_mgmt/daily_maintenance/daily_result", component: "./views/Placeholder.js" },
                ]
            },
            {
                id: "rotational_maintenance",
                title: "精准轮保",
                icon: "bi-arrow-repeat",
                children: [
                    { id: "rotational_std", title: "保养标准", url: "#!/maintenance_mgmt/rotational_maintenance/rotational_std", component: "./views/MaintenanceStandard.js" },
                    { id: "rotational_plan", title: "生成计划", url: "#!/maintenance_mgmt/rotational_maintenance/rotational_plan", component: "./views/Placeholder.js" },
                    { id: "rotational_exec", title: "执行任务", url: "#!/maintenance_mgmt/rotational_maintenance/rotational_exec", component: "./views/Placeholder.js" },
                    { id: "rotational_check", title: "任务检查", url: "#!/maintenance_mgmt/rotational_maintenance/rotational_check", component: "./views/Placeholder.js" },
                    { id: "rotational_result", title: "执行结果", url: "#!/maintenance_mgmt/rotational_maintenance/rotational_result", component: "./views/Placeholder.js" },
                ]
            },
            {
                id: "monthly_maintenance",
                title: "精深月保",
                icon: "bi-calendar-month",
                children: [
                    { id: "monthly_std", title: "保养标准", url: "#!/maintenance_mgmt/monthly_maintenance/monthly_std", component: "./views/MaintenanceStandard.js" },
                    { id: "monthly_plan", title: "生成计划", url: "#!/maintenance_mgmt/monthly_maintenance/monthly_plan", component: "./views/Placeholder.js" },
                    { id: "monthly_exec", title: "执行任务", url: "#!/maintenance_mgmt/monthly_maintenance/monthly_exec", component: "./views/Placeholder.js" },
                    { id: "monthly_check", title: "任务检查", url: "#!/maintenance_mgmt/monthly_maintenance/monthly_check", component: "./views/Placeholder.js" },
                    { id: "monthly_result", title: "执行结果", url: "#!/maintenance_mgmt/monthly_maintenance/monthly_result", component: "./views/Placeholder.js" },
                ]
            },
            {
                id: "professional_check",
                title: "专业点检",
                icon: "bi-check-circle-fill",
                children: [
                    { id: "prof_check_std", title: "保养标准", url: "#!/maintenance_mgmt/professional_check/prof_check_std", component: "./views/ProfessionalCheckStandard.js" },
                    { id: "prof_check_plan", title: "生成计划", url: "#!/maintenance_mgmt/professional_check/prof_check_plan", component: "./views/Placeholder.js" },
                    { id: "prof_check_exec", title: "执行任务", url: "#!/maintenance_mgmt/professional_check/prof_check_exec", component: "./views/Placeholder.js" },
                    { id: "prof_check_check", title: "任务检查", url: "#!/maintenance_mgmt/professional_check/prof_check_check", component: "./views/Placeholder.js" },
                    { id: "prof_check_result", title: "执行结果", url: "#!/maintenance_mgmt/professional_check/prof_check_result", component: "./views/Placeholder.js" },
                ]
            },
            {
                id: "mechanical_lubrication",
                title: "机械润滑",
                icon: "bi-water",
                children: [
                    { id: "mech_lube_std", title: "保养标准", url: "#!/maintenance_mgmt/mechanical_lubrication/mech_lube_std", component: "./views/MechanicalLubricationStandard.js" },
                    { id: "mech_lube_plan", title: "生成计划", url: "#!/maintenance_mgmt/mechanical_lubrication/mech_lube_plan", component: "./views/Placeholder.js" },
                    { id: "mech_lube_exec", title: "执行任务", url: "#!/maintenance_mgmt/mechanical_lubrication/mech_lube_exec", component: "./views/Placeholder.js" },
                    { id: "mech_lube_check", title: "任务检查", url: "#!/maintenance_mgmt/mechanical_lubrication/mech_lube_check", component: "./views/Placeholder.js" },
                    { id: "mech_lube_result", title: "执行结果", url: "#!/maintenance_mgmt/mechanical_lubrication/mech_lube_result", component: "./views/Placeholder.js" },
                ]
            },
            { id: "maintenance_recovery", title: "保后回复", icon: "bi-bar-chart-steps", url: "#!/maintenance_mgmt/maintenance_recovery", component: "./views/Placeholder.js" },
            { id: "maintenance_history", title: "保养历史", icon: "bi-bar-chart-line", url: "#!/maintenance_mgmt/maintenance_history", component: "./views/Placeholder.js" },
        ]
    }
];

export default menuConfig;

