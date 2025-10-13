package com.lucksoft.qingdao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 配置类
 *
 * @author Gemini
 */
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // 1. 设置 Redis 连接工厂
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 2. 创建并配置序列化器
        // 使用 StringRedisSerializer 来序列化和反序列化 redis 的 key 值
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        // 使用 GenericJackson2JsonRedisSerializer 来序列化和反序列化 redis 的 value 值
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        // 3. 配置具体的序列化方式
        // Key 的序列化
        redisTemplate.setKeySerializer(stringSerializer);
        // Hash 的 Key 的序列化
        redisTemplate.setHashKeySerializer(stringSerializer);

        // Value 的序列化
        redisTemplate.setValueSerializer(jsonSerializer);
        // Hash 的 Value 的序列化
        redisTemplate.setHashValueSerializer(jsonSerializer);

        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
