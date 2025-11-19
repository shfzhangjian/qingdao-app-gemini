package com.lucksoft.qingdao.tmis.metrology.service;

import com.lucksoft.qingdao.qdjl.mapper.PointCheckStatsMapper;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckRawStatsDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckStatsDTO;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.github.pagehelper.PageHelper;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 点检统计服务类
 * 负责处理从数据库获取真实统计数据的业务逻辑
 */
@Service
public class PointCheckStatsService {

    private final PointCheckStatsMapper pointCheckStatsMapper;

    public PointCheckStatsService(PointCheckStatsMapper pointCheckStatsMapper) {
        this.pointCheckStatsMapper = pointCheckStatsMapper;
    }

    /**
     * 获取点检统计数据,并聚合成前端所需的上半年/下半年格式。
     * 使用 @Cacheable 注解为此方法的返回结果启用缓存。
     * - cacheNames: 指定缓存存储的命名空间。
     * - key: 定义缓存的键。这里使用年份、ABC分类和用户ID动态生成唯一的键，
     * 确保不同用户的不同查询条件都有独立的缓存。
     * @param query 查询参数
     * @return 统计数据列表
     */
    @Cacheable(cacheNames = "pointCheckStats", key = "#query.year + '-' + #query.category + '-' + #query.userId")
    public List<PointCheckStatsDTO> getStatistics(PointCheckQuery query) {
        // 从 dateRange 提取年份，默认为当前年份
        String year = String.valueOf(LocalDate.now().getYear());
        if (query.getDateRange() != null && !query.getDateRange().isEmpty() && query.getDateRange().length() >= 4) {
            year = query.getDateRange().substring(0, 4);
        }
        query.setYear(year);
        // todo: 在真实的权限集成中，应从SecurityContext获取当前登录用户的ID
        // query.setUserId(getCurrentUserId());

        // 从数据库获取按周期（双月/季度/半年）聚合的原始数据
        PageHelper.clearPage();
        List<PointCheckRawStatsDTO> rawStatsList = pointCheckStatsMapper.getRawStatistics(query);
        List<PointCheckStatsDTO> finalStatsList = new ArrayList<>();

        for (PointCheckRawStatsDTO rawStats : rawStatsList) {
            PointCheckStatsDTO finalStats = new PointCheckStatsDTO();
            finalStats.setDept(rawStats.getSusedept());

            long yingJianH1 = 0, yiJianH1 = 0, zhengChangH1 = 0, yiChangH1 = 0; // [FIX] 新增 yiChangH1
            long yingJianH2 = 0, yiJianH2 = 0, zhengChangH2 = 0, yiChangH2 = 0; // [FIX] 新增 yiChangH2

            String category = query.getCategory() != null ? query.getCategory().toUpperCase() : "A";

            // 根据ABC类别，将不同周期的数据累加到上半年(H1)和下半年(H2)
            switch(category) {
                case "A": // A类是双月周期, F1-F3为上半年, F4-F6为下半年
                    yingJianH1 = rawStats.getF1() + rawStats.getF2() + rawStats.getF3();
                    yiJianH1 = rawStats.getF1a1() + rawStats.getF2a1() + rawStats.getF3a1();
                    zhengChangH1 = rawStats.getF1a2() + rawStats.getF2a2() + rawStats.getF3a2();
                    yiChangH1 = rawStats.getF1a3() + rawStats.getF2a3() + rawStats.getF3a3(); // [FIX] 直接累加SQL查出的异常数

                    yingJianH2 = rawStats.getF4() + rawStats.getF5() + rawStats.getF6();
                    yiJianH2 = rawStats.getF4a1() + rawStats.getF5a1() + rawStats.getF6a1();
                    zhengChangH2 = rawStats.getF4a2() + rawStats.getF5a2() + rawStats.getF6a2();
                    yiChangH2 = rawStats.getF4a3() + rawStats.getF5a3() + rawStats.getF6a3(); // [FIX] 直接累加SQL查出的异常数
                    break;
                case "B": // B类是季度周期, F1-F2为上半年, F3-F4为下半年
                    yingJianH1 = rawStats.getF1() + rawStats.getF2();
                    yiJianH1 = rawStats.getF1a1() + rawStats.getF2a1();
                    zhengChangH1 = rawStats.getF1a2() + rawStats.getF2a2();
                    yiChangH1 = rawStats.getF1a3() + rawStats.getF2a3(); // [FIX] 直接累加SQL查出的异常数

                    yingJianH2 = rawStats.getF3() + rawStats.getF4();
                    yiJianH2 = rawStats.getF3a1() + rawStats.getF4a1();
                    zhengChangH2 = rawStats.getF3a2() + rawStats.getF4a2();
                    yiChangH2 = rawStats.getF3a3() + rawStats.getF4a3(); // [FIX] 直接累加SQL查出的异常数
                    break;
                case "C": // C类是半年周期, F1为上半年, F2为下半年
                default:
                    yingJianH1 = rawStats.getF1();
                    yiJianH1 = rawStats.getF1a1();
                    zhengChangH1 = rawStats.getF1a2();
                    yiChangH1 = rawStats.getF1a3(); // [FIX] 直接累加SQL查出的异常数

                    yingJianH2 = rawStats.getF2();
                    yiJianH2 = rawStats.getF2a1();
                    zhengChangH2 = rawStats.getF2a2();
                    yiChangH2 = rawStats.getF2a3(); // [FIX] 直接累加SQL查出的异常数
                    break;
            }

            // --- 计算并设置上半年派生数据 ---
            finalStats.setYingJianShu1(yingJianH1);
            finalStats.setYiJianShu1(yiJianH1);
            finalStats.setZhengChangShu1(zhengChangH1);
            // [FIX] 增加负数保护，如果已检 > 应检，则未检为0
            finalStats.setWeiJianShu1(Math.max(0, yingJianH1 - yiJianH1));
            // [FIX] 增加负数保护，确保异常数不为负
            finalStats.setYiChangShu1(Math.max(0, yiChangH1));
            finalStats.setZhiXingLv1(yingJianH1 > 0 ? String.format("%.0f%%", (double) yiJianH1 * 100 / yingJianH1) : "0%");

            // --- 计算并设置下半年派生数据 ---
            finalStats.setYingJianShu2(yingJianH2);
            finalStats.setYiJianShu2(yiJianH2);
            finalStats.setZhengChangShu2(zhengChangH2);
            // [FIX] 增加负数保护
            finalStats.setWeiJianShu2(Math.max(0, yingJianH2 - yiJianH2));
            // [FIX] 增加负数保护
            finalStats.setYiChangShu2(Math.max(0, yiChangH2));
            finalStats.setZhiXingLv2(yingJianH2 > 0 ? String.format("%.0f%%", (double) yiJianH2 * 100 / yingJianH2) : "0%");

            finalStatsList.add(finalStats);
        }

        return finalStatsList;
    }
}