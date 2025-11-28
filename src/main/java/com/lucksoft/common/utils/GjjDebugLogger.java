package com.lucksoft.common.utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 国家局接口调试日志记录工具
 * 记录路径: D:/logs/yyyy-MM-dd/描述_ID.log
 */
public class GjjDebugLogger {

    private static final String BASE_LOG_PATH = "D:" + File.separator + "logs";

    // 用于在线程中传递当前的日志文件名（描述_ID）
    private static final ThreadLocal<String> CURRENT_LOG_NAME = new ThreadLocal<>();

    /**
     * 设置当前线程的日志文件名称上下文
     * @param name 格式：描述_ID (例如: 年度计划_annualPlan)
     */
    public static void setLogNameContext(String name) {
        CURRENT_LOG_NAME.set(name);
    }

    public static void clearLogNameContext() {
        CURRENT_LOG_NAME.remove();
    }

    /**
     * 记录日志
     * @param interfaceId 接口ID (兜底用)
     * @param title 标题
     * @param content 内容
     */
    public static void log(String interfaceId, String title, String content) {
        try {
            // 1. 确定目录 D:/logs/2023-10-27/
            String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            String dirPath = BASE_LOG_PATH + File.separator + dateStr;
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 2. 确定文件名
            // 优先使用 ThreadLocal 中的 "描述_ID"，如果没有则使用 interfaceId
            String fileNameBase = CURRENT_LOG_NAME.get();
            if (fileNameBase == null || fileNameBase.trim().isEmpty()) {
                fileNameBase = interfaceId;
            }
            // 净化文件名，移除非法字符
            fileNameBase = fileNameBase.replaceAll("[\\\\/:*?\"<>|]", "_").replace(".html", "");

            File logFile = new File(dir, fileNameBase + ".log");

            // 3. 构造日志内容
            String timeStr = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
            StringBuilder sb = new StringBuilder();
            sb.append("================================================================================\n");
            sb.append("[").append(timeStr).append("] ").append("[").append(title).append("] \n");
            if (content != null && !content.isEmpty()) {
                sb.append(content).append("\n");
            }
            sb.append("\n");

            // 4. 追加写入
            try (FileWriter fw = new FileWriter(logFile, true);
                 PrintWriter pw = new PrintWriter(fw)) {
                pw.print(sb.toString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void logError(String interfaceId, String title, Throwable e) {
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        log(interfaceId, title + " [异常]", sw.toString());
    }

    // --- 日志管理功能 ---

    /**
     * 获取所有有日志的日期目录
     */
    public static List<String> getLogDates() {
        File baseDir = new File(BASE_LOG_PATH);
        if (!baseDir.exists()) return Collections.emptyList();

        File[] files = baseDir.listFiles(File::isDirectory);
        if (files == null) return Collections.emptyList();

        return Arrays.stream(files)
                .map(File::getName)
                .sorted(Comparator.reverseOrder()) // 最新日期在前
                .collect(Collectors.toList());
    }

    /**
     * 获取指定日期的所有日志文件
     */
    public static List<Map<String, Object>> getLogFiles(String dateStr) {
        File dateDir = new File(BASE_LOG_PATH + File.separator + dateStr);
        if (!dateDir.exists()) return Collections.emptyList();

        File[] files = dateDir.listFiles((dir, name) -> name.endsWith(".log"));
        if (files == null) return Collections.emptyList();

        return Arrays.stream(files)
                .sorted((f1, f2) -> Long.compare(f2.lastModified(), f1.lastModified())) // 最新修改在前
                .map(f -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", f.getName());
                    map.put("size", f.length());
                    map.put("time", new Date(f.lastModified()));
                    return map;
                })
                .collect(Collectors.toList());
    }

    /**
     * 获取日志文件文件对象
     */
    public static File getLogFile(String dateStr, String fileName) {
        // 安全检查
        if (dateStr.contains("..") || fileName.contains("..")) return null;
        return new File(BASE_LOG_PATH + File.separator + dateStr + File.separator + fileName);
    }
}