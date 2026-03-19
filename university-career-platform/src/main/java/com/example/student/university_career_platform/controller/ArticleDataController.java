package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.model.ArticleData;
import com.example.student.university_career_platform.service.ArticleDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/articles")
@CrossOrigin // 允许跨域请求，前端是Vue或小程序时必须加
public class ArticleDataController {

    @Autowired
    private ArticleDataService articleDataService;

    // 接口 1: 获取全部竞赛文章 (GET /api/articles)
    @GetMapping
    public ResponseEntity<List<ArticleData>> getAll() {
        return ResponseEntity.ok(articleDataService.getAllArticles());
    }

    // 接口 2: 获取指定主题的文章 (GET /api/articles/topic?name=数学建模)
    @GetMapping("/topic")
    public ResponseEntity<List<ArticleData>> getByTopic(@RequestParam String name) {
        return ResponseEntity.ok(articleDataService.getArticlesByTopic(name));
    }

    // 接口 3: 获取某篇文章详情 (GET /api/articles/1)
    @GetMapping("/{id}")
    public ResponseEntity<ArticleData> getById(@PathVariable Long id) {
        ArticleData article = articleDataService.getArticleById(id);
        if (article != null) {
            return ResponseEntity.ok(article);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}