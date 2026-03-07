package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.model.User;
import com.example.student.university_career_platform.repository.UserRepository;
import com.example.student.university_career_platform.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    // 🌟 修正1：接收前端传来的 username（手机号）
    public static class RegisterRequest {
        public String phone;
        public String password;
        public String role;
        public String org;
        public String username;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String registerPhone = request.phone != null ? request.phone : request.username;

        if (userRepository.existsByPhone(registerPhone)) {
            return ResponseEntity.badRequest().body("该手机号已被注册！");
        }

        User newUser = new User();
        newUser.setPhone(registerPhone);
        newUser.setPassword(passwordEncoder.encode(request.password));
        newUser.setRole(request.role);
        newUser.setOrg(request.org);

        // 注册时给个默认的临时昵称，比如 "用户9610"
        if (registerPhone != null && registerPhone.length() >= 4) {
            newUser.setNickName("用户" + registerPhone.substring(registerPhone.length() - 4));
        } else {
            newUser.setNickName("新用户");
        }

        userRepository.save(newUser);
        return ResponseEntity.ok("注册成功！");
    }

    // 🌟 修正2：登录时前端传的也是 username
    public static class LoginRequest {
        public String username;
        public String password;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<User> userOptional = userRepository.findByPhone(request.username);

        if (userOptional.isEmpty()) {
            return ResponseEntity.status(401).body("账号不存在");
        }

        User dbUser = userOptional.get();

        if (!passwordEncoder.matches(request.password, dbUser.getPassword())) {
            return ResponseEntity.status(401).body("密码错误");
        }

        String token = jwtUtil.generateToken(dbUser.getPhone());

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("username", dbUser.getNickName());
        response.put("role", dbUser.getRole());
        response.put("org", dbUser.getOrg());

        return ResponseEntity.ok(response);
    }
}