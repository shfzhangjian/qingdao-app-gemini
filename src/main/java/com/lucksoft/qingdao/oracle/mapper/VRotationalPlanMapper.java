package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.oracle.dto.VRotationalPlanDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * [新] MyBatis Mapper 接口
 * 专门用于查询 V_ROTATIONAL_PLANS_RECENT 视图。
 * 此 Mapper 假设连接的是主数据源 (PrimaryDataSource)。
 */
@Mapper
public interface VRotationalPlanMapper {

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