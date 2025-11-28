package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.oracle.dto.VRotationalPlanDTO;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * [新] MyBatis Mapper 接口
 * 专门用于查询 V_ROTATIONAL_PLANS_RECENT 视图。
 * 此 Mapper 假设连接的是主数据源 (PrimaryDataSource)。
 */
@Mapper
public interface VRotationalPlanMapper {

    /**
     * [新增] 通用查询接口
     * 对应 Topic: tims.sync.rotational.plan
     */
    @ResultMap("vRotationalPlanResultMap")
    @Select("<script>" +
            "SELECT * FROM V_ROTATIONAL_PLANS_RECENT " +
            "<where>" +
            "   <if test='updateTime != null and updateTime != \"\"'>" +
            "       AND \"createDate\" &gt;= #{updateTime}" +
            "   </if>" +
            "</where>" +
            "ORDER BY \"createDate\" DESC" +
            "</script>")
    List<VRotationalPlanDTO> findPlansByCondition(@Param("updateTime") String updateTime, @Param("params") Map<String, Object> params);


    /**
     * 查询 V_ROTATIONAL_PLANS_RECENT 视图中的所有数据。
     * 视图本身已经按最近24小时过滤，所以这里直接 SELECT *
     * @return VRotationalPlanDTO 列表
     */
    @Results(id = "vRotationalPlanResultMap", value = {
            @Result(property = "planId", column = "planId"),
            @Result(property = "equipmentCode", column = "equipmentCode"),
            @Result(property = "planDate", column = "planDate"),
            @Result(property = "createDate", column = "createDate"),
            @Result(property = "deDupeKey", column = "deDupeKey") // 映射防重键
    })
    @Select("SELECT * FROM V_ROTATIONAL_PLANS_RECENT")
    List<VRotationalPlanDTO> findRecentTasks();

}