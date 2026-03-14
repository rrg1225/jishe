package com.example.student.university_career_platform.repository;


import com.example.student.university_career_platform.model.UserCommunityRelation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCommunityRelationRepository extends JpaRepository<UserCommunityRelation, Long> {
    // 按照 JPA 命名规范，只需定义方法名，JPA 会自动翻译成：SELECT count(*) > 0 FROM user_community_relation WHERE user_id = ? AND community_id = ?
    boolean existsByUserIdAndCommunityId(Long userId, Long communityId);
}