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

        // 🚀 终极免检补丁：看到白名单路径或者跨域预检，直接放行，连 Authorization 头都不看！
        if (path.startsWith("/api/auth") ||
                path.startsWith("/api/chat") ||
                path.startsWith("/api/resume") ||
                path.startsWith("/error") ||   // 🚀 加上这行
                "OPTIONS".equalsIgnoreCase(request.getMethod())) {

            filterChain.doFilter(request, response);
            return; // 直接 return，跳过下面的全部 Jwt 解析逻辑
        }

        // ==========================================
        // 下面保留你原本的代码，一点都不用改
        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        try {
            String phone = jwtUtil.extractPhone(token);
            if (phone != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtUtil.validateToken(token)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            phone, null, new ArrayList<>()
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    System.out.println("✅ [后端安检通过] 用户手机号: " + phone + "，允许访问！");
                } else {
                    System.err.println("❌ [后端安检拦截] Token 验证未通过（可能是假冒的或已失效）");
                }
            }
        } catch (Exception e) {
            System.err.println("❌ [后端安检报错] Token 解析彻底失败！报错信息: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}