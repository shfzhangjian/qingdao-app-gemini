package com.lucksoft.qingdao.tmis.metrology.service;

import com.lucksoft.qingdao.qdjl.mapper.PointCheckStatsMapper;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckRawStatsDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckStatsDTO;
import org.springframework.stereotype.Service;

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
     * 获取点检统计数据,并聚合成前端所需的上半年/下半年格式
     * @param query 查询参数
     * @return 统计数据列表
     */
    public List<PointCheckStatsDTO> getStatistics(PointCheckQuery query) {
        // 从 dateRange 提取年份，默认为当前年份
        String year = LocalDate.now().getYear() + "";
        if (query.getDateRange() != null && !query.getDateRange().isEmpty()) {
            year = query.getDateRange().substring(0, 4);
        }
        query.setYear(year);
        // todo: 从SecurityContext获取当前用户ID
        // query.setUserId(1L);

        // 从数据库获取原始聚合数据
        List<PointCheckRawStatsDTO> rawStatsList = pointCheckStatsMapper.getRawStatistics(query);
        List<PointCheckStatsDTO> finalStatsList = new ArrayList<>();

        for (PointCheckRawStatsDTO rawStats : rawStatsList) {
            PointCheckStatsDTO finalStats = new PointCheckStatsDTO();
            finalStats.setDept(rawStats.getSusedept());

            long yingJianH1 = 0, yiJianH1 = 0, zhengChangH1 = 0;
            long yingJianH2 = 0, yiJianH2 = 0, zhengChangH2 = 0;

            String category = query.getCategory() != null ? query.getCategory().toUpperCase() : "A";

            // 根据类别聚合数据到上半年/下半年
            switch(category) {
                case "A": // 双月
                    yingJianH1 = rawStats.getF1() + rawStats.getF2() + rawStats.getF3();
                    yiJianH1 = rawStats.getF1a1() + rawStats.getF2a1() + rawStats.getF3a1();
                    zhengChangH1 = rawStats.getF1a2() + rawStats.getF2a2() + rawStats.getF3a2();
                    yingJianH2 = rawStats.getF4() + rawStats.getF5() + rawStats.getF6();
                    yiJianH2 = rawStats.getF4a1() + rawStats.getF5a1() + rawStats.getF6a1();
                    zhengChangH2 = rawStats.getF4a2() + rawStats.getF5a2() + rawStats.getF6a2();
                    break;
                case "B": // 季度
                    yingJianH1 = rawStats.getF1() + rawStats.getF2();
                    yiJianH1 = rawStats.getF1a1() + rawStats.getF2a1();
                    zhengChangH1 = rawStats.getF1a2() + rawStats.getF2a2();
                    yingJianH2 = rawStats.getF3() + rawStats.getF4();
                    yiJianH2 = rawStats.getF3a1() + rawStats.getF4a1();
                    zhengChangH2 = rawStats.getF3a2() + rawStats.getF4a2();
                    break;
                case "C": // 半年
                default:
                    yingJianH1 = rawStats.getF1();
                    yiJianH1 = rawStats.getF1a1();
                    zhengChangH1 = rawStats.getF1a2();
                    yingJianH2 = rawStats.getF2();
                    yiJianH2 = rawStats.getF2a1();
                    zhengChangH2 = rawStats.getF2a2();
                    break;
            }

            // --- 计算并设置上半年数据 ---
            finalStats.setYingJianShu1(yingJianH1);
            finalStats.setYiJianShu1(yiJianH1);
            finalStats.setZhengChangShu1(zhengChangH1);
            finalStats.setWeiJianShu1(yingJianH1 - yiJianH1);
            finalStats.setYiChangShu1(yiJianH1 - zhengChangH1);
            finalStats.setZhiXingLv1(yingJianH1 > 0 ? String.format("%.0f%%", (double) yiJianH1 * 100 / yingJianH1) : "0%");

            // --- 计算并设置下半年数据 ---
            finalStats.setYingJianShu2(yingJianH2);
            finalStats.setYiJianShu2(yiJianH2);
            finalStats.setZhengChangShu2(zhengChangH2);
            finalStats.setWeiJianShu2(yingJianH2 - yiJianH2);
            finalStats.setYiChangShu2(yiJianH2 - zhengChangH2);
            finalStats.setZhiXingLv2(yingJianH2 > 0 ? String.format("%.0f%%", (double) yiJianH2 * 100 / yingJianH2) : "0%");

            finalStatsList.add(finalStats);
        }

        return finalStatsList;
    }
}

