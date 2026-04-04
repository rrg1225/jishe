package com.example.student.university_career_platform.config;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean("hotSearchLocalCache")
    public Cache<String, Object> hotSearchLocalCache() {
        return Caffeine.newBuilder()
                // 本地缓存过期时间略短于 Redis，防止数据不一致过久
                .expireAfterWrite(3, TimeUnit.MINUTES)
                .initialCapacity(100)
                .maximumSize(1000)
                .build();
    }
}