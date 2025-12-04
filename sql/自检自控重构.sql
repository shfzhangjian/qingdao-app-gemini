-- ============================================================
-- 1. 清理旧表 (如果存在)
-- ============================================================
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE ZJZK_TASK_DETAIL';
EXCEPTION WHEN OTHERS THEN NULL; END;
/
BEGIN
EXECUTE IMMEDIATE 'DROP TABLE ZJZK_TASK';
EXCEPTION WHEN OTHERS THEN NULL; END;
/

-- ============================================================
-- 2. 自检自控任务主表: ZJZK_TASK
-- 按设备(SBNAME/SFNAME)聚合生成的任务单
-- ============================================================
CREATE TABLE ZJZK_TASK
(
    INDOCNO        NUMBER NOT NULL,   -- 主键
    TASK_NO        VARCHAR2(100),     -- 任务编号 (自动生成)

    -- 任务属性 (从弹窗表单获取)
    TASK_TIME      DATE,              -- 任务时间 (YYYY-MM-DD HH:mm:ss)
    TASK_TYPE      VARCHAR2(50),      -- 任务类型 (三班电气/白班/年检)
    PROD_STATUS    VARCHAR2(50),      -- 生产状态 (生产/停产)
    SHIFT_TYPE     VARCHAR2(50),      -- 班别 (甲/乙/丙/白)
    SHIFT          VARCHAR2(50),      -- 班次 (早/中/夜)

    -- 设备信息 (从 ZJZK_TOOL 分组获取)
    SJX            VARCHAR2(500),     -- 所属机型
    SFNAME         VARCHAR2(500),     -- 所属设备
    SBNAME         VARCHAR2(500),     -- 所属设备主数据名称
    SPMCODE        VARCHAR2(500),     -- PM设备编码

    -- 状态与人员
    CHECK_STATUS   VARCHAR2(50) DEFAULT '待检',    -- 点检状态
    CONFIRM_STATUS VARCHAR2(50) DEFAULT '待确认',  -- 确认状态
    IS_OVERDUE     VARCHAR2(10) DEFAULT '否',      -- 是否超期

    CHECKER        VARCHAR2(100),     -- 检查人
    CHECK_TIME     DATE,              -- 检查时间
    CONFIRMER      VARCHAR2(100),     -- 确认人
    CONFIRM_TIME   DATE,              -- 确认时间

    CREATE_TIME    DATE DEFAULT SYSDATE,
    CONSTRAINT PK_ZJZK_TASK PRIMARY KEY (INDOCNO)
) TABLESPACE USERS;

CREATE SEQUENCE SEQ_ZJZK_TASK START WITH 1 INCREMENT BY 1;

-- ============================================================
-- 3. 自检自控任务明细表: ZJZK_TASK_DETAIL
-- 对应具体的 ZJZK_TOOL 台账记录
-- ============================================================
CREATE TABLE ZJZK_TASK_DETAIL
(
    INDOCNO        NUMBER NOT NULL,   -- 主键
    TASK_ID        NUMBER NOT NULL,   -- 关联主表 ZJZK_TASK.INDOCNO
    TOOL_ID        NUMBER,            -- 关联台账 ZJZK_TOOL.INDOCNO (外键引用)

    -- 核心检查项 (取自 ZJZK_TOOL.SAZWZ)
    ITEM_NAME      VARCHAR2(500),     -- 点检项目名 (安装位置)

    -- 执行结果
    CHECK_RESULT   VARCHAR2(50),      -- 检查结果 (正常/异常/不用)
    CHECK_REMARK   VARCHAR2(500),     -- 检查说明
    IS_CONFIRMED   NUMBER(1) DEFAULT 0, -- 是否确认 (0:否 1:是)

    -- 详细操作记录
    OPERATOR_NAME  VARCHAR2(100),     -- 检查操作人
    OP_TIME        DATE,              -- 检查操作时间
    CONFIRM_NAME   VARCHAR2(100),     -- 确认人
    CONFIRM_OP_TIME DATE,             -- 确认操作时间

    CONSTRAINT PK_ZJZK_TASK_DETAIL PRIMARY KEY (INDOCNO)
) TABLESPACE USERS;

CREATE SEQUENCE SEQ_ZJZK_TASK_DETAIL START WITH 1 INCREMENT BY 1;
CREATE INDEX IDX_ZJZK_DETAIL_TASK ON ZJZK_TASK_DETAIL(TASK_ID);
CREATE INDEX IDX_ZJZK_DETAIL_TOOL ON ZJZK_TASK_DETAIL(TOOL_ID);