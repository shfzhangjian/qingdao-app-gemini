package com.lucksoft.qingdao.qdjl.mapper;

import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckListItemDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckRawStatsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.builder.annotation.ProviderMethodResolver;
import org.apache.ibatis.jdbc.SQL;


import java.util.List;
import java.util.Map;

/**
 * 点检统计与列表的数据访问层接口
 */
@Mapper
public interface PointCheckStatsMapper {

    // ... (getRawStatistics method is unchanged) ...
    @SelectProvider(type = PointCheckStatsSqlProvider.class, method = "getRawStatistics")
    @Results({
            @Result(property = "iusedept", column = "IUSEDEPT"),
            @Result(property = "susedept", column = "SUSEDEPT"),
            // --- 周期 1 ---
            @Result(property = "f1", column = "F1"),
            @Result(property = "f1a1", column = "F1A1"),
            @Result(property = "f1a2", column = "F1A2"),
            @Result(property = "f1a3", column = "F1A3"),
            // --- 周期 2 ---
            @Result(property = "f2", column = "F2"),
            @Result(property = "f2a1", column = "F2A1"),
            @Result(property = "f2a2", column = "F2A2"),
            @Result(property = "f2a3", column = "F2A3"),
            // --- 周期 3 ---
            @Result(property = "f3", column = "F3"),
            @Result(property = "f3a1", column = "F3A1"),
            @Result(property = "f3a2", column = "F3A2"),
            @Result(property = "f3a3", column = "F3A3"),
            // --- 周期 4 ---
            @Result(property = "f4", column = "F4"),
            @Result(property = "f4a1", column = "F4A1"),
            @Result(property = "f4a2", column = "F4A2"),
            @Result(property = "f4a3", column = "F4A3"),
            // --- 周期 5 ---
            @Result(property = "f5", column = "F5"),
            @Result(property = "f5a1", column = "F5A1"),
            @Result(property = "f5a2", column = "F5A2"),
            @Result(property = "f5a3", column = "F5A3"),
            // --- 周期 6 ---
            @Result(property = "f6", column = "F6"),
            @Result(property = "f6a1", column = "F6A1"),
            @Result(property = "f6a2", column = "F6A2"),
            @Result(property = "f6a3", column = "F6A3")
    })
    List<PointCheckRawStatsDTO> getRawStatistics(PointCheckQuery query);


    @SelectProvider(type = PointCheckStatsSqlProvider.class, method = "findPointCheckList")
    @Results({
            @Result(property = "indocno", column = "INDOCNO"),
            @Result(property = "sjid", column = "SJID"),
            @Result(property = "sjno", column = "SJNO"),
            @Result(property = "sjname", column = "SJNAME"),
            @Result(property = "dinit", column = "DINIT"),
            @Result(property = "iusedept", column = "IUSEDEPT"),
            @Result(property = "susedept", column = "SUSEDEPT"),
            @Result(property = "sabc", column = "SABC"),
            @Result(property = "idjstate", column = "IDJSTATE"),
            @Result(property = "istate", column = "ISTATE"),
            @Result(property = "iqj", column = "IQJ"),
            @Result(property = "snytype", column = "SNYTYPE")
    })
    List<PointCheckListItemDTO> findPointCheckList(PointCheckQuery query);


    @SelectProvider(type = PointCheckStatsSqlProvider.class, method = "countPointCheckList")
    long countPointCheckList(PointCheckQuery query);


    /**
     * 内部类，用于构建复杂的动态SQL
     */
    class PointCheckStatsSqlProvider implements ProviderMethodResolver {

        // --- 统计SQL构建 (不变) ---
        public String getRawStatistics(PointCheckQuery query) {
            String category = query.getCategory() != null ? query.getCategory().toUpperCase() : "A";
            switch (category) {
                case "B":
                    return buildStatsSql(2, 4, 3, query.getYear(), query.getUserId());
                case "C":
                    return buildStatsSql(3, 2, 6, query.getYear(), query.getUserId());
                case "A":
                default:
                    return buildStatsSql(1, 6, 2, query.getYear(), query.getUserId());
            }
        }

