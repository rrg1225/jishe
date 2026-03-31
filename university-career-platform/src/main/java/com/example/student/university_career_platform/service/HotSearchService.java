package com.example.student.university_career_platform.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


@Service
public class HotSearchService {

    // 使用线程安全的哈希表，记录全网用户的搜索词频次
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
        searchCounts.put(cleanKeyword, searchCounts.getOrDefault(cleanKeyword, 0) + 1);
    }

    /**
     * 🌟 核心算法：利用 PriorityQueue (最小堆) 获取 Top K 热搜词
     * 时间复杂度：O(N * logK)，远优于全局排序的 O(N * logN)
     */
    public List<Map<String, Object>> getTopKHotSearches(int k) {
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