package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.oracle.dto.VMaintenanceTaskDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * [新] MyBatis Mapper 接口
 * 专门用于查询 V_MAINTENANCE_TASKS_RECENT 视图。
 * 此 Mapper 假设连接的是主数据源 (PrimaryDataSource)。
 */
@Mapper
public interface VMaintenanceTasksMapper {

    /**
     * 查询 V_MAINTENANCE_TASKS_RECENT 视图中的所有数据。
     * 视图本身已经按最近24小时过滤，所以这里直接 SELECT *
     * @return VMaintenanceTaskDTO 列表
     */
    @Results(id = "vMaintenanceTaskResultMap", value = {
            @Result(property = "planId", column = "planId"),
            @Result(property = "taskId", column = "taskId"),
            @Result(property = "equipmentCode", column = "equipmentCode"),
            @Result(property = "type", column = "type"),
            @Result(property = "project", column = "project"),
            @Result(property = "content", column = "content"),
            @Result(property = "standard", column = "standard"),
            @Result(property = "tool", column = "tool"),
            @Result(property = "fullScore", column = "fullScore"),
            @Result(property = "createDateTime", column = "createDateTime"),
            @Result(property = "planStartTime", column = "planStartTime"),
            @Result(property = "operator", column = "operator"),
            @Result(property = "oilId", column = "oilId"),
            @Result(property = "oilQuantity", column = "oilQuantity"),
            @Result(property = "deDupeKey", column = "deDupeKey") // 映射防重键
    })
    @Select("SELECT * FROM V_MAINTENANCE_TASKS_RECENT")
    List<VMaintenanceTaskDTO> findRecentTasks();

}