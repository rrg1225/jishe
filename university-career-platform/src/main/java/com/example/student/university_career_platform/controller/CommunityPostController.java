package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.model.CommunityPost;
import com.example.student.university_career_platform.service.CommunityPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/community")
public class CommunityPostController {

    @Autowired
    private CommunityPostService postService;

    /**
     * 获取某个社区的所有帖子
     * 前端调用方式: GET http://localhost:8080/api/community/1/posts  (1代表社区ID)
     */
    @GetMapping("/{communityId}/posts")
    public List<CommunityPost> getPosts(@PathVariable Long communityId) {
        return postService.getPostsByCommunityId(communityId);
    }

    /**
     * 发布新帖子
     * 前端调用方式: POST http://localhost:8080/api/community/post (请求体带上 JSON 格式的帖子数据)
     */
    @PostMapping("/post")
    public String publishPost(@RequestBody CommunityPost post) {
        postService.publishPost(post);
        return "发布成功";
    }
}