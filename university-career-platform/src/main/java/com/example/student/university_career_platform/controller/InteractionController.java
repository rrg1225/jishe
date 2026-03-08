package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.model.Comment;
import com.example.student.university_career_platform.model.LikeRecord;
import com.example.student.university_career_platform.model.User;
import com.example.student.university_career_platform.repository.CommentRepository;
import com.example.student.university_career_platform.repository.LikeRecordRepository;
import com.example.student.university_career_platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/interaction")
@CrossOrigin
public class InteractionController {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private LikeRecordRepository likeRecordRepository;
    @Autowired
    private UserRepository userRepository;

    // 🌟 终极防御：安全获取当前用户手机号的方法，彻底解决 NullPointerException
    private String getCurrentUserPhone() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // 如果没登录，或者认证信息为空，或者等于匿名用户，统一返回 null
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            return null;
        }
        return authentication.getName();
    }

    // ================= 1. 评论模块 =================

    public static class CommentRequest {
        public String targetId;
        public String content;
    }

    @PostMapping("/comment")
    public ResponseEntity<?> addComment(@RequestBody CommentRequest request) {
        // 安全获取用户信息
        String currentPhone = getCurrentUserPhone();
        if (currentPhone == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("请先登录后再发表评论");
        }

        User user = userRepository.findByPhone(currentPhone).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("用户不存在，请重新登录");
        }

        Comment comment = new Comment();
        comment.setTargetId(request.targetId);
        comment.setContent(request.content);
        comment.setAuthorPhone(currentPhone);
        comment.setAuthorName(user.getNickName());
        comment.setAuthorAvatar(user.getAvatar());
        // gmtCreated 会由 @PrePersist 自动生成，这里不需要 set 了

        commentRepository.save(comment);
        return ResponseEntity.ok("评论发布成功！");
    }

    @GetMapping("/comments")
    public ResponseEntity<?> getComments(@RequestParam String targetId) {
        // 调用 Repository 里的最新方法
        return ResponseEntity.ok(commentRepository.findByTargetIdOrderByGmtCreatedDesc(targetId));
    }

    // ================= 2. 点赞模块 =================

    @PostMapping("/like/toggle")
    @Transactional
    public ResponseEntity<?> toggleLike(@RequestParam String targetId) {
        // 安全获取用户信息
        String currentPhone = getCurrentUserPhone();
        if (currentPhone == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("请先登录后再点赞");
        }

        boolean alreadyLiked = likeRecordRepository.existsByTargetIdAndUserPhone(targetId, currentPhone);
        if (alreadyLiked) {
            likeRecordRepository.deleteByTargetIdAndUserPhone(targetId, currentPhone);
            return ResponseEntity.ok("取消点赞");
        } else {
            LikeRecord like = new LikeRecord();
            like.setTargetId(targetId);
            like.setUserPhone(currentPhone);
            likeRecordRepository.save(like);
            return ResponseEntity.ok("点赞成功");
        }
    }

    @GetMapping("/like/status")
    public ResponseEntity<?> getLikeStatus(@RequestParam String targetId) {
        long totalLikes = likeRecordRepository.countByTargetId(targetId);
        boolean isLikedByMe = false;

        String currentPhone = getCurrentUserPhone();
        // 只有当前真正有登录用户时，才去查他有没有点过赞
        if (currentPhone != null) {
            isLikedByMe = likeRecordRepository.existsByTargetIdAndUserPhone(targetId, currentPhone);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalLikes", totalLikes);
        result.put("isLiked", isLikedByMe);
        return ResponseEntity.ok(result);
    }
}