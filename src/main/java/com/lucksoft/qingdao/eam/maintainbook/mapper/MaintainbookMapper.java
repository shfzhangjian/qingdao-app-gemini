package com.lucksoft.qingdao.eam.maintainbook.mapper;

import com.lucksoft.qingdao.eam.maintainbook.dto.MaintainbookCriteriaDTO;
import com.lucksoft.qingdao.eam.maintainbook.entity.Maintainbook;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * Maintainbook 数据访问层接口 (MyBatis Mapper)
 */
@Mapper
public interface MaintainbookMapper {

    /**
     * 根据动态条件分页查询
     * @param criteria 查询条件
     * @return 实体列表
     */
    @Select("<script>" +
            "SELECT * FROM MAINTAINBOOK " +
            "<where>" +
            "<if test='criteria.iclassid != null'>" +
            "    AND ICLASSID = #{criteria.iclassid}" +
            "</if>" +
            "<if test='criteria.imachinetype != null'>" +
            "    AND IMACHINETYPE = #{criteria.imachinetype}" +
            "</if>" +
            "<if test='criteria.istate != null'>" +
            "    AND ISTATE = #{criteria.istate}" +
            "</if>" +
            "<if test=\"criteria.sclassnm != null and criteria.sclassnm != ''\">" +
            "    AND SCLASSNM LIKE CONCAT(CONCAT('%', #{criteria.sclassnm}), '%')" +
            "</if>" +
            "<if test=\"criteria.smachintetype != null and criteria.smachintetype != ''\">" +
            "    AND SMACHINTETYPE LIKE CONCAT(CONCAT('%', #{criteria.smachintetype}), '%')" +
            "</if>" +
            "<if test=\"criteria.stitle != null and criteria.stitle != ''\">" +
            "    AND STITLE LIKE CONCAT(CONCAT('%', #{criteria.stitle}), '%')" +
            "</if>" +
            "</where>" +
            // "ORDER BY YOUR_SORT_COLUMN DESC" +
            "</script>")
    @Results(id = "maintainbookResultMap", value = {
        @Result(property = "indocno", column = "INDOCNO", id = true),
        @Result(property = "dmodt", column = "DMODT"),
        @Result(property = "dregt", column = "DREGT"),
        @Result(property = "iclassid", column = "ICLASSID"),
        @Result(property = "idept", column = "IDEPT"),
        @Result(property = "imachinetype", column = "IMACHINETYPE"),
        @Result(property = "iparent", column = "IPARENT"),
        @Result(property = "istate", column = "ISTATE"),
        @Result(property = "sbookno", column = "SBOOKNO"),
        @Result(property = "sclassnm", column = "SCLASSNM"),
        @Result(property = "sdept", column = "SDEPT"),
        @Result(property = "smachintetype", column = "SMACHINTETYPE"),
        @Result(property = "smodid", column = "SMODID"),
        @Result(property = "smodnm", column = "SMODNM"),
        @Result(property = "snotes", column = "SNOTES"),
        @Result(property = "sparent", column = "SPARENT"),
        @Result(property = "sregid", column = "SREGID"),
        @Result(property = "sregnm", column = "SREGNM"),
        @Result(property = "stitle", column = "STITLE"),
        @Result(property = "sversion", column = "SVERSION"),
        @Result(property = "version", column = "VERSION")
    })
    List<Maintainbook> findByCriteria(@Param("criteria") MaintainbookCriteriaDTO criteria);

    /**
     * 插入一条新记录
     * @param maintainbook 实体对象
     * @return 影响的行数
     */
    @Insert("INSERT INTO MAINTAINBOOK (INDOCNO, DMODT, DREGT, ICLASSID, IDEPT, IMACHINETYPE, IPARENT, ISTATE, SBOOKNO, SCLASSNM, SDEPT, SMACHINTETYPE, SMODID, SMODNM, SNOTES, SPARENT, SREGID, SREGNM, STITLE, SVERSION, VERSION) " +
            "VALUES (#{indocno}, #{dmodt}, #{dregt}, #{iclassid}, #{idept}, #{imachinetype}, #{iparent}, #{istate}, #{sbookno}, #{sclassnm}, #{sdept}, #{smachintetype}, #{smodid}, #{smodnm}, #{snotes}, #{sparent}, #{sregid}, #{sregnm}, #{stitle}, #{sversion}, #{version})")
    int insert(Maintainbook maintainbook);

    /**
     * 根据主键查询
     * @param id 主键
     * @return 实体对象
     */
    @Select("SELECT * FROM MAINTAINBOOK WHERE INDOCNO = #{id}")
    @ResultMap("maintainbookResultMap")
    Maintainbook findById(@Param("id") BigDecimal id);

    /**
     * 根据主键动态更新记录
     * @param maintainbook 实体对象
     * @return 影响的行数
     */
    @Update("<script>" +
            "UPDATE MAINTAINBOOK " +
            "<set>" +
            "<if test=\"dmodt != null\">" +
            "    DMODT = #{dmodt}," +
            "</if>" +
            "<if test=\"dregt != null\">" +
            "    DREGT = #{dregt}," +
            "</if>" +
            "<if test=\"iclassid != null\">" +
            "    ICLASSID = #{iclassid}," +
            "</if>" +
            "<if test=\"idept != null\">" +
            "    IDEPT = #{idept}," +
            "</if>" +
            "<if test=\"imachinetype != null\">" +
            "    IMACHINETYPE = #{imachinetype}," +
            "</if>" +
            "<if test=\"iparent != null\">" +
            "    IPARENT = #{iparent}," +
            "</if>" +
            "<if test=\"istate != null\">" +
            "    ISTATE = #{istate}," +
            "</if>" +
            "<if test=\"sbookno != null\">" +
            "    SBOOKNO = #{sbookno}," +
            "</if>" +
            "<if test=\"sclassnm != null\">" +
            "    SCLASSNM = #{sclassnm}," +
            "</if>" +
            "<if test=\"sdept != null\">" +
            "    SDEPT = #{sdept}," +
            "</if>" +
            "<if test=\"smachintetype != null\">" +
            "    SMACHINTETYPE = #{smachintetype}," +
            "</if>" +
            "<if test=\"smodid != null\">" +
            "    SMODID = #{smodid}," +
            "</if>" +
            "<if test=\"smodnm != null\">" +
            "    SMODNM = #{smodnm}," +
            "</if>" +
            "<if test=\"snotes != null\">" +
            "    SNOTES = #{snotes}," +
            "</if>" +
            "<if test=\"sparent != null\">" +
            "    SPARENT = #{sparent}," +
            "</if>" +
            "<if test=\"sregid != null\">" +
            "    SREGID = #{sregid}," +
            "</if>" +
            "<if test=\"sregnm != null\">" +
            "    SREGNM = #{sregnm}," +
            "</if>" +
            "<if test=\"stitle != null\">" +
            "    STITLE = #{stitle}," +
            "</if>" +
            "<if test=\"sversion != null\">" +
            "    SVERSION = #{sversion}," +
            "</if>" +
            "<if test=\"version != null\">" +
            "    VERSION = #{version}," +
            "</if>" +
            "</set>" +
            "WHERE INDOCNO = #{indocno}" +
            "</script>")
    int update(Maintainbook maintainbook);

    /**
     * 根据主键删除记录
     * @param id 主键
     * @return 影响的行数
     */
    @Delete("DELETE FROM MAINTAINBOOK WHERE INDOCNO = #{id}")
    int deleteById(@Param("id") BigDecimal id);

}
