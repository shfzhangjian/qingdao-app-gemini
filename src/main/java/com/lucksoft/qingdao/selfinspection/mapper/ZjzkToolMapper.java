package com.lucksoft.qingdao.selfinspection.mapper;

import com.lucksoft.qingdao.selfinspection.entity.ZjzkTool;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 自检自控台账 Mapper
 * 对应表: ZJZK_TOOL
 */
@Mapper
public interface ZjzkToolMapper {

    // ... (保留原有的 findList, insert, update, deleteById, findDistinctValues 方法) ...
    @Results(id = "zjzkToolMap", value = {
            @Result(property = "indocno", column = "INDOCNO", id = true),
            @Result(property = "sregid", column = "SREGID"),
            @Result(property = "sregnm", column = "SREGNM"),
            @Result(property = "dregt", column = "DREGT"),
            @Result(property = "smodid", column = "SMODID"),
            @Result(property = "smodnm", column = "SMODNM"),
            @Result(property = "dmodt", column = "DMODT"),
            @Result(property = "sname", column = "SNAME"),
            @Result(property = "szznolb", column = "SZZNOLB"),
            @Result(property = "sdept", column = "SDEPT"),
            @Result(property = "idept", column = "IDEPT"),
            @Result(property = "sjx", column = "SJX"),
            @Result(property = "ijx", column = "IJX"),
            @Result(property = "sfname", column = "SFNAME"),
            @Result(property = "sfcode", column = "SFCODE"),
            @Result(property = "sbname", column = "SBNAME"),
            @Result(property = "scj", column = "SCJ"),
            @Result(property = "sxh", column = "SXH"),
            @Result(property = "sazwz", column = "SAZWZ"),
            @Result(property = "ssm", column = "SSM"),
            @Result(property = "syl", column = "SYL"),
            @Result(property = "iszc", column = "ISZC"),
            @Result(property = "spmcode", column = "SPMCODE"),
            @Result(property = "sddno", column = "SDDNO"),
            @Result(property = "szcno", column = "SZCNO"),
            @Result(property = "dtime", column = "DTIME"),
            @Result(property = "istepid", column = "ISTEPID"),
            @Result(property = "sstepnm", column = "SSTEPNM"),
            @Result(property = "sstepstate", column = "SSTEPSTATE"),
            @Result(property = "istepcharttype", column = "ISTEPCHARTTYPE"),
            @Result(property = "sstepoperid", column = "SSTEPOPERID"),
            @Result(property = "sstepopernm", column = "SSTEPOPERNM"),

            // [新增] 映射车速字段
            @Result(property = "lastAvgSpeed", column = "LAST_AVG_SPEED"),
            @Result(property = "lastSpeedTime", column = "LAST_SPEED_TIME"),

            @Result(property = "snote", column = "SNOTE")
    })
    @Select("<script>" +
            "SELECT t.*, s.AVG_SPEED as LAST_AVG_SPEED, s.END_TIME as LAST_SPEED_TIME " +
            "FROM ZJZK_TOOL t " +
            // [新增] LEFT JOIN 子查询：获取每个 SPMCODE 最新的一条车速记录
            "LEFT JOIN (" +
            "    SELECT SPMCODE, AVG_SPEED, END_TIME FROM (" +
            "        SELECT SPMCODE, AVG_SPEED, END_TIME, " +
            "               ROW_NUMBER() OVER(PARTITION BY SPMCODE ORDER BY END_TIME DESC) as rn " +
            "        FROM ZJZK_SPEED_CHECK" +
            "    ) WHERE rn = 1" +
            ") s ON t.SPMCODE = s.SPMCODE " +
            "<where>" +
            "   <if test='params.sdept != null and params.sdept != \"\"'>AND t.SDEPT LIKE '%' || #{params.sdept} || '%'</if>" +
            "   <if test='params.sjx != null and params.sjx != \"\"'>AND t.SJX LIKE '%' || #{params.sjx} || '%'</if>" +
            "   <if test='params.sbname != null and params.sbname != \"\"'>AND t.SBNAME LIKE '%' || #{params.sbname} || '%'</if>" +
            "   <if test='params.sname != null and params.sname != \"\"'>AND t.SNAME LIKE '%' || #{params.sname} || '%'</if>" +
            "   <if test='params.spmcode != null and params.spmcode != \"\"'>AND t.SPMCODE LIKE '%' || #{params.spmcode} || '%'</if>" +
            "   <if test='params.szcno != null and params.szcno != \"\"'>AND t.SZCNO LIKE '%' || #{params.szcno} || '%'</if>" +
            "</where>" +
            "ORDER BY t.INDOCNO DESC" +
            "</script>")
    List<ZjzkTool> findList(@Param("params") Map<String, Object> params);

