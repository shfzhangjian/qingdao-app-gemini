package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.oracle.dto.PmissionBoardDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MyBatis Mapper 接口，用于查询 PMISSIONBOARD (轮保/月保) 任务
 */
@Mapper
public interface PmissionBoardMapper {

    /**
     * 查询过去5分钟内新生成的看板任务 (基于 MAKEDATE 字段)
     * @return DTO 列表
     */
    @Results(id = "pmissionBoardResultMap", value = {
            @Result(property = "idocid", column = "IDOCID"),
            @Result(property = "djClass", column = "DJ_CLASS"),
            @Result(property = "mitype", column = "MITYPE"),
            @Result(property = "mititle", column = "MITITLE"),
            @Result(property = "midate", column = "MIDATE"),
            @Result(property = "makedate", column = "MAKEDATE"),
            @Result(property = "makepeople", column = "MAKEPEOPLE"),
            @Result(property = "ieqno", column = "IEQNO"),
            @Result(property = "makeflname", column = "MAKEFLNAME"),
            @Result(property = "iduty", column = "IDUTY"),
            @Result(property = "sduty", column = "SDUTY")
    })
    @Select("SELECT IDOCID, DJ_CLASS, MITYPE, MITITLE, MIDATE, MAKEDATE, MAKEPEOPLE, IEQNO, MAKEFLNAME, IDUTY, SDUTY " +
            "FROM PMISSIONBOARD " +
            "WHERE MAKEDATE >= SYSDATE - (5 / (24 * 60)) " + // 查询过去5分钟内的数据
            "ORDER BY MAKEDATE DESC")
    List<PmissionBoardDTO> findRecentTasks();

}