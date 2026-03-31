package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.model.LikeRecord;
import com.example.student.university_career_platform.repository.LikeRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

/**
 * 企业级高并发互动服务：Redis 旁路缓存 + 异步落盘架构
 */
@Service
public class RedisLikeService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private LikeRecordRepository likeRecordRepository;

    // 🌟 高级架构技巧：注入自身代理对象，解决 Spring AOP 同类方法调用时 @Async 失效的经典坑！
    @Autowired
    @Lazy
    private RedisLikeService self;

    /**
     * 1. 获取点赞总数 (先查 Redis，缓存未命中再查 MySQL 兜底)
     */
    public long getLikeCount(String targetId) {
        String countKey = "like:count:" + targetId;
        String countStr = redisTemplate.opsForValue().get(countKey);

        if (countStr != null) {
            return Long.parseLong(countStr); // 毫秒级返回
        }

        // 缓存穿透兜底：查数据库，并回写到 Redis，缓存 2 小时
        long dbCount = likeRecordRepository.countByTargetId(targetId);
        redisTemplate.opsForValue().set(countKey, String.valueOf(dbCount), 2, TimeUnit.HOURS);
        return dbCount;
    }

    /**
     * 2. 获取当前用户是否点过赞
     */
    public boolean isLiked(String targetId, String userPhone) {
        if (userPhone == null) return false;
        String statusKey = "like:status:" + targetId + ":" + userPhone;
        String statusStr = redisTemplate.opsForValue().get(statusKey);

        if (statusStr != null) {
            return "1".equals(statusStr); // 毫秒级返回
        }

        // 缓存穿透兜底：查数据库
        boolean isLikedDb = likeRecordRepository.existsByTargetIdAndUserPhone(targetId, userPhone);
        redisTemplate.opsForValue().set(statusKey, isLikedDb ? "1" : "0", 2, TimeUnit.HOURS);
        return isLikedDb;
    }

    /**
     * 3. 核心：执行点赞/取消点赞 (极速操作 Redis，不阻塞主线程)
     */
    public boolean toggleLike(String targetId, String userPhone) {
        boolean currentStatus = isLiked(targetId, userPhone);
        boolean newStatus = !currentStatus;

        String countKey = "like:count:" + targetId;
        String statusKey = "like:status:" + targetId + ":" + userPhone;

        if (newStatus) {
            // 点赞：总数+1，状态设为1
            redisTemplate.opsForValue().increment(countKey);
            redisTemplate.opsForValue().set(statusKey, "1", 2, TimeUnit.HOURS);
        } else {
            // 取消点赞：总数-1，状态设为0
            redisTemplate.opsForValue().decrement(countKey);
            redisTemplate.opsForValue().set(statusKey, "0", 2, TimeUnit.HOURS);
        }

        // 🌟 核心：丢给后台线程慢慢去写 MySQL，主线程瞬间将成功状态返回给用户前端！
        self.saveToDbAsync(targetId, userPhone, newStatus);

        return newStatus;
    }

    /**
     * 4. 异步落盘任务 (火灾隔离区：即使数据库卡顿，也不会影响前端用户的点赞体验)
     */
    @Async
    @Transactional
    public void saveToDbAsync(String targetId, String userPhone, boolean isLike) {
        if (isLike) {
            // 防御性检查，防止唯一约束冲突
            if (!likeRecordRepository.existsByTargetIdAndUserPhone(targetId, userPhone)) {
                LikeRecord like = new LikeRecord();
                like.setTargetId(targetId);
                like.setUserPhone(userPhone);
                likeRecordRepository.save(like);
            }
        } else {
            likeRecordRepository.deleteByTargetIdAndUserPhone(targetId, userPhone);
        }
    }
}
