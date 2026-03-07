package com.example.student.university_career_platform.config;

import com.example.student.university_career_platform.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
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
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 从前端请求的 Header 中寻找名为 "Authorization" 的字段
        String header = request.getHeader("Authorization");

        // 2. 国际惯例：JWT Token 通常以 "Bearer " 开头，检查是否符合规范
        if (header != null && header.startsWith("Bearer ")) {
            // 3. 把前缀 "Bearer " 砍掉（占7个字符），剩下的就是纯纯的 Token 字符串
            String token = header.substring(7);

            // 4. 把 Token 塞进我们的印钞机里验一验真伪
            if (jwtUtil.validateToken(token)) {
                // 如果是真的，解析出里面的手机号
                String phone = jwtUtil.extractPhone(token);

                // 5. 拿着手机号告诉 Spring 的安保中心：“这个人我查过了，身份没问题，放行！”
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(phone, null, new ArrayList<>());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 6. 检查完毕，无论放不放行，都让请求继续往下走（交给后面的规则判断）
        filterChain.doFilter(request, response);
    }
}