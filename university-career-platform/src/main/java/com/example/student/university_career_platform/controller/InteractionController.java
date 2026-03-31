package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.annotation.LogRecord;
import com.example.student.university_career_platform.annotation.NoRepeatSubmit;
import com.example.student.university_career_platform.model.Comment;
import com.example.student.university_career_platform.model.User;
import com.example.student.university_career_platform.repository.CommentRepository;
import com.example.student.university_career_platform.repository.UserRepository;
import com.example.student.university_career_platform.service.RedisLikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private UserRepository userRepository;

    // 🌟 注入我们刚刚写好的高并发点赞引擎
    @Autowired
    private RedisLikeService redisLikeService;

    // 🌟 终极防御：安全获取当前用户手机号的方法
    private String getCurrentUserPhone() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
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
    @LogRecord("用户发表文章/帖子评论")
    @NoRepeatSubmit(lockTime = 5)
    public ResponseEntity<?> addComment(@RequestBody CommentRequest request) {
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

        commentRepository.save(comment);
        return ResponseEntity.ok("评论发布成功！");
    }

    @GetMapping("/comments")
    public ResponseEntity<?> getComments(@RequestParam String targetId) {
        return ResponseEntity.ok(commentRepository.findByTargetIdOrderByGmtCreatedDesc(targetId));
    }

    // ================= 2. 高并发点赞模块 (全方位升级) =================

    @PostMapping("/like/toggle")
    @LogRecord("用户进行点赞/取消点赞操作")
    @NoRepeatSubmit(lockTime = 2) // 防止恶意物理连点
    public ResponseEntity<?> toggleLike(@RequestParam String targetId) {
        String currentPhone = getCurrentUserPhone();
        if (currentPhone == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("请先登录后再点赞");
        }

        // 🌟 只有这一行！将沉重的数据库操作交给了 Redis 引擎处理
        boolean finalStatus = redisLikeService.toggleLike(targetId, currentPhone);

        return ResponseEntity.ok(finalStatus ? "点赞成功" : "取消点赞");
    }

    @GetMapping("/like/status")
    public ResponseEntity<?> getLikeStatus(@RequestParam String targetId) {
        // 🌟 走 Redis 高速缓存拿总数
        long totalLikes = redisLikeService.getLikeCount(targetId);
        boolean isLikedByMe = false;

        String currentPhone = getCurrentUserPhone();
        if (currentPhone != null) {
            // 🌟 走 Redis 高速缓存拿个人状态
            isLikedByMe = redisLikeService.isLiked(targetId, currentPhone);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("totalLikes", totalLikes);
        result.put("isLiked", isLikedByMe);
        return ResponseEntity.ok(result);
    }
}