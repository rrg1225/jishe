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
// 🚀 核心优化：改用原生 SQL，强行加上 LIMIT 50，只拉取最近的 50 条记录
    // 注意：这里需要配合 ORDER BY gmt_created DESC 先拿到最新，然后再在代码里倒序，或者直接交给前端处理。
    // 为了兼容您的前端顺序，我们查出最新的 50 条。
    @Query(value = "SELECT * FROM (SELECT * FROM chat_message WHERE (sender_id = :uId AND receiver_id = :fId) OR (sender_id = :fId AND receiver_id = :uId) ORDER BY gmt_created DESC LIMIT 50) sub ORDER BY gmt_created ASC", nativeQuery = true)
    List<ChatMessage> findChatHistory(@Param("uId") String uId, @Param("fId") String fId);

    // 🚀 新增：基于消息 ID 游标的历史记录拉取（真正的企业级下拉加载更多）
    // 逻辑：查询双向聊天记录，且 ID 必须小于 lastMsgId（代表更老的消息），按时间倒序拿 20 条，再正序返回
    @Query(value = "SELECT * FROM (SELECT * FROM chat_message WHERE ((sender_id = :uId AND receiver_id = :fId) OR (sender_id = :fId AND receiver_id = :uId)) AND id < :lastMsgId ORDER BY id DESC LIMIT 20) sub ORDER BY id ASC", nativeQuery = true)
    List<ChatMessage> findChatHistoryBefore(@Param("uId") String uId, @Param("fId") String fId, @Param("lastMsgId") Long lastMsgId);

    // 双向删除聊天记录
    @Modifying
    @Transactional
    @Query("DELETE FROM ChatMessage m WHERE (m.senderId = :userId AND m.receiverId = :friendId) OR (m.senderId = :friendId AND m.receiverId = :userId)")
    void deleteChatHistoryBetween(@Param("userId") String userId, @Param("friendId") String friendId);

    // 1. 查询发送给我的、且未读的消息列表（用于登录后弹出提醒，或加载未读消息）
    @Query("SELECT m FROM ChatMessage m WHERE m.receiverId = :userId AND m.isRead = 0 ORDER BY m.gmtCreated ASC")
    List<ChatMessage> findUnreadMessages(@Param("userId") String userId);

    // 2. 查询某个具体好友发给我的未读消息数量（用于在好友列表中显示小红点 🔴）
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.senderId = :friendId AND m.receiverId = :userId AND m.isRead = 0")
    long countUnreadFromFriend(@Param("userId") String userId, @Param("friendId") String friendId);

    // 3. 将某个好友发给我的未读消息全部标记为已读（当用户点开聊天窗口时调用）
    @Modifying
    @Transactional
    @Query("UPDATE ChatMessage m SET m.isRead = 1 WHERE m.senderId = :friendId AND m.receiverId = :userId AND m.isRead = 0")
    void markAsRead(@Param("userId") String userId, @Param("friendId") String friendId);
}