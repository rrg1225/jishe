package com.example.student.university_career_platform.repository;

import com.example.student.university_career_platform.model.CommunityPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {

    /**
     * JPA 会自动将这个方法名翻译成类似以下的 SQL：
     * SELECT * FROM community_post WHERE community_id = ? ORDER BY gmt_created DESC
     */
    List<CommunityPost> findByCommunityIdOrderByGmtCreatedDesc(Long communityId);

}