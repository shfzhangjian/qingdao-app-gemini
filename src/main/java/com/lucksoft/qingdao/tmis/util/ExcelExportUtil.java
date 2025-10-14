package com.lucksoft.qingdao.tmis.util;

import com.lucksoft.qingdao.tmis.metrology.ExportColumn;
import com.lucksoft.qingdao.tmis.metrology.dto.MetrologyLedgerDTO;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ExcelExportUtil {

    public static void exportLedger(HttpServletResponse response, List<ExportColumn> columns, List<MetrologyLedgerDTO> data) throws IOException {
        // 使用 SXSSFWorkbook 支持大数据量导出，防止OOM
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("计量台账");

            // 1. 创建表头
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i).getTitle());
            }

            // 2. 填充数据行
            int rowNum = 1;
            for (MetrologyLedgerDTO ledger : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < columns.size(); i++) {
                    String key = columns.get(i).getKey();
                    Cell cell = row.createCell(i);
                    // 使用 switch 语句根据 key 获取对应的值，比反射更安全高效
                    setCellValue(cell, key, ledger);
                }
            }

            // 3. 设置响应头并写出文件
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = URLEncoder.encode("计量台账.xlsx", StandardCharsets.UTF_8.toString());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            workbook.write(response.getOutputStream());
        }
    }

    private static void setCellValue(Cell cell, String key, MetrologyLedgerDTO ledger) {
        switch (key) {
            case "expired":
                cell.setCellValue(ledger.isExpired() ? "是" : "否");
                break;
            case "isLinked":
                cell.setCellValue(ledger.isLinked() ? "是" : "否");
                break;
            case "sysId":
                cell.setCellValue(ledger.getSysId());
                break;
            case "seq":
                cell.setCellValue(ledger.getSeq());
                break;
            case "enterpriseId":
                cell.setCellValue(ledger.getEnterpriseId());
                break;
            case "erpId":
                cell.setCellValue(ledger.getErpId());
                break;
            case "deviceName":
                cell.setCellValue(ledger.getDeviceName());
                break;
            case "model":
                cell.setCellValue(ledger.getModel());
                break;
            case "factoryId":
                cell.setCellValue(ledger.getFactoryId());
                break;
            case "range":
                cell.setCellValue(ledger.getRange());
                break;
            case "location":
                cell.setCellValue(ledger.getLocation());
                break;
            case "accuracy":
                cell.setCellValue(ledger.getAccuracy());
                break;
            case "parentDevice":
                cell.setCellValue(ledger.getParentDevice());
                break;
            case "abc":
                cell.setCellValue(ledger.getAbc());
                break;
            case "nextDate":
                cell.setCellValue(ledger.getNextDate());
                break;
            case "status":
                cell.setCellValue(ledger.getStatus());
                break;
            case "department":
                cell.setCellValue(ledger.getDepartment());
                break;
            // 为其他可能的导出字段添加 case
            default:
                cell.setCellValue(""); // 如果找不到匹配的key，则填空字符串
                break;
        }
    }
}
