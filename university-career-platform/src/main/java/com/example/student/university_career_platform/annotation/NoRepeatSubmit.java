package com.example.student.university_career_platform.annotation;

import java.lang.annotation.*;

/**
 * 企业级 Redis 接口防刷/幂等性注解
 * 防止用户利用脚本或网络卡顿疯狂连点导致数据库脏数据
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface NoRepeatSubmit {
    /**
     * 默认锁定时间 3 秒（3秒内同一个用户相同的请求将被拦截）
     */
    long lockTime() default 3;
}

