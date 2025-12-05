package com.lucksoft.qingdao.tmis.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucksoft.qingdao.tmis.metrology.ExportColumn;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 通用Excel导出工具类 (增强版)
 * 集成 Jackson ObjectMapper，支持 @JsonProperty 注解的 DTO 和 Map 导出。
 */
public class ExcelExportUtil {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportUtil.class);

    // 使用静态 ObjectMapper 实例，复用配置
    private static final ObjectMapper objectMapper = new ObjectMapper();

    static {
        // 可以在这里配置 objectMapper，例如日期格式等
        // objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * 导出Excel文件
     *
     * @param response  HttpServletResponse对象
     * @param fileName  导出的文件名
     * @param columns   列配置列表
     * @param data      数据列表 (可以是 DTO 列表，也可以是 Map 列表)
     * @param dtoClass  (已废弃，保留兼容性) 数据对象的Class类型
     * @param <T>       泛型
     * @throws IOException IO异常
     */
    public static <T> void export(HttpServletResponse response, String fileName, List<ExportColumn> columns, List<T> data, Class<T> dtoClass) throws IOException {
        log.info("开始生成Excel文件: {}, 数据量: {}", fileName, data != null ? data.size() : 0);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(fileName);

            // 1. 创建表头样式
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);

            // 2. 创建表头行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columns.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns.get(i).getTitle());
                cell.setCellStyle(headerStyle);
                // 初始列宽
                sheet.setColumnWidth(i, 256 * 20);
            }

            // 3. 创建数据行
            int rowNum = 1;
            for (T item : data) {
                Row row = sheet.createRow(rowNum++);

                // [核心修改] 将 item 统一转换为 Map
                // 如果 item 本身是 Map，直接强转；如果是 Bean，用 Jackson 转换
                Map<?, ?> itemMap;
                try {
                    if (item instanceof Map) {
                        itemMap = (Map<?, ?>) item;
                    } else {
                        // Jackson 会自动处理 @JsonProperty, @JsonFormat 等注解
                        itemMap = objectMapper.convertValue(item, Map.class);
                    }
                } catch (Exception e) {
                    log.error("数据转换失败", e);
                    continue;
                }

                for (int i = 0; i < columns.size(); i++) {
                    String key = columns.get(i).getKey();

                    try {
                        // 直接从 Map 中根据 key 获取值
                        // 这里的 key 就是前端传来的列名，也就是 @JsonProperty 定义的名字
                        Object value = itemMap.get(key);

                        // 格式化输出
                        String cellValue = formatValue(value);
                        row.createCell(i).setCellValue(cellValue);

                    } catch (Exception e) {
                        log.error("获取属性值失败, key: {}", key, e);
                        row.createCell(i).setCellValue("ERROR");
                    }
                }
            }

            // 4. 设置响应头并写入输出流
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName + ".xlsx");

            workbook.write(response.getOutputStream());
            log.info("Excel文件 '{}' 生成并发送成功", fileName);

        } catch (Exception e) {
            log.error("生成Excel时发生错误", e);
            if (!response.isCommitted()) {
                response.reset();
                response.setContentType("application/json");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"error\": \"导出Excel失败: " + e.getMessage() + "\"}");
            }
        }
    }

    /**
     * 格式化单元格的值
     */
    private static String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        // Jackson 转换 Map 后，日期可能变成了 Long (时间戳) 或 String
        // 如果是 Date 对象 (Map原始数据)
        if (value instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) value);
        }
        // 如果是 Boolean
        if (value instanceof Boolean) {
            return (Boolean) value ? "是" : "否";
        }
        // 其他情况直接 toString
        return value.toString();
    }
}