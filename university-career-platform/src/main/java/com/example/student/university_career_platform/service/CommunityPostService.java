package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.model.CommunityPost;
import com.example.student.university_career_platform.repository.CommunityPostRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommunityPostService {

    @Autowired
    private CommunityPostRepository communityPostRepository;

    /**
     * 获取指定社区下的所有帖子（按照时间倒序排列）
     */
    public List<CommunityPost> getPostsByCommunityId(Long communityId) {
        // 调用 Repository 中根据 JPA 命名规范写好的方法
        return communityPostRepository.findByCommunityIdOrderByGmtCreatedDesc(communityId);
    }

    /**
     * 发布新的社区帖子/博客
     */
    @Transactional
    public void publishPost(CommunityPost post) {
        // 保存帖子，主键 ID 和时间（如果配置了 @PrePersist）会自动生成
        communityPostRepository.save(post);
    }
}
