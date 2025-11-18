package com.lucksoft.qingdao.tmis.metrology.controller;

import com.lucksoft.qingdao.system.util.AuthUtil;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckStatsDTO;
import com.lucksoft.qingdao.tmis.metrology.service.PointCheckStatsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 点检统计真实数据接口控制器
 */
@RestController
@RequestMapping("/api/metrology/point-check-stats")
public class PointCheckStatsController {


    @Autowired // 3. 注入 AuthUtil
    private AuthUtil authUtil;

    private static final Logger log = LoggerFactory.getLogger(PointCheckStatsController.class);

    private final PointCheckStatsService pointCheckStatsService;

    public PointCheckStatsController(PointCheckStatsService pointCheckStatsService) {
        this.pointCheckStatsService = pointCheckStatsService;
    }

    /**
     * 提供基于数据库真实数据的统计查询接口
     * @param query 查询参数
     * @return 统计数据列表
     */
    @GetMapping("/statistics")
    public ResponseEntity<List<PointCheckStatsDTO>> getRealStatistics(PointCheckQuery query, HttpServletRequest request) {

        log.info("接收到真实数据点检统计查询: {}", query);
        com.lucksoft.qingdao.system.dto.UserInfo userInfo = authUtil.getCurrentUserInfo(request);
        if (userInfo != null && userInfo.getUser() != null) {
            query.setUserId(userInfo.getUser().getId());
            log.info("查询已根据用户ID: {} 进行过滤", query.getUserId());
        } else {
            log.warn("无法获取当前用户信息，查询将不过滤用户。");
        }


        List<PointCheckStatsDTO> stats = pointCheckStatsService.getStatistics(query);
        return ResponseEntity.ok(stats);
    }
}
