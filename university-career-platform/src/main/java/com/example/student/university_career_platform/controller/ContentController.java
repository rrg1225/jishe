package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.common.Result;
import com.example.student.university_career_platform.model.Content;
import com.example.student.university_career_platform.model.User;
import com.example.student.university_career_platform.repository.ContentRepository;
import com.example.student.university_career_platform.repository.UserRepository;
import com.example.student.university_career_platform.service.HotSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/content")
@CrossOrigin
public class ContentController {

    @Autowired
    private ContentRepository contentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private HotSearchService hotSearchService;

    private static final List<Map<String, Object>> contestCache = new CopyOnWriteArrayList<>();

    @PostMapping("/updateContests")
    public Result<?> updateContests(@RequestBody List<Map<String, Object>> data) {
        if (data != null && !data.isEmpty()) {
            contestCache.clear();
            contestCache.addAll(data);
            return Result.success("后端已收到竞赛推送，内存刷新成功");
        }
        return Result.error("接收数据为空");
    }

    @PostMapping("/publish")
    public Result<?> publishContent(@RequestBody Content request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return Result.error("请先登录后发布内容");
        }

        String phone = authentication.getName();
        User user = userRepository.findByPhone(phone).orElse(null);

        request.setAuthorPhone(phone);
        request.setAuthorName(user != null ? user.getNickName() : "匿名校友");

        if (!StringUtils.hasText(request.getCategory())) return Result.error("内容分类不能为空");
        if (!StringUtils.hasText(request.getTitle())) return Result.error("内容标题不能为空");

        contentRepository.save(request);
        return Result.success("发布成功");
    }

    @GetMapping("/list")
    public Result<?> getContentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword
    ) {
        if ("竞赛信息大厅".equals(category)) {
            return Result.success(contestCache);
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("gmtCreated").descending());
        Page<Content> resultPage;

        if (StringUtils.hasText(category) && StringUtils.hasText(keyword)) {
            resultPage = contentRepository.findByCategoryAndTitleContaining(category, keyword, pageable);
        } else if (StringUtils.hasText(category)) {
            resultPage = contentRepository.findByCategory(category, pageable);
        } else {
            resultPage = contentRepository.findAll(pageable);
        }
        return Result.success(resultPage);
    }

    @GetMapping("/video/recommend")
    public Result<?> getVideoRecommendations() {
        Pageable topFive = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "gmtCreated"));
        Page<Content> videoPage = contentRepository.findByType("video", topFive);

        List<Map<String, Object>> recommendVideos = videoPage.getContent().stream().map(video -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", video.getId());
            map.put("title", video.getTitle());
            map.put("author", video.getAuthorName() != null ? video.getAuthorName() : "全栈小导师");
            map.put("url", video.getVideoUrl());
            map.put("coverUrl", "https://picsum.photos/100/56?random=" + video.getId());
            map.put("duration", "12:34");
            map.put("views", (int)(Math.random() * 10000) + 500);
            return map;
        }).collect(Collectors.toList());

        List<Map<String, Object>> hotTopics = List.of(
                Map.of("title", "2026全国大学生计算机设计大赛", "hotValue", 124000),
                Map.of("title", "考研数学全程复习规划", "hotValue", 87000),
                Map.of("title", "Java并发编程实战", "hotValue", 62000),
                Map.of("title", "字节跳动春招内推", "hotValue", 51000),
                Map.of("title", "Vue3企业级开发模板", "hotValue", 43000)
        );

        Map<String, Object> response = new HashMap<>();
        response.put("recommendVideos", recommendVideos);
        response.put("hotTopics", hotTopics);
        return Result.success(response);
    }

    // 🌟 已净化：去掉 try-catch
    @GetMapping("/hot-search")
    public Result<List<Map<String, Object>>> getHotSearches() {
        List<Map<String, Object>> top8 = hotSearchService.getTopKHotSearches(8);
        return Result.success(top8);
    }

    // 🌟 已净化：去掉 try-catch
    @GetMapping("/search")
    public Result<?> searchContent(@RequestParam String keyword) {
        hotSearchService.recordSearch(keyword);
        return Result.success("假设这是你原有的返回结果");
    }
}