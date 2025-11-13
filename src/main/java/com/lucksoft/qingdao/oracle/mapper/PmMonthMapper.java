package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.tspm.dto.ProductionHaltTaskDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * [已重构]
 * MyBatis Mapper 接口, 用于查询维修计划 (PM_MONTH) 及其明细 (PM_MONTH_ITEM)
 * 1. 移除了 findMainByIndocno 和 findItemsByIlinkno
 * 2. 新增 findTasksFromViewByIndocno, 直接查询 v_pm_month_item 视图
 */
@Mapper
public interface PmMonthMapper {

    /**
     * [新] 接口 13: (停产检修)
     * 根据主表 INDOCNO, 直接从视图 v_pm_month_item 查询已转换的 DTO 列表
     *
     * @param indocno 主表ID (PM_MONTH.INDOCNO)
     * @return List<ProductionHaltTaskDTO>
     */
    @Results(id = "productionHaltTaskResultMap", value = {
            // 视图列名 -> DTO 属性名
            @Result(property = "taskId", column = "TASKID"),
            @Result(property = "equipmentCode", column = "EQUIPMENTCODE"),
            @Result(property = "content", column = "CONTENT"),
            @Result(property = "head", column = "HEAD"),
            @Result(property = "executor", column = "EXECUTOR"),
            @Result(property = "planStartTime", column = "PLANSTARTTIME"),
            @Result(property = "planEndTime", column = "PLANENDTIME"),
            @Result(property = "teamName", column = "TEAMNAME")
    })
    @Select("SELECT * FROM v_pm_month_item WHERE indocno = #{indocno}")
    List<ProductionHaltTaskDTO> findTasksFromViewByIndocno(@Param("indocno") Long indocno);

}