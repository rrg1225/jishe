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
    private StringRedisTemplate redisTemplate;

    private static final String REDIS_KEY_KAOYAN = "board:realtime:kaoyan";

    // 🌟 已净化：去掉 try-catch
    @PostMapping("/updateData")
    public ResponseEntity<String> receiveScrapedData(@RequestBody List<Map<String, Object>> universityList) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        String jsonData = mapper.writeValueAsString(universityList);

        redisTemplate.opsForValue().set(REDIS_KEY_KAOYAN, jsonData);
        webSocketServer.sendAllMessage(jsonData);

        return ResponseEntity.ok("数据已更新至 Redis 并完成实时广播");
    }

    @GetMapping("/getLatestData")
    public ResponseEntity<String> getLatestData() {
        String data = redisTemplate.opsForValue().get(REDIS_KEY_KAOYAN);
        if (data == null) {
            return ResponseEntity.ok("[]");
        }
        return ResponseEntity.ok(data);
    }
}