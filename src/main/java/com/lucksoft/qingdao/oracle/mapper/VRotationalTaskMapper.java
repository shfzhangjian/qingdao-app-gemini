package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.oracle.dto.VRotationalTaskDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * [新] MyBatis Mapper 接口
 * 专门用于查询 V_ROTATIONAL_TASK_RECENT 视图。
 * 此 Mapper 假设连接的是主数据源 (PrimaryDataSource)。
 */
@Mapper
public interface VRotationalTaskMapper {

    /**
     * [新增] 通用查询接口
     * 对应 Topic: tims.sync.rotational.task
     */
    @ResultMap("vRotationalTaskResultMap")
    @Select("<script>" +
            "SELECT * FROM V_ROTATIONAL_TASK_RECENT " +
            "<where>" +
            "   <if test='updateTime != null and updateTime != \"\"'>" +
            "       AND \"createDateTime\" &gt;= #{updateTime}" +
            "   </if>" +
            "</where>" +
            "ORDER BY \"createDateTime\" DESC" +
            "</script>")
    List<VRotationalTaskDTO> findTasksByCondition(@Param("updateTime") String updateTime, @Param("params") Map<String, Object> params);


    /**
     * 查询 V_ROTATIONAL_TASK_RECENT 视图中的所有数据。
     * 视图本身已经按最近24小时过滤，所以这里直接 SELECT *
     * @return VRotationalTaskDTO 列表
     */
    @Results(id = "vRotationalTaskResultMap", value = {
            @Result(property = "planId", column = "planId"),
            @Result(property = "taskId", column = "taskId"),
            @Result(property = "equipmentCode", column = "equipmentCode"),
            @Result(property = "project", column = "project"),
            @Result(property = "content", column = "content"),
            @Result(property = "standard", column = "standard"),
            @Result(property = "source", column = "source"),
            @Result(property = "fullScore", column = "fullScore"),
            @Result(property = "createDateTime", column = "createDateTime"),
            @Result(property = "operator", column = "operator"),
            @Result(property = "deDupeKey", column = "deDupeKey") // 映射防重键
    })
    @Select("SELECT * FROM V_ROTATIONAL_TASK_RECENT")
    List<VRotationalTaskDTO> findRecentTasks();

}