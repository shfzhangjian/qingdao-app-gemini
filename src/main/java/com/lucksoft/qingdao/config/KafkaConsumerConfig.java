package com.lucksoft.qingdao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;

/**
 * Kafka 消费者高级配置
 */
@Configuration
@EnableKafka
public class KafkaConsumerConfig {

    /**
     * 这个Bean的目的是获取所有Kafka监听器的注册表。
     * 我们通过设置autoStartup为false，来禁止所有@KafkaListener在应用启动时自动连接。
     *
     * @param registry Spring Kafka提供的监听器端点注册表
     * @return 返回配置好的注册表
     */
    @Bean
    public KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry(final KafkaListenerEndpointRegistry registry) {

        for (MessageListenerContainer listenerContainer : registry.getListenerContainers()) {
            // 禁止监听器容器在应用启动时自动启动。
            // 这可以防止在Kafka broker不可用时，应用启动过程被长时间阻塞或失败。
            // 消费者将在首次需要时（或手动启动时）才尝试连接。
            listenerContainer.setAutoStartup(false);
        }
        return registry;
    }
}
