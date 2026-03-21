package com.example.student.university_career_platform.config;

import com.example.student.university_career_platform.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // 🚀 修复：加入了 "/chat" 以放行 WebSocket 请求
        if (path.startsWith("/api/auth") ||
                path.startsWith("/api/chat") ||
                path.startsWith("/chat") ||     // 放行 WebSocket
                path.startsWith("/api/resume") ||
                path.startsWith("/error") ||
                "OPTIONS".equalsIgnoreCase(request.getMethod())) {

            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            // 🚀 优化：只要 extractPhone 不报错，Token 就是合法的，无需重复校验
            String phone = jwtUtil.extractPhone(token);
            if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        phone, null, new ArrayList<>()
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("✅ [后端安检通过] 用户手机号: " + phone + "，允许访问！");
            }
        } catch (Exception e) {
            // 解析失败、签名错误、Token 过期都会进入这里
            System.err.println("❌ [后端安检拦截] Token 验证失败或已过期: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}