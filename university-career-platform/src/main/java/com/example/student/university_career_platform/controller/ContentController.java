package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.model.Content;
import com.example.student.university_career_platform.model.User;
import com.example.student.university_career_platform.repository.ContentRepository;
import com.example.student.university_career_platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/content")
@CrossOrigin
public class ContentController {

    @Autowired
    private ContentRepository contentRepository;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/publish")
    public ResponseEntity<?> publishContent(@RequestBody Content request) {
        // 安全获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("请先登录");
        }
        String phone = authentication.getName();
        User user = userRepository.findByPhone(phone).orElse(null);

         // 将作者信息填入内容对象中
        request.setAuthorPhone(phone);
        request.setAuthorName(user != null ? user.getNickName() : "匿名校友");

        // 存入数据库
        contentRepository.save(request);
        return ResponseEntity.ok("发布成功");
    }

    @GetMapping("/list")
    public ResponseEntity<?> getContentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword // 🌟 新增这个接收搜索词
    ) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("gmtCreated").descending());

        // 🌟 如果传了分类和搜索词
        if (category != null && keyword != null && !keyword.isEmpty()) {
            return ResponseEntity.ok(contentRepository.findByCategoryAndTitleContaining(category, keyword, pageable));
        }
        // 原来的普通分类查询
        else if (category != null) {
            return ResponseEntity.ok(contentRepository.findByCategory(category, pageable));
        }

        return ResponseEntity.ok(contentRepository.findAll(pageable));
    }

// 视频侧边栏推荐接口
    @GetMapping("/video/recommend")
    public ResponseEntity<?> getVideoRecommendations() {
        // 1. 获取最新发布的5个视频作为推荐
        Pageable topFive = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "gmtCreated"));
        Page<Content> videoPage = contentRepository.findByType("video", topFive);

        // 2. 将 Content 实体映射为前端期望的 JSON 格式
        List<Map<String, Object>> recommendVideos = videoPage.getContent().stream().map(video -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", video.getId());
            map.put("title", video.getTitle());
            map.put("author", video.getAuthorName() != null ? video.getAuthorName() : "全栈小导师");
            map.put("url", video.getVideoUrl());

            // 下面这些是你的 Content 实体里目前没有的字段，暂时用占位数据填充，保证前端不出错
            // 如果以后你在 Content.java 加上了这些字段，这里直接换成 video.getCoverUrl() 即可
            map.put("coverUrl", "https://picsum.photos/100/56?random=" + video.getId());
            map.put("duration", "12:34");
            map.put("views", (int)(Math.random() * 100000) + 10000); // 随机生成个播放量

            return map;
        }).collect(Collectors.toList());

        // 3. 模拟大家都在学（热门话题），也可以去数据库查浏览量最高的文章
        List<Map<String, Object>> hotTopics = List.of(
                Map.of("title", "考研数学全程复习规划", "hotValue", 124000),
                Map.of("title", "Java并发编程实战", "hotValue", 87000),
                Map.of("title", "Vue3源码解析", "hotValue", 62000),
                Map.of("title", "产品经理入门指南", "hotValue", 51000),
                Map.of("title", "数据分析师必备技能", "hotValue", 43000)
        );

        // 4. 组装返回结果
        Map<String, Object> response = new HashMap<>();
        response.put("recommendVideos", recommendVideos);
        response.put("hotTopics", hotTopics);

        return ResponseEntity.ok(response);
    }
}