        private String buildStatsSql(int sabc, int periods, int monthStep, String year, Long userId) {
            SQL sql = new SQL();
            StringBuilder selects = new StringBuilder("DISTINCT V.IUSEDEPT, V.SUSEDEPT ");

            for (int i = 0; i < periods; i++) {
                int startMonth = i * monthStep + 1;
                int endMonth = (i + 1) * monthStep;
                String startMonthStr = String.format("%02d", startMonth);
                String endMonthStr = String.format("%02d", endMonth);

                String periodAlias = "F" + (i + 1);
                String yingjian = String.format("(SELECT COUNT(1) FROM JL_EQUIP_DXJ C WHERE C.SABC=%d AND IUSEDEPT=V.IUSEDEPT AND c.istate=1 AND TO_CHAR(C.DINIT,'YYYYMM')='%s%s') AS %s", sabc, year, startMonthStr, periodAlias);
                String yijian = String.format("(SELECT COUNT(1) FROM JL_EQUIP_DXJ C WHERE NVL(IDEL,0)=0 AND C.SABC=%d AND IUSEDEPT=V.IUSEDEPT AND c.istate=1 AND IDJSTATE=1 AND TO_CHAR(C.DINIT,'YYYYMM')>='%s%s' AND TO_CHAR(C.DINIT,'YYYYMM')<='%s%s') AS %sA1", sabc, year, startMonthStr, year, endMonthStr, periodAlias);
                String zhengchang = String.format("(SELECT COUNT(1) FROM JL_EQDXJLOG C WHERE NVL(IDEL,0)=0 AND C.SABC=%d AND IDEPT=V.IUSEDEPT AND TO_CHAR(C.DWUSER,'YYYYMM')>='%s%s' AND TO_CHAR(C.DWUSER,'YYYYMM')<='%s%s' AND C.IRESULT=1) AS %sA2", sabc, year, startMonthStr, year, endMonthStr, periodAlias);
                String yichang = String.format("(SELECT COUNT(1) FROM JL_EQDXJLOG C WHERE NVL(IDEL,0)=0 AND C.SABC=%d AND IDEPT=V.IUSEDEPT AND TO_CHAR(C.DWUSER,'YYYYMM')>='%s%s' AND TO_CHAR(C.DWUSER,'YYYYMM')<='%s%s' AND C.IRESULT=2) AS %sA3", sabc, year, startMonthStr, year, endMonthStr, periodAlias);

                selects.append(", ").append(yingjian).append(", ").append(yijian).append(", ").append(zhengchang).append(", ").append(yichang);
            }

            sql.SELECT(selects.toString());
            sql.FROM("JL_EQUIP V");
            sql.WHERE("V.istate = 1", "NVL(V.IDEL,0) = 0", "nvl(length(V.IUSEDEPT),0) > 0");
            // 注意: 此处的用户权限过滤逻辑需要根据您的实际USERS表和权限设计进行调整
            // sql.WHERE("EXISTS (SELECT 1 FROM USERS B WHERE B.ID=#{userId} AND INSTR(decode('', 'true', ','||B.IDEPTSEE||',', '', ','||B.DEP_SCOPE_ID||','), ','||V.IUSEDEPT||',') > 0)");

            return "SELECT A.* FROM (" + sql.toString() + ") A ORDER BY A.IUSEDEPT";
        }


        // --- 列表SQL构建 ---
        public String findPointCheckList(PointCheckQuery query) {
            return buildListSql(query, false);
        }

        public String countPointCheckList(PointCheckQuery query) {
            return buildListSql(query, true);
        }

        private String buildListSql(PointCheckQuery query, boolean isCount) {
            SQL sql = new SQL();
            if(isCount) {
                sql.SELECT("COUNT(*)");
            } else {
                sql.SELECT("INDOCNO, SJID, SJNO, SJNAME, DINIT, IUSEDEPT, SUSEDEPT, SABC, IDJSTATE, ISTATE, IQJ, SNYTYPE");
            }
            sql.FROM("JL_EQUIP_DXJ");

            if (query.getSjname() != null && !query.getSjname().isEmpty()) {
                sql.WHERE("SJNAME LIKE '%' || #{sjname} || '%'");
            }
            if (query.getSjno() != null && !query.getSjno().isEmpty()) {
                sql.WHERE("SJNO LIKE '%' || #{sjno} || '%'");
            }
            if (query.getDepartment() != null && !query.getDepartment().isEmpty()) {
                sql.WHERE("SUSEDEPT LIKE '%' || #{department} || '%'");
            }
            if (query.getIstate() != null) {
                sql.WHERE("ISTATE = #{istate}");
            }
            if (query.getIqj() != null) {
                sql.WHERE("IQJ = #{iqj}");
            }
            if (query.getCategory() != null && !"all".equalsIgnoreCase(query.getCategory())) {
                int sabcValue = "A".equals(query.getCategory()) ? 1 : "B".equals(query.getCategory()) ? 2 : 3;
                sql.WHERE("SABC = " + sabcValue);
            }

            // [FIX 1] 修复日期范围查询
            if (query.getDateRange() != null && !query.getDateRange().isEmpty() && query.getDateRange().contains(" 至 ")) {
                // Flatpickr 中文包使用 " 至 " 作为分隔符
                String[] dates = query.getDateRange().split(" 至 ");
                if (dates.length == 2) {
                    // 使用 TO_DATE 进行日期比较
                    sql.WHERE("DINIT >= TO_DATE('" + dates[0] + "', 'YYYY-MM-DD')");
                    // 包含当天，所以用 23:59:59
                    sql.WHERE("DINIT <= TO_DATE('" + dates[1] + " 23:59:59', 'YYYY-MM-DD HH24:MI:SS')");
                }
            }

            // [FIX 2] 增加对计划状态 (planStatus) 的过滤
            if (query.getPlanStatus() != null && !"all".equalsIgnoreCase(query.getPlanStatus())) {
                // 前端传来 "已检" 或 "未检"
                if ("已检".equals(query.getPlanStatus())) {
                    sql.WHERE("IDJSTATE = 1");
                } else if ("未检".equals(query.getPlanStatus())) {
                    sql.WHERE("IDJSTATE = 2"); // 假设 2 是未检
                }
            }

            // [FIX 3] 增加对结果状态 (resultStatus) 的过滤
            // 注意：列表SQL只能查 JL_EQUIP_DXJ，它没有 IRESULT 字段。
            // 我们只能根据 IDJSTATE 来模拟。
            if (query.getResultStatus() != null && !"all".equalsIgnoreCase(query.getResultStatus())) {
                if ("未检".equals(query.getResultStatus())) {
                    sql.WHERE("IDJSTATE = 2");
                }
                // 备注： "正常" 和 "异常" 状态似乎存储在 JL_EQDXJLOG 表中。
                // 列表查询 (findPointCheckList) 目前没有关联此表，因此无法按“正常”或“异常”进行精确过滤。
                // 这是一个更复杂的查询，目前我们只添加对“未检”的过滤。
            }


            if (!isCount) {
                sql.ORDER_BY("SUSEDEPT DESC");
            }

            return sql.toString();
        }
    }
}