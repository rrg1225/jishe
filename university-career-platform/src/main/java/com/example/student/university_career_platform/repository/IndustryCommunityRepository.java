package com.example.student.university_career_platform.repository;

import com.example.student.university_career_platform.model.IndustryCommunity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IndustryCommunityRepository extends JpaRepository<IndustryCommunity, Long> {
    // 继承 JpaRepository 后，自动拥有 save, findById, findAll, delete 等基础方法
}