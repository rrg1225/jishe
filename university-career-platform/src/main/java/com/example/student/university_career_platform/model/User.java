package com.example.student.university_career_platform.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "users")
public class User {

    // ====== 下面是 Getter 和 Setter 方法 (千万别漏了) ======
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(nullable = false)
    @JsonIgnore
    private String password;

    private String role;      // 前端传 'student' 或 'enterprise'，现在对应数据库 varchar，完美！

    private String nickName;  // 对应数据库 nick_name

    private String avatar;

    private String org;

    private Integer age;

    private String gender;

    private String bio; // 个性签名

    @Column(nullable = false)
    private Integer followers = 0; // 粉丝数，默认 0

    @Column(nullable = false)
    private Integer following = 0; // 关注数，默认 0

    @Column(nullable = false)
    private Integer likes = 0; // 获赞数，默认 0

    // Spring 会自动把驼峰映射为下划线：gmtCreated -> gmt_created
    @Column(updatable = false)
    private LocalDateTime gmtCreated;

    private LocalDateTime gmtModified;

    // ===== JPA 自动填充时间的回调方法 =====
    // ===== 极其重要：在下面的 @PrePersist 方法里，确保新用户注册时它们都是 0 =====
    @PrePersist
    protected void onCreate() {
        this.gmtCreated = LocalDateTime.now();
        this.gmtModified = LocalDateTime.now();
        if (this.followers == null) this.followers = 0;
        if (this.following == null) this.following = 0;
        if (this.likes == null) this.likes = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.gmtModified = LocalDateTime.now();
    }

}