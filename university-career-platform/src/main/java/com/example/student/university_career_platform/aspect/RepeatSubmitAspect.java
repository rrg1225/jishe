package com.example.student.university_career_platform.aspect;

import com.example.student.university_career_platform.annotation.NoRepeatSubmit;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.TimeUnit;

/**
 * 接口防刷与幂等性切面 (基于 Redis 分布式锁机制)
 */
@Aspect
@Component
public class RepeatSubmitAspect {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Around("@annotation(noRepeatSubmit)")
    public Object around(ProceedingJoinPoint point, NoRepeatSubmit noRepeatSubmit) throws Throwable {

        // 1. 获取请求特征
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        if (request == null) {
            return point.proceed(); // 非 Web 请求直接放行
        }

        // 2. 组装唯一的 Redis 锁 Key (防刷特征值)
        String ip = request.getRemoteAddr();
        String path = request.getRequestURI();

        // 获取当前用户账号（如果有），没有则用 IP 兜底
        String userAccount = ip;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            userAccount = auth.getName();
        }

        // 生成锁的 Key，例如： repeat_submit:/api/interaction/comment:13800138000
        String lockKey = "repeat_submit:" + path + ":" + userAccount;

        // 3. 执行 Redis 分布式锁原子操作 SETNX
        long lockTime = noRepeatSubmit.lockTime();
        Boolean isSuccess = redisTemplate.opsForValue().setIfAbsent(lockKey, "LOCKED", lockTime, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(isSuccess)) {
            // 锁获取失败，说明在 lockTime 秒内已经有完全相同的请求进来了，直接拒绝！
            throw new RuntimeException("操作过于频繁，请 " + lockTime + " 秒后再试！");
        }

        // 4. 获取锁成功，放行去执行原 Controller 逻辑
        return point.proceed();
    }
}
