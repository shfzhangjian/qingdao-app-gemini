package com.lucksoft.qingdao.system.mapper;

import com.lucksoft.qingdao.system.entity.TmisData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TmisDataMapper {

    @Select("SELECT " +
            "    TOPIC, " +
            "    DESCRIPTION, " +
            "    Enabled,         " +
            "    LAST_UPDATE_TIME AS lastUpdateTime,  " +
            "    API_URL AS apiUrl,          " +
            "    FIXED_PARAMS AS fixedParams    " +
            "FROM TMIS_DATA  WHERE ENABLED = 1")
    List<TmisData> findAllEnabled();

    /**
     * [新增] 查询所有配置（包括禁用的），用于管理界面展示
     */
    @Select("SELECT " +
            "    TOPIC, " +
            "    DESCRIPTION, " +
            "    Enabled,         " +
            "    LAST_UPDATE_TIME AS lastUpdateTime,  " +
            "    API_URL AS apiUrl,          " +
            "    FIXED_PARAMS AS fixedParams    " +
            "FROM TMIS_DATA ORDER BY TOPIC")
    List<TmisData> findAll();

    @Select("SELECT " +
            "    TOPIC, " +
            "    DESCRIPTION, " +
            "    Enabled,         " +
            "    LAST_UPDATE_TIME AS lastUpdateTime,  " +
            "    API_URL AS apiUrl,          " +
            "    FIXED_PARAMS AS fixedParams    " +
            "FROM TMIS_DATA WHERE TOPIC = #{topic}")
    TmisData findByTopic(@Param("topic") String topic);

    /**
     * 更新最后更新时间
     * 只有当新的时间大于旧的时间时才更新，保证水位线单调递增
     */
    @Update("UPDATE TMIS_DATA SET LAST_UPDATE_TIME = #{newTime} " +
            "WHERE TOPIC = #{topic} AND (LAST_UPDATE_TIME IS NULL OR LAST_UPDATE_TIME < #{newTime})")
    void updateLastTime(@Param("topic") String topic, @Param("newTime") String newTime);

    /**
     * [新增] 更新配置状态（启用/禁用）
     */
    @Update("UPDATE TMIS_DATA SET ENABLED = #{Enabled} WHERE TOPIC = #{topic}")
    void updateStatus(@Param("topic") String topic, @Param("Enabled") Integer isEnabled);

    /**
     * [新增] 手动重置最后更新时间（例如用于重新同步历史数据）
     */
    @Update("UPDATE TMIS_DATA SET LAST_UPDATE_TIME = #{newTime} WHERE TOPIC = #{topic}")
    void resetLastTime(@Param("topic") String topic, @Param("newTime") String newTime);
}