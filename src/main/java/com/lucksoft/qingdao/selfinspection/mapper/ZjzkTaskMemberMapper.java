package com.lucksoft.qingdao.selfinspection.mapper;

import com.lucksoft.qingdao.selfinspection.entity.ZjzkTaskMember;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ZjzkTaskMemberMapper {

    @Insert("<script>" +
            "INSERT INTO ZJZK_TASK_MEMBER (INDOCNO, TASK_ID, USER_CODE, USER_NAME, USER_TYPE) " +
            "SELECT SEQ_ZJZK_TASK_MEMBER.NEXTVAL, A.* FROM (" +
            "<foreach collection='list' item='item' separator='UNION ALL'>" +
            " SELECT #{item.taskId}, #{item.userCode, jdbcType=VARCHAR}, #{item.userName, jdbcType=VARCHAR}, #{item.userType, jdbcType=VARCHAR} FROM DUAL" +
            "</foreach>" +
            ") A" +
            "</script>")
    int batchInsert(@Param("list") List<ZjzkTaskMember> list);

    @Select("SELECT * FROM ZJZK_TASK_MEMBER WHERE TASK_ID = #{taskId}")
    List<ZjzkTaskMember> findByTaskId(Long taskId);
}