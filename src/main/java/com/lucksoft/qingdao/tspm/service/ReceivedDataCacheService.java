package com.lucksoft.qingdao.tspm.service;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 在内存中缓存从TIMS接收到的最新消息.
 */
@Service
public class ReceivedDataCacheService {

    private static final int MAX_CACHE_SIZE = 50;
    private final Map<String, Deque<Map<String, Object>>> receivedDataCache = new ConcurrentHashMap<>();

    /**
     * 向指定Topic的缓存中添加一条新数据.
     * @param topic Kafka Topic.
     * @param data  消息数据 (已转换为Map).
     */
    public void addData(String topic, Map<String, Object> data) {
        receivedDataCache.computeIfAbsent(topic, k -> new LinkedList<>()).addFirst(data);
        // 保持缓存大小
        Deque<Map<String, Object>> queue = receivedDataCache.get(topic);
        while (queue.size() > MAX_CACHE_SIZE) {
            queue.removeLast();
        }
    }

    /**
     * 获取指定Topic的所有缓存数据.
     * @param topic Kafka Topic.
     * @return 缓存的消息列表.
     */
    public List<Map<String, Object>> getData(String topic) {
        Deque<Map<String, Object>> queue = receivedDataCache.get(topic);
        if (queue == null) {
            return Collections.emptyList();
        }
        return new LinkedList<>(queue);
    }
}

