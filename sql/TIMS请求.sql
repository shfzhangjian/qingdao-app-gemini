-- ============================================================
-- TMIS_DATA 配置更新 (匹配 D1 版本接口文档)
-- 注意：我们需要在表中增加 METHOD 字段来区分请求方式，或者在代码中硬编码
-- 这里假设我们简单扩展逻辑：如果 FIXED_PARAMS 中包含 "method":"GET"，则走 GET 逻辑
-- ============================================================

-- 1. 同步保养、点检、润滑任务完成情况
-- 对应 Kafka: tims.feedback.completed.maintenance.task
-- URL: http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/completed/task
-- Params: type=1 (日保为例), lastSyncDateTime=...
UPDATE TMIS_DATA
SET API_URL = 'http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/completed/task',
    FIXED_PARAMS = '{"method":"GET", "type":1}'
WHERE TOPIC = 'tims.feedback.completed.maintenance.task';

-- 2. 同步保养、点检、润滑任务完成得分
-- 对应 Kafka: tims.feedback.maintenance.task.score
-- URL: http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/task/score
UPDATE TMIS_DATA
SET API_URL = 'http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/task/score',
    FIXED_PARAMS = '{"method":"GET", "type":1}'
WHERE TOPIC = 'tims.feedback.maintenance.task.score';

-- 3. 同步TIMS智能推荐的预测性维修轮保任务 (接口6)
-- 对应 Kafka: tims.recommend.rotational.task
-- URL: http://tims.qd.com/ctmc-api/mro-edge-integration/rotational/sync/recommend/task
UPDATE TMIS_DATA
SET API_URL = 'http://tims.qd.com/ctmc-api/mro-edge-integration/rotational/sync/recommend/task',
    FIXED_PARAMS = '{"method":"GET"}'
WHERE TOPIC = 'tims.recommend.rotational.task';

-- 4. 同步轮保任务完成情况 (接口8)
-- 对应 Kafka: tims.feedback.completed.rotational.task
-- URL: http://tims.qd.com/ctmc-api/mro-edge-integration/rotational/sync/completed/task
UPDATE TMIS_DATA
SET API_URL = 'http://tims.qd.com/ctmc-api/mro-edge-integration/rotational/sync/completed/task',
    FIXED_PARAMS = '{"method":"GET"}'
WHERE TOPIC = 'tims.feedback.completed.rotational.task';

-- 5. 同步轮保任务完成得分 (接口9)
-- 对应 Kafka: tims.feedback.rotational.task.score
-- URL: http://tims.qd.com/ctmc-api/mro-edge-integration/rotational/sync/task/score
UPDATE TMIS_DATA
SET API_URL = 'http://tims.qd.com/ctmc-api/mro-edge-integration/rotational/sync/task/score',
    FIXED_PARAMS = '{"method":"GET"}'
WHERE TOPIC = 'tims.feedback.rotational.task.score';

-- 6. 同步停产检修计划任务完成情况 (接口14)
-- 对应 Kafka: tims.feedback.completed.production.halt.maintenance.task
-- URL: http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/completed/production-halt/task
UPDATE TMIS_DATA
SET API_URL = 'http://tims.qd.com/ctmc-api/mro-edge-integration/maintenance/sync/completed/production-halt/task',
    FIXED_PARAMS = '{"method":"GET"}'
WHERE TOPIC = 'tims.feedback.completed.production.halt.maintenance.task';

COMMIT;