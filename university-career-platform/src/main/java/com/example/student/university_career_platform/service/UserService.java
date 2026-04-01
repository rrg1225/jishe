package com.example.student.university_career_platform.service;

import com.example.student.university_career_platform.controller.UserController.ProfileUpdateRequest;
import com.example.student.university_career_platform.model.User;

public interface UserService {
    User updateProfile(String currentPhone, ProfileUpdateRequest request);
    User getUserInfo(String currentPhone);
}