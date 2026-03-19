package com.example.student.university_career_platform.repository;

import com.example.student.university_career_platform.model.ArticleData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleDataRepository extends JpaRepository<ArticleData, Long> {

    // 帮你写好了一个自定义查询：根据竞赛类别(topic)查找文章
    List<ArticleData> findByTopic(String topic);
}