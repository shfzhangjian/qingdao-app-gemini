-- ============================================================
-- TMIS_DATA 表结构
-- 用于记录接口配置和最后更新时间，实现数据补漏
-- ============================================================

-- 如果表已存在则删除 (慎用)
-- DROP TABLE TMIS_DATA;

CREATE TABLE TMIS_DATA (
                           TOPIC VARCHAR2(200) NOT NULL,          -- Kafka 主题名 (主键)
                           LAST_UPDATE_TIME VARCHAR2(30),         -- 最后更新时间 (yyyy-MM-dd HH:mm:ss)
                           API_URL VARCHAR2(500),                 -- 补漏调用的接口 URL
                           FIXED_PARAMS VARCHAR2(2000),           -- 固定参数 (JSON 字符串)
                           DESCRIPTION VARCHAR2(200),             -- 描述
                           IS_ENABLED NUMBER(1) DEFAULT 1,        -- 是否启用补漏 (1:启用, 0:禁用)
                           CONSTRAINT PK_TMIS_DATA PRIMARY KEY (TOPIC)
);

COMMENT ON TABLE TMIS_DATA IS 'TMIS接口配置与水位线表';
COMMENT ON COLUMN TMIS_DATA.TOPIC IS 'Kafka主题';
COMMENT ON COLUMN TMIS_DATA.LAST_UPDATE_TIME IS '最后同步时间';
COMMENT ON COLUMN TMIS_DATA.API_URL IS '补漏接口URL';
COMMENT ON COLUMN TMIS_DATA.FIXED_PARAMS IS '查询时的Body参数';

-- ============================================================
-- 初始化数据 (针对 8 个消费接口)
-- 假设 API URL 都是本机的查询接口
-- ============================================================

INSERT INTO TMIS_DATA (TOPIC, LAST_UPDATE_TIME, API_URL, FIXED_PARAMS, DESCRIPTION)
VALUES ('tims.feedback.completed.maintenance.task', '2023-01-01 00:00:00', 'http://localhost:8091/tmis/api/tmis/data/query', '{"taskId": ""}', '接口2: 任务完成情况');

INSERT INTO TMIS_DATA (TOPIC, LAST_UPDATE_TIME, API_URL, FIXED_PARAMS, DESCRIPTION)
VALUES ('tims.feedback.maintenance.task.score', '2023-01-01 00:00:00', 'http://localhost:8091/tmis/api/tmis/data/query', '{"taskId": ""}', '接口3: 任务得分');

INSERT INTO TMIS_DATA (TOPIC, LAST_UPDATE_TIME, API_URL, FIXED_PARAMS, DESCRIPTION)
VALUES ('tims.create.fault.report', '2023-01-01 00:00:00', 'http://localhost:8091/tmis/api/tmis/data/query', '{"equipmentCode": ""}', '接口4/10: 故障报告');

INSERT INTO TMIS_DATA (TOPIC, LAST_UPDATE_TIME, API_URL, FIXED_PARAMS, DESCRIPTION)
VALUES ('tims.recommend.rotational.task', '2023-01-01 00:00:00', 'http://localhost:8091/tmis/api/tmis/data/query', '{"planId": ""}', '接口6: 推荐任务');

INSERT INTO TMIS_DATA (TOPIC, LAST_UPDATE_TIME, API_URL, FIXED_PARAMS, DESCRIPTION)
VALUES ('tims.feedback.completed.rotational.task', '2023-01-01 00:00:00', 'http://localhost:8091/tmis/api/tmis/data/query', '{"taskId": ""}', '接口8: 轮保完成');

INSERT INTO TMIS_DATA (TOPIC, LAST_UPDATE_TIME, API_URL, FIXED_PARAMS, DESCRIPTION)
VALUES ('tims.feedback.rotational.task.score', '2023-01-01 00:00:00', 'http://localhost:8091/tmis/api/tmis/data/query', '{"taskId": ""}', '接口9: 轮保得分');

INSERT INTO TMIS_DATA (TOPIC, LAST_UPDATE_TIME, API_URL, FIXED_PARAMS, DESCRIPTION)
VALUES ('tims.create.fault.analysis.report', '2023-01-01 00:00:00', 'http://localhost:8091/tmis/api/tmis/data/query', '{}', '接口11: 故障分析');

INSERT INTO TMIS_DATA (TOPIC, LAST_UPDATE_TIME, API_URL, FIXED_PARAMS, DESCRIPTION)
VALUES ('tims.feedback.completed.production.halt.maintenance.task', '2023-01-01 00:00:00', 'http://localhost:8091/tmis/api/tmis/data/query', '{"taskId": ""}', '接口14: 停产检修完成');

COMMIT;