    @Insert("INSERT INTO ZJZK_TOOL (INDOCNO, SDEPT, SNAME, SJX, SFNAME, SBNAME, SCJ, SXH, SAZWZ, SYL, SPMCODE, SDDNO, SZCNO, DTIME, SSTEPSTATE, DREGT) " +
            "VALUES (SEQ_ZJZK_TOOL.NEXTVAL, #{sdept}, #{sname}, #{sjx}, #{sfname}, #{sbname}, #{scj}, #{sxh}, #{sazwz}, #{syl}, #{spmcode}, #{sddno}, #{szcno}, #{dtime}, #{sstepstate}, SYSDATE)")
    @Options(useGeneratedKeys = true, keyProperty = "indocno", keyColumn = "INDOCNO")
    int insert(ZjzkTool tool);

    @Update("UPDATE ZJZK_TOOL SET SDEPT=#{sdept}, SNAME=#{sname}, SJX=#{sjx}, SFNAME=#{sfname}, SBNAME=#{sbname}, SCJ=#{scj}, SXH=#{sxh}, SAZWZ=#{sazwz}, SYL=#{syl}, SPMCODE=#{spmcode}, SDDNO=#{sddno}, SZCNO=#{szcno}, DTIME=#{dtime}, DMODT=SYSDATE WHERE INDOCNO=#{indocno}")
    int update(ZjzkTool tool);

    @Delete("DELETE FROM ZJZK_TOOL WHERE INDOCNO=#{id}")
    int deleteById(Long id);

    @Select("SELECT DISTINCT ${field} FROM ZJZK_TOOL WHERE ${field} IS NOT NULL ORDER BY NLSSORT(${field}, 'NLS_SORT=SCHINESE_PINYIN_M')")
    List<String> findDistinctValues(@Param("field") String field);

    // -------------------------------------------------------------
    // [新增] 专门用于生成任务的接口
    // -------------------------------------------------------------

    /**
     * [新增] 1. 分组查询设备列表 (Group By SJX, SFNAME, SBNAME, SNAME, SPMCODE)
     * 必须过滤 SPMCODE IS NOT NULL
     */
    @Select("<script>" +
            "SELECT SJX, SFNAME, SBNAME, SPMCODE " +
            "FROM ZJZK_TOOL " +
            "WHERE SPMCODE IS NOT NULL AND LENGTH(TRIM(SPMCODE)) > 0 " +
            "<if test='params.sbname != null and params.sbname != \"\"'>AND SBNAME LIKE '%' || #{params.sbname} || '%'</if>" +
            "<if test='params.spmcode != null and params.spmcode != \"\"'>AND SPMCODE LIKE '%' || #{params.spmcode} || '%'</if>" +
            "<if test='params.sjx != null and params.sjx != \"\"'>AND SJX LIKE '%' || #{params.sjx} || '%'</if>" +     // 新增
            "<if test='params.sfname != null and params.sfname != \"\"'>AND SFNAME LIKE '%' || #{params.sfname} || '%'</if>" + // 新增
            "GROUP BY SJX, SFNAME, SBNAME, SPMCODE " +
            "ORDER BY SPMCODE" +
            "</script>")
    List<ZjzkTool> selectDeviceGroupList(@Param("params") Map<String, Object> params);



    /**
     * [修复] 2. 根据 SPMCODE 查询该设备下的所有明细 (用于生成任务子表)
     * 移除了 AND SNAME = #{sname}，因为生成任务时要获取该设备下所有的检查点
     */
    @Select("SELECT * FROM ZJZK_TOOL WHERE SPMCODE = #{spmcode}")
    @ResultMap("zjzkToolMap")
    List<ZjzkTool> selectBySpmCode(@Param("spmcode") String spmcode);

    // [新增] 严格根据7个联合键查找记录 (用于导入去重)
    // 注意：NULL 值的处理。这里假设导入时关键字段不为空，或者数据库中匹配也是基于非空匹配。
    @Select("SELECT * FROM ZJZK_TOOL WHERE " +
            "SDEPT = #{sdept} AND " +
            "SNAME = #{sname} AND " +
            "SJX = #{sjx} AND " +
            "SFNAME = #{sfname} AND " +
            "SBNAME = #{sbname} AND " +
            "SPMCODE = #{spmcode} AND " +
            "SAZWZ = #{sazwz}")
    List<ZjzkTool> findByUniqueKey(ZjzkTool tool);

    /**
     * [新增] 获取所有不重复的 SPMCODE 列表，用于批量更新车速
     */
    @Select("SELECT DISTINCT SPMCODE FROM ZJZK_TOOL WHERE SPMCODE IS NOT NULL")
    List<String> findAllSpmCodes();
}