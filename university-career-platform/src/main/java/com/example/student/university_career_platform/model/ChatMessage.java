package com.example.student.university_career_platform.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "chat_message")
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderId;
    private String receiverId;

    // 🌟 新增：消息类型
    @Column(name = "message_type", length = 20)
    private String messageType = "TEXT";

    @Column(columnDefinition = "TEXT")
    private String content; // 如果是文本就是字；如果是图片/文件就是 OSS 上的 URL

    // 🌟 新增：文件相关元数据
    @Column(name = "file_name")
    private String fileName;

    @Column(name = "file_size")
    private Long fileSize;

    private Integer isRead = 0;

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
