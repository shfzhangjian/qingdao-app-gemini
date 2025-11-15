package com.lucksoft.qingdao.config;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature; // [新增]
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream; // [新增]
import java.io.IOException;
import java.io.OutputStreamWriter; // [新增]
import java.nio.charset.StandardCharsets; // [新增]
import java.nio.file.Files;
import java.nio.file.Path; // [新增]
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 服务，用于加载和检查IP白名单
 */
@Service
public class IpWhitelistService {

    private static final Logger log = LoggerFactory.getLogger(IpWhitelistService.class);
    private static final String WHITELIST_FILENAME = "ip-whitelist.json";

    private Set<String> whitelistedIps = new HashSet<>();
    private Path externalFilePath = Paths.get(WHITELIST_FILENAME); // [新增] 存储文件路径
    private ObjectMapper objectMapper = new ObjectMapper(); // [新增]

    /**
     * Spring 启动时，加载 ip-whitelist.json 文件
     */
    @PostConstruct
    public void init() {
        try {
            // [修改] 启用漂亮的 JSON 格式
            objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

            File externalFile = externalFilePath.toFile();
            File fileToLoad = null;

            if (externalFile.exists() && externalFile.isFile()) {
                fileToLoad = externalFile;
                log.info("正在从外部配置文件加载 IP 白名单: {}", fileToLoad.getAbsolutePath());
            } else {
                log.warn("在外部路径未找到 '{}'。正在尝试从 classpath (资源文件) 中加载...", WHITELIST_FILENAME);
                try {
                    fileToLoad = ResourceUtils.getFile("classpath:" + WHITELIST_FILENAME);
                    log.info("成功从 Classpath 加载 IP 白名单: {}", fileToLoad.getAbsolutePath());
                    // [新增] 既然是从 classpath 加载的，我们就把外部路径设置为 classpath 所在的目录
                    // 注意：这在 JAR 包中会失败，所以保存功能只在找到外部文件时才真正可靠
                    if (!externalFile.exists()) {
                        // 回退保存路径到JAR包同级目录
                        log.info("设置外部文件路径为: {}", externalFile.getAbsolutePath());
                    }
                } catch (IOException e) {
                    log.error("无法从外部路径或 Classpath 加载 IP 白名单文件 '{}'。", WHITELIST_FILENAME, e);
                    this.whitelistedIps = Collections.emptySet();
                    return;
                }
            }

            // [新增] 存储找到的绝对路径，以便保存时使用
            // 如果是从classpath加载的，它会保存到target/classes/ip-whitelist.json，
            // 如果是从jar同级目录加载的，它会保存到jar同级目录。
            // 我们优先使用JAR同级目录。
            if (externalFile.exists()) {
                this.externalFilePath = externalFile.toPath().toAbsolutePath();
            } else if (fileToLoad != null) {
                // 如果只在classpath找到，这在开发时有用
                // 但在生产环境（JAR包），我们仍强制写入到JAR包同级
                this.externalFilePath = Paths.get(WHITELIST_FILENAME).toAbsolutePath();
            }

            log.info("IP 白名单文件将被读写于: {}", this.externalFilePath);

            String jsonContent = new String(Files.readAllBytes(fileToLoad.toPath()));

            List<String> ips = objectMapper.readValue(jsonContent, new TypeReference<List<String>>() {});
            this.whitelistedIps = new HashSet<>(ips);

            log.info("成功加载 IP 白名单, 包含 {} 个地址。", whitelistedIps.size());
            log.debug("白名单内容: {}", whitelistedIps);

        } catch (IOException e) {
            log.error("加载 IP 白名单失败 ({}): {}", WHITELIST_FILENAME, e.getMessage());
            this.whitelistedIps = Collections.emptySet();
        }
    }

    /**
     * [新增] 保存白名单列表到文件并重新加载
     * @param ips 要保存的 IP 列表
     * @throws IOException
     */
    public synchronized void saveWhitelistedIps(List<String> ips) throws IOException {
        if (this.externalFilePath == null) {
            log.error("无法保存 IP 白名单：外部文件路径未初始化。");
            throw new IOException("Whitelist file path is not configured correctly.");
        }

        // 1. 将 List<String> 写入 JSON 文件
        try (OutputStreamWriter writer = new OutputStreamWriter(
                new FileOutputStream(this.externalFilePath.toFile()), StandardCharsets.UTF_8)) {

            objectMapper.writeValue(writer, ips);
        }

        log.info("已将 {} 个 IP 保存到 {}", ips.size(), this.externalFilePath);

        // 2. 重新加载配置
        // (简单起见，我们直接调用 init() 来重新读取文件并更新内存中的 Set)
        init();
    }

    /**
     * [新增] 获取当前内存中的白名单列表
     * @return IP 集合
     */
    public Set<String> getWhitelistedIps() {
        return Collections.unmodifiableSet(this.whitelistedIps);
    }


    /**
     * 检查给定的IP是否在白名单中
     * @param ip 要检查的 IP 地址
     * @return true 如果在白名单中
     */
    public boolean isWhitelisted(String ip) {
        if (ip == null) {
            return false;
        }
        return whitelistedIps.contains(ip);
    }

    /**
     * 从 HttpServletRequest 中获取客户端的真实 IP 地址。
     * 考虑了 X-Forwarded-For 代理的情况。
     * @param request HTTP 请求
     * @return 客户端 IP 地址
     */
    public String getClientIp(HttpServletRequest request) {
        String xffHeader = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xffHeader)) {
            // X-Forwarded-For 可能包含多个IP，取第一个
            return xffHeader.split(",")[0].trim();
        }
        String realIpHeader = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(realIpHeader)) {
            return realIpHeader.trim();
        }
        return request.getRemoteAddr();
    }
}