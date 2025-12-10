-- ============================================================
-- 1. 新增：计量任务异常记录表
-- ============================================================
CREATE TABLE T_METROLOGY_EXCEPTION (
                                       ID NUMBER(19,0) NOT NULL,
                                       TASK_ID NUMBER(19,0),          -- 关联 JL_EQUIP_DXJ.INDOCNO
                                       EXCEPTION_DESC VARCHAR2(1000), -- 异常描述
                                       REPORT_TIME DATE DEFAULT SYSDATE,
                                       REPORTER_ID VARCHAR2(50),
                                       REPORTER_NAME VARCHAR2(100),
                                       CONSTRAINT PK_T_METROLOGY_EXCEPTION PRIMARY KEY (ID)
);

CREATE SEQUENCE SEQ_T_METROLOGY_EXCEPTION START WITH 1 INCREMENT BY 1;
CREATE INDEX IDX_METRO_EXC_TASK ON T_METROLOGY_EXCEPTION(TASK_ID);

-- ============================================================
-- 2. 修改：计量任务表 (JL_EQUIP_DXJ) 增加关联字段
-- ============================================================
-- 假设您的主任务表是 JL_EQUIP_DXJ
ALTER TABLE JL_EQUIP_DXJ ADD EXCEPTION_ID NUMBER(19,0);
COMMENT ON COLUMN JL_EQUIP_DXJ.EXCEPTION_ID IS '关联的异常记录ID';

COMMIT;

-- ============================================================
-- 3. 存储过程包: PKG_METROLOGY
-- ============================================================
CREATE OR REPLACE PACKAGE PKG_METROLOGY IS
  -- 批量提报异常
  PROCEDURE P_REPORT_BATCH_EXCEPTION(
    p_ids_str      IN VARCHAR2,  -- 逗号分隔的任务ID字符串 (e.g. "1001,1002")
    p_reason       IN VARCHAR2,  -- 异常描述
    p_login_id     IN VARCHAR2,  -- 操作人账号
    p_user_name    IN VARCHAR2,  -- 操作人姓名
    o_count        OUT NUMBER,   -- 成功处理的条数
    o_result_msg   OUT VARCHAR2  -- 返回消息
  );
END PKG_METROLOGY;
/

CREATE OR REPLACE PACKAGE BODY PKG_METROLOGY IS

  PROCEDURE P_REPORT_BATCH_EXCEPTION(
    p_ids_str      IN VARCHAR2,
    p_reason       IN VARCHAR2,
    p_login_id     IN VARCHAR2,
    p_user_name    IN VARCHAR2,
    o_count        OUT NUMBER,
    o_result_msg   OUT VARCHAR2
  ) IS
    TYPE t_id_list IS TABLE OF NUMBER;
    v_ids t_id_list;
    v_exc_id NUMBER;
BEGIN
    o_count := 0;

    -- 1. 将逗号分隔的字符串转换为集合 (假设兼容性写法)
    -- 注意：这里使用了简单的正则分割，如果 Oracle 版本较老可能需要其他 split 函数
SELECT REGEXP_SUBSTR(p_ids_str, '[^,]+', 1, LEVEL)
           BULK COLLECT INTO v_ids
FROM DUAL
    CONNECT BY REGEXP_SUBSTR(p_ids_str, '[^,]+', 1, LEVEL) IS NOT NULL;

-- 2. 遍历 ID 列表进行处理
IF v_ids.COUNT > 0 THEN
      FOR i IN v_ids.FIRST .. v_ids.LAST LOOP

        -- A. 生成异常记录 ID
SELECT SEQ_T_METROLOGY_EXCEPTION.NEXTVAL INTO v_exc_id FROM DUAL;

-- B. 插入异常表
INSERT INTO T_METROLOGY_EXCEPTION (
    ID, TASK_ID, EXCEPTION_DESC, REPORT_TIME, REPORTER_ID, REPORTER_NAME
) VALUES (
             v_exc_id, v_ids(i), p_reason, SYSDATE, p_login_id, p_user_name
         );

-- C. 更新任务主表 (JL_EQUIP_DXJ)
-- 状态更新为 '已检'(1)，结果 '异常'，并关联异常ID
UPDATE JL_EQUIP_DXJ
SET IDJSTATE = 1,                 -- 状态: 已检
    SCHECKRESULT = '异常',        -- 结果: 异常
    SCHECKREMARK = p_reason,      -- 备注同步
    EXCEPTION_ID = v_exc_id,      -- [关键] 关联异常ID
    DCHECK = SYSDATE,             -- 检查时间
    SCHECKUSER = p_user_name      -- 检查人
WHERE INDOCNO = v_ids(i);

o_count := o_count + 1;
END LOOP;
END IF;

    o_result_msg := 'SUCCESS';
COMMIT;

EXCEPTION
    WHEN OTHERS THEN
      ROLLBACK;
      o_count := 0;
      o_result_msg := 'ERROR: ' || SQLERRM;
END P_REPORT_BATCH_EXCEPTION;

END PKG_METROLOGY;
/