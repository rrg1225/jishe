package com.example.student.university_career_platform.controller;

import com.example.student.university_career_platform.model.IndustryCommunity;
import com.example.student.university_career_platform.service.IndustryCommunityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("/api/community")
public class IndustryCommunityController {

    @Autowired
    private IndustryCommunityService industryCommunityService;

    /**
     * 前端调用方式: GET http://localhost:8080/api/community/list
     */
    @GetMapping("/list")
    public List<IndustryCommunity> getList() {
        return industryCommunityService.getAllCommunities();
    }
}
