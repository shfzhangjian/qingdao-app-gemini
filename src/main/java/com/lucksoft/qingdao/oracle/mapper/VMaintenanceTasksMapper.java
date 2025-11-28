package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.oracle.dto.VMaintenanceTaskDTO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;
import java.util.Map;

/**
 * [新] MyBatis Mapper 接口
 * 专门用于查询 V_MAINTENANCE_TASKS_RECENT 视图。
 * 此 Mapper 假设连接的是主数据源 (PrimaryDataSource)。
 */
@Mapper
public interface VMaintenanceTasksMapper {

    /**
     * [新增] 通用查询接口 (支持分页和时间过滤)
     * 对应 Topic: tims.sync.maintenance.task
     */
    @ResultMap("vMaintenanceTaskResultMap")
    @Select("<script>" +
            "SELECT * FROM V_MAINTENANCE_TASKS_RECENT " +
            "<where>" +
            "   <if test='updateTime != null and updateTime != \"\"'>" +
            "       AND \"createDateTime\" &gt;= #{updateTime}" +
            "   </if>" +
            "   <if test='params != null'>" +
            "       <if test='params.equipmentCode != null and params.equipmentCode != \"\"'>" +
            "           AND \"equipmentCode\" LIKE '%' || #{params.equipmentCode} || '%'" +
            "       </if>" +
            "   </if>" +
            "</where>" +
            "ORDER BY \"createDateTime\" DESC" +
            "</script>")
    List<VMaintenanceTaskDTO> findTasksByCondition(@Param("updateTime") String updateTime, @Param("params") Map<String, Object> params);


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

    /**
     * [新增] 调用无参数存储过程 tmis.genlb
     */
    @Update("{CALL tmis.genlb()}")
    @Options(statementType = StatementType.CALLABLE)
    void callGenLbProcedure();

    /**
     * [新增] 查询 view_lb_task 视图
     * 字段结构与 V_MAINTENANCE_TASKS_RECENT 一致，复用 ResultMap
     */
    @ResultMap("vMaintenanceTaskResultMap")
    @Select("SELECT * FROM view_lb_task")
    List<VMaintenanceTaskDTO> findLbTasks();
}