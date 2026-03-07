package com.example.student.university_career_platform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String phone;

    @Column(nullable = false)
    private String password;

    private String role;      // 前端传 'student' 或 'enterprise'，现在对应数据库 varchar，完美！

    private String nickName;  // 对应数据库 nick_name

    private String avatar;

    private String org;

    private Integer age;

    private String gender;

    @Column(length = 255) // 签名长一点
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

    // ====== 下面是 Getter 和 Setter 方法 (千万别漏了) ======
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getNickName() { return nickName; }
    public void setNickName(String nickname) { this.nickName = nickName; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public String getOrg() { return org; }
    public void setOrg(String org) { this.org = org; }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public Integer getFollowers() { return followers; }
    public void setFollowers(Integer followers) { this.followers = followers; }

    public Integer getFollowing() { return following; }
    public void setFollowing(Integer following) { this.following = following; }

    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }

    public LocalDateTime getGmtCreated() { return gmtCreated; }
    public void setGmtCreated(LocalDateTime gmtCreated) { this.gmtCreated = gmtCreated; }

    public LocalDateTime getGmtModified() { return gmtModified; }
    public void setGmtModified(LocalDateTime gmtModified) { this.gmtModified = gmtModified; }
}