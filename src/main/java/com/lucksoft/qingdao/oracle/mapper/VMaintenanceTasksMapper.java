package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.oracle.dto.VMaintenanceTaskDTO;
import org.apache.ibatis.annotations.*;
import org.apache.ibatis.mapping.StatementType;

import java.util.List;
import java.util.Map;

/**
 * [已修复] MyBatis Mapper 接口
 * 专门用于查询 V_MAINTENANCE_TASKS_RECENT 视图。
 * 修复了忽略 'type' 过滤条件导致返回所有类型数据的问题。
 * * 更新：支持按类型动态路由到不同视图 (V_MAINTENANCE_TASKS_RECENT_{type})。
 */
@Mapper
public interface VMaintenanceTasksMapper {

    /**
     * [已修复] 通用查询接口 (支持分页、时间过滤和类型动态视图路由)
     * 对应 Topic: tims.sync.maintenance.task
     * * 逻辑变更：
     * 1. 如果 params.types 仅包含 1 个类型，尝试查询对应的分视图 V_MAINTENANCE_TASKS_RECENT_{type}。
     * 2. 否则，查询总视图 V_MAINTENANCE_TASKS_RECENT 并使用 WHERE type IN (...) 过滤。
     */
    @ResultMap("vMaintenanceTaskResultMap")
    @Select("<script>" +
            "<choose>" +
            // 情况1: 如果只指定了唯一的 type，则查询对应的分视图 V_MAINTENANCE_TASKS_RECENT_{type}
            "   <when test='params != null and params.types != null and params.types.size() == 1'>" +
            "       SELECT * FROM V_MAINTENANCE_TASKS_RECENT_${params.types[0]} " +
            "       <where>" +
            "           <if test='updateTime != null and updateTime != \"\"'>" +
            "               AND \"createDateTime\" &gt;= #{updateTime}" +
            "           </if>" +
            "           <if test='params.equipmentCode != null and params.equipmentCode != \"\"'>" +
            "               AND \"equipmentCode\" LIKE '%' || #{params.equipmentCode} || '%'" +
            "           </if>" +
            "       </where>" +
            "   </when>" +
            // 情况2: 否则 (未指定类型 或 指定了多个类型)，查询总视图并进行 WHERE 过滤
            "   <otherwise>" +
            "       SELECT * FROM V_MAINTENANCE_TASKS_RECENT " +
            "       <where>" +
            "           <if test='updateTime != null and updateTime != \"\"'>" +
            "               AND \"createDateTime\" &gt;= #{updateTime}" +
            "           </if>" +
            "           <if test='params != null'>" +
            "               <if test='params.equipmentCode != null and params.equipmentCode != \"\"'>" +
            "                   AND \"equipmentCode\" LIKE '%' || #{params.equipmentCode} || '%'" +
            "               </if>" +
            "               <if test='params.types != null and params.types.size() > 0'>" +
            "                   AND \"type\" IN " +
            "                   <foreach item='item' index='index' collection='params.types' open='(' separator=',' close=')'>" +
            "                       #{item}" +
            "                   </foreach>" +
            "               </if>" +
            "           </if>" +
            "       </where>" +
            "   </otherwise>" +
            "</choose>" +
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