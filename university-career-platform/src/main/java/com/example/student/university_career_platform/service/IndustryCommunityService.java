package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.model.IndustryCommunity;
import com.example.student.university_career_platform.repository.IndustryCommunityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IndustryCommunityService {

    @Autowired
    private IndustryCommunityRepository industryCommunityRepository;

    /**
     * 获取所有行业社区列表
     */
    public List<IndustryCommunity> getAllCommunities() {
        // 直接调用 JPA 提供的 findAll() 方法
        return industryCommunityRepository.findAll();
    }
}
