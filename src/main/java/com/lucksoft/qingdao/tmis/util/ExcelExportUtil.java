package com.lucksoft.qingdao.tmis.util;

import com.lucksoft.qingdao.tmis.metrology.ExportColumn;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 通用Excel导出工具类
 */
public class ExcelExportUtil {

    private static final Logger log = LoggerFactory.getLogger(ExcelExportUtil.class);

    /**
     * 导出Excel文件
     *
     * @param response  HttpServletResponse对象，用于写入文件流
     * @param fileName  导出的文件名 (不含.xlsx后缀)
     * @param columns   列配置列表，定义了要导出的列、顺序和标题
     * @param data      要导出的数据列表
     * @param dtoClass  数据列表中对象的Class类型
     * @param <T>       数据的泛型类型
     * @throws IOException IO异常
     */
    public static <T> void export(HttpServletResponse response, String fileName, List<ExportColumn> columns, List<T> data, Class<T> dtoClass) throws IOException {
        log.info("开始生成Excel文件: {}", fileName);

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
                // 自动调整列宽
                sheet.autoSizeColumn(i);
            }

            // 3. 创建数据行
            int rowNum = 1;
            for (T item : data) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < columns.size(); i++) {
                    String key = columns.get(i).getKey();
                    try {
                        // 使用反射获取getter方法并调用
                        String methodName = "get" + key.substring(0, 1).toUpperCase() + key.substring(1);
                        Method method;
                        Object value;
                        try {
                            method = dtoClass.getMethod(methodName);
                            value = method.invoke(item);
                        } catch (NoSuchMethodException e) {
                            // 针对 boolean isXXX() 的情况
                            methodName = "is" + key.substring(0, 1).toUpperCase() + key.substring(1);
                            method = dtoClass.getMethod(methodName);
                            value = method.invoke(item);
                        }

                        // 格式化输出
                        String cellValue = formatValue(value);
                        row.createCell(i).setCellValue(cellValue);

                    } catch (Exception e) {
                        log.error("通过反射获取属性值失败, key: {}", key, e);
                        row.createCell(i).setCellValue("ERROR");
                    }
                }
            }

            // 调整所有列的宽度以适应内容
            for (int i = 0; i < columns.size(); i++) {
                int currentWidth = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, currentWidth + 1024); // 额外增加一点宽度
            }


            // 4. 设置响应头并写入输出流
            String encodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8.toString()).replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFileName + ".xlsx");

            workbook.write(response.getOutputStream());
            log.info("Excel文件 '{}' 生成并发送成功", fileName);

        } catch (Exception e) {
            log.error("生成Excel时发生错误", e);
            // 可以设置一个错误响应
            response.reset();
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"导出Excel失败\"}");
        }
    }

    /**
     * 格式化单元格的值
     */
    private static String formatValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Date) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format((Date) value);
        }
        if (value instanceof Boolean) {
            return (Boolean) value ? "是" : "否";
        }
        return value.toString();
    }
}

