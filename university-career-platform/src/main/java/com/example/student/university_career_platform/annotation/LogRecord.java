package com.example.student.university_career_platform.annotation;

import java.lang.annotation.*;

/**
 * 企业级 AOP 日志监控注解
 * 贴在 Controller 方法上，自动记录接口耗时、入参、出参和操作人 IP
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogRecord {
    String value() default ""; // 接口的功能描述，如 "发布帖子"
}
