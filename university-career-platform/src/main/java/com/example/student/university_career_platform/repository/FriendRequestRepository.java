package com.example.student.university_career_platform.repository;

import com.example.student.university_career_platform.model.FriendRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long> {
    // 查询某个人收到的所有“待处理”的好友请求
    List<FriendRequest> findByReceiverIdAndStatus(String receiverId, String status);
}