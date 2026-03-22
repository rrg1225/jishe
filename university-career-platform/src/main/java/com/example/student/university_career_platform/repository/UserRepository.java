package com.example.student.university_career_platform.repository;

import com.example.student.university_career_platform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 通过手机号查找用户（用于登录）
    Optional<User> findByPhone(String phone);

    // 判断该手机号是否已被注册（用于注册校验）
    boolean existsByPhone(String phone);

    // 根据昵称或手机号模糊搜索用户
    List<User> findByNickNameContainingOrPhoneContaining(String nickName, String phone);
}