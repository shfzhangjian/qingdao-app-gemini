package com.lucksoft.qingdao.tmis.metrology.controller;

import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tmis.metrology.dto.LedgerQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.MetrologyLedgerDTO;
import com.lucksoft.qingdao.tmis.metrology.service.MetrologyLedgerService;
import com.lucksoft.qingdao.tmis.util.ExcelExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/metrology/ledger")
public class MetrologyLedgerController {

    private static final Logger log = LoggerFactory.getLogger(MetrologyLedgerController.class);

    private final MetrologyLedgerService metrologyLedgerService;

    // 使用构造函数注入 Service
    public MetrologyLedgerController(MetrologyLedgerService metrologyLedgerService) {
        this.metrologyLedgerService = metrologyLedgerService;
    }

    /**
     * 根据查询条件分页获取计量台账列表
     * @param query 查询参数 DTO，Spring Boot 会自动绑定 URL 查询参数到对象属性
     * @return 分页结果
     */
    @GetMapping("/list")
    public ResponseEntity<PageResult<MetrologyLedgerDTO>> getLedger(LedgerQuery query) {
        log.info("接收到台账查询请求: {}", query);
        PageResult<MetrologyLedgerDTO> pageResult = metrologyLedgerService.getLedgerPage(query);
        return ResponseEntity.ok(pageResult);
    }

    /**
     * 导出计量台账数据为 Excel
     * @param query 查询参数，包含要导出的列信息
     * @param response HttpServletResponse 用于写入文件流
     * @throws IOException IO 异常
     */
    @PostMapping("/export")
    public void exportLedger(@RequestBody LedgerQuery query, HttpServletResponse response) throws IOException {
        log.info("接收到台账导出请求: {}", query);
        // 调用 Service 获取所有符合条件的数据（不分页）
        List<MetrologyLedgerDTO> dataToExport = metrologyLedgerService.getLedgerListForExport(query);

        // 使用通用工具类导出 Excel
        ExcelExportUtil.export(response, "计量台账", query.getColumns(), dataToExport, MetrologyLedgerDTO.class);
    }
}
