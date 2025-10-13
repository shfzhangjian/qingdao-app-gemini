package com.lucksoft.qingdao.eam.maintainbook.mapper;

import com.lucksoft.qingdao.eam.maintainbook.entity.Maintainbookdt;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Maintainbookdt 数据访问层接口 (MyBatis Mapper)
 */
@Mapper
public interface MaintainbookdtMapper {

    /**
     * 根据动态条件分页查询
     * @param criteria 查询条件
     * @return 实体列表
     */
    @Select("<script>" +
            "SELECT * FROM MAINTAINBOOKDT " +
            "<where>" +
            "</where>" +
            // "ORDER BY YOUR_SORT_COLUMN DESC" +
            "</script>")
    @Results(id = "maintainbookdtResultMap", value = {
        @Result(property = "indocno", column = "INDOCNO", id = true),
        @Result(property = "dmodt", column = "DMODT"),
        @Result(property = "dregt", column = "DREGT"),
        @Result(property = "f1", column = "F1"),
        @Result(property = "f2", column = "F2"),
        @Result(property = "ibasevalue", column = "IBASEVALUE"),
        @Result(property = "icheckdutys", column = "ICHECKDUTYS"),
        @Result(property = "icheckflag", column = "ICHECKFLAG"),
        @Result(property = "idtno", column = "IDTNO"),
        @Result(property = "idutys", column = "IDUTYS"),
        @Result(property = "ilinkno", column = "ILINKNO"),
        @Result(property = "iparent", column = "IPARENT"),
        @Result(property = "istate", column = "ISTATE"),
        @Result(property = "iworkhour", column = "IWORKHOUR"),
        @Result(property = "scheckdutys", column = "SCHECKDUTYS"),
        @Result(property = "sdetail", column = "SDETAIL"),
        @Result(property = "sdutys", column = "SDUTYS"),
        @Result(property = "smodid", column = "SMODID"),
        @Result(property = "smodnm", column = "SMODNM"),
        @Result(property = "snotes", column = "SNOTES"),
        @Result(property = "sregid", column = "SREGID"),
        @Result(property = "sregnm", column = "SREGNM"),
        @Result(property = "sstandard", column = "SSTANDARD"),
        @Result(property = "stitle", column = "STITLE")
    })
    List<Maintainbookdt> findByCriteria(Object criteria);

    /**
     * 插入一条新记录
     * @param maintainbookdt 实体对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO MAINTAINBOOKDT (INDOCNO, DMODT, DREGT, F1, F2, IBASEVALUE, ICHECKDUTYS, ICHECKFLAG, IDTNO, IDUTYS, ILINKNO, IPARENT, ISTATE, IWORKHOUR, SCHECKDUTYS, SDETAIL, SDUTYS, SMODID, SMODNM, SNOTES, SREGID, SREGNM, SSTANDARD, STITLE) " +
            "VALUES (#{indocno}, #{dmodt}, #{dregt}, #{f1}, #{f2}, #{ibasevalue}, #{icheckdutys}, #{icheckflag}, #{idtno}, #{idutys}, #{ilinkno}, #{iparent}, #{istate}, #{iworkhour}, #{scheckdutys}, #{sdetail}, #{sdutys}, #{smodid}, #{smodnm}, #{snotes}, #{sregid}, #{sregnm}, #{sstandard}, #{stitle})")
    int insert(Maintainbookdt maintainbookdt);

    /**
     * 根据主键查询
     * @param id 主键
     * @return 实体对象
     */
    @Select("SELECT * FROM MAINTAINBOOKDT WHERE INDOCNO = #{id}")
    @ResultMap("maintainbookdtResultMap")
    Maintainbookdt findById(@Param("id") BigDecimal id);

    /**
     * 根据主键动态更新记录
     * @param maintainbookdt 实体对象
     * @return 影响的行数
     */
    @Update("<script>" +
            "UPDATE MAINTAINBOOKDT " +
            "<set>" +
            "<if test='dmodt != null'>" +
            "    DMODT = #{dmodt}," +
            "</if>" +
            "<if test='dregt != null'>" +
            "    DREGT = #{dregt}," +
            "</if>" +
            "<if test='f1 != null'>" +
            "    F1 = #{f1}," +
            "</if>" +
            "<if test='f2 != null'>" +
            "    F2 = #{f2}," +
            "</if>" +
            "<if test='ibasevalue != null'>" +
            "    IBASEVALUE = #{ibasevalue}," +
            "</if>" +
            "<if test='icheckdutys != null'>" +
            "    ICHECKDUTYS = #{icheckdutys}," +
            "</if>" +
            "<if test='icheckflag != null'>" +
            "    ICHECKFLAG = #{icheckflag}," +
            "</if>" +
            "<if test='idtno != null'>" +
            "    IDTNO = #{idtno}," +
            "</if>" +
            "<if test='idutys != null'>" +
            "    IDUTYS = #{idutys}," +
            "</if>" +
            "<if test='ilinkno != null'>" +
            "    ILINKNO = #{ilinkno}," +
            "</if>" +
            "<if test='iparent != null'>" +
            "    IPARENT = #{iparent}," +
            "</if>" +
            "<if test='istate != null'>" +
            "    ISTATE = #{istate}," +
            "</if>" +
            "<if test='iworkhour != null'>" +
            "    IWORKHOUR = #{iworkhour}," +
            "</if>" +
            "<if test='scheckdutys != null'>" +
            "    SCHECKDUTYS = #{scheckdutys}," +
            "</if>" +
            "<if test='sdetail != null'>" +
            "    SDETAIL = #{sdetail}," +
            "</if>" +
            "<if test='sdutys != null'>" +
            "    SDUTYS = #{sdutys}," +
            "</if>" +
            "<if test='smodid != null'>" +
            "    SMODID = #{smodid}," +
            "</if>" +
            "<if test='smodnm != null'>" +
            "    SMODNM = #{smodnm}," +
            "</if>" +
            "<if test='snotes != null'>" +
            "    SNOTES = #{snotes}," +
            "</if>" +
            "<if test='sregid != null'>" +
            "    SREGID = #{sregid}," +
            "</if>" +
            "<if test='sregnm != null'>" +
            "    SREGNM = #{sregnm}," +
            "</if>" +
            "<if test='sstandard != null'>" +
            "    SSTANDARD = #{sstandard}," +
            "</if>" +
            "<if test='stitle != null'>" +
            "    STITLE = #{stitle}," +
            "</if>" +
            "</set>" +
            "WHERE INDOCNO = #{indocno}" +
            "</script>")
    int update(Maintainbookdt maintainbookdt);

    /**
     * 根据主键删除记录
     * @param id 主键
     * @return 影响的行数
     */
    @Delete("DELETE FROM MAINTAINBOOKDT WHERE INDOCNO = #{id}")
    int deleteById(@Param("id") BigDecimal id);

}
