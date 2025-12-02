package com.lucksoft.qingdao.oracle.tmis.controller;

import com.lucksoft.common.utils.GjjDebugLogger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * TMIS 调试日志管理控制器
 */
@RestController
@RequestMapping("/api/tmis/logs")
public class TmisLogController {

    /**
     * 获取有日志的日期列表
     */
    @GetMapping("/dates")
    public ResponseEntity<List<String>> getLogDates() {
        return ResponseEntity.ok(GjjDebugLogger.getLogDates());
    }

    /**
     * 获取指定日期的文件列表
     * @param date 日期字符串 (yyyy-MM-dd)
     */
    @GetMapping("/files")
    public ResponseEntity<List<Map<String, Object>>> getLogFiles(@RequestParam String date) {
        return ResponseEntity.ok(GjjDebugLogger.getLogFiles(date));
    }

    /**
     * 下载日志文件
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadLog(@RequestParam String date, @RequestParam String fileName) throws IOException {
        File file = GjjDebugLogger.getLogFile(date, fileName);

        if (file == null || !file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);

        // 处理中文文件名
        String encodedFileName = URLEncoder.encode(file.getName(), "UTF-8").replaceAll("\\+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedFileName)
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(file.length())
                .body(resource);
    }
}