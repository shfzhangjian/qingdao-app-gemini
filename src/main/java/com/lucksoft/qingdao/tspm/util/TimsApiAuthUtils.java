package com.lucksoft.qingdao.tspm.util;

import cn.hutool.crypto.digest.DigestUtil;
import com.lucksoft.common.utils.GjjDebugLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils; // 引入 StringUtils
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;
import java.util.TreeMap;

/**
 * TIMS 接口 API_AUTH 认证工具类
 * 负责生成签名、构建带签名的URL和Header
 */
@Component
public class TimsApiAuthUtils {

    private static final Logger log = LoggerFactory.getLogger(TimsApiAuthUtils.class);

    @Value("${tims.api.id:tspm}")
    private String apiId;

    @Value("${tims.api.secret:tspm-1234}")
    private String apiSecret;

    private static final String VERSION = "1.0";
    private static final String HEADER_KEY = "apiAuth";
    private static final String HEADER_VALUE = "API";

    /**
     * 获取认证需要的请求头
     */
    public String getAuthHeaderKey() {
        return HEADER_KEY;
    }

    public String getAuthHeaderValue() {
        return HEADER_VALUE;
    }

    /**
     * 对原始URL进行处理：
     * 1. 解析原有URL中的参数
     * 2. 添加公共参数 (_timestamp, _version, _apiId)
     * 3. 计算签名 (_sign)
     * 4. 返回构建好的完整URI
     *
     * @param originalUrl 原始接口地址 (可能包含查询参数)
     * @param extraParams 额外的查询参数 (比如GET请求中的 type=1)
     * @param topic 用于日志记录的主题
     * @return 签名后的 URI
     */
    public URI signAndBuildUrl(String originalUrl, Map<String, Object> extraParams, String topic) {
        // [关键修复] 在方法开始时检查 apiId 是否已注入
        if (!StringUtils.hasText(apiId)) {
            log.error("配置项 tims.api.id 为空，无法进行 API 签名。");
            throw new RuntimeException("System Configuration Error: tims.api.id is missing or empty.");
        }

        try {
            // 1. 解析原始URL
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(originalUrl);
            MultiValueMap<String, String> queryParams = builder.build().getQueryParams();

            // 2. 准备用于签名的 Map (使用 TreeMap 保证自然顺序排序)
            TreeMap<String, Object> signMap = new TreeMap<>();

            // 2.1 添加原始URL中的参数
            if (queryParams != null) {
                queryParams.forEach((k, v) -> {
                    if (v != null && !v.isEmpty()) {
                        signMap.put(k, v.get(0)); // 假设每个key只有一个值
                    }
                });
            }

            // 2.2 添加传入的额外参数
            if (extraParams != null) {
                signMap.putAll(extraParams);
            }

            // 2.3 添加公共参数
            long timestamp = System.currentTimeMillis();

            // [关键修正] 文档OCR识别为 "_apild"，但根据错误提示 "apiId不能为空"，
            // 以及常规命名习惯，正确参数名极有可能是 "_apiId"。
            // 为了兼容性，我们保留 _apiId 作为签名参数，同时如果对方后端直接取 "apiId"，
            // 我们也在 URL 中带上它（不参与签名，或者参与签名，视对方规则而定）。
            // 但标准做法是只带文档规定的参数。
            // 这里我们将 _apild 修正为 _apiId。
            signMap.put("_apiId", apiId);

            // 如果依然报错，请尝试取消下面这行的注释，将不带下划线的 apiId 也加入
            // signMap.put("apiId", apiId);

            signMap.put("_timestamp", String.valueOf(timestamp));
            signMap.put("_version", VERSION);

            // 3. 计算签名
            String sign = calculateSign(signMap, topic);

            // 4. 将所有参数 (包括签名) 重新构建到 URL 中
            // 注意：先清除原有的 query params，防止重复
            builder.replaceQueryParams(new LinkedMultiValueMap<>());

            signMap.forEach((k, v) -> builder.queryParam(k, v));
            builder.queryParam("_sign", sign);

            URI finalUri = builder.build().encode().toUri();

            // Debug 日志
            String logMsg = String.format("URL签名生成完毕。\nApiId: %s\nTimestamp: %d\nSign: %s\nFinal URL: %s",
                    apiId, timestamp, sign, finalUri.toString());
            log.debug(logMsg);
            GjjDebugLogger.log(topic, "API_AUTH签名", logMsg);

            return finalUri;

        } catch (Exception e) {
            log.error("构建签名URL失败", e);
            GjjDebugLogger.logError(topic, "API_AUTH签名失败", e);
            throw new RuntimeException("构建签名URL失败: " + e.getMessage());
        }
    }

    /**
     * 核心签名逻辑
     * 规则: url参数按照自然顺序排序,然后加key和value相加,最后加上密钥进行MD5加密
     */
    private String calculateSign(TreeMap<String, Object> params, String topic) {
        StringBuilder sb = new StringBuilder();

        // 拼接 key + value
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (!"_sign".equals(key) && value != null) {
                sb.append(key).append(value);
            }
        }

        // 最后加上密钥
        sb.append(apiSecret);

        String rawString = sb.toString();
        String sign = DigestUtil.md5Hex(rawString);

        // 记录敏感的签名过程日志 (仅用于调试)
        GjjDebugLogger.log(topic, "签名明文计算", "待加密字符串: [" + rawString + "]\nMD5结果: " + sign);

        return sign;
    }
}