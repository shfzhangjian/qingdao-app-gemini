package com.lucksoft.qingdao.kb.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.StatementType;

import java.util.Map;

@Mapper
public interface KanbanTaskMapper {

    /**
     * 调用存储过程 PKG_KANBAN.P_REPORT_EXCEPTION
     * @param params 包含 taskId, reason, user 以及输出参数
     */
    @Select(value = "{call PKG_KANBAN.P_REPORT_EXCEPTION(" +
            "#{taskId, mode=IN, jdbcType=VARCHAR}, " +
            "#{reason, mode=IN, jdbcType=VARCHAR}, " +
            "#{user, mode=IN, jdbcType=VARCHAR}, " +
            "#{outCode, mode=OUT, jdbcType=INTEGER}, " +
            "#{outMsg, mode=OUT, jdbcType=VARCHAR})}")
    @Options(statementType = StatementType.CALLABLE)
    void callReportException(Map<String, Object> params);
}