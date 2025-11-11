package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.oracle.dto.PmissionBoardDayDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用于查询 PMISSIONBOARDDAY (看板任务) 的 Mapper 接口.
 * 此 Mapper 假设连接的是主数据源 (PrimaryDataSource).
 */
@Mapper
public interface PmissionBoardDayMapper {

    /**
     * 查询过去一小时内新生成的看板任务
     * @return DTO 列表
     */
    @Results(id = "pmissionBoardDayResultMap", value = {
            @Result(property = "idocid", column = "IDOCID"),
            @Result(property = "miType", column = "MITYPE"),
            @Result(property = "miTitle", column = "MITITLE"),
            @Result(property = "miDate", column = "MIDATE"),
            @Result(property = "makePeople", column = "MAKEPEOPLE"),
            @Result(property = "makeFlName", column = "MAKEFLNAME"),
            @Result(property = "sDuty", column = "SDUTY"),
            @Result(property = "dBegin", column = "DBEGIN"),
            @Result(property = "dEnd", column = "DEND"),
            @Result(property = "dRegt", column = "DREGT")
    })
    @Select("SELECT IDOCID, MITYPE, MITITLE, MIDATE, MAKEPEOPLE, MAKEFLNAME, SDUTY, DBEGIN, DEND, DREGT " +
            "FROM PMISSIONBOARDDAY " +
            "WHERE DREGT >= SYSDATE - (1/24) " + // 查询过去1小时内的数据
            "ORDER BY DREGT DESC")
    List<PmissionBoardDayDTO> findRecentTasks();

}