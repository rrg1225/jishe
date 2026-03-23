package com.example.student.university_career_platform.repository;

import com.example.student.university_career_platform.model.Content;
import org.springframework.data.domain.Page;      // 🌟 必须是这个
import org.springframework.data.domain.Pageable;  // 🌟 必须是这个
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<Content, Long> {

    // 根据分类和类型分页查询
    @SuppressWarnings("unused")
    Page<Content> findByCategoryAndType(String category, String type, Pageable pageable);

    // 根据分类分页查询（用于保研/留学等大板块）
    Page<Content> findByCategory(String category, Pageable pageable);

    // 加上这行代码：根据分类和标题模糊查询
    Page<Content> findByCategoryAndTitleContaining(String category, String title, Pageable pageable);

    // 根据内容类型分页查询（用于专门提取视频做推荐）
    Page<Content> findByType(String type, Pageable pageable);
}