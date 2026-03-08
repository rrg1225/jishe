package com.example.student.university_career_platform.repository;

import com.example.student.university_career_platform.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 修改为：OrderByGmtCreatedDesc
    List<Comment> findByTargetIdOrderByGmtCreatedDesc(String targetId);
}