package com.example.student.university_career_platform.service;

import com.github.benmanes.caffeine.cache.Cache;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class HotSearchService {

    // 注入 Redis 分布式缓存
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 注入 Caffeine 本地缓存 (需确保之前提过的 CacheConfig 已配置)
    @Autowired
    @Qualifier("hotSearchLocalCache")
    private Cache<String, Object> localCache;

    // Redis 缓存 Key 前缀
    private static final String HOT_SEARCH_CACHE_KEY_PREFIX = "hot_search:top:";

    // 使用线程安全的哈希表，记录全网用户的搜索词频次 (充当底层数据源/DB)
    private final ConcurrentHashMap<String, Integer> searchCounts = new ConcurrentHashMap<>();

    // 系统启动时，预置一些基础热搜数据（避免刚启动时列表为空）
    @PostConstruct
    public void init() {
        searchCounts.put("2026考研大纲", 128);
        searchCounts.put("互联网+大赛", 96);
        searchCounts.put("字节跳动实习", 85);
        searchCounts.put("春招时间表", 64);
        searchCounts.put("雅思机考", 52);
    }

    /**
     * 记录用户的搜索行为（每次搜索调用一次）
     */
    public void recordSearch(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }
        // 清洗数据：去除首尾空格并转小写
        String cleanKeyword = keyword.trim().toLowerCase();

        // 高并发安全的累加操作
        // 【答辩亮点备忘】：在真实分布式集群下，可以将此处的内存累加替换为 Redis 的 ZSET (ZINCRBY) 或者通过 Kafka 异步削峰落库。
        searchCounts.put(cleanKeyword, searchCounts.getOrDefault(cleanKeyword, 0) + 1);
    }

    /**
     * 🌟 核心升级：采用 Caffeine + Redis 多级缓存架构，并防击穿与雪崩
     */
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> getTopKHotSearches(int k) {
        String cacheKey = HOT_SEARCH_CACHE_KEY_PREFIX + k;

        // 1. 查询 Caffeine 本地缓存 (L1 Cache)，如果命中则直接毫秒级返回，不走网络 I/O
        List<Map<String, Object>> cachedData = (List<Map<String, Object>>) localCache.getIfPresent(cacheKey);
        if (cachedData != null) {
            return cachedData;
        }

        // 2. 查询 Redis 分布式缓存 (L2 Cache)
        cachedData = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedData != null) {
            // 如果 Redis 有数据，回写到本地缓存，并返回
            localCache.put(cacheKey, cachedData);
            return cachedData;
        }

        // 3. 缓存均未命中，准备进行复杂计算（模拟查询数据库/搜索引擎）。
        // 加入 synchronized 锁（双重检查锁 DCL），防止高并发下的【缓存击穿】（并发请求同时涌入压垮 CPU/DB）
        synchronized (this) {
            // 再次检查 Redis (防止在排队等锁的过程中，已经被其他获得锁的线程计算完毕并放入了 Redis)
            cachedData = (List<Map<String, Object>>) redisTemplate.opsForValue().get(cacheKey);
            if (cachedData != null) {
                localCache.put(cacheKey, cachedData);
                return cachedData;
            }

            // 4. 真正执行耗时的 Top K 计算逻辑
            cachedData = calculateTopKFromSource(k);

            // 5. 写入 Redis
            // 【防雪崩策略】：设置基础过期时间 + 随机波动时间，防止大量热点缓存同时过期导致雪崩
            long baseExpireSeconds = 300; // 基础 5 分钟
            long randomFluctuation = (long) (Math.random() * 120); // 0~2分钟的随机波动
            long finalExpireSeconds = baseExpireSeconds + randomFluctuation;

            redisTemplate.opsForValue().set(cacheKey, cachedData, finalExpireSeconds, TimeUnit.SECONDS);

            // 6. 写入本地缓存
            localCache.put(cacheKey, cachedData);

            return cachedData;
        }
    }

    /**
     * 抽取出的底层 Top K 计算逻辑：利用 PriorityQueue (最小堆) 获取 Top K 热搜词
     * 时间复杂度：O(N * logK)
     */
    private List<Map<String, Object>> calculateTopKFromSource(int k) {
        // 维护一个大小为 k 的最小堆
        PriorityQueue<Map.Entry<String, Integer>> minHeap = new PriorityQueue<>(
                Comparator.comparingInt(Map.Entry::getValue)
        );

        for (Map.Entry<String, Integer> entry : searchCounts.entrySet()) {
            minHeap.offer(entry);
            // 如果堆的大小超过了 K，弹出频次最小的，剩下的就是频次最大的 K 个
            if (minHeap.size() > k) {
                minHeap.poll();
            }
        }

        // 堆中元素弹出的顺序是从小到大，我们需要反转它（从大到小排列）
        List<Map<String, Object>> result = new ArrayList<>();
        while (!minHeap.isEmpty()) {
            Map.Entry<String, Integer> entry = minHeap.poll();
            Map<String, Object> map = new HashMap<>();
            map.put("keyword", entry.getKey());
            map.put("count", entry.getValue());
            result.add(0, map); // 每次插到头部，实现降序
        }

        return result;
    }
}