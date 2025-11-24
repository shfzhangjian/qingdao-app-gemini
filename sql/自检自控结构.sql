-- ============================================================
-- 清理自检自控模块数据结构 (用于重置调试)
-- ============================================================

-- 1. 删除表 (注意删除顺序：先删子表，再删主表，防止外键约束报错)
-- 虽然结构定义中未显式添加外键约束，但保持此习惯是好的

-- 删除任务明细表
DROP TABLE T_SI_TASK_DETAIL;

-- 删除任务主表
DROP TABLE T_SI_TASK;

-- 删除标准明细表
DROP TABLE T_SI_STANDARD;

-- 删除台账主表
DROP TABLE T_SI_LEDGER;

-- 2. 删除序列

DROP SEQUENCE SEQ_SI_TASK_DETAIL;
DROP SEQUENCE SEQ_SI_TASK;
DROP SEQUENCE SEQ_SI_STANDARD;
DROP SEQUENCE SEQ_SI_LEDGER;

COMMIT;

-- ============================================================
-- 1. 自检自控台账表 (T_SI_LEDGER)
-- ============================================================
CREATE TABLE T_SI_LEDGER (
                             ID NUMBER(19,0) NOT NULL,
                             WORKSHOP VARCHAR2(100),          -- 车间
                             NAME VARCHAR2(200),              -- 名称
                             MODEL VARCHAR2(100),             -- 所属机型
                             DEVICE VARCHAR2(100),            -- 所属设备
                             MAIN_DEVICE VARCHAR2(200),       -- 所属设备主数据名称
                             FACTORY VARCHAR2(100),           -- 厂家
                             SPEC VARCHAR2(100),              -- 规格型号
                             LOCATION VARCHAR2(200),          -- 安装位置
                             PRINCIPLE VARCHAR2(200),         -- 测量原理
                             PM_CODE VARCHAR2(100),           -- PM设备编码
                             ORDER_NO VARCHAR2(100),          -- 订单号
                             ASSET_CODE VARCHAR2(100),        -- 资产编码
                             FIRST_USE_DATE DATE,             -- 初次使用时间
                             AUDIT_STATUS VARCHAR2(50) DEFAULT '草稿', -- 审批状态
                             HAS_STANDARD NUMBER(1,0) DEFAULT 0,       -- 是否上传标准 (0:否, 1:是)
                             CREATE_TIME DATE DEFAULT SYSDATE,
                             UPDATE_TIME DATE DEFAULT SYSDATE,
                             CONSTRAINT PK_T_SI_LEDGER PRIMARY KEY (ID)
);

-- 序列
CREATE SEQUENCE SEQ_SI_LEDGER START WITH 1 INCREMENT BY 1;

COMMENT ON TABLE T_SI_LEDGER IS '自检自控-设备台账表';

-- ============================================================
-- 2. 点检标准明细表 (T_SI_STANDARD) - 用于存储导入的Excel内容
-- ============================================================
CREATE TABLE T_SI_STANDARD (
                               ID NUMBER(19,0) NOT NULL,
                               LEDGER_ID NUMBER(19,0) NOT NULL, -- 关联台账ID
                               DEVICE_PART VARCHAR2(100),       -- 检测装置 (SJCZZ)
                               ITEM_NAME VARCHAR2(200),         -- 检测项目 (SJCXM)
                               STANDARD_DESC VARCHAR2(500),     -- 检测标准 (SJCBZ)
                               EXECUTOR_ROLE VARCHAR2(100),     -- 执行人 (SEXUSER)
                               CHECK_CYCLE NUMBER(10,0),        -- 检查周期(天)
                               NEXT_EXEC_DATE DATE,             -- 下次执行时间
                               CONSTRAINT PK_T_SI_STANDARD PRIMARY KEY (ID)
);
CREATE SEQUENCE SEQ_SI_STANDARD START WITH 1 INCREMENT BY 1;
CREATE INDEX IDX_SI_STD_LEDGER ON T_SI_STANDARD(LEDGER_ID);

