package com.lucksoft.qingdao.qdjl.mapper;

import com.lucksoft.qingdao.tmis.metrology.dto.LedgerQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.MetrologyLedgerDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 计量台账数据访问层接口 (连接 qdjl 数据源)
 */
@Mapper
public interface MetrologyLedgerMapper {

    @Results(id = "metrologyLedgerResultMap", value = {
            @Result(property = "indocno", column = "INDOCNO"),
            @Result(property = "sjno", column = "SJNO"),
            @Result(property = "sjname", column = "SJNAME"),
            @Result(property = "istate", column = "ISTATE"),
            @Result(property = "iqj", column = "IQJ"),
            @Result(property = "izj", column = "IZJ"),
            @Result(property = "sabc", column = "SABC"),
            @Result(property = "sggxh", column = "SGGXH"),
            @Result(property = "slevel", column = "SLEVEL"),
            @Result(property = "slc", column = "SLC"),
            @Result(property = "sproduct", column = "SPRODUCT"),
            @Result(property = "dfactory", column = "DFACTORY"),
            @Result(property = "sfactoryno", column = "SFACTORYNO"),
            @Result(property = "splace", column = "SPLACE"),
            @Result(property = "seq", column = "SEQ"),
            @Result(property = "susedept", column = "SUSEDEPT"),
            @Result(property = "suser", column = "SUSER"),
            @Result(property = "sverifier", column = "SCHECKUSER"),
            @Result(property = "sdefine1", column = "SDEFINE1"),
            @Result(property = "scertificate", column = "SCERTIFICATE"),
            @Result(property = "sbuytype", column = "SBUYTYPE"),
            @Result(property = "dcheck", column = "DCHECK"),
            @Result(property = "dupcheck", column = "DUPCHECK"),
            @Result(property = "sconfirmbasis", column = "SCONFIRMBASIS"),
            @Result(property = "snotes", column = "SNOTES")
    })
    @Select("<script>" +
            "SELECT * FROM V_JL_EQUIP " +
            "<where>" +
            "   NVL(IDEL, 0) = 0  and ISTATE != '备用'"+
            "   <if test='deviceName != null and deviceName != \"\"'>" +
            "       AND SJNAME LIKE '%' || #{deviceName} || '%'" +
            "   </if>" +
            "   <if test='enterpriseId != null and enterpriseId != \"\"'>" +
            "       AND SJNO LIKE '%' || #{enterpriseId} || '%'" +
            "   </if>" +
            "   <if test='factoryId != null and factoryId != \"\"'>" +
            "       AND SFACTORYNO LIKE '%' || #{factoryId} || '%'" +
            "   </if>" +
            "   <if test='department != null and department != \"\"'>" +
            "       AND SUSEDEPT LIKE '%' || #{department} || '%'" +
            "   </if>" +
            "   <if test='locationUser != null and locationUser != \"\"'>" +
            "       AND SPLACE LIKE '%' || #{locationUser} || '%'" +
            "   </if>" +
            "   <if test='parentDevice != null and parentDevice != \"\"'>" +
            "       AND SEQ LIKE '%' || #{parentDevice} || '%'" +
            "   </if>" +
            "   <if test='abcCategory != null and abcCategory != \"\" and abcCategory != \"all\"'>" +
            "       AND SABC = #{abcCategory}" +
            "   </if>" +
            "   <if test='deviceStatus != null and deviceStatus != \"\" and deviceStatus != \"all\"'>" +
            "       AND ISTATE = #{deviceStatus, jdbcType=VARCHAR}" +
            "   </if>" +
            "</where>" +
            "ORDER BY SJNO" +
            "</script>")
    List<MetrologyLedgerDTO> findLedgerByCriteria(LedgerQuery query);

    /**
     * 查询指定列的去重值，用于前端下拉补全。
     * 注意：columnName 由 Service 层校验，防止 SQL 注入。
     */
    @Select("SELECT DISTINCT ${columnName} FROM V_JL_EQUIP WHERE ${columnName} IS NOT NULL ORDER BY NLSSORT(${columnName}, 'NLS_SORT=SCHINESE_PINYIN_M')")
    List<String> findDistinctValues(@Param("columnName") String columnName);
}