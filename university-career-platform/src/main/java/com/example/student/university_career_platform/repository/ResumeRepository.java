package com.example.student.university_career_platform.repository;

import com.example.student.university_career_platform.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {

    // 🌟 核心查询魔法：通过用户的手机号，精准找到他唯一的那份简历
    Optional<Resume> findByUserPhone(String userPhone);

}