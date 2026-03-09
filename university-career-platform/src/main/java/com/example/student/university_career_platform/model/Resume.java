package com.example.student.university_career_platform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Data
@Getter
@Setter
@Table(name = "resumes")
public class Resume {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 核心绑定：这份简历属于哪个用户？
    @Column(name = "user_phone", nullable = false, unique = true)
    private String userPhone;

    // 当前使用的模板名称
    private String templateName;

    // ================== 1. 基础信息 ==================
    private String fullName;
    private String contactPhone;
    private String email;
    private String targetCity;

    // ================== 2. 复杂列表 (存为 JSON 字符串) ==================

    // 教育经历 (存放前端传来的 JSON 数组，如: [{"school":"XX大学", "degree":"本科"}])
    @Column(columnDefinition = "LONGTEXT")
    private String educationInfo;

    // 项目与实习经历
    @Column(columnDefinition = "LONGTEXT")
    private String projectExperience;

    // 专业技能
    @Column(columnDefinition = "LONGTEXT")
    private String professionalSkills;

    // ================== 3. 大厂规范：审计时间戳 ==================
    @Column(name = "gmt_created", nullable = false, updatable = false)
    private LocalDateTime gmtCreated;

    @Column(name = "gmt_modified", nullable = false)
    private LocalDateTime gmtModified;

    @PrePersist
    protected void onCreate() {
        this.gmtCreated = LocalDateTime.now();
        this.gmtModified = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.gmtModified = LocalDateTime.now();
    }
}
