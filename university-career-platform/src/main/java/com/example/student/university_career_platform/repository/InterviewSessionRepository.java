package com.example.student.university_career_platform.repository;

import com.example.student.university_career_platform.model.InterviewSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterviewSessionRepository extends JpaRepository<InterviewSession, Long> {
    // 继承了 JpaRepository，Spring Boot 会自动帮你实现所有的增删改查方法，不需要你写任何具体的 SQL 代码
}
