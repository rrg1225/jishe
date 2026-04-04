package com.example.student.university_career_platform.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Entity
@Table(name = "interview_session")
public class InterviewSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;

    private String jobTitle; // 面试岗位

    private Long resumeId; // 绑定的简历ID（结合简历提问）

    @Column(columnDefinition = "TEXT")
    private String chatHistory; // 存JSON数组，记录整个[{"q":"..", "a":".."}]的过程

    private Integer currentRound; // 当前第几轮

    private String status; // "ONGOING" 进行中, "FINISHED" 已结束

    @Column(columnDefinition = "TEXT")
    private String finalReport; // 存储大模型生成的 JSON 格式打分报告

    @Column(name = "gmt_created", updatable = false)
    private LocalDateTime gmtCreated;

    @Column(name = "gmt_modified")
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