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
@Table(name = "comments")
public class Comment {

    // ================= 所有的 Getter 和 Setter =================
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String targetId;

    @Column(nullable = false, length = 500)
    private String content;

    private String authorPhone;
    private String authorName;
    private String authorAvatar;

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