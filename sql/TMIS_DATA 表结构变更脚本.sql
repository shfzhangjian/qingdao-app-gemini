-- ============================================================
-- TMIS_DATA 表结构变更脚本
-- 增加 CRON_EXPRESSION 字段，用于自定义每个接口的补漏时间
-- ============================================================

ALTER TABLE TMIS_DATA ADD CRON_EXPRESSION VARCHAR2(500);

COMMENT ON COLUMN TMIS_DATA.CRON_EXPRESSION IS '定时补漏Cron表达式';

-- 初始化默认值 (例如: 每天 02:00 执行)
UPDATE TMIS_DATA SET CRON_EXPRESSION = '0 0 2 * * ?';

-- 针对特定接口设置演示值 (例如: 接口2 每天 14:00, 20:00, 22:00 执行)
-- 注意: Spring 的 CronTrigger 不支持在一个表达式里写多个时间点(如 "0 0 14,20,22 * * ?")
-- 如果需要一天执行多次，请使用标准的 Cron 语法，例如 "0 0 14,20,22 * * ?" 是合法的
UPDATE TMIS_DATA
SET CRON_EXPRESSION = '0 0 14,20,22 * * ?'
WHERE TOPIC = 'tims.feedback.completed.maintenance.task';

COMMIT;