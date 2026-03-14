package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.service.UserCommunityRelationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@CrossOrigin
@RestController
@RequestMapping("/api/community")
public class UserCommunityRelationController {

    @Autowired
    private UserCommunityRelationService relationService;

    /**
     * 前端调用方式: POST http://localhost:8080/api/community/join?userId=1&communityId=2
     */
    @PostMapping("/join")
    public String join(@RequestParam Long userId, @RequestParam Long communityId) {
        return relationService.joinCommunity(userId, communityId);
    }
}