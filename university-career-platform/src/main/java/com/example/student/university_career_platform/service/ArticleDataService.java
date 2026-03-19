package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.model.ArticleData;
import com.example.student.university_career_platform.repository.ArticleDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ArticleDataService {

    @Autowired
    private ArticleDataRepository articleDataRepository;

    // 1. 获取所有文章
    public List<ArticleData> getAllArticles() {
        return articleDataRepository.findAll();
    }

    // 2. 根据竞赛主题查询文章（比如前端只看"数学建模"的文章）
    public List<ArticleData> getArticlesByTopic(String topic) {
        return articleDataRepository.findByTopic(topic);
    }

    // 3. 根据 ID 获取单篇文章详情
    public ArticleData getArticleById(Long id) {
        return articleDataRepository.findById(id).orElse(null);
    }
}
