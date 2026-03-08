package com.example.student.university_career_platform.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Data
@Table(name = "like_records")
public class LikeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 目标ID：点赞的是哪个视频/文章
    private String targetId;

    // 谁点赞的（用手机号记录，防止同一个人重复点赞）
    private String userPhone;

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