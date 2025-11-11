package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.oracle.dto.PmissionBoardBaoYangDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MyBatis Mapper 接口，用于查询 PMISSIONBOARDBAOYANG (例保) 任务
 */
@Mapper
public interface PmissionBoardBaoYangMapper {

    /**
     * 查询过去5分钟内新生成的看板任务 (基于 MAKEDATE 字段)
     * @return DTO 列表
     */
    @Results(id = "pmissionBaoYangResultMap", value = {
            @Result(property = "iidc", column = "IIDCID"),
            @Result(property = "idocid", column = "IDOCID"),
            @Result(property = "djClass", column = "DJ_CLASS"),
            @Result(property = "mitype", column = "MITYPE"),
            @Result(property = "mititle", column = "MITITLE"),
            @Result(property = "midate", column = "MIDATE"),
            @Result(property = "makedate", column = "MAKEDATE"),
            @Result(property = "makepeople", column = "MAKEPEOPLE"),
            @Result(property = "ieqno", column = "IEQNO"),
            @Result(property = "makeflcode", column = "MAKEFLCODE"),
            @Result(property = "makeflname", column = "MAKEFLNAME"),
            @Result(property = "iduty", column = "IDUTY"),
            @Result(property = "sduty", column = "SDUTY"),
            @Result(property = "dbegin", column = "DBEGIN"),
            @Result(property = "dend", column = "DEND")
    })
    @Select("SELECT IIDCID, IDOCID, DJ_CLASS, MITYPE, MITITLE, MIDATE, MAKEDATE, MAKEPEOPLE, IEQNO, MAKEFLCODE, MAKEFLNAME, IDUTY, SDUTY, DBEGIN, DEND " +
            "FROM PMISSIONBOARDBAOYANG " +
            "WHERE MAKEDATE >= SYSDATE - (5 / (24 * 60)) " + // 查询过去5分钟内的数据
            "ORDER BY MAKEDATE DESC")
    List<PmissionBoardBaoYangDTO> findRecentTasks();

}