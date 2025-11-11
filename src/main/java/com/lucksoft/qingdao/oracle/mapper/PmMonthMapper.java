package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.oracle.dto.PmMonthDTO;
import com.lucksoft.qingdao.oracle.dto.PmMonthItemDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MyBatis Mapper 接口，用于查询维修计划 (PM_MONTH) 及其明细 (PM_MONTH_ITEM)
 */
@Mapper
public interface PmMonthMapper {

    /**
     * 根据主键查询维修计划主表信息
     * @param indocno 主键 (来自 PM_MONTH.INDOCNO)
     * @return PmMonthDTO
     */
    @Results(id = "pmMonthResultMap", value = {
            @Result(property = "indocno", column = "INDOCNO"),
            @Result(property = "splanno", column = "SPLANNO"),
            @Result(property = "stitle", column = "STITLE"),
            @Result(property = "sapplyer", column = "SAPPLYER"),
            @Result(property = "sdept", column = "SDEPT"),
            @Result(property = "iplantype", column = "IPLANTYPE"),
            @Result(property = "iyear", column = "IYEAR"),
            @Result(property = "imonth", column = "IMONTH"),
            @Result(property = "dapplydate", column = "DAPPLYDATE"),
            @Result(property = "dplanbegin", column = "DPLANBEGIN"),
            @Result(property = "dplanend", column = "DPLANEND"),
            @Result(property = "istate", column = "ISTATE"),
            @Result(property = "iworkstate", column = "IWORKSTATE"),
            @Result(property = "sstepstate", column = "SSTEPSTATE"),
            @Result(property = "dregt", column = "DREGT")
    })
    @Select("SELECT INDOCNO, SPLANNO, STITLE, SAPPLYER, SDEPT, IPLANTYPE, IYEAR, IMONTH, DAPPLYDATE, DPLANBEGIN, DPLANEND, ISTATE, IWORKSTATE, SSTEPSTATE, DREGT " +
            "FROM PM_MONTH " +
            "WHERE INDOCNO = #{indocno}")
    PmMonthDTO findMainByIndocno(@Param("indocno") Long indocno);

    /**
     * 根据主表ID (ILINKNO) 查询所有维修计划明细
     * @param ilinkno 主表ID (PM_MONTH.INDOCNO)
     * @return List<PmMonthItemDTO>
     */
    @Results(id = "pmMonthItemResultMap", value = {
            @Result(property = "indocno", column = "INDOCNO"),
            @Result(property = "ilinkno", column = "ILINKNO"),
            @Result(property = "idocid", column = "IDOCID"),
            @Result(property = "sfcode", column = "SFCODE"),
            @Result(property = "sfname", column = "SFNAME"),
            @Result(property = "sitem", column = "SITEM"),
            @Result(property = "stodo", column = "STODO"),
            @Result(property = "iplanhour", column = "IPLANHOUR"),
            @Result(property = "iplanmoney", column = "IPLANMONEY"),
            @Result(property = "sdept", column = "SDEPT"),
            @Result(property = "sduty", column = "SDUTY"),
            @Result(property = "dplanbegin", column = "DPLANBEGIN"),
            @Result(property = "dplanend", column = "DPLANEND"),
            @Result(property = "istate", column = "ISTATE"),
            @Result(property = "ikind", column = "IKIND"),
            @Result(property = "dregt", column = "DREGT")
    })
    @Select("SELECT INDOCNO, ILINKNO, IDOCID, SFCODE, SFNAME, SITEM, STODO, IPLANHOUR, IPLANMONEY, SDEPT, SDUTY, DPLANBEGIN, DPLANEND, ISTATE, IKIND, DREGT " +
            "FROM PM_MONTH_ITEM " +
            "WHERE ILINKNO = #{ilinkno} " +
            "ORDER BY IDTNO")
    List<PmMonthItemDTO> findItemsByIlinkno(@Param("ilinkno") Long ilinkno);

}