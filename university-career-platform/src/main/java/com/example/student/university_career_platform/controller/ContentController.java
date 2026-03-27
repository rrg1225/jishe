package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.common.Result;
import com.example.student.university_career_platform.model.Content;
import com.example.student.university_career_platform.model.User;
import com.example.student.university_career_platform.repository.ContentRepository;
import com.example.student.university_career_platform.repository.UserRepository;
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

    // 🌟 核心：竞赛大厅专属内存缓冲区（对接 Python 引擎，脱离 MySQL）
    private static final List<Map<String, Object>> contestCache = new CopyOnWriteArrayList<>();

    /**
     * 🌟 新增：接收 Python 脚本推送过来的实时竞赛数据（带官网链接）
     */
    @PostMapping("/updateContests")
    public Result<?> updateContests(@RequestBody List<Map<String, Object>> data) {
        if (data != null && !data.isEmpty()) {
            contestCache.clear();
            contestCache.addAll(data);
            return Result.success("后端已收到竞赛推送，内存刷新成功");
        }
        return Result.error("接收数据为空");
    }

    /**
     * 发布内容（保留原有的数据库发布逻辑，供其他板块使用）
     */
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

        if (!StringUtils.hasText(request.getCategory())) {
            return Result.error("内容分类不能为空");
        }
        if (!StringUtils.hasText(request.getTitle())) {
            return Result.error("内容标题不能为空");
        }

        contentRepository.save(request);
        return Result.success("发布成功");
    }

    /**
     * 获取内容列表（改造：竞赛大厅走内存，其他走数据库）
     */
    @GetMapping("/list")
    public Result<?> getContentList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword
    ) {
        // 🌟 拦截：如果前端请求的是“竞赛信息大厅”，直接返回 Python 推送到内存里的数据！
        if ("竞赛信息大厅".equals(category)) {
            return Result.success(contestCache);
        }

        // ================= 其他分类依然走数据库 =================
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

    /**
     * 视频侧边栏推荐接口（保留原有逻辑不动）
     */
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
}