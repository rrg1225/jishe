package com.example.student.university_career_platform.repository;

import com.example.student.university_career_platform.model.ChatMessage;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 参数全部修改为 String 类型
    @Query("SELECT m FROM ChatMessage m WHERE (m.senderId = :userId AND m.receiverId = :otherId) OR (m.senderId = :otherId AND m.receiverId = :userId) ORDER BY m.gmtCreated ASC")
    List<ChatMessage> findChatHistory(@Param("userId") String userId, @Param("otherId") String otherId);

    // 双向删除聊天记录
    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMessage m WHERE (m.senderId = :userId AND m.receiverId = :friendId) OR (m.senderId = :friendId AND m.receiverId = :userId)")
    void deleteChatHistoryBetween(@Param("userId") String userId, @Param("friendId") String friendId);

}