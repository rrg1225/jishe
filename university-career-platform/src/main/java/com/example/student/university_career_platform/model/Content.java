package com.example.student.university_career_platform.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "contents")
public class Content {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;      // 标题
    private String type;       // 类型: article 或 video
    private String category;   // 专区: matrix, job, contest
    private String authorPhone; // 发布者手机号
    private String authorName;  // 发布者昵称

    @Column(columnDefinition = "LONGTEXT")
    private String content;    // 图文内容的HTML字符串，或者视频的简介

    private String videoUrl;   //绝对路径

    @Column(name = "gmt_created", updatable = false)
    private LocalDateTime gmtCreated;

    @PrePersist
    protected void onCreate() {
        this.gmtCreated = LocalDateTime.now();
    }
}
