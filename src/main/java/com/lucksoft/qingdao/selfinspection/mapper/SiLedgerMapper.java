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
     * [修改] 增加 CALC_HAS_STANDARD 计算字段
     * 根据 T_SI_STANDARD 表中是否存在关联记录来判断是否上传了标准
     */
    @Select("<script>" +
            "SELECT A.*, " +
            " (CASE WHEN EXISTS (SELECT 1 FROM T_SI_STANDARD s WHERE s.LEDGER_ID = A.ID) THEN 1 ELSE 0 END) AS CALC_HAS_STANDARD " +
            "FROM T_SI_LEDGER A " +
            "<where>" +
            "   <if test='params.workshop != null and params.workshop != \"\"'>AND A.WORKSHOP LIKE '%' || #{params.workshop} || '%'</if>" +
            "   <if test='params.model != null and params.model != \"\"'>AND A.MODEL LIKE '%' || #{params.model} || '%'</if>" +
            "   <if test='params.mainDevice != null and params.mainDevice != \"\"'>AND A.MAIN_DEVICE LIKE '%' || #{params.mainDevice} || '%'</if>" +
            "   <if test='params.name != null and params.name != \"\"'>AND A.NAME LIKE '%' || #{params.name} || '%'</if>" +
            "   <if test='params.pmCode != null and params.pmCode != \"\"'>AND A.PM_CODE LIKE '%' || #{params.pmCode} || '%'</if>" +
            "   <if test='params.assetCode != null and params.assetCode != \"\"'>AND A.ASSET_CODE LIKE '%' || #{params.assetCode} || '%'</if>" +
            "</where>" +
            "ORDER BY A.ID DESC" +
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
            // [关键修改] 将 hasStandard 属性映射到计算列 CALC_HAS_STANDARD
            @Result(property = "hasStandard", column = "CALC_HAS_STANDARD"),
            @Result(property = "createTime", column = "CREATE_TIME"),
            @Result(property = "updateTime", column = "UPDATE_TIME")
    })
    List<SiLedger> findList(@Param("params") Map<String, Object> params);

    @Insert("INSERT INTO T_SI_LEDGER (ID, WORKSHOP, NAME, MODEL, DEVICE, MAIN_DEVICE, FACTORY, SPEC, LOCATION, PRINCIPLE, PM_CODE, ORDER_NO, ASSET_CODE, FIRST_USE_DATE, AUDIT_STATUS, HAS_STANDARD, CREATE_TIME) " +
            "VALUES (SEQ_SI_LEDGER.NEXTVAL, #{workshop}, #{name}, #{model}, #{device}, #{mainDevice}, #{factory}, #{spec}, #{location}, #{principle}, #{pmCode}, #{orderNo}, #{assetCode}, #{firstUseDate}, #{auditStatus}, #{hasStandard}, SYSDATE)")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "ID")
    int insert(SiLedger ledger);

    @Update("UPDATE T_SI_LEDGER SET WORKSHOP=#{workshop}, NAME=#{name}, MODEL=#{model}, DEVICE=#{device}, MAIN_DEVICE=#{mainDevice}, FACTORY=#{factory}, SPEC=#{spec}, LOCATION=#{location}, PRINCIPLE=#{principle}, PM_CODE=#{pmCode}, ORDER_NO=#{orderNo}, ASSET_CODE=#{assetCode}, FIRST_USE_DATE=#{firstUseDate}, UPDATE_TIME=SYSDATE WHERE ID=#{id}")
    int update(SiLedger ledger);

    @Delete("DELETE FROM T_SI_LEDGER WHERE ID=#{id}")
    int deleteById(Long id);

    @Select("SELECT DISTINCT ${field} FROM T_SI_LEDGER WHERE ${field} IS NOT NULL ORDER BY NLSSORT(${field}, 'NLS_SORT=SCHINESE_PINYIN_M')")
    List<String> findDistinctValues(@Param("field") String field);

    @Select("SELECT * FROM T_SI_LEDGER WHERE ID = #{id}")
    @ResultMap("siLedgerMap")
    SiLedger findById(Long id);

    // 此方法虽然在 Service 中被调用，但现在查询时是动态计算的，
    // 所以这个更新实际上只影响数据库中的冗余字段（如果还保留的话），不影响查询结果。
    @Update("UPDATE T_SI_LEDGER SET HAS_STANDARD = #{hasStandard}, UPDATE_TIME = SYSDATE WHERE ID = #{id}")
    int updateStandardStatus(@Param("id") Long id, @Param("hasStandard") Integer hasStandard);
}