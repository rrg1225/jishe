package com.example.student.university_career_platform.aspect;

import com.example.student.university_career_platform.annotation.LogRecord;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;

/**
 * 接口访问性能监控与日志追踪切面
 */
@Aspect
@Component
public class LogAspect {

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    @Autowired
    private ObjectMapper objectMapper;

    @Around("@annotation(com.example.student.university_career_platform.annotation.LogRecord)")
    public Object around(ProceedingJoinPoint point) throws Throwable {
        long beginTime = System.currentTimeMillis();

        // 1. 获取请求上下文
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes != null ? attributes.getRequest() : null;

        // 2. 执行原方法（即真正去执行 Controller 里的代码）
        Object result = point.proceed();

        // 3. 计算接口耗时
        long time = System.currentTimeMillis() - beginTime;

        // 4. 异步打印标准化企业级日志
        recordLog(point, time, request, result);

        return result;
    }

    private void recordLog(ProceedingJoinPoint joinPoint, long time, HttpServletRequest request, Object result) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();
            LogRecord logAnnotation = method.getAnnotation(LogRecord.class);

            String operation = logAnnotation != null ? logAnnotation.value() : "未知操作";
            String className = joinPoint.getTarget().getClass().getName();
            String methodName = signature.getName();
            String ip = request != null ? request.getRemoteAddr() : "未知IP";

            // 使用专业的日志框架输出
            log.info("===================== 企业级监控日志 =====================");
            log.info("接口描述 : {}", operation);
            log.info("请求方法 : {}.{}()", className, methodName);
            log.info("请求 IP  : {}", ip);
            log.info("执行耗时 : {} ms", time);
            log.info("=========================================================");

        } catch (Exception e) {
            log.error("日志记录异常", e);
        }
    }
}
