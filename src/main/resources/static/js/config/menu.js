/**
 * @file /js/config/menu.js
 * @description 应用程序的导航菜单配置文件
 * @version 1.1.0 - 2025-10-13 - Gemini - 添加文件头注释
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
            { id: "plan", title: "维保计划", url: "#!/maintenance_mgmt/plan", component: "./views/Placeholder.js" }
        ]
    }
];

export default menuConfig;

