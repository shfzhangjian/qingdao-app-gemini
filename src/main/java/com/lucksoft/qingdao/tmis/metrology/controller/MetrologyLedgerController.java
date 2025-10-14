package com.lucksoft.qingdao.tmis.metrology.controller;

import com.lucksoft.qingdao.tmis.dto.PageResult;
import com.lucksoft.qingdao.tmis.metrology.ExportColumn;
import com.lucksoft.qingdao.tmis.metrology.dto.LedgerQuery;
import com.lucksoft.qingdao.tmis.metrology.dto.MetrologyLedgerDTO;
import com.lucksoft.qingdao.tmis.util.ExcelExportUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@RestController
@RequestMapping("/api/metrology/ledger")
public class MetrologyLedgerController {

    private static final Logger log = LoggerFactory.getLogger(MetrologyLedgerController.class);
    private static final List<MetrologyLedgerDTO> allLedgerData;

    static {
        allLedgerData = IntStream.rangeClosed(1, 53).mapToObj(i -> {
            boolean expired = i % 10 == 0;
            boolean isLinked = i % 3 != 0;
            String status;
            String abc;
            switch (i % 4) {
                case 0: status = "正常"; abc = "A"; break;
                case 1: status = "维修中"; abc = "B"; break;
                case 2: status = "已报废"; abc = "C"; break;
                default: status = "正常"; abc = "A"; break;
            }
            return new MetrologyLedgerDTO(
                    expired, isLinked, "SYS" + String.format("%03d", i), i,
                    "012000" + String.format("%02d", i), "JL" + (1000 + i),
                    "电子台秤-" + i, "型号-V" + (i % 5 + 1), "FAC" + (200000 + i),
                    (i * 10) + "-" + (i * 10 + 100) + "kg", "车间A-" + i, "III级", "46#ZB48",
                    abc, "2025-10-" + (i % 28 + 1), status, "生产部"
            );
        }).collect(Collectors.toList());
    }

    private <T> PageResult<T> paginate(List<T> sourceList, int pageNum, int pageSize) {
        if (sourceList == null || sourceList.isEmpty()) {
            return new PageResult<>(Collections.emptyList(), pageNum, pageSize, 0, 0);
        }
        long total = sourceList.size();
        int pages = (int) Math.ceil((double) total / pageSize);
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, sourceList.size());
        List<T> pagedList = (fromIndex >= sourceList.size()) ? Collections.emptyList() : sourceList.subList(fromIndex, toIndex);
        return new PageResult<>(pagedList, pageNum, pageSize, total, pages);
    }

    private List<MetrologyLedgerDTO> filterData(LedgerQuery query) {
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("normal", "正常");
        statusMap.put("repair", "维修中");
        statusMap.put("scrapped", "已报废");
        final String targetStatus = statusMap.get(query.getDeviceStatus());

        return allLedgerData.stream()
                .filter(ledger -> query.getDeviceName() == null || query.getDeviceName().isEmpty() || ledger.getDeviceName().contains(query.getDeviceName()))
                .filter(ledger -> query.getEnterpriseId() == null || query.getEnterpriseId().isEmpty() || ledger.getEnterpriseId().contains(query.getEnterpriseId()))
                .filter(ledger -> query.getFactoryId() == null || query.getFactoryId().isEmpty() || ledger.getFactoryId().contains(query.getFactoryId()))
                .filter(ledger -> query.getDepartment() == null || query.getDepartment().isEmpty() || ledger.getDepartment().contains(query.getDepartment()))
                .filter(ledger -> query.getLocationUser() == null || query.getLocationUser().isEmpty() || ledger.getLocation().contains(query.getLocationUser()))
                .filter(ledger -> query.getParentDevice() == null || query.getParentDevice().isEmpty() || ledger.getParentDevice().contains(query.getParentDevice()))
                .filter(ledger -> "all".equals(query.getDeviceStatus()) || (targetStatus != null && targetStatus.equals(ledger.getStatus())))
                .filter(ledger -> "all".equals(query.getAbcCategory()) || query.getAbcCategory().equalsIgnoreCase(ledger.getAbc()))
                .collect(Collectors.toList());
    }

    @GetMapping("/list")
    public ResponseEntity<PageResult<MetrologyLedgerDTO>> getLedger(LedgerQuery query) {
        log.info("接收到台账查询请求: {}", query);
        List<MetrologyLedgerDTO> filteredList = filterData(query);
        PageResult<MetrologyLedgerDTO> pageResult = paginate(filteredList, query.getPageNum(), query.getPageSize());
        return ResponseEntity.ok(pageResult);
    }

    @PostMapping("/export")
    public void exportLedger(@RequestBody LedgerQuery query, HttpServletResponse response) throws IOException {
        log.info("接收到台账导出请求: {}", query);
        List<MetrologyLedgerDTO> dataToExport = filterData(query);
        List<ExportColumn> columns = query.getColumns();

        ExcelExportUtil.export(response, "计量台账", columns, dataToExport, MetrologyLedgerDTO.class);
    }
}
