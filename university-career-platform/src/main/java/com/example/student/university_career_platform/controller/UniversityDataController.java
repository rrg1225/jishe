package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.service.WebSocketServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/university")
@CrossOrigin
public class UniversityDataController {

    @Autowired
    private WebSocketServer webSocketServer;

    @Autowired
    private StringRedisTemplate redisTemplate; // 引入 Redis

    private static final String REDIS_KEY_KAOYAN = "board:realtime:kaoyan";

    /**
     * 1. 接收爬虫数据 (写接口)
     */
    @PostMapping("/updateData")
    public ResponseEntity<String> receiveScrapedData(@RequestBody List<Map<String, Object>> universityList) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String jsonData = mapper.writeValueAsString(universityList);

            // 【关键新增】：将最新数据持久化到 Redis，永不丢失
            redisTemplate.opsForValue().set(REDIS_KEY_KAOYAN, jsonData);

            // 广播给所有在线的老用户
            webSocketServer.sendAllMessage(jsonData);

            return ResponseEntity.ok("数据已更新至 Redis 并完成实时广播");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("处理失败: " + e.getMessage());
        }
    }

    /**
     * 2. 提供给前端初次加载时拉取快照的接口 (读接口)
     */
    @GetMapping("/getLatestData")
    public ResponseEntity<String> getLatestData() {
        // 直接从内存数据库 Redis 中秒读，不查 MySQL，性能极高
        String data = redisTemplate.opsForValue().get(REDIS_KEY_KAOYAN);
        if (data == null) {
            return ResponseEntity.ok("[]");
        }
        return ResponseEntity.ok(data);
    }
}