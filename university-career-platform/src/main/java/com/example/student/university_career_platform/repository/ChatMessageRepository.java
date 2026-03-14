package com.example.student.university_career_platform.repository;

import com.example.student.university_career_platform.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage , Long> {
}
