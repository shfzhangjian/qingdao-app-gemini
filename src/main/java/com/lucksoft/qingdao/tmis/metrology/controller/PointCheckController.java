package com.lucksoft.qingdao.tmis.metrology.controller;

import com.github.pagehelper.PageInfo;
import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tmis.metrology.ExportColumn;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckListItemDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckListDTO;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.PointCheckStatsDTO;
import com.lucksoft.qingdao.tmis.metrology.service.PointCheckStatsService;
import com.lucksoft.qingdao.qdjl.mapper.PointCheckStatsMapper;
import com.lucksoft.qingdao.tmis.util.ExcelExportUtil;
import com.github.pagehelper.PageHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/metrology/point-check")
public class PointCheckController {

    private static final Logger log = LoggerFactory.getLogger(PointCheckController.class);

    private final PointCheckStatsService pointCheckStatsService;
    private final PointCheckStatsMapper pointCheckStatsMapper; // Directly inject mapper for list view

    public PointCheckController(PointCheckStatsService pointCheckStatsService, PointCheckStatsMapper pointCheckStatsMapper) {
        this.pointCheckStatsService = pointCheckStatsService;
        this.pointCheckStatsMapper = pointCheckStatsMapper;
    }

    @GetMapping("/statistics")
    public ResponseEntity<List<PointCheckStatsDTO>> getStatistics(PointCheckQuery query) {
        log.info("接收到点检统计查询 (真实数据): {}", query);
        List<PointCheckStatsDTO> stats = pointCheckStatsService.getStatistics(query);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/list")
    public ResponseEntity<PageResult<PointCheckListDTO>> getList(PointCheckQuery query) {
        log.info("接收到点检列表查询 (真实数据): {}", query);

        PageHelper.startPage(query.getPageNum(), query.getPageSize());
        List<PointCheckListItemDTO> rawList = pointCheckStatsMapper.findPointCheckList(query);
        PageInfo<PointCheckListItemDTO> pageInfo = new PageInfo<>(rawList);

        // 转换 DTO 以匹配前端期望的格式
        List<PointCheckListDTO> finalList = pageInfo.getList().stream()
                .map(this::transformToPointCheckListDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new PageResult<>(finalList, pageInfo.getPageNum(), pageInfo.getPageSize(), pageInfo.getTotal(), pageInfo.getPages()));
    }

    private PointCheckListDTO transformToPointCheckListDTO(PointCheckListItemDTO rawItem) {
        String abc = rawItem.getSabc() == 1 ? "A" : rawItem.getSabc() == 2 ? "B" : "C";
        String planStatus = rawItem.getIdjstate() == 1 ? "已检" : "未检";
        String resultStatus = "未检".equals(planStatus) ? "未检" : "正常"; // 列表查询无法获取真实结果，默认为正常

        return new PointCheckListDTO(
                rawItem.getIndocno().intValue(),
                rawItem.getSusedept(),
                rawItem.getSjname(),
                abc,
                new SimpleDateFormat("yyyy-MM-dd").format(rawItem.getDinit()),
                planStatus,
                resultStatus
        );
    }

    @PostMapping("/export")
    public void exportPointCheck(@RequestBody PointCheckQuery query, HttpServletResponse response) throws IOException {
        log.info("接收到点检导出请求: {}", query);

        String viewMode = query.getViewMode();

        if ("list".equals(viewMode)) {
            // --- 导出列表 ---
            List<PointCheckListItemDTO> rawData = pointCheckStatsMapper.findPointCheckList(query);
            List<PointCheckListDTO> dataToExport = rawData.stream()
                    .map(this::transformToPointCheckListDTO)
                    .collect(Collectors.toList());
            ExcelExportUtil.export(response, "点检列表", query.getColumns(), dataToExport, PointCheckListDTO.class);
        } else {
            // --- 导出统计 (默认) ---
            List<PointCheckStatsDTO> statsData = pointCheckStatsService.getStatistics(query);
            List<ExportColumn> statsColumns = new ArrayList<>();
            statsColumns.add(createColumn("dept", "部门名称"));
            statsColumns.add(createColumn("yingJianShu1", "上半年应检数量"));
            statsColumns.add(createColumn("yiJianShu1", "上半年已检数量"));
            statsColumns.add(createColumn("weiJianShu1", "上半年未检数量"));
            statsColumns.add(createColumn("zhiXingLv1", "上半年执行率"));
            statsColumns.add(createColumn("zhengChangShu1", "上半年正常数量"));
            statsColumns.add(createColumn("yiChangShu1", "上半年异常数量"));
            statsColumns.add(createColumn("yingJianShu2", "下半年应检数量"));
            statsColumns.add(createColumn("yiJianShu2", "下半年已检数量"));
            statsColumns.add(createColumn("weiJianShu2", "下半年未检数量"));
            statsColumns.add(createColumn("zhiXingLv2", "下半年执行率"));
            statsColumns.add(createColumn("zhengChangShu2", "下半年正常数量"));
            statsColumns.add(createColumn("yiChangShu2", "下半年异常数量"));

            ExcelExportUtil.export(response, "点检统计", statsColumns, statsData, PointCheckStatsDTO.class);
        }
    }

    private ExportColumn createColumn(String key, String title) {
        ExportColumn col = new ExportColumn();
        col.setKey(key);
        col.setTitle(title);
        return col;
    }
}
