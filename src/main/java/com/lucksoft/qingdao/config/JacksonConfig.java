package com.lucksoft.qingdao.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.TimeZone;

/**
 * 全局 Jackson 配置
 * 强制设置 JSON 解析和序列化的默认时区为 GMT+8 (北京时间)。
 * 这解决了删除实体类 @JsonFormat timezone 属性后导致的时间相差8小时问题。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonObjectMapperCustomization() {
        return jacksonObjectMapperBuilder ->
                jacksonObjectMapperBuilder.timeZone(TimeZone.getTimeZone("GMT+8"));
    }
}