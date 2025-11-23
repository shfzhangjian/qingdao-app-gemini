package com.lucksoft.qingdao.selfinspection.mapper;

import com.lucksoft.qingdao.selfinspection.entity.SiLedger;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 自检自控台账 Mapper
 * 对应表: T_SI_LEDGER
 */
@Mapper
public interface SiLedgerMapper {

    /**
     * 动态查询台账列表
     */
    @Select("<script>" +
            "SELECT * FROM T_SI_LEDGER " +
            "<where>" +
            "   <if test='params.workshop != null and params.workshop != \"\"'>AND WORKSHOP LIKE '%' || #{params.workshop} || '%'</if>" +
            "   <if test='params.model != null and params.model != \"\"'>AND MODEL LIKE '%' || #{params.model} || '%'</if>" +
            "   <if test='params.mainDevice != null and params.mainDevice != \"\"'>AND MAIN_DEVICE LIKE '%' || #{params.mainDevice} || '%'</if>" +
            "   <if test='params.name != null and params.name != \"\"'>AND NAME LIKE '%' || #{params.name} || '%'</if>" +
            "   <if test='params.pmCode != null and params.pmCode != \"\"'>AND PM_CODE LIKE '%' || #{params.pmCode} || '%'</if>" +
            "   <if test='params.assetCode != null and params.assetCode != \"\"'>AND ASSET_CODE LIKE '%' || #{params.assetCode} || '%'</if>" +
            "</where>" +
            "ORDER BY ID DESC" +
            "</script>")
    @Results(id = "siLedgerMap", value = {
            @Result(property = "id", column = "ID", id = true),
            @Result(property = "workshop", column = "WORKSHOP"),
            @Result(property = "name", column = "NAME"),
            @Result(property = "model", column = "MODEL"),
            @Result(property = "device", column = "DEVICE"),
            @Result(property = "mainDevice", column = "MAIN_DEVICE"),
            @Result(property = "factory", column = "FACTORY"),
            @Result(property = "spec", column = "SPEC"),
            @Result(property = "location", column = "LOCATION"),
            @Result(property = "principle", column = "PRINCIPLE"),
            @Result(property = "pmCode", column = "PM_CODE"),
            @Result(property = "orderNo", column = "ORDER_NO"),
            @Result(property = "assetCode", column = "ASSET_CODE"),
            @Result(property = "firstUseDate", column = "FIRST_USE_DATE"),
            @Result(property = "auditStatus", column = "AUDIT_STATUS"),
            @Result(property = "hasStandard", column = "HAS_STANDARD"),
            @Result(property = "createTime", column = "CREATE_TIME"),
            @Result(property = "updateTime", column = "UPDATE_TIME")
    })
    List<SiLedger> findList(@Param("params") Map<String, Object> params);

    @Insert("INSERT INTO T_SI_LEDGER (ID, WORKSHOP, NAME, MODEL, DEVICE, MAIN_DEVICE, FACTORY, SPEC, LOCATION, PRINCIPLE, PM_CODE, ORDER_NO, ASSET_CODE, FIRST_USE_DATE, AUDIT_STATUS, HAS_STANDARD, CREATE_TIME) " +
            "VALUES (SEQ_SI_LEDGER.NEXTVAL, #{workshop}, #{name}, #{model}, #{device}, #{mainDevice}, #{factory}, #{spec}, #{location}, #{principle}, #{pmCode}, #{orderNo}, #{assetCode}, #{firstUseDate}, #{auditStatus}, #{hasStandard}, SYSDATE)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "ID") // Oracle 需要指定 keyColumn
    int insert(SiLedger ledger);

    @Update("UPDATE T_SI_LEDGER SET WORKSHOP=#{workshop}, NAME=#{name}, MODEL=#{model}, DEVICE=#{device}, MAIN_DEVICE=#{mainDevice}, FACTORY=#{factory}, SPEC=#{spec}, LOCATION=#{location}, PRINCIPLE=#{principle}, PM_CODE=#{pmCode}, ORDER_NO=#{orderNo}, ASSET_CODE=#{assetCode}, FIRST_USE_DATE=#{firstUseDate}, UPDATE_TIME=SYSDATE WHERE ID=#{id}")
    int update(SiLedger ledger);

    @Delete("DELETE FROM T_SI_LEDGER WHERE ID=#{id}")
    int deleteById(Long id);

    @Select("SELECT DISTINCT ${field} FROM T_SI_LEDGER WHERE ${field} IS NOT NULL ORDER BY NLSSORT(${field}, 'NLS_SORT=SCHINESE_PINYIN_M')")
    List<String> findDistinctValues(@Param("field") String field);
}