package com.lucksoft.qingdao.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Spring 缓存配置类
 * 通过 @EnableCaching 注解开启Spring Boot的声明式缓存功能。
 * 这允许我们在Service方法上使用 @Cacheable, @CachePut, @CacheEvict 等注解。
 */
@Configuration
@EnableCaching
public class CachingConfig {
    // 此处无需更多配置，Spring Boot会自动配置一个RedisCacheManager
    // 如果需要更复杂的缓存策略（如不同的过期时间），则可以在此配置自定义的CacheManager Bean。
}
