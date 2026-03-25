package com.example.student.university_career_platform.repository;

import com.example.student.university_career_platform.model.ResumeTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResumeTemplateRepository extends JpaRepository<ResumeTemplate , Long> {

    List<ResumeTemplate> findByType(Integer type);
}
