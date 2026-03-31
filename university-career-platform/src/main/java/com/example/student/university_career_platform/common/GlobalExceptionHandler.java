package com.example.student.university_career_platform.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 企业级优化：AOP 全局异常处理器
 * 将所有的异常拦截在这一层，保证给前端返回永远是统一的 Result JSON 结构
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public Result<?> handleGlobalException(Exception e) {
        // 打印异常堆栈（企业中这里通常换成 log.error("系统异常", e)）
        System.err.println("【全局异常拦截】: " + e.getMessage());
        e.printStackTrace();

        // 统一返回友好的 JSON 格式，防止前端因为收到 500 HTML 报错页而解析崩溃
        return Result.error(500, "服务器开小差了，请稍后再试：" + e.getMessage());
    }
}