package com.example.student.university_career_platform.repository;

import com.example.student.university_career_platform.model.LikeRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LikeRecordRepository extends JpaRepository<LikeRecord, Long> {
    // 查某个人是否对某个视频点过赞
    boolean existsByTargetIdAndUserPhone(String targetId, String userPhone);
    // 删掉某个人的点赞记录（取消点赞用）
    void deleteByTargetIdAndUserPhone(String targetId, String userPhone);
    // 统计某个视频一共有多少个赞
    long countByTargetId(String targetId);
}