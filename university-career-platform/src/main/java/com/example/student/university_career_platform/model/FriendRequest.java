package com.example.student.university_career_platform.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "friend_request")
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderId;

    private String receiverId;

    // PENDING(待处理), ACCEPTED(已同意), REJECTED(已拒绝)
    private String status = "PENDING";

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
