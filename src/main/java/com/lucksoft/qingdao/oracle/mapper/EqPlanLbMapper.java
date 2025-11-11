package com.lucksoft.qingdao.oracle.mapper;

import com.lucksoft.qingdao.oracle.dto.EqPlanLbDTO;
import com.lucksoft.qingdao.oracle.dto.EqPlanLbDtDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * MyBatis Mapper 接口，用于查询轮保计划 (EQ_PLANLB) 及其明细 (EQ_PLANLBDT)
 */
@Mapper
public interface EqPlanLbMapper {

    /**
     * 根据主键查询轮保计划主表信息
     * @param indocno 主键 (来自 EQ_PLANLB.INDOCNO)
     * @return EqPlanLbDTO
     */
    @Results(id = "eqPlanLbResultMap", value = {
            @Result(property = "indocno", column = "INDOCNO"),
            @Result(property = "sno", column = "SNO"),
            @Result(property = "stitle", column = "STITLE"),
            @Result(property = "dday", column = "DDAY"),
            @Result(property = "dend", column = "DEND"),
            @Result(property = "smaker", column = "SMAKER"),
            @Result(property = "sdept", column = "SDEPT"),
            @Result(property = "istate", column = "ISTATE"),
            @Result(property = "dsave", column = "DSAVE"),
            @Result(property = "ssave", column = "SSAVE"),
            @Result(property = "sstepstate", column = "SSTEPSTATE"),
            @Result(property = "dregt", column = "DREGT")
    })
    @Select("SELECT INDOCNO, SNO, STITLE, DDAY, DEND, SMAKER, SDEPT, ISTATE, DSAVE, SSAVE, SSTEPSTATE, DREGT " +
            "FROM EQ_PLANLB " +
            "WHERE INDOCNO = #{indocno}")
    EqPlanLbDTO findMainByIndocno(@Param("indocno") Long indocno);

    /**
     * 根据主表ID (ILINKNO) 查询所有轮保计划明细
     * @param ilinkno 主表ID (EQ_PLANLB.INDOCNO)
     * @return List<EqPlanLbDtDTO>
     */
    @Results(id = "eqPlanLbDtResultMap", value = {
            @Result(property = "indocno", column = "INDOCNO"),
            @Result(property = "ilinkno", column = "ILINKNO"),
            @Result(property = "ibookid", column = "IBOOKID"),
            @Result(property = "idocid", column = "IDOCID"),
            @Result(property = "sfcode", column = "SFCODE"),
            @Result(property = "sfname", column = "SFNAME"),
            @Result(property = "itimer", column = "ITIMER"),
            @Result(property = "soccteam", column = "SOCCTEAM"),
            @Result(property = "itype", column = "ITYPE"),
            @Result(property = "dbegin", column = "DBEGIN"),
            @Result(property = "iop", column = "IOP"),
            @Result(property = "dregt", column = "DREGT")
    })
    @Select("SELECT INDOCNO, ILINKNO, IBOOKID, IDOCID, SFCODE, SFNAME, ITIMER, SOCCTEAM, ITYPE, DBEGIN, IOP, DREGT " +
            "FROM EQ_PLANLBDT " +
            "WHERE ILINKNO = #{ilinkno} " +
            "ORDER BY INDOCNO") // 按明细主键排序
    List<EqPlanLbDtDTO> findItemsByIlinkno(@Param("ilinkno") Long ilinkno);

}