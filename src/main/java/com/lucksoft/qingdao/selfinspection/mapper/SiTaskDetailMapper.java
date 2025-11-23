package com.lucksoft.qingdao.selfinspection.mapper;

import com.lucksoft.qingdao.selfinspection.entity.SiTaskDetail;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 点检任务明细 Mapper
 * 对应表: T_SI_TASK_DETAIL
 */
@Mapper
public interface SiTaskDetailMapper {

    @Select("SELECT * FROM T_SI_TASK_DETAIL WHERE TASK_ID = #{taskId} ORDER BY ID ASC")
    @Results({
            @Result(property = "id", column = "ID", id = true),
            @Result(property = "taskId", column = "TASK_ID"),
            @Result(property = "mainDevice", column = "MAIN_DEVICE"),
            @Result(property = "itemName", column = "ITEM_NAME"),
            @Result(property = "result", column = "CHECK_RESULT"),
            @Result(property = "remarks", column = "CHECK_REMARKS"),
            @Result(property = "checkTime", column = "CHECK_TIME"),
            @Result(property = "isConfirmed", column = "IS_CONFIRMED"),
            @Result(property = "confirmTime", column = "CONFIRM_TIME")
    })
    List<SiTaskDetail> findByTaskId(Long taskId);

    // 批量插入通常使用 XML 映射更方便，这里使用动态 SQL script
    @Insert("<script>" +
            "INSERT INTO T_SI_TASK_DETAIL (ID, TASK_ID, MAIN_DEVICE, ITEM_NAME, CHECK_RESULT, CHECK_REMARKS, CHECK_TIME, IS_CONFIRMED, CONFIRM_TIME) " +
            "SELECT SEQ_SI_TASK_DETAIL.NEXTVAL, A.* FROM (" +
            "<foreach collection='list' item='item' separator='UNION ALL'>" +
            " SELECT #{item.taskId}, #{item.mainDevice}, #{item.itemName}, #{item.result}, #{item.remarks}, #{item.checkTime}, #{item.isConfirmed}, #{item.confirmTime} FROM DUAL" +
            "</foreach>" +
            ") A" +
            "</script>")
    int batchInsert(@Param("list") List<SiTaskDetail> details);

    // 更新单条明细
    @Update("UPDATE T_SI_TASK_DETAIL SET " +
            "CHECK_RESULT=#{result}, " +
            "CHECK_REMARKS=#{remarks}, " +
            "CHECK_TIME=#{checkTime}, " +
            "IS_CONFIRMED=#{isConfirmed}, " +
            "CONFIRM_TIME=#{confirmTime} " +
            "WHERE ID=#{id}")
    int update(SiTaskDetail detail);
}