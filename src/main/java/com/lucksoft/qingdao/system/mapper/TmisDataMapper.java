package com.lucksoft.qingdao.system.mapper;

import com.lucksoft.qingdao.system.entity.TmisData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface TmisDataMapper {

    /**
     * [修改] 增加 CRON_EXPRESSION 字段查询
     */
    @Select("SELECT " +
            "    TOPIC, " +
            "    DESCRIPTION, " +
            "    Enabled,         " +
            "    LAST_UPDATE_TIME AS lastUpdateTime,  " +
            "    API_URL AS apiUrl,          " +
            "    FIXED_PARAMS AS fixedParams,    " +
            "    CRON_EXPRESSION AS cronExpression " +
            "FROM TMIS_DATA  WHERE ENABLED = 1")
    List<TmisData> findAllEnabled();

    @Select("SELECT " +
            "    TOPIC, " +
            "    DESCRIPTION, " +
            "    Enabled,         " +
            "    LAST_UPDATE_TIME AS lastUpdateTime,  " +
            "    API_URL AS apiUrl,          " +
            "    FIXED_PARAMS AS fixedParams,    " +
            "    CRON_EXPRESSION AS cronExpression " +
            "FROM TMIS_DATA ORDER BY TOPIC")
    List<TmisData> findAll();

    @Select("SELECT " +
            "    TOPIC, " +
            "    DESCRIPTION, " +
            "    Enabled,         " +
            "    LAST_UPDATE_TIME AS lastUpdateTime,  " +
            "    API_URL AS apiUrl,          " +
            "    FIXED_PARAMS AS fixedParams,    " +
            "    CRON_EXPRESSION AS cronExpression " +
            "FROM TMIS_DATA WHERE TOPIC = #{topic}")
    TmisData findByTopic(@Param("topic") String topic);

    @Update("UPDATE TMIS_DATA SET LAST_UPDATE_TIME = #{newTime} " +
            "WHERE TOPIC = #{topic} AND (LAST_UPDATE_TIME IS NULL OR LAST_UPDATE_TIME < #{newTime})")
    void updateLastTime(@Param("topic") String topic, @Param("newTime") String newTime);

    @Update("UPDATE TMIS_DATA SET ENABLED = #{Enabled} WHERE TOPIC = #{topic}")
    void updateStatus(@Param("topic") String topic, @Param("Enabled") Integer isEnabled);

    @Update("UPDATE TMIS_DATA SET LAST_UPDATE_TIME = #{newTime} WHERE TOPIC = #{topic}")
    void resetLastTime(@Param("topic") String topic, @Param("newTime") String newTime);

    /**
     * [新增] 更新 Cron 表达式
     */
    @Update("UPDATE TMIS_DATA SET CRON_EXPRESSION = #{cron} WHERE TOPIC = #{topic}")
    void updateCron(@Param("topic") String topic, @Param("cron") String cron);
}