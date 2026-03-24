package com.example.student.university_career_platform.repository;

import com.example.student.university_career_platform.model.JobOffer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobOfferRepository extends JpaRepository<JobOffer, Long> {

    // Spring Data JPA 自动帮你实现这个方法，按创建时间倒序查询所有岗位（把最新的岗位排在最前面）
    List<JobOffer> findAllByOrderByGmtCreatedDesc();

}
