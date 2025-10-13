package com.lucksoft.qingdao.tspm.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 提供执行动态SQL查询功能的服务.
 */
@Service
public class DynamicQueryService {

    private static final Logger logger = LoggerFactory.getLogger(DynamicQueryService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 执行给定的参数化SQL查询并返回指定行数的结果.
     * @param sql   用户提供的SQL查询语句 (可能包含?).
     * @param limit 要返回的最大行数.
     * @param params 与SQL中?对应的参数列表.
     * @return 查询结果列表.
     */
    public List<Map<String, Object>> executeQuery(String sql, int limit, List<Object> params) {
        logger.info("准备执行动态SQL查询...");
        logger.debug("原始SQL: {}", sql);
        logger.debug("参数: {}", params);
        logger.debug("查询行数限制: {}", limit);

        String wrappedSql = "SELECT * FROM (" + sql + ") WHERE ROWNUM <= ?";

        // 【关键改进】: 将用户参数和分页参数合并，并安全地传递给JdbcTemplate
        List<Object> allParams = new ArrayList<>();
        if (params != null) {
            allParams.addAll(params);
        }
        allParams.add(limit);

        logger.info("执行分页SQL: {}", wrappedSql);
        logger.debug("完整参数列表: {}", allParams);

        try {
            List<Map<String, Object>> results = jdbcTemplate.queryForList(wrappedSql, allParams.toArray());
            logger.info("查询成功，返回 {} 条记录。", results.size());

            List<Map<String, Object>> processedResults = new ArrayList<>();
            for (Map<String, Object> row : results) {
                processedResults.add(convertKeysToLowerCase(row));
            }
            return processedResults;

        } catch (Exception e) {
            logger.error("动态SQL查询执行失败！SQL: [{}], Params: [{}], Limit: [{}]", sql, params, limit, e);
            throw new RuntimeException("数据库查询执行失败，请检查后台日志获取详细错误信息。", e);
        }
    }

    private Map<String, Object> convertKeysToLowerCase(Map<String, Object> originalMap) {
        if (originalMap == null) {
            return null;
        }
        Map<String, Object> newMap = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : originalMap.entrySet()) {
            String key = entry.getKey();
            if (key != null) {
                newMap.put(key.toLowerCase(), entry.getValue());
            }
        }
        return newMap;
    }
}

