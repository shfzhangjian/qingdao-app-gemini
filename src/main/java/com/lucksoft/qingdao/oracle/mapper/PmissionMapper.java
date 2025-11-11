package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.oracle.dto.PmissionDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MyBatis Mapper 接口，用于查询 PMISSION (专业/精密点检) 任务
 */
@Mapper
public interface PmissionMapper {

    /**
     * 查询过去5分钟内新生成的看板任务 (基于 MAKEDATE 字段)
     * @return DTO 列表
     */
    @Results(id = "pmissionResultMap", value = {
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
            @Result(property = "spmcode", column = "SPMCODE"),
            @Result(property = "ibookno", column = "IBOOKNO"),
            @Result(property = "makeplanno", column = "MAKEPLANNO"),
            @Result(property = "makeponit", column = "MAKEPONIT"),
            @Result(property = "makeckposition", column = "MAKECKPOSITION"),
            @Result(property = "makeproject", column = "MAKEPROJECT"),
            @Result(property = "iduty", column = "IDUTY"),
            @Result(property = "sduty", column = "SDUTY"),
            @Result(property = "srunnerid", column = "SRUNNERID"),
            @Result(property = "srunnernm", column = "SRUNNERNM"),
            @Result(property = "iplandept", column = "IPLANDEPT"),
            @Result(property = "splandept", column = "SPLANDEPT"),
            @Result(property = "iplanbzno", column = "IPLANBZNO")
    })
    @Select("SELECT IDOCID, DJ_CLASS, MITYPE, MITITLE, MIDATE, MAKEDATE, MAKEPEOPLE, IEQNO, MAKEFLCODE, MAKEFLNAME, SPMCODE, " +
            "IBOOKNO, MAKEPLANNO, MAKEPONIT, MAKECKPOSITION, MAKEPROJECT, IDUTY, SDUTY, SRUNNERID, SRUNNERNM, IPLANDEPT, SPLANDEPT, IPLANBZNO " +
            "FROM PMISSION " +
            "WHERE MAKEDATE >= SYSDATE - (5 / (24 * 60)) " + // 查询过去5分钟内的数据
            "ORDER BY MAKEDATE DESC")
    List<PmissionDTO> findRecentTasks();

}