-- ============================================================
-- 3. 点检任务主表 (T_SI_TASK)
-- ============================================================
CREATE TABLE T_SI_TASK (
                           ID NUMBER(19,0) NOT NULL,
                           LEDGER_ID NUMBER(19,0),          -- 关联台账ID (可选，用于回溯)
                           MODEL VARCHAR2(100),             -- 所属机型 (冗余快照)
                           DEVICE VARCHAR2(100),            -- 所属设备 (冗余快照)
                           PROD_STATUS VARCHAR2(50),        -- 生产状态
                           SHIFT_TYPE VARCHAR2(50),         -- 班别 (甲/乙/丙/白)
                           SHIFT VARCHAR2(50),              -- 班次 (早/中/晚)
                           CHECK_STATUS VARCHAR2(50) DEFAULT '待检', -- 点检状态
                           CONFIRM_STATUS VARCHAR2(50) DEFAULT '待确认', -- 确认状态
                           TASK_TIME DATE,                  -- 任务日期 (应检日期)
                           TASK_TYPE VARCHAR2(50),          -- 任务类型 (三班电气/年检等)
                           IS_OVERDUE VARCHAR2(10) DEFAULT '否',

                           CHECKER VARCHAR2(100),           -- 检查人
                           CHECK_TIME DATE,                 -- 实际检查时间

                           CONFIRMER VARCHAR2(100),         -- 确认人
                           CONFIRM_TIME DATE,               -- 确认时间

                           CREATE_TIME DATE DEFAULT SYSDATE,
                           CONSTRAINT PK_T_SI_TASK PRIMARY KEY (ID)
);
CREATE SEQUENCE SEQ_SI_TASK START WITH 1 INCREMENT BY 1;
CREATE INDEX IDX_SI_TASK_TIME ON T_SI_TASK(TASK_TIME);

-- ============================================================
-- 4. 点检任务执行明细表 (T_SI_TASK_DETAIL)
-- ============================================================
CREATE TABLE T_SI_TASK_DETAIL (
                                  ID NUMBER(19,0) NOT NULL,
                                  TASK_ID NUMBER(19,0) NOT NULL,   -- 关联任务ID
                                  STANDARD_ID NUMBER(19,0),        -- 关联标准ID (可选)

    -- 快照字段，防止标准变更影响历史记录
                                  MAIN_DEVICE VARCHAR2(200),       -- 所属设备主数据名称
                                  ITEM_NAME VARCHAR2(200),         -- 检查项目名

                                  CHECK_RESULT VARCHAR2(50),       -- 检查结果 (正常/异常/不用)
                                  CHECK_REMARKS VARCHAR2(500),     -- 检查说明
                                  CHECK_TIME DATE,                 -- 单项检查时间

                                  IS_CONFIRMED NUMBER(1,0) DEFAULT 0, -- 是否确认 (0:否, 1:是)
                                  CONFIRM_TIME DATE,               -- 单项确认时间

                                  CONSTRAINT PK_T_SI_TASK_DETAIL PRIMARY KEY (ID)
);
CREATE SEQUENCE SEQ_SI_TASK_DETAIL START WITH 1 INCREMENT BY 1;
CREATE INDEX IDX_SI_DETAIL_TASK ON T_SI_TASK_DETAIL(TASK_ID);


-- ============================================================
-- 自检自控结构变更：为任务主表添加人员详细信息字段
-- ============================================================

-- 添加检查人 ID 和 工号
ALTER TABLE T_SI_TASK ADD CHECKER_ID NUMBER(19,0);
ALTER TABLE T_SI_TASK ADD CHECKER_NO VARCHAR2(50);

-- 添加确认人 ID 和 工号
ALTER TABLE T_SI_TASK ADD CONFIRMER_ID NUMBER(19,0);
ALTER TABLE T_SI_TASK ADD CONFIRMER_NO VARCHAR2(50);

-- 添加注释
COMMENT ON COLUMN T_SI_TASK.CHECKER_ID IS '检查人ID';
COMMENT ON COLUMN T_SI_TASK.CHECKER_NO IS '检查人工号';
COMMENT ON COLUMN T_SI_TASK.CONFIRMER_ID IS '确认人ID';
COMMENT ON COLUMN T_SI_TASK.CONFIRMER_NO IS '确认人工号';

COMMIT;