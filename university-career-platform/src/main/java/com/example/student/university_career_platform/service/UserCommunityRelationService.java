package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.model.UserCommunityRelation;
import com.example.student.university_career_platform.repository.UserCommunityRelationRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserCommunityRelationService {

    @Autowired
    private UserCommunityRelationRepository userCommunityRelationRepository;

    /**
     * 用户加入社区的业务逻辑
     */

    @Transactional
    public String joinCommunity(Long userId, Long CommunityId) {
        // 调用 Repository 中我们自定义的方法，判断是否已经加入
        if (userCommunityRelationRepository.existsByUserIdAndCommunityId(userId, CommunityId)) {
            return "您已经加入了该社区，无需重复加入！";
        }

        // 2. 如果没有加入，则实例化一个关联对象
        UserCommunityRelation relation = new UserCommunityRelation();
        relation.setUserId(userId);
        relation.setCommunityId(CommunityId);

        // 3. 保存到数据库
        userCommunityRelationRepository.save(relation);

        return "成功加入社区！";
    }
}
