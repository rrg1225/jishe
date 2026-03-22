package com.example.student.university_career_platform.repository; // 注意核对你的包名

import com.example.student.university_career_platform.model.UserFriend;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserFriendRepository extends JpaRepository<UserFriend, Long> {

    // 新增这一行：根据用户ID查询他所有的好友关系记录
    List<UserFriend> findByUserId(String userId);

    /* 根据用户ID和好友ID删除关系 */
    @Transactional
    void deleteByUserIdAndFriendId(String userId, String friendId);

